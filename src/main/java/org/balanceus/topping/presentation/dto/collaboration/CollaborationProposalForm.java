package org.balanceus.topping.presentation.dto.collaboration;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CollaborationProposalForm {
    private UUID sourceStoreId;
    private UUID targetStoreId;
    private UUID proposerProductId;
    private UUID targetProductId;
    private String collaborationTitle;
    private String description;
    private String startDate;
    private String endDate;
    private String category;
    private String collaborationDuration;
    private String collaborationLocation;
    private String revenueStructure;
}
