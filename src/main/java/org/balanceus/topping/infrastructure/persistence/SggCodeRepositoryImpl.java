package org.balanceus.topping.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import org.balanceus.topping.domain.model.SggCode;
import org.balanceus.topping.domain.repository.SggCodeRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SggCodeRepositoryImpl implements SggCodeRepository {
    
    private final SggCodeJpaRepository sggCodeJpaRepository;
    
    // Create/Update operations
    @Override
    public <S extends SggCode> S save(S entity) {
        return sggCodeJpaRepository.save(entity);
    }
    
    @Override
    public <S extends SggCode> List<S> saveAll(Iterable<S> entities) {
        return sggCodeJpaRepository.saveAll(entities);
    }
    
    // Read operations
    @Override
    public Optional<SggCode> findById(Integer sggCd5) {
        return sggCodeJpaRepository.findById(sggCd5);
    }
    
    @Override
    public List<SggCode> findAll() {
        return sggCodeJpaRepository.findAll();
    }
    
    @Override
    public List<SggCode> findAllById(Iterable<Integer> ids) {
        return sggCodeJpaRepository.findAllById(ids);
    }
    
    // Custom business queries
    @Override
    public List<SggCode> findBySggCdNmRegion(String sggCdNmRegion) {
        return sggCodeJpaRepository.findBySggCdNmRegion(sggCdNmRegion);
    }
    
    @Override
    public List<SggCode> findBySggCdNmCity(String sggCdNmCity) {
        return sggCodeJpaRepository.findBySggCdNmCity(sggCdNmCity);
    }
    
    @Override
    public List<SggCode> findBySggCdNmContaining(String sggCdNm) {
        return sggCodeJpaRepository.findBySggCdNmContaining(sggCdNm);
    }
    
    @Override
    public List<SggCode> findBySggCdNmRegionAndSggCdNmCity(String sggCdNmRegion, String sggCdNmCity) {
        return sggCodeJpaRepository.findBySggCdNmRegionAndSggCdNmCity(sggCdNmRegion, sggCdNmCity);
    }
    
    @Override
    public Optional<SggCode> findBySggCdNm(String sggCdNm) {
        return sggCodeJpaRepository.findBySggCdNm(sggCdNm);
    }
    
    // Existence checks
    @Override
    public boolean existsById(Integer sggCd5) {
        return sggCodeJpaRepository.existsById(sggCd5);
    }
    
    @Override
    public boolean existsBySggCdNm(String sggCdNm) {
        return sggCodeJpaRepository.existsBySggCdNm(sggCdNm);
    }
    
    // Count operations
    @Override
    public long count() {
        return sggCodeJpaRepository.count();
    }
    
    // Delete operations
    @Override
    public void deleteById(Integer sggCd5) {
        sggCodeJpaRepository.deleteById(sggCd5);
    }
    
    @Override
    public void delete(SggCode entity) {
        sggCodeJpaRepository.delete(entity);
    }
    
    @Override
    public void deleteAllById(Iterable<? extends Integer> ids) {
        sggCodeJpaRepository.deleteAllById(ids);
    }
    
    @Override
    public void deleteAll(Iterable<? extends SggCode> entities) {
        sggCodeJpaRepository.deleteAll(entities);
    }
    
    @Override
    public void deleteAll() {
        sggCodeJpaRepository.deleteAll();
    }
}