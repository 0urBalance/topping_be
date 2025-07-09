package org.balanceus.topping.presentation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/policy")
public class PolicyController {
    
    @GetMapping("/privacy")
    public String privacyPolicy() {
        return "policy/privacy-policy";
    }
    
    @GetMapping("/terms")
    public String termsOfService() {
        return "policy/terms-of-service";
    }
    
    @GetMapping("/privacy-modal")
    public String privacyPolicyModal() {
        return "policy/privacy-policy";
    }
    
    @GetMapping("/terms-modal")
    public String termsOfServiceModal() {
        return "policy/terms-of-service";
    }
}