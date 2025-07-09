package org.balanceus.topping.infrastructure.persistence;

import org.balanceus.topping.domain.model.SggCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SggCodeJpaRepository extends JpaRepository<SggCode, Integer> {
    
    // Custom finder methods for business logic
    List<SggCode> findBySggCdNmRegion(String sggCdNmRegion);
    List<SggCode> findBySggCdNmCity(String sggCdNmCity);
    List<SggCode> findBySggCdNmContaining(String sggCdNm);
    List<SggCode> findBySggCdNmRegionAndSggCdNmCity(String sggCdNmRegion, String sggCdNmCity);
    Optional<SggCode> findBySggCdNm(String sggCdNm);
    boolean existsBySggCdNm(String sggCdNm);
}