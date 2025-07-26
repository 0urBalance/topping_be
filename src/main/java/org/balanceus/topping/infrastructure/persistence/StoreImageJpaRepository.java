package org.balanceus.topping.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.balanceus.topping.domain.model.Store;
import org.balanceus.topping.domain.model.StoreImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreImageJpaRepository extends JpaRepository<StoreImage, UUID> {
    
    List<StoreImage> findByStoreOrderByDisplayOrderAscCreatedAtAsc(Store store);
    
    List<StoreImage> findByStoreAndImageType(Store store, StoreImage.ImageType imageType);
    
    void deleteByStore(Store store);
    
    long countByStore(Store store);
}