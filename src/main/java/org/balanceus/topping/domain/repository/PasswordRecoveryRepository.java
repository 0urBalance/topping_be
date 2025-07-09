package org.balanceus.topping.domain.repository;

import org.balanceus.topping.domain.model.PasswordRecovery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PasswordRecoveryRepository extends JpaRepository<PasswordRecovery, UUID> {
    
    Optional<PasswordRecovery> findByEmailAndVerificationCodeAndIsUsedFalse(String email, String verificationCode);
    
    Optional<PasswordRecovery> findFirstByEmailAndIsUsedFalseOrderByCreatedAtDesc(String email);
    
    void deleteByEmail(String email);
}