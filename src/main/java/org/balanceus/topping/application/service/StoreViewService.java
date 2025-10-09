package org.balanceus.topping.application.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.balanceus.topping.application.dto.StoreDetailView;
import org.balanceus.topping.application.exception.ApplicationErrorCode;
import org.balanceus.topping.application.exception.ApplicationException;
import org.balanceus.topping.domain.model.Collaboration;
import org.balanceus.topping.domain.model.Store;
import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.domain.repository.CollaborationRepository;
import org.balanceus.topping.domain.repository.ReviewRepository;
import org.balanceus.topping.domain.repository.StoreLikeRepository;
import org.balanceus.topping.domain.repository.UserRepository;
import org.balanceus.topping.domain.repository.WishlistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreViewService {

    private final StoreService storeService;
    private final CollaborationService collaborationService;
    private final CollaborationRepository collaborationRepository;
    private final StoreLikeRepository storeLikeRepository;
    private final WishlistRepository wishlistRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    public StoreDetailView getStoreDetail(UUID storeId, UUID requesterUuid) {
        Store store = loadStore(storeId);

        boolean isOwner = requesterUuid != null && store.getUser().getUuid().equals(requesterUuid);
        User requester = loadUser(requesterUuid);

        List<Collaboration> acceptedCollaborations =
            collaborationRepository.findByStoreAndStatus(store, Collaboration.CollaborationStatus.ACCEPTED);
        List<Store> collaboratingStores = acceptedCollaborations.stream()
            .map(collaboration -> collaboration.getInitiatorProduct() != null
                ? collaboration.getInitiatorProduct().getStore()
                : null)
            .filter(collaboratingStore -> collaboratingStore != null
                && !collaboratingStore.getUuid().equals(store.getUuid()))
            .distinct()
            .toList();

        long likeCount = storeLikeRepository.countByStore(store);
        long wishlistCount = wishlistRepository.countByStore(store);
        boolean isLiked = requester != null && storeLikeRepository.existsByUserAndStore(requester, store);
        boolean isWishlisted = requester != null && wishlistRepository.existsByUserAndStore(requester, store);

        long reviewCount = reviewRepository.countByStoreAndIsActiveTrue(store);
        Double averageRating = reviewRepository.findAverageRatingByStoreAndIsActiveTrue(store);
        double rating = averageRating != null ? Math.round(averageRating * 10.0) / 10.0 : 0.0;

        return StoreDetailView.builder()
            .store(store)
            .owner(isOwner)
            .popularMenus(nonNullList(store.getPopularProducts()))
            .signatureMenus(nonNullList(store.getSignatureProducts()))
            .likeCount(likeCount)
            .wishlistCount(wishlistCount)
            .liked(isLiked)
            .wishlisted(isWishlisted)
            .reviewCount(reviewCount)
            .rating(rating)
            .collaborationProductCount(collaborationService.getCollaborationProductCount(store))
            .collaboratingStores(collaboratingStores)
            .build();
    }

    private Store loadStore(UUID storeId) {
        Optional<Store> storeWithProducts = storeService.getStoreByIdWithProductsAndTags(storeId);
        if (storeWithProducts.isPresent()) {
            return storeWithProducts.get();
        }

        return storeService.getStoreById(storeId)
            .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.NOT_FOUND, "Store not found"));
    }

    private User loadUser(UUID userUuid) {
        if (userUuid == null) {
            return null;
        }
        return userRepository.findById(userUuid).orElse(null);
    }

    private <T> List<T> nonNullList(List<T> source) {
        return source != null ? source : List.of();
    }
}
