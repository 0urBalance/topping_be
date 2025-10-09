package org.balanceus.topping.presentation.dto.collaboration;

import java.util.UUID;

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
public class StoreOptionView {
    private UUID uuid;
    private String name;
    private String category;
    private String description;
}
