package org.balanceus.topping.application.service;

import java.util.Optional;
import java.util.UUID;

import org.balanceus.topping.application.dto.StoreEngagementResult;
import org.balanceus.topping.application.exception.ApplicationErrorCode;
import org.balanceus.topping.application.exception.ApplicationException;
import org.balanceus.topping.domain.model.Store;
import org.balanceus.topping.domain.model.StoreLike;
import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.domain.model.Wishlist;
import org.balanceus.topping.domain.repository.StoreLikeRepository;
import org.balanceus.topping.domain.repository.UserRepository;
import org.balanceus.topping.domain.repository.WishlistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class StoreEngagementService {

    private final StoreService storeService;
    private final UserRepository userRepository;
    private final StoreLikeRepository storeLikeRepository;
    private final WishlistRepository wishlistRepository;

    @Transactional
    public StoreEngagementResult toggleStoreLike(UUID storeId, UUID userUuid) {
        Store store = resolveStore(storeId);
        User user = resolveUser(userUuid);

        Optional<StoreLike> existing = storeLikeRepository.findByUserAndStore(user, store);
        boolean active;
        if (existing.isPresent()) {
            storeLikeRepository.delete(existing.get());
            active = false;
            log.debug("User {} unliked store {}", user.getEmail(), store.getName());
        } else {
            StoreLike like = new StoreLike();
            like.setStore(store);
            like.setUser(user);
            storeLikeRepository.save(like);
            active = true;
            log.debug("User {} liked store {}", user.getEmail(), store.getName());
        }

        long likeCount = storeLikeRepository.countByStore(store);
        return new StoreEngagementResult(active, likeCount);
    }

    @Transactional
    public StoreEngagementResult toggleStoreWishlist(UUID storeId, UUID userUuid) {
        Store store = resolveStore(storeId);
        User user = resolveUser(userUuid);

        Optional<Wishlist> existing = wishlistRepository.findByUserAndStore(user, store);
        boolean active;
        if (existing.isPresent()) {
            wishlistRepository.delete(existing.get());
            active = false;
            log.debug("User {} removed store {} from wishlist", user.getEmail(), store.getName());
        } else {
            Wishlist wishlist = new Wishlist();
            wishlist.setStore(store);
            wishlist.setUser(user);
            wishlistRepository.save(wishlist);
            active = true;
            log.debug("User {} added store {} to wishlist", user.getEmail(), store.getName());
        }

        long wishlistCount = wishlistRepository.countByStore(store);
        return new StoreEngagementResult(active, wishlistCount);
    }

    public boolean isStoreWishlisted(UUID storeId, UUID userUuid) {
        Store store = resolveStore(storeId);
        User user = resolveUser(userUuid);
        return wishlistRepository.existsByUserAndStore(user, store);
    }

    private Store resolveStore(UUID storeId) {
        return storeService.getStoreById(storeId)
            .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.NOT_FOUND, "Store not found"));
    }

    private User resolveUser(UUID userUuid) {
        return userRepository.findById(userUuid)
            .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.NOT_FOUND, "User not found"));
    }
}
