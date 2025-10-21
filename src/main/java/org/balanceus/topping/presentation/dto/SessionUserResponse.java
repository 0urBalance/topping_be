package org.balanceus.topping.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionUserResponse {
    private UUID uuid;
    private String username;
    private String email;
    private String role;
}