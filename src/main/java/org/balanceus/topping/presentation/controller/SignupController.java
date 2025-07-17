package org.balanceus.topping.presentation.controller;

import java.time.LocalDate;

import org.balanceus.topping.application.service.UserService;
import org.balanceus.topping.domain.model.SggCode;
import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.domain.repository.SggCodeRepository;
import org.balanceus.topping.infrastructure.security.Role;
import org.balanceus.topping.presentation.dto.SignupSessionData;
import org.balanceus.topping.presentation.dto.SignupStepRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/signup")
public class SignupController {

    @Autowired
    private UserService userService;

    @Autowired
    private SggCodeRepository sggCodeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String SIGNUP_SESSION_KEY = "signupData";

    @GetMapping("/start")
    public String startSignup(HttpSession session) {
        session.removeAttribute(SIGNUP_SESSION_KEY);
        return "redirect:/signup/step/1";
    }

    @GetMapping("/step/1")
    public String showStep1(Model model, HttpSession session) {
        SignupSessionData sessionData = getOrCreateSessionData(session);
        sessionData.setCurrentStep(1);
        model.addAttribute("signupData", sessionData);
        return "auth/signup/step1";
    }

    @PostMapping("/step/1")
    @ResponseBody
    public String processStep1(@RequestBody SignupStepRequest request, HttpSession session) {
        SignupSessionData sessionData = getOrCreateSessionData(session);
        sessionData.setUserType(request.getUserType());
        sessionData.setCurrentStep(2);
        session.setAttribute(SIGNUP_SESSION_KEY, sessionData);
        return "success";
    }

    @GetMapping("/step/2")
    public String showStep2(Model model, HttpSession session) {
        SignupSessionData sessionData = getOrCreateSessionData(session);
        if (sessionData.getCurrentStep() == null || sessionData.getCurrentStep() < 2) {
            return "redirect:/signup/step/1";
        }
        sessionData.setCurrentStep(2);
        model.addAttribute("signupData", sessionData);
        return "auth/signup/step2";
    }

    @PostMapping("/step/2")
    @ResponseBody
    public String processStep2(@RequestBody SignupStepRequest request, HttpSession session) {
        SignupSessionData sessionData = getOrCreateSessionData(session);
        sessionData.setUsername(request.getUsername());
        
        String birthDate = request.getBirthYear() + "-" + 
                          String.format("%02d", Integer.parseInt(request.getBirthMonth())) + "-" + 
                          String.format("%02d", Integer.parseInt(request.getBirthDay()));
        sessionData.setBirthDate(birthDate);
        sessionData.setGender(request.getGender());
        sessionData.setRegion(request.getRegion());
        sessionData.setCurrentStep(3);
        session.setAttribute(SIGNUP_SESSION_KEY, sessionData);
        return "success";
    }

    @GetMapping("/step/3")
    public String showStep3(Model model, HttpSession session) {
        SignupSessionData sessionData = getOrCreateSessionData(session);
        if (sessionData.getCurrentStep() == null || sessionData.getCurrentStep() < 3) {
            return "redirect:/signup/step/1";
        }
        sessionData.setCurrentStep(3);
        model.addAttribute("signupData", sessionData);
        return "auth/signup/step3";
    }

    @PostMapping("/step/3")
    @ResponseBody
    public String processStep3(@RequestBody SignupStepRequest request, HttpSession session) {
        try {
            SignupSessionData sessionData = getOrCreateSessionData(session);
            
            if (userService.existsByEmail(request.getEmail())) {
                return "email_exists";
            }

            sessionData.setEmail(request.getEmail());
            sessionData.setPassword(request.getPassword());
            sessionData.setPhoneNumber(request.getPhoneNumber());
            sessionData.setTermsAgreement(request.getTermsAgreement());
            sessionData.setPrivacyAgreement(request.getPrivacyAgreement());

            User user = createUserFromSessionData(sessionData);
            User savedUser = userService.save(user);

            sessionData.setCurrentStep(4);
            session.setAttribute(SIGNUP_SESSION_KEY, sessionData);
            
            return "success";
        } catch (Exception e) {
            // Log the actual error for debugging
            System.err.println("Error in processStep3: " + e.getMessage());
            e.printStackTrace();
            return "error";
        }
    }

    @GetMapping("/step/4")
    public String showStep4(Model model, HttpSession session) {
        SignupSessionData sessionData = getOrCreateSessionData(session);
        if (sessionData.getCurrentStep() == null || sessionData.getCurrentStep() < 4) {
            return "redirect:/signup/step/1";
        }
        model.addAttribute("signupData", sessionData);
        return "auth/signup/step4";
    }

    @GetMapping("/complete")
    public String completeSignup(HttpSession session) {
        session.removeAttribute(SIGNUP_SESSION_KEY);
        return "redirect:/auth/login";
    }

    private SignupSessionData getOrCreateSessionData(HttpSession session) {
        SignupSessionData sessionData = (SignupSessionData) session.getAttribute(SIGNUP_SESSION_KEY);
        if (sessionData == null) {
            sessionData = new SignupSessionData();
            sessionData.setCurrentStep(1);
            session.setAttribute(SIGNUP_SESSION_KEY, sessionData);
        }
        return sessionData;
    }

    private User createUserFromSessionData(SignupSessionData sessionData) {
        User user = new User();
        user.setEmail(sessionData.getEmail());
        user.setPassword(passwordEncoder.encode(sessionData.getPassword()));
        user.setUsername(sessionData.getUsername());
        user.setBirthDate(LocalDate.parse(sessionData.getBirthDate()));
        user.setGender(User.Gender.valueOf(sessionData.getGender()));
        user.setPhoneNumber(sessionData.getPhoneNumber());
        user.setTermsAgreement(sessionData.getTermsAgreement());

        if ("점주".equals(sessionData.getUserType())) {
            user.setRole(Role.ROLE_BUSINESS_OWNER);
        } else {
            user.setRole(Role.ROLE_USER);
        }

        if (sessionData.getRegion() != null && !sessionData.getRegion().isEmpty()) {
            try {
                SggCode sggCode = sggCodeRepository.findById(Integer.parseInt(sessionData.getRegion())).orElse(null);
                user.setSggCode(sggCode);
            } catch (NumberFormatException e) {
            }
        }

        return user;
    }
}