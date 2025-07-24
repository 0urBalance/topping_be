package org.balanceus.topping.domain.repository;

import java.util.List;
import java.util.UUID;

import org.balanceus.topping.domain.model.Menu;
import org.balanceus.topping.domain.model.Store;

public interface MenuRepository {
    Menu save(Menu menu);
    List<Menu> findByStore(Store store);
    List<Menu> findByStoreAndMenuType(Store store, Menu.MenuType menuType);
    List<Menu> findByStoreAndIsAvailable(Store store, Boolean isAvailable);
    void deleteById(UUID menuId);
}