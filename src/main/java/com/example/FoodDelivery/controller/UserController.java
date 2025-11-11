package com.example.FoodDelivery.controller;

import org.springframework.web.bind.annotation.RestController;

import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import com.example.FoodDelivery.domain.User;
import com.example.FoodDelivery.domain.res.ResultPaginationDTO;
import com.example.FoodDelivery.domain.res.user.ResCreateUserDTO;
import com.example.FoodDelivery.domain.res.user.ResUpdateUserDTO;
import com.example.FoodDelivery.domain.res.user.ResUserDTO;
import com.example.FoodDelivery.service.UserService;
import com.example.FoodDelivery.util.annotation.ApiMessage;
import com.example.FoodDelivery.util.error.IdInvalidException;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PutMapping;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1")
public class UserController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/users/{id}")
    @ApiMessage("Get user by id")
    public ResponseEntity<ResUserDTO> getUserById(@PathVariable("id") Long id) throws IdInvalidException {
        User user = this.userService.getUserById(id);
        if (user == null) {
            throw new IdInvalidException("User with id " + id + " does not exist");
        }
        ResUserDTO resUserDTO = this.userService.convertToResUserDTO(user);
        return ResponseEntity.status(HttpStatus.OK).body(resUserDTO);
    }

    @GetMapping("/users")
    @ApiMessage("Get all users")
    public ResponseEntity<ResultPaginationDTO> getAllUsers(
            @Filter Specification<User> spec, Pageable pageable) {
        ResultPaginationDTO result = userService.getAllUsers(spec, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @PostMapping("/users")
    @ApiMessage("Create new user")
    public ResponseEntity<ResCreateUserDTO> createNewUser(@Valid @RequestBody User hieuduong)
            throws IdInvalidException {

        boolean isEmailExist = this.userService.checkEmailExists(hieuduong.getEmail());
        if (isEmailExist) {
            throw new IdInvalidException("Email already exists: " +
                    hieuduong.getEmail());
        }
        String encodedPassword = this.passwordEncoder.encode(hieuduong.getPassword());
        hieuduong.setPassword(encodedPassword);

        User hieuduongUser = this.userService.handleCreateUser(hieuduong);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(this.userService.convertToResCreateUserDTO(hieuduongUser));
    }

    @PutMapping("/users")
    @ApiMessage("Update user by id")
    public ResponseEntity<ResUpdateUserDTO> updateUser(@RequestBody User user) throws IdInvalidException {
        User updateUser = this.userService.handleUpdateUser(user);
        if (updateUser == null) {
            throw new IdInvalidException("User with id " + user.getId() + " does not exist");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(this.userService.convertToResUpdateUserDTO(updateUser));
    }

    @DeleteMapping("/users/{id}")
    @ApiMessage("Delete user by id")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") Long id) throws IdInvalidException {
        User user = this.userService.getUserById(id);
        if (user == null) {
            throw new IdInvalidException("User with id " + id + " does not exist");
        }

        this.userService.handleDeleteUser(id);
        return ResponseEntity.ok(null);
    }
}
