package org.balanceus.topping.domain.model;

public enum ProposalSource {
    BUSINESS_OWNER("사업주"),
    CUSTOMER("고객");

    private final String displayName;

    ProposalSource(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}