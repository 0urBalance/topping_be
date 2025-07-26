package org.balanceus.topping.domain.repository;

import org.balanceus.topping.domain.model.SggCode;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface SggCodeRepository extends Repository<SggCode, Integer> {
    
    // Create/Update operations
    <S extends SggCode> S save(S entity);
    <S extends SggCode> List<S> saveAll(Iterable<S> entities);
    
    // Read operations
    Optional<SggCode> findById(Integer sggCd5);
    List<SggCode> findAll(PageRequest pageRequest);
    List<SggCode> findAllById(Iterable<Integer> ids);
    
    // Custom business queries
    List<SggCode> findBySggCdNmRegion(String sggCdNmRegion);
    List<SggCode> findBySggCdNmCity(String sggCdNmCity);
    List<SggCode> findBySggCdNmContaining(String sggCdNm);
    List<SggCode> findBySggCdNmRegionAndSggCdNmCity(String sggCdNmRegion, String sggCdNmCity);
    Optional<SggCode> findBySggCdNm(String sggCdNm);
    
    // Existence checks
    boolean existsById(Integer sggCd5);
    boolean existsBySggCdNm(String sggCdNm);
    
    // Count operations
    long count();
    
    // Delete operations
    void deleteById(Integer sggCd5);
    void delete(SggCode entity);
    void deleteAllById(Iterable<? extends Integer> ids);
    void deleteAll(Iterable<? extends SggCode> entities);
    void deleteAll();
}