package org.balanceus.topping.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.balanceus.topping.domain.model.Store;
import org.balanceus.topping.domain.model.StoreImage;
import org.balanceus.topping.domain.repository.StoreImageRepository;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class StoreImageRepositoryImpl implements StoreImageRepository {

    private final StoreImageJpaRepository storeImageJpaRepository;

    @Override
    public StoreImage save(StoreImage storeImage) {
        return storeImageJpaRepository.save(storeImage);
    }

    @Override
    public Optional<StoreImage> findByUuid(UUID uuid) {
        return storeImageJpaRepository.findById(uuid);
    }

    @Override
    public List<StoreImage> findByStore(Store store) {
        return storeImageJpaRepository.findByStoreOrderByDisplayOrderAscCreatedAtAsc(store);
    }

    @Override
    public List<StoreImage> findByStoreOrderByDisplayOrderAscCreatedAtAsc(Store store) {
        return storeImageJpaRepository.findByStoreOrderByDisplayOrderAscCreatedAtAsc(store);
    }

    @Override
    public List<StoreImage> findByStoreAndImageType(Store store, StoreImage.ImageType imageType) {
        return storeImageJpaRepository.findByStoreAndImageType(store, imageType);
    }

    @Override
    public void deleteByUuid(UUID uuid) {
        storeImageJpaRepository.deleteById(uuid);
    }

    @Override
    public void deleteByStore(Store store) {
        storeImageJpaRepository.deleteByStore(store);
    }

    @Override
    public long countByStore(Store store) {
        return storeImageJpaRepository.countByStore(store);
    }
}