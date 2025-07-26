package org.balanceus.topping.domain.repository;

import java.util.List;
import java.util.UUID;

import org.balanceus.topping.domain.model.Menu;
import org.balanceus.topping.domain.model.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MenuRepository {
    Menu save(Menu menu);
    List<Menu> findByStore(Store store);
    List<Menu> findByStoreAndMenuType(Store store, Menu.MenuType menuType);
    List<Menu> findByStoreAndIsAvailable(Store store, Boolean isAvailable);
    Page<Menu> findByMenuTypeOrderByReviewCountDesc(Menu.MenuType menuType, Pageable pageable);
    Page<Menu> findAllByOrderByReviewCountDesc(Pageable pageable);
    void deleteById(UUID menuId);
}