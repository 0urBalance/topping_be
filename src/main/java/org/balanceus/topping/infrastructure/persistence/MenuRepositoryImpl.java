package org.balanceus.topping.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.balanceus.topping.domain.model.Menu;
import org.balanceus.topping.domain.model.Store;
import org.balanceus.topping.domain.repository.MenuRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MenuRepositoryImpl implements MenuRepository {

    private final MenuJpaRepository menuJpaRepository;

    @Override
    public Menu save(Menu menu) {
        return menuJpaRepository.save(menu);
    }

    @Override
    public List<Menu> findByStore(Store store) {
        return menuJpaRepository.findByStoreOrderByMenuTypeAscNameAsc(store);
    }

    @Override
    public List<Menu> findByStoreAndMenuType(Store store, Menu.MenuType menuType) {
        return menuJpaRepository.findByStoreAndMenuType(store, menuType);
    }

    @Override
    public List<Menu> findByStoreAndIsAvailable(Store store, Boolean isAvailable) {
        return menuJpaRepository.findByStoreAndIsAvailable(store, isAvailable);
    }

    @Override
    public Page<Menu> findByMenuTypeOrderByReviewCountDesc(Menu.MenuType menuType, Pageable pageable) {
        return menuJpaRepository.findByMenuTypeOrderByReviewCountDesc(menuType, pageable);
    }

    @Override
    public Page<Menu> findAllByOrderByReviewCountDesc(Pageable pageable) {
        return menuJpaRepository.findAllByOrderByReviewCountDesc(pageable);
    }

    @Override
    public void deleteById(UUID menuId) {
        menuJpaRepository.deleteById(menuId);
    }
}