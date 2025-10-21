package org.balanceus.topping.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionLoginResponse {
    private String message;
    private String redirectUrl;
    private SessionUserResponse user;
}