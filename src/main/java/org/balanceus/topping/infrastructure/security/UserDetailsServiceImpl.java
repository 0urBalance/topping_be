package org.balanceus.topping.infrastructure.security;

import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.domain.repository.UserRepository;
import org.balanceus.topping.infrastructure.exception.BaseException;
import org.balanceus.topping.infrastructure.response.Code;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

	private final UserRepository userRepository;

	public UserDetailsServiceImpl(UserRepository userRepository) {
		this.userRepository = userRepository;

	}

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		log.info("loadUserByUsername called with email: {}", email);
		User user = userRepository.findByEmail(email)
			.orElseThrow(() -> new BaseException(Code.SIGN001, "일치하는 이메일 없음"));
		log.info("User found: {}", user);
		return new UserDetailsImpl(user);
	}
}
