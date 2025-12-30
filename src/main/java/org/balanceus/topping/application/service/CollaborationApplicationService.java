package org.balanceus.topping.application.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.balanceus.topping.domain.model.Collaboration;
import org.balanceus.topping.domain.model.CollaborationProposal;
import org.balanceus.topping.domain.model.Product;
import org.balanceus.topping.domain.model.ProposalSource;
import org.balanceus.topping.domain.model.Store;
import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.domain.repository.CollaborationProposalRepository;
import org.balanceus.topping.domain.repository.CollaborationRepository;
import org.balanceus.topping.domain.repository.ProductRepository;
import org.balanceus.topping.domain.repository.StoreRepository;
import org.balanceus.topping.domain.repository.UserRepository;
import org.balanceus.topping.presentation.dto.collaboration.CollaborationApplyViewModel;
import org.balanceus.topping.presentation.dto.collaboration.CollaborationCardView;
import org.balanceus.topping.presentation.dto.collaboration.CollaborationProposalForm;
import org.balanceus.topping.presentation.dto.collaboration.ProductCategoryView;
import org.balanceus.topping.presentation.dto.collaboration.ProductOptionView;
import org.balanceus.topping.presentation.dto.collaboration.ProductSummaryView;
import org.balanceus.topping.presentation.dto.collaboration.StoreOptionView;
import org.balanceus.topping.presentation.dto.collaboration.StoreSummaryView;
import org.balanceus.topping.presentation.dto.collaboration.UserSummaryView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CollaborationApplicationService {

    private static final int STORE_PAGE_SIZE = 100;

    private final CollaborationRepository collaborationRepository;
    private final CollaborationProposalRepository collaborationProposalRepository;
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final ProductService productService;
    private final CollaborationService collaborationService;
    private final ChatService chatService;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public List<CollaborationCardView> getCollaborationCards() {
        return collaborationRepository.findAll().stream()
                .map(this::mapCollaboration)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CollaborationCardView> getCollaborationsForUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CollaborationApplicationException(
                        "user_not_found",
                        null,
                        "User not found: " + userId));

        return storeRepository.findByUser(user)
                .map(collaborationRepository::findByStoreParticipation)
                .orElse(List.of())
                .stream()
                .map(this::mapCollaboration)
                .toList();
    }

    @Transactional(readOnly = true)
    public CollaborationApplyViewModel prepareApplyForm(Optional<UUID> productId,
                                                        Optional<UUID> storeId,
                                                        String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CollaborationApplicationException(
                        "user_not_found",
                        "redirect:/login?error=user_not_found",
                        "Authenticated user not found: " + userEmail));

        boolean isBusinessOwner = user.getRole() != null && "ROLE_BUSINESS_OWNER".equals(user.getRole().name());

        Optional<Store> userStoreOpt = storeRepository.findByUser(user);
        if (isBusinessOwner && userStoreOpt.isEmpty()) {
            throw new CollaborationApplicationException(
                    "no_store_registered",
                    "redirect:/stores/register?error=no_store_registered",
                    "Business owner has no registered store");
        }

        StoreOptionView userStoreView = userStoreOpt.map(this::mapStoreOption).orElse(null);
        List<ProductOptionView> userProducts = userStoreOpt
                .map(store -> productService.getProductsByCreator(user.getUuid()))
                .orElse(List.of())
                .stream()
                .map(this::mapProductOption)
                .toList();

        List<Store> allStoresRaw = storeRepository.findAll(org.springframework.data.domain.PageRequest.of(0, STORE_PAGE_SIZE));
        List<Store> allStores = allStoresRaw;
        if (isBusinessOwner && userStoreOpt.isPresent()) {
            UUID userStoreId = userStoreOpt.get().getUuid();
            allStores = allStoresRaw.stream()
                    .filter(store -> !store.getUuid().equals(userStoreId))
                    .toList();
        }
        List<Product> allProducts = productRepository.findAll();

        StoreOptionView targetStore = storeId
                .flatMap(storeRepository::findById)
                .map(this::mapStoreOption)
                .orElse(null);

        ProductOptionView targetProduct = productId
                .flatMap(productRepository::findById)
                .map(this::mapProductOption)
                .orElse(null);

        CollaborationApplyViewModel viewModel = CollaborationApplyViewModel.builder()
                .user(mapUser(user))
                .businessOwner(isBusinessOwner)
                .userStore(userStoreView)
                .userProducts(userProducts)
                .allStores(allStores.stream().map(this::mapStoreOption).toList())
                .allProducts(allProducts.stream().map(this::mapProductOption).toList())
                .targetStore(targetStore)
                .targetProduct(targetProduct)
                .storeDataJson(buildStoreDataJson(allStores))
                .build();

        return viewModel;
    }

    @Transactional
    public UUID submitProposal(CollaborationProposalForm form, String applicantEmail) {
        User applicant = userRepository.findByEmail(applicantEmail)
                .orElseThrow(() -> new CollaborationApplicationException(
                        "user_not_found",
                        "redirect:/login?error=user_not_found",
                        "Authenticated user not found: " + applicantEmail));

        validateMandatoryFields(form);

        LocalDate startDate = parseDate(form.getStartDate(), "invalid_date_format", "시작일이 필요합니다.");
        LocalDate endDate = parseDate(form.getEndDate(), "invalid_date_format", "종료일이 필요합니다.");
        if (!endDate.isAfter(startDate)) {
            throw new CollaborationApplicationException(
                    "invalid_date_range",
                    "redirect:/collaborations/apply?error=invalid_date_range",
                    "End date must be after start date");
        }

        Store targetStore = Optional.ofNullable(form.getTargetStoreId())
                .flatMap(storeRepository::findById)
                .orElseThrow(() -> new CollaborationApplicationException(
                        "target_store_not_found",
                        "redirect:/collaborations/apply?error=target_store_not_found",
                        "Target store not found"));

        Store sourceStore = storeRepository.findByUser(applicant).orElse(null);
        if (form.getSourceStoreId() != null && (sourceStore == null
                || !form.getSourceStoreId().equals(sourceStore.getUuid()))) {
            throw new CollaborationApplicationException(
                    "source_store_mismatch",
                    "redirect:/collaborations/apply?error=source_store_mismatch",
                    "Source store mismatch");
        }

        Product proposerProduct = Optional.ofNullable(form.getProposerProductId())
                .flatMap(productRepository::findById)
                .orElse(null);
        if (proposerProduct != null && sourceStore != null
                && !proposerProduct.getStore().getUuid().equals(sourceStore.getUuid())) {
            throw new CollaborationApplicationException(
                    "source_product_mismatch",
                    "redirect:/collaborations/apply?error=source_product_mismatch",
                    "Source product does not belong to the applicant store");
        }

        Product targetProduct = Optional.ofNullable(form.getTargetProductId())
                .flatMap(productRepository::findById)
                .orElse(null);
        if (targetProduct != null
                && !targetProduct.getStore().getUuid().equals(targetStore.getUuid())) {
            throw new CollaborationApplicationException(
                    "target_product_mismatch",
                    "redirect:/collaborations/apply?error=target_product_mismatch",
                    "Target product does not belong to the selected store");
        }

        CollaborationProposal proposal = collaborationService.propose(
                ProposalSource.CUSTOMER,
                applicant,
                sourceStore,
                targetStore,
                proposerProduct,
                targetProduct,
                form.getCollaborationTitle().trim(),
                form.getDescription().trim(),
                startDate,
                endDate);

        if (form.getCollaborationDuration() != null && !form.getCollaborationDuration().isBlank()) {
            proposal.setDuration(form.getCollaborationDuration().trim());
        }
        if (form.getCollaborationLocation() != null && !form.getCollaborationLocation().isBlank()) {
            proposal.setLocation(form.getCollaborationLocation().trim());
        }
        if (form.getRevenueStructure() != null && !form.getRevenueStructure().isBlank()) {
            proposal.setProfitShare(form.getRevenueStructure().trim());
        }

        // Handle new fields for redesigned form
        if (form.getExpectedBenefit() != null && !form.getExpectedBenefit().isBlank()) {
            proposal.setExpectedBenefit(form.getExpectedBenefit().trim());
        }
        if (form.getCollaborationStartDate() != null && !form.getCollaborationStartDate().isBlank()) {
            try {
                LocalDate collabStart = LocalDate.parse(form.getCollaborationStartDate());
                proposal.setCollaborationStartDate(collabStart);
            } catch (Exception e) {
                log.warn("Failed to parse collaboration start date: {}", form.getCollaborationStartDate());
            }
        }
        if (form.getCollaborationEndDate() != null && !form.getCollaborationEndDate().isBlank()) {
            try {
                LocalDate collabEnd = LocalDate.parse(form.getCollaborationEndDate());
                proposal.setCollaborationEndDate(collabEnd);
            } catch (Exception e) {
                log.warn("Failed to parse collaboration end date: {}", form.getCollaborationEndDate());
            }
        }

        collaborationProposalRepository.save(proposal);

        try {
            chatService.createChatRoomForCollaborationProposal(proposal.getUuid());
            log.info("Chat room created for collaboration proposal: {}", proposal.getUuid());
        } catch (Exception chatError) {
            log.warn("Failed to create chat room for proposal {}: {}", proposal.getUuid(), chatError.getMessage());
        }

        return proposal.getUuid();
    }

    @Transactional
    public void acceptCollaboration(UUID collaborationId, String userEmail) {
        User currentUser = resolveUser(userEmail);
        Collaboration collaboration = collaborationRepository.findById(collaborationId)
                .orElseThrow(() -> new CollaborationApplicationException(
                        "collaboration_not_found",
                        "redirect:/mypage/received?error=collaboration_not_found",
                        "Collaboration not found: " + collaborationId));

        validateCollaborationOwnership(collaboration, currentUser);

        if (collaboration.getStatus() != Collaboration.CollaborationStatus.PENDING) {
            throw new CollaborationApplicationException(
                    "already_processed",
                    "redirect:/mypage/received?error=already_processed",
                    "Collaboration is already processed");
        }

        collaborationService.acceptCollaboration(collaborationId);
    }

    @Transactional
    public void rejectCollaboration(UUID collaborationId, String userEmail) {
        User currentUser = resolveUser(userEmail);
        Collaboration collaboration = collaborationRepository.findById(collaborationId)
                .orElseThrow(() -> new CollaborationApplicationException(
                        "collaboration_not_found",
                        "redirect:/mypage/received?error=collaboration_not_found",
                        "Collaboration not found: " + collaborationId));

        validateCollaborationOwnership(collaboration, currentUser);

        if (collaboration.getStatus() != Collaboration.CollaborationStatus.PENDING) {
            throw new CollaborationApplicationException(
                    "already_processed",
                    "redirect:/mypage/received?error=already_processed",
                    "Collaboration is already processed");
        }

        collaborationService.rejectCollaboration(collaborationId);
    }

    private void validateMandatoryFields(CollaborationProposalForm form) {
        if (form.getCollaborationTitle() == null || form.getCollaborationTitle().isBlank()) {
            throw new CollaborationApplicationException(
                    "title_required",
                    "redirect:/collaborations/apply?error=title_required",
                    "Collaboration title is mandatory");
        }
        if (form.getDescription() == null || form.getDescription().isBlank()) {
            throw new CollaborationApplicationException(
                    "description_required",
                    "redirect:/collaborations/apply?error=description_required",
                    "Description is mandatory");
        }
        if (form.getTargetStoreId() == null) {
            throw new CollaborationApplicationException(
                    "target_store_required",
                    "redirect:/collaborations/apply?error=target_store_required",
                    "Target store must be selected");
        }
    }

    private LocalDate parseDate(String value, String errorCode, String message) {
        try {
            return LocalDate.parse(value);
        } catch (Exception e) {
            throw new CollaborationApplicationException(
                    errorCode,
                    "redirect:/collaborations/apply?error=" + errorCode,
                    message);
        }
    }

    private void validateCollaborationOwnership(Collaboration collaboration, User currentUser) {
        if (collaboration.getPartnerStore() == null) {
            throw new CollaborationApplicationException(
                    "missing_store_data",
                    "redirect:/mypage/received?error=missing_store_data",
                    "Collaboration missing partner store");
        }

        if (collaboration.getPartnerStore().getUser() == null
                || !collaboration.getPartnerStore().getUser().getUuid().equals(currentUser.getUuid())) {
            throw new CollaborationApplicationException(
                    "unauthorized_action",
                    "redirect:/mypage/received?error=unauthorized_action",
                    "Current user cannot modify this collaboration");
        }
    }

    private CollaborationCardView mapCollaboration(Collaboration collaboration) {
        return CollaborationCardView.builder()
                .uuid(collaboration.getUuid())
                .status(collaboration.getStatus())
                .description(collaboration.getDescription())
                .createdAt(collaboration.getCreatedAt())
                .partnerProduct(mapProduct(collaboration.getPartnerProduct()))
                .initiatorProduct(mapProduct(collaboration.getInitiatorProduct()))
                .initiatorStore(mapStore(collaboration.getInitiatorStore()))
                .partnerStore(mapStore(collaboration.getPartnerStore()))
                .build();
    }

    private ProductSummaryView mapProduct(Product product) {
        if (product == null) {
            return null;
        }

        ProductCategoryView categoryView = null;
        if (product.getCategory() != null) {
            categoryView = ProductCategoryView.builder()
                    .name(product.getCategory().name())
                    .displayName(product.getCategory().getDisplayName())
                    .build();
        }

        String productTypeDisplay = product.getProductType() != null ? product.getProductType().getDisplayName() : null;

        return ProductSummaryView.builder()
                .uuid(product.getUuid())
                .name(product.getName())
                .productType(product.getProductType() != null ? product.getProductType().name() : null)
                .productTypeDisplayName(productTypeDisplay)
                .category(categoryView)
                .price(product.getPrice())
                .available(product.getIsAvailable())
                .creator(mapUser(product.getCreator()))
                .store(mapStore(product.getStore()))
                .build();
    }

    private StoreSummaryView mapStore(Store store) {
        if (store == null) {
            return null;
        }

        return StoreSummaryView.builder()
                .uuid(store.getUuid())
                .name(store.getName())
                .description(store.getDescription())
                .category(store.getCategory() != null ? store.getCategory().name() : null)
                .user(mapUser(store.getUser()))
                .build();
    }

    private ProductOptionView mapProductOption(Product product) {
        return ProductOptionView.builder()
                .uuid(product.getUuid())
                .name(product.getName())
                .category(product.getCategory() != null ? product.getCategory().name() : null)
                .productType(product.getProductType() != null ? product.getProductType().name() : null)
                .available(product.getIsAvailable())
                .price(product.getPrice())
                .storeId(product.getStore() != null ? product.getStore().getUuid() : null)
                .build();
    }

    private StoreOptionView mapStoreOption(Store store) {
        return StoreOptionView.builder()
                .uuid(store.getUuid())
                .name(store.getName())
                .category(store.getCategory() != null ? store.getCategory().name() : null)
                .description(store.getDescription())
                .build();
    }

    private UserSummaryView mapUser(User user) {
        if (user == null) {
            return null;
        }

        return UserSummaryView.builder()
                .uuid(user.getUuid())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .build();
    }

    private User resolveUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CollaborationApplicationException(
                        "user_not_found",
                        "redirect:/login?error=user_not_found",
                        "Authenticated user not found: " + email));
    }

    private String buildStoreDataJson(List<Store> stores) {
        Map<String, Object> storeData = new HashMap<>();
        for (Store store : stores) {
            Map<String, Object> storePayload = new HashMap<>();
            storePayload.put("uuid", store.getUuid().toString());
            storePayload.put("name", store.getName());
            storePayload.put("category", store.getCategory() != null ? store.getCategory().name() : "");

            List<Map<String, Object>> productPayloads = new ArrayList<>();
            for (Product product : productRepository.findByStore(store)) {
                if (!Boolean.TRUE.equals(product.getIsAvailable())) {
                    continue;
                }
                Map<String, Object> productPayload = new HashMap<>();
                productPayload.put("uuid", product.getUuid().toString());
                productPayload.put("name", product.getName());
                productPayload.put("category", product.getCategory() != null ? product.getCategory().name() : "");
                productPayload.put("productType", product.getProductType() != null ? product.getProductType().name() : "");
                productPayload.put("isAvailable", product.getIsAvailable());
                productPayload.put("price", product.getPrice());
                productPayloads.add(productPayload);
            }
            storePayload.put("products", productPayloads);
            storeData.put(store.getUuid().toString(), storePayload);
        }

        try {
            return objectMapper.writeValueAsString(storeData);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize store data: {}", e.getMessage());
            return "{}";
        }
    }
}
