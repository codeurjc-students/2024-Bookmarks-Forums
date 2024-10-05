package com.example.backend.controller;

import com.example.backend.security.jwt.AuthResponse;
import com.example.backend.security.jwt.LoginRequest;
import com.example.backend.security.jwt.UserLoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class APISessionController {

    private final UserLoginService userLoginService;

    public APISessionController(UserLoginService userLoginService) {
        this.userLoginService = userLoginService;
    }

    // test operation
    @Operation(summary = "Test operation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Test operation successful", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class)),
            }),
    })
    @GetMapping("/test")
    public ResponseEntity<AuthResponse> test() {
        return ResponseEntity.ok(new AuthResponse(AuthResponse.Status.SUCCESS, "Test operation successful"));
    }

    @Operation(summary = "Login")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class)),
            }),
            @ApiResponse(responseCode = "400", description = "Invalid credentials"),
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @CookieValue(name = "accessToken", required = false) String accessToken,
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            @RequestBody LoginRequest loginRequest) {

        return userLoginService.login(loginRequest, accessToken, refreshToken);
    }

    @Operation(summary = "Refresh token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token refreshed", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class)),
            }),
            @ApiResponse(responseCode = "400", description = "Invalid token"),
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            @CookieValue(name = "refreshToken", required = false) String refreshToken) {

        return userLoginService.refresh(refreshToken);
    }

    @Operation(summary = "Logout")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout successful", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class)),
            }),
    })
    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout(HttpServletRequest request, HttpServletResponse response) {

        return ResponseEntity.ok(new AuthResponse(AuthResponse.Status.SUCCESS, userLoginService.logout(request, response)));
    }
}
