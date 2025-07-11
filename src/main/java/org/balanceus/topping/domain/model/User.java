package org.balanceus.topping.domain.model;

import java.util.UUID;

import org.balanceus.topping.infrastructure.security.Role;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
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

	@Enumerated(EnumType.STRING)
	private Role role;

	private String username;

	private Boolean termsAgreement;

	@ManyToOne
	@JoinColumn(name = "sgg_cd_5")
	private SggCode sggCode;

	@OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private Store store;
}
