package org.balanceus.topping.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.balanceus.topping.domain.model.Menu;
import org.balanceus.topping.domain.model.MenuImage;
import org.balanceus.topping.domain.repository.MenuImageRepository;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MenuImageRepositoryImpl implements MenuImageRepository {

    private final MenuImageJpaRepository menuImageJpaRepository;

    @Override
    public MenuImage save(MenuImage menuImage) {
        return menuImageJpaRepository.save(menuImage);
    }

    @Override
    public List<MenuImage> findByMenu(Menu menu) {
        return menuImageJpaRepository.findByMenuOrderByDisplayOrderAscCreatedAtAsc(menu);
    }

    @Override
    public List<MenuImage> findByMenuOrderByDisplayOrderAscCreatedAtAsc(Menu menu) {
        return menuImageJpaRepository.findByMenuOrderByDisplayOrderAscCreatedAtAsc(menu);
    }

    @Override
    public List<MenuImage> findByMenuAndImageType(Menu menu, MenuImage.ImageType imageType) {
        return menuImageJpaRepository.findByMenuAndImageType(menu, imageType);
    }

    @Override
    public void deleteByUuid(UUID uuid) {
        menuImageJpaRepository.deleteById(uuid);
    }

    @Override
    public void deleteByMenu(Menu menu) {
        menuImageJpaRepository.deleteByMenu(menu);
    }

    @Override
    public long countByMenu(Menu menu) {
        return menuImageJpaRepository.countByMenu(menu);
    }
}