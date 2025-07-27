package org.balanceus.topping.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final UserDetailsService userDetailsService;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.csrf(csrf -> csrf.disable()) // Disable CSRF protection completely
			.authorizeHttpRequests(authz -> authz
				// Public endpoints
				.requestMatchers("/", "/auth/**", "/login").permitAll()
				.requestMatchers("/signup/**").permitAll()
				.requestMatchers("/explore", "/stores/**", "/css/**", "/js/**", "/images/**", "/image/**").permitAll()
				.requestMatchers("/h2-console/**").permitAll() // For testing
				// Public support endpoints
				.requestMatchers("/support/cs", "/support/faq/**").permitAll()
				.requestMatchers("/support/api/faqs").permitAll()
				// Public policy endpoints
				.requestMatchers("/policy/**").permitAll()
				// Public API endpoints
				.requestMatchers("/api/member/signup").permitAll()
				.requestMatchers("/api/session/login", "/api/session/logout", "/api/session/status").permitAll()
				.requestMatchers("/api/auth/check-email").permitAll()
				.requestMatchers("/api/sggcode/**").permitAll()
				// Protected endpoints - must be authenticated
				.requestMatchers("/mypage/**", "/logout").authenticated()
				.requestMatchers("/collabo/**").authenticated()
				.requestMatchers("/chat/**").authenticated()
				.requestMatchers("/products/**").authenticated()
				.requestMatchers("/proposals/**").authenticated()
				.requestMatchers("/collaborations/**").authenticated()
				.requestMatchers("/collaboration-products/**").authenticated()
				.requestMatchers("/stores/**").authenticated()
				// Support endpoints requiring authentication
				.requestMatchers("/support/inquiry-form", "/support/inquiry", "/support/my-inquiries", "/support/inquiry/**").authenticated()
				// Protected API endpoints
				.requestMatchers("/api/**").authenticated()
				// Default to authenticated for any other request
				.anyRequest().authenticated()
			)
			.formLogin(form -> form
				.loginPage("/auth/login")
				.loginProcessingUrl("/login")
				.defaultSuccessUrl("/", true)
				.failureUrl("/auth/login?error=true")
				.permitAll()
			)
			.logout(logout -> logout
				.logoutUrl("/logout")
				.logoutSuccessUrl("/")
				.invalidateHttpSession(true)
				.deleteCookies("JSESSIONID")
				.permitAll()
			)
			.sessionManagement(session -> session
				.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
				.maximumSessions(1)
				.maxSessionsPreventsLogin(false)
			)
			.authenticationProvider(authenticationProvider())
			.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()));
		
		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
		authProvider.setUserDetailsService(userDetailsService);
		authProvider.setPasswordEncoder(passwordEncoder());
		return authProvider;
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}
}