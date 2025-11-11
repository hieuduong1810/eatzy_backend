package com.example.FoodDelivery.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.method.P;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.FoodDelivery.domain.User;
import com.example.FoodDelivery.domain.req.ReqLoginDTO;
import com.example.FoodDelivery.domain.res.ResLoginDTO;
import com.example.FoodDelivery.domain.res.ResLoginDTO.UserLogin;
import com.example.FoodDelivery.domain.res.user.ResCreateUserDTO;
import com.example.FoodDelivery.service.UserService;
import com.example.FoodDelivery.util.SecurityUtil;
import com.example.FoodDelivery.util.annotation.ApiMessage;
import com.example.FoodDelivery.util.error.IdInvalidException;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class AuthController {

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final SecurityUtil securityUtil;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Value("${foodDelivery.jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenExpiration;

    public AuthController(AuthenticationManagerBuilder authenticationManagerBuilder, SecurityUtil securityUtil,
            UserService userService, PasswordEncoder passwordEncoder) {
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.securityUtil = securityUtil;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/auth/login")
    @ApiMessage("User login")
    public ResponseEntity<ResLoginDTO> login(@Valid @RequestBody ReqLoginDTO loginDTO) {
        // Nạp input gồm username/password và Security
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                loginDTO.getUsername(), loginDTO.getPassword());

        // xác thực người dùng => cần viết hàm loadUserByUsername
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        // set thông tin người dùng đăng nhập vào context (có thể sử dụng sau này)
        SecurityContextHolder.getContext().setAuthentication(authentication);

        ResLoginDTO resLoginDTO = new ResLoginDTO();
        User currentUserDB = this.userService
                .handleGetUserByUsername(loginDTO.getUsername());
        if (currentUserDB != null) {
            ResLoginDTO.UserLogin userLogin = resLoginDTO.new UserLogin(currentUserDB.getId(),
                    currentUserDB.getEmail(), currentUserDB.getName(), currentUserDB.getRole());
            resLoginDTO.setUser(userLogin);
        }

        String access_token = this.securityUtil.createAccessToken(authentication.getName(), resLoginDTO);

        resLoginDTO.setAccessToken(access_token);

        // create refresh token
        String refresh_token = this.securityUtil.createRefreshToken(loginDTO.getUsername(), resLoginDTO);

        // update user
        this.userService.updateUserToken(refresh_token, loginDTO.getUsername());

        // set cookies
        ResponseCookie resCookies = ResponseCookie.from("refresh_token", refresh_token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, resCookies.toString()).body(resLoginDTO);
    }

    @GetMapping("/auth/account")
    @ApiMessage("fetch account")
    public ResponseEntity<ResLoginDTO.UserGetAccount> getAccount() {
        String email = SecurityUtil.getCurrentUserLogin().isPresent() ? SecurityUtil.getCurrentUserLogin().get() : "";
        ResLoginDTO resLoginDTO = new ResLoginDTO();
        User currentUserDB = this.userService
                .handleGetUserByUsername(email);
        ResLoginDTO.UserLogin userLogin = resLoginDTO.new UserLogin();
        ResLoginDTO.UserGetAccount userGetAcount = resLoginDTO.new UserGetAccount();
        if (currentUserDB != null) {
            userLogin.setId(currentUserDB.getId());
            userLogin.setEmail(currentUserDB.getEmail());
            userLogin.setName(currentUserDB.getName());
            userLogin.setRole(currentUserDB.getRole());
            userGetAcount.setUser(userLogin);
        }
        return ResponseEntity.ok(userGetAcount);
    }

    @GetMapping("/auth/refresh")
    @ApiMessage("Get user by refresh token")
    public ResponseEntity<ResLoginDTO> getRefreshToken(
            @CookieValue(name = "refresh_token", defaultValue = "abc") String refresh_token) throws IdInvalidException {
        if (refresh_token.equals("abc")) {
            throw new IdInvalidException("Bạn không có refresh token ở cookie");
        }
        // check valid
        Jwt decodedToken = this.securityUtil.checkValidRefreshToken(refresh_token);
        String email = decodedToken.getSubject();

        // check user by token + email
        User currentUser = this.userService.getUserByRefreshTokenAndEmail(refresh_token, email);
        if (currentUser == null) {
            throw new IdInvalidException("Refresh token không hợp lệ");
        }

        // issue new token/set refresh token as cookies
        ResLoginDTO resLoginDTO = new ResLoginDTO();
        User currentUserDB = this.userService
                .handleGetUserByUsername(email);
        if (currentUserDB != null) {
            ResLoginDTO.UserLogin userLogin = resLoginDTO.new UserLogin(currentUserDB.getId(),
                    currentUserDB.getEmail(), currentUserDB.getName(), currentUserDB.getRole());
            resLoginDTO.setUser(userLogin);
        }

        String access_token = this.securityUtil.createAccessToken(email,
                resLoginDTO);

        resLoginDTO.setAccessToken(access_token);

        // create refresh token
        String new_refresh_token = this.securityUtil.createRefreshToken(email,
                resLoginDTO);

        // update user
        this.userService.updateUserToken(new_refresh_token, email);

        // set cookies
        ResponseCookie resCookies = ResponseCookie.from("refresh_token",
                new_refresh_token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,
                resCookies.toString()).body(resLoginDTO);
    }

    @PostMapping("/auth/logout")
    @ApiMessage("User logout")
    public ResponseEntity<Void> logout() throws IdInvalidException {
        String email = SecurityUtil.getCurrentUserLogin().isPresent() ? SecurityUtil.getCurrentUserLogin().get() : "";
        if (email.equals("")) {
            throw new IdInvalidException("Access Token không hợp lệ");
        }
        this.userService.updateUserToken(null, email);

        // set cookies
        ResponseCookie deleteSpringCookie = ResponseCookie.from("refresh_token", null)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, deleteSpringCookie.toString()).body(null);
    }

    @PostMapping("/auth/register")
    @ApiMessage("User registration")
    public ResponseEntity<ResCreateUserDTO> createNewUser(@Valid @RequestBody User hieuduong)
            throws IdInvalidException {

        boolean isEmailExist = this.userService.checkEmailExists(hieuduong.getEmail());
        if (isEmailExist) {
            throw new IdInvalidException("Email already exists: " + hieuduong.getEmail());
        }
        String encodedPassword = this.passwordEncoder.encode(hieuduong.getPassword());
        hieuduong.setPassword(encodedPassword);

        User hieuduongUser = this.userService.handleCreateUser(hieuduong);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(this.userService.convertToResCreateUserDTO(hieuduongUser));
    }

}
