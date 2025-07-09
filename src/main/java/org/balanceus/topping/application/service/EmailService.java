package org.balanceus.topping.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    private final JavaMailSender mailSender;
    
    public void sendVerificationCode(String toEmail, String verificationCode) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Topping 비밀번호 찾기 - 인증코드");
            message.setText(buildVerificationEmailContent(verificationCode));
            
            mailSender.send(message);
            log.info("Verification code sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send verification code to: {}", toEmail, e);
            throw new RuntimeException("이메일 전송에 실패했습니다. 다시 시도해주세요.");
        }
    }
    
    private String buildVerificationEmailContent(String verificationCode) {
        return String.format(
            "안녕하세요, Topping입니다.\n\n" +
            "비밀번호 찾기를 위한 인증코드를 안내드립니다.\n\n" +
            "인증코드: %s\n\n" +
            "이 코드는 15분간 유효하며, 한 번만 사용할 수 있습니다.\n" +
            "만약 본인이 요청하지 않은 경우, 이 메일을 무시하시기 바랍니다.\n\n" +
            "감사합니다.\n" +
            "Topping 팀",
            verificationCode
        );
    }
}