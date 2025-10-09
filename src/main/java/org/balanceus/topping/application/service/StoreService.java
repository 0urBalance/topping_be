package org.balanceus.topping.application.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.balanceus.topping.application.dto.StoreForm;
import org.balanceus.topping.application.dto.StoreRegistrationRequest;
import org.balanceus.topping.domain.model.Store;
import org.balanceus.topping.domain.model.StoreCategory;
import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.application.exception.ApplicationErrorCode;
import org.balanceus.topping.application.exception.ApplicationException;
import org.balanceus.topping.domain.repository.StoreRepository;
import org.balanceus.topping.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class StoreService {

    private final StoreRepository storeRepository;
    private final UserRepository userRepository;

    public Store registerStore(StoreRegistrationRequest request, UUID userUuid) {
        StoreForm form = new StoreForm();
        form.setName(request.getName());
        form.setCategory(request.getCategory());
        form.setAddress(request.getAddress());
        form.setContactNumber(request.getContactNumber());
        form.setBusinessHours(request.getBusinessHours());
        form.setMainImageUrl(request.getMainImageUrl());
        form.setSnsOrWebsiteLink(request.getSnsOrWebsiteLink());
        return registerStore(form, userUuid);
    }

    public Store registerStore(StoreForm form, UUID userUuid) {
        User user = userRepository.findById(userUuid)
            .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.NOT_FOUND, "User not found"));

        if (storeRepository.existsByUser(user)) {
            throw new ApplicationException(ApplicationErrorCode.ALREADY_EXISTS, "User already has a registered store");
        }

        if (storeRepository.existsByName(form.getName())) {
            throw new ApplicationException(ApplicationErrorCode.ALREADY_EXISTS, "Store name already exists");
        }

        Store store = new Store();
        store.setUser(user);
        applyBasicFields(store, form);
        applyAdditionalFields(store, form);
        return storeRepository.save(store);
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

    public Store updateStore(UUID storeUuid, StoreForm form, UUID userUuid) {
        Store store = storeRepository.findById(storeUuid)
            .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.NOT_FOUND, "Store not found"));

        if (!store.getUser().getUuid().equals(userUuid)) {
            throw new ApplicationException(ApplicationErrorCode.FORBIDDEN, "You can only update your own store");
        }

        if (!store.getName().equals(form.getName()) && storeRepository.existsByName(form.getName())) {
            throw new ApplicationException(ApplicationErrorCode.ALREADY_EXISTS, "Store name already exists");
        }

        applyBasicFields(store, form);
        applyAdditionalFields(store, form);
        return storeRepository.save(store);
    }

    @Transactional(readOnly = true)
    public List<Store> getAllStores() {
        return storeRepository.findAllByOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public List<Store> getStoresByCategory(StoreCategory category) {
        return storeRepository.findByCategoryOrderByNameAsc(category);
    }

    @Transactional(readOnly = true)
    public List<Store> getStoresByCategory(String categoryString) {
        if (categoryString == null || categoryString.trim().isEmpty()) {
            return getAllStores();
        }
        StoreCategory category = StoreCategory.fromString(categoryString);
        return getStoresByCategory(category);
    }

    private void applyBasicFields(Store store, StoreForm form) {
        store.setName(form.getName());
        store.setAddress(form.getAddress());
        store.setContactNumber(form.getContactNumber());
        store.setBusinessHours(form.getBusinessHours());
        store.setCategory(StoreCategory.fromString(form.getCategory()));
        store.setMainImageUrl(form.getMainImageUrl());
        store.setSnsOrWebsiteLink(form.getSnsOrWebsiteLink());
    }

    private void applyAdditionalFields(Store store, StoreForm form) {
        if (form.getDescription() != null && !form.getDescription().trim().isEmpty()) {
            store.setDescription(form.getDescription().trim());
        }

        if (form.getIsCollaborationOpen() != null) {
            store.setIsCollaborationOpen(form.getIsCollaborationOpen());
        }

        var tags = form.getTagsList();
        if (!tags.isEmpty()) {
            store.getTags().clear();
            tags.forEach(store::addTag);
        }
    }
    
}
