package org.balanceus.topping.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserEditForm {
    
    @NotBlank(message = "사용자명은 필수입니다.")
    @Size(min = 2, max = 50, message = "사용자명은 2자 이상 50자 이하여야 합니다.")
    private String username;
    
    @Pattern(regexp = "^010-[0-9]{4}-[0-9]{4}$", message = "전화번호는 010-1234-5678 형식이어야 합니다.")
    private String phoneNumber;
    
    public UserEditForm() {}
    
    public UserEditForm(String username, String phoneNumber) {
        this.username = username;
        this.phoneNumber = phoneNumber;
    }
}