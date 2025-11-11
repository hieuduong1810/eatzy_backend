package com.example.FoodDelivery.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.example.FoodDelivery.domain.Role;
import com.example.FoodDelivery.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.FoodDelivery.domain.User;
import com.example.FoodDelivery.domain.res.ResultPaginationDTO;
import com.example.FoodDelivery.domain.res.user.ResCreateUserDTO;
import com.example.FoodDelivery.domain.res.user.ResUpdateUserDTO;
import com.example.FoodDelivery.domain.res.user.ResUserDTO;
import com.example.FoodDelivery.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleService roleService;

    public UserService(UserRepository userRepository, RoleService roleService) {
        this.userRepository = userRepository;
        this.roleService = roleService;
    }

    @Transactional
    public User handleCreateUser(User user) {

        // check role
        if (user.getRole() != null) {
            Role role = this.roleService.getRoleById(user.getRole().getId());
            user.setRole(role != null ? role : null);
        }

        User savedUser = this.userRepository.save(user);
        return savedUser;
    }

    @Transactional
    public void handleDeleteUser(Long id) {
        this.userRepository.deleteById(id);
    }

    @Transactional
    public User getUserById(Long id) {
        Optional<User> userOpt = this.userRepository.findById(id);
        if (userOpt.isPresent()) {
            return userOpt.get();
        }
        return null; // or throw an exception if preferred
    }

    @Transactional
    public ResultPaginationDTO getAllUsers(Specification<User> spec, Pageable pageable) {
        Page<User> page = this.userRepository.findAll(spec, pageable);
        ResultPaginationDTO result = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setTotal(page.getTotalElements());
        meta.setPages(page.getTotalPages());
        result.setMeta(meta);

        // remove sensitive data

        List<ResUserDTO> listUser = page.getContent().stream().map(item -> this.convertToResUserDTO(item))
                .collect(Collectors.toList());

        result.setResult(listUser);
        return result;
    }

    @Transactional
    public User handleUpdateUser(User user) {

        User currentUser = this.getUserById(user.getId());

        if (currentUser != null) {
            currentUser.setName(user.getName());
            currentUser.setAddress(user.getAddress());
            currentUser.setAge(user.getAge());
            currentUser.setGender(user.getGender());
            if (user.getRole() != null) {
                Role role = this.roleService.getRoleById(user.getRole().getId());
                currentUser.setRole(role != null ? role : null);
            }
            currentUser = this.userRepository.save(currentUser);
        }
        return currentUser;
    }

    public User handleGetUserByUsername(String username) {
        return this.userRepository.findByEmail(username);
    }

    public boolean checkEmailExists(String email) {
        return this.userRepository.existsByEmail(email);
    }

    public ResCreateUserDTO convertToResCreateUserDTO(User user) {
        ResCreateUserDTO resUserDTO = new ResCreateUserDTO();
        ResCreateUserDTO.Role role = new ResCreateUserDTO.Role();
        resUserDTO.setId(user.getId());
        resUserDTO.setName(user.getName());
        resUserDTO.setEmail(user.getEmail());
        resUserDTO.setGender(user.getGender());
        resUserDTO.setAddress(user.getAddress());
        resUserDTO.setAge(user.getAge());
        resUserDTO.setCreatedAt(user.getCreatedAt());
        if (user.getRole() != null) {
            role.setId(user.getRole().getId());
            role.setName(user.getRole().getName());
            resUserDTO.setRole(role);
        }
        return resUserDTO;
    }

    public ResUserDTO convertToResUserDTO(User user) {
        ResUserDTO resUserDTO = new ResUserDTO();
        ResUserDTO.Role role = new ResUserDTO.Role();
        resUserDTO.setId(user.getId());
        resUserDTO.setName(user.getName());
        resUserDTO.setEmail(user.getEmail());
        resUserDTO.setGender(user.getGender());
        resUserDTO.setAddress(user.getAddress());
        resUserDTO.setAge(user.getAge());
        resUserDTO.setCreatedAt(user.getCreatedAt());
        resUserDTO.setUpdatedAt(user.getUpdatedAt());
        if (user.getRole() != null) {
            role.setId(user.getRole().getId());
            role.setName(user.getRole().getName());
            resUserDTO.setRole(role);
        }
        return resUserDTO;
    }

    public ResUpdateUserDTO convertToResUpdateUserDTO(User user) {
        ResUpdateUserDTO resUserDTO = new ResUpdateUserDTO();
        ResUpdateUserDTO.Role role = new ResUpdateUserDTO.Role();
        resUserDTO.setId(user.getId());
        resUserDTO.setName(user.getName());
        resUserDTO.setGender(user.getGender());
        resUserDTO.setAddress(user.getAddress());
        resUserDTO.setAge(user.getAge());
        resUserDTO.setUpdatedAt(user.getUpdatedAt());
        if (user.getRole() != null) {
            role.setId(user.getRole().getId());
            role.setName(user.getRole().getName());
            resUserDTO.setRole(role);
        }
        return resUserDTO;
    }

    public void updateUserToken(String token, String email) {
        User currentUser = this.handleGetUserByUsername(email);
        if (currentUser != null) {
            currentUser.setRefreshToken(token);
            this.userRepository.save(currentUser);
        }
    }

    public User getUserByRefreshTokenAndEmail(String refreshToken, String email) {
        return this.userRepository.findByRefreshTokenAndEmail(refreshToken, email);
    }
}
