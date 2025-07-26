package org.balanceus.topping.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.balanceus.topping.domain.model.Store;
import org.balanceus.topping.domain.model.StoreImage;

public interface StoreImageRepository {
    
    StoreImage save(StoreImage storeImage);
    
    Optional<StoreImage> findByUuid(UUID uuid);
    
    List<StoreImage> findByStore(Store store);
    
    List<StoreImage> findByStoreOrderByDisplayOrderAscCreatedAtAsc(Store store);
    
    List<StoreImage> findByStoreAndImageType(Store store, StoreImage.ImageType imageType);
    
    void deleteByUuid(UUID uuid);
    
    void deleteByStore(Store store);
    
    long countByStore(Store store);
}