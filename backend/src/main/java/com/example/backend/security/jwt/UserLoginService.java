package com.example.backend.security.jwt;

import com.example.backend.entity.User;
import com.example.backend.security.RepositoryUserDetailService;
import com.example.backend.service.UserService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class UserLoginService {
	
	@Autowired
	private AuthenticationManager authenticationManager;
	
	@Autowired
	private RepositoryUserDetailService userDetailsService;

	@Autowired
	private JwtTokenProvider jwtTokenProvider;

	@Autowired
	private JwtCookieManager cookieUtil;

	@Autowired
	private UserService userService;

	public ResponseEntity<AuthResponse> login(LoginRequest loginRequest, String encryptedAccessToken, String 
			encryptedRefreshToken) {

		try {
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		String accessToken = SecurityCipher.decrypt(encryptedAccessToken);
		String refreshToken = SecurityCipher.decrypt(encryptedRefreshToken);
		
		String username = loginRequest.getUsername();
		UserDetails user = userDetailsService.loadUserByUsername(username);

		// check if user account is disabled
		if (userService.isAccountDisabled(username)) {
			AuthResponse loginResponse = new AuthResponse(AuthResponse.Status.FAILURE,
					"Account is disabled !");
			return ResponseEntity.status(400).body(loginResponse);
		}

		Boolean accessTokenValid = jwtTokenProvider.validateToken(accessToken);
		Boolean refreshTokenValid = jwtTokenProvider.validateToken(refreshToken);

		HttpHeaders responseHeaders = new HttpHeaders();
		Token newAccessToken;
		Token newRefreshToken;

		if (!accessTokenValid && !refreshTokenValid) {
			newAccessToken = jwtTokenProvider.generateToken(user);
			newRefreshToken = jwtTokenProvider.generateRefreshToken(user);
			addAccessTokenCookie(responseHeaders, newAccessToken);
			addRefreshTokenCookie(responseHeaders, newRefreshToken);
		}

		if (!accessTokenValid && refreshTokenValid) {
			newAccessToken = jwtTokenProvider.generateToken(user);
			addAccessTokenCookie(responseHeaders, newAccessToken);
		}

		if (accessTokenValid && refreshTokenValid) {
			newAccessToken = jwtTokenProvider.generateToken(user);
			newRefreshToken = jwtTokenProvider.generateRefreshToken(user);
			addAccessTokenCookie(responseHeaders, newAccessToken);
			addRefreshTokenCookie(responseHeaders, newRefreshToken);
		}

		AuthResponse loginResponse = new AuthResponse(AuthResponse.Status.SUCCESS,
				"Auth successful. Tokens are created in cookie.");
		return ResponseEntity.ok().headers(responseHeaders).body(loginResponse);
		} catch (Exception e) {
			AuthResponse loginResponse = new AuthResponse(AuthResponse.Status.FAILURE,
					"Invalid credentials !");
			return ResponseEntity.status(400).body(loginResponse);
		}
	}

	public ResponseEntity<AuthResponse> refresh(String encryptedRefreshToken) {
		
		String refreshToken = SecurityCipher.decrypt(encryptedRefreshToken);
		
		Boolean refreshTokenValid = jwtTokenProvider.validateToken(refreshToken);
		
		if (!refreshTokenValid) {
			AuthResponse loginResponse = new AuthResponse(AuthResponse.Status.FAILURE,
					"Invalid refresh token !");
			return ResponseEntity.status(400).body(loginResponse);
		}

		String username = jwtTokenProvider.getUsername(refreshToken);
		UserDetails user = userDetailsService.loadUserByUsername(username);
				
		Token newAccessToken = jwtTokenProvider.generateToken(user);
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.add(HttpHeaders.SET_COOKIE, cookieUtil
				.createAccessTokenCookie(newAccessToken.getTokenValue(), newAccessToken.getDuration()).toString());

		AuthResponse loginResponse = new AuthResponse(AuthResponse.Status.SUCCESS,
				"Auth successful. Tokens are created in cookie.");
		return ResponseEntity.ok().headers(responseHeaders).body(loginResponse);
	}

	public String getUserName() {
		
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		return authentication.getName();
	}

	public String logout(HttpServletRequest request, HttpServletResponse response) {

		HttpSession session = request.getSession(false);
		SecurityContextHolder.clearContext();
		session = request.getSession(false);
		if (session != null) {
			session.invalidate();
		}

		if (request.getCookies() != null) {
			for (Cookie cookie : request.getCookies()) {
				cookie.setMaxAge(0);
				cookie.setValue("");
				cookie.setHttpOnly(true);
				cookie.setPath("/");
				response.addCookie(cookie);
			}
		}

		return "logout successfully";
	}

	private void addAccessTokenCookie(HttpHeaders httpHeaders, Token token) {
		httpHeaders.add(HttpHeaders.SET_COOKIE,
				cookieUtil.createAccessTokenCookie(token.getTokenValue(), token.getDuration()).toString());
	}

	private void addRefreshTokenCookie(HttpHeaders httpHeaders, Token token) {
		httpHeaders.add(HttpHeaders.SET_COOKIE,
				cookieUtil.createRefreshTokenCookie(token.getTokenValue(), token.getDuration()).toString());
	}
}
