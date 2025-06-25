package org.balanceus.topping.domain.model;

import java.util.UUID;

import org.balanceus.topping.infrastructure.security.Role;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

	@Id
	@GeneratedValue
	@UuidGenerator
	private UUID uuid;

	private String email;

	private String password;

	private Role role;

	private String username;
}
