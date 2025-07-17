package org.balanceus.topping.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignupStepRequest {
    private String userType;
    private String username;
    private String birthYear;
    private String birthMonth;
    private String birthDay;
    private String gender;
    private String region;
    private String email;
    private String password;
    private String confirmPassword;
    private String phoneNumber;
    private Boolean termsAgreement;
    private Boolean privacyAgreement;
}