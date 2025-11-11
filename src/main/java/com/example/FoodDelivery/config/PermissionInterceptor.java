// package com.example.FoodDelivery.config;

// import java.util.List;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.transaction.annotation.Transactional;
// import org.springframework.web.servlet.HandlerInterceptor;
// import org.springframework.web.servlet.HandlerMapping;

// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;
// import com.example.FoodDelivery.domain.Permission;
// import com.example.FoodDelivery.domain.Role;
// import com.example.FoodDelivery.domain.User;
// import com.example.FoodDelivery.service.UserService;
// import com.example.FoodDelivery.util.SecurityUtil;
// import com.example.FoodDelivery.util.error.PermissionException;

// public class PermissionInterceptor implements HandlerInterceptor {

// @Autowired
// UserService userService;

// @Override
// @Transactional
// public boolean preHandle(
// HttpServletRequest request,
// HttpServletResponse response, Object handler)
// throws Exception {

// String path = (String)
// request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
// String requestURI = request.getRequestURI();
// String httpMethod = request.getMethod();
// System.out.println(">>> RUN preHandle");
// System.out.println(">>> path=" + path);
// System.out.println(">>> httpMethod=" + httpMethod);
// System.out.println(">>>requestURI=" + requestURI);

// // check permission
// String email = SecurityUtil.getCurrentUserLogin().isPresent() ?
// SecurityUtil.getCurrentUserLogin().get() : "";
// if (email != null && !email.isEmpty()) {
// User user = userService.handleGetUserByUsername(email);
// if (user != null) {
// Role role = user.getRole();
// if (role != null) {
// List<Permission> permissions = role.getPermissions();
// boolean isAllow = permissions.stream()
// .anyMatch(item -> item.getApiPath().equals(path) &&
// item.getMethod().equals(httpMethod));
// System.out.println(">>> isAllow=" + isAllow);
// if (isAllow == false) {
// throw new PermissionException("User not permission to access this api");
// }
// } else {
// throw new PermissionException("User not permission to access this api");
// }
// }
// }
// return true;

// }

// }