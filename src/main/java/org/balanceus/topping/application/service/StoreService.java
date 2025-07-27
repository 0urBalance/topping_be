package org.balanceus.topping.application.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.balanceus.topping.application.dto.StoreRegistrationRequest;
import org.balanceus.topping.domain.model.Product;
import org.balanceus.topping.domain.model.Store;
import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.domain.repository.ProductRepository;
import org.balanceus.topping.domain.repository.StoreRepository;
import org.balanceus.topping.domain.repository.UserRepository;
import org.balanceus.topping.infrastructure.exception.BaseException;
import org.balanceus.topping.infrastructure.response.Code;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class StoreService {

    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public Store registerStore(StoreRegistrationRequest request, UUID userUuid) {
        Optional<User> userOptional = userRepository.findById(userUuid);
        if (userOptional.isEmpty()) {
            throw new BaseException(Code.NOT_FOUND, "User not found");
        }

        User user = userOptional.get();

        if (storeRepository.existsByUser(user)) {
            throw new BaseException(Code.ALREADY_EXISTS, "User already has a registered store");
        }

        if (storeRepository.existsByName(request.getName())) {
            throw new BaseException(Code.ALREADY_EXISTS, "Store name already exists");
        }

        Store store = new Store();
        store.setName(request.getName());
        store.setAddress(request.getAddress());
        store.setContactNumber(request.getContactNumber());
        store.setBusinessHours(request.getBusinessHours());
        store.setCategory(request.getCategory());
        store.setMainImageUrl(request.getMainImageUrl());
        store.setSnsOrWebsiteLink(request.getSnsOrWebsiteLink());
        store.setUser(user);

        Store savedStore = storeRepository.save(store);
        
        
        return savedStore;
    }

    @Transactional(readOnly = true)
    public Optional<Store> getStoreByUser(UUID userUuid) {
        return storeRepository.findByUserUuid(userUuid);
    }

    @Transactional(readOnly = true)
    public Optional<Store> getStoreById(UUID storeId) {
        return storeRepository.findById(storeId);
    }

    @Transactional(readOnly = true)
    public Optional<Store> getStoreByIdWithProductsAndTags(UUID storeId) {
        return storeRepository.findByIdWithProductsAndTags(storeId);
    }

    @Transactional(readOnly = true)
    public List<Store> getStoresByUser(UUID userUuid) {
        Optional<Store> store = storeRepository.findByUserUuid(userUuid);
        return store.map(List::of).orElse(List.of());
    }

    public Store updateStore(UUID storeUuid, StoreRegistrationRequest request, UUID userUuid) {
        Optional<Store> storeOptional = storeRepository.findById(storeUuid);
        if (storeOptional.isEmpty()) {
            throw new BaseException(Code.NOT_FOUND, "Store not found");
        }

        Store store = storeOptional.get();

        if (!store.getUser().getUuid().equals(userUuid)) {
            throw new BaseException(Code.FORBIDDEN, "You can only update your own store");
        }

        if (!store.getName().equals(request.getName()) && storeRepository.existsByName(request.getName())) {
            throw new BaseException(Code.ALREADY_EXISTS, "Store name already exists");
        }

        store.setName(request.getName());
        store.setAddress(request.getAddress());
        store.setContactNumber(request.getContactNumber());
        store.setBusinessHours(request.getBusinessHours());
        store.setCategory(request.getCategory());
        store.setMainImageUrl(request.getMainImageUrl());
        store.setSnsOrWebsiteLink(request.getSnsOrWebsiteLink());

        return storeRepository.save(store);
    }

    public Store updateStoreEntity(Store store) {
        return storeRepository.save(store);
    }
    
}