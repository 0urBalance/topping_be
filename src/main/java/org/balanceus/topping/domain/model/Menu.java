package org.balanceus.topping.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "menus")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Menu {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID uuid;

    @Column(nullable = false)
    @NotBlank(message = "Menu name is required")
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Price is required")
    private BigDecimal price;

    private String imageUrl;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MenuType menuType = MenuType.REGULAR;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MenuCategory category = MenuCategory.BEVERAGE;

    private String ingredients;

    @Column(nullable = false)
    private Boolean isAvailable = true;

    @Column(nullable = false)
    private Integer reviewCount = 0;

    @ManyToOne
    @JoinColumn(name = "store_uuid", nullable = false)
    private Store store;

    public enum MenuType {
        REGULAR("일반 메뉴"),
        SIGNATURE("시그니처 메뉴"),
        COLLABORATION("콜라보 메뉴"),
        SEASONAL("시즌 메뉴");

        private final String displayName;

        MenuType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum MenuCategory {
        BEVERAGE("음료"),
        FOOD("음식"),
        DESSERT("디저트"),
        BAKERY("베이커리"),
        OTHER("기타");

        private final String displayName;

        MenuCategory(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}