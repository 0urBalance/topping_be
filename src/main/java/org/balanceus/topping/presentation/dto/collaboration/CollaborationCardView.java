package org.balanceus.topping.presentation.dto.collaboration;

import java.time.Instant;
import java.util.UUID;

import org.balanceus.topping.domain.model.Collaboration.CollaborationStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollaborationCardView {
    private UUID uuid;
    private CollaborationStatus status;
    private String description;
    private Instant createdAt;
    private ProductSummaryView partnerProduct;
    private ProductSummaryView initiatorProduct;
    private StoreSummaryView initiatorStore;
    private StoreSummaryView partnerStore;
}
