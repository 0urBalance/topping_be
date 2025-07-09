package org.balanceus.topping.presentation.dto;

import org.balanceus.topping.infrastructure.security.Role;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {
    private String username;
    private String email;
    private String password;
    private Role role;
    private Integer sggCode;
    private Boolean termsAgreement;
}