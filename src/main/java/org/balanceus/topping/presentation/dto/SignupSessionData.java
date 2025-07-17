package org.balanceus.topping.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignupSessionData {
    private String userType;
    private String username;
    private String birthDate;
    private String gender;
    private String region;
    private String email;
    private String password;
    private String phoneNumber;
    private Boolean termsAgreement;
    private Boolean privacyAgreement;
    private Integer currentStep;
}