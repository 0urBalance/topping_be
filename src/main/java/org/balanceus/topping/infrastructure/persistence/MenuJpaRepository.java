package org.balanceus.topping.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.balanceus.topping.domain.model.Menu;
import org.balanceus.topping.domain.model.Store;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuJpaRepository extends JpaRepository<Menu, UUID> {
    List<Menu> findByStore(Store store);
    List<Menu> findByStoreAndMenuType(Store store, Menu.MenuType menuType);
    List<Menu> findByStoreAndIsAvailable(Store store, Boolean isAvailable);
    List<Menu> findByStoreOrderByMenuTypeAscNameAsc(Store store);
}