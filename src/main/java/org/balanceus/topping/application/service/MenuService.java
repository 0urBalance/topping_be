package org.balanceus.topping.application.service;

import java.util.List;
import java.util.UUID;

import org.balanceus.topping.domain.model.Menu;
import org.balanceus.topping.domain.model.Store;
import org.balanceus.topping.domain.repository.MenuRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class MenuService {

    private final MenuRepository menuRepository;

    @Transactional(readOnly = true)
    public List<Menu> getMenusByStore(Store store) {
        return menuRepository.findByStore(store);
    }

    @Transactional(readOnly = true)
    public List<Menu> getMenusByStoreAndType(Store store, Menu.MenuType menuType) {
        return menuRepository.findByStoreAndMenuType(store, menuType);
    }

    @Transactional(readOnly = true)
    public List<Menu> getAvailableMenusByStore(Store store) {
        return menuRepository.findByStoreAndIsAvailable(store, true);
    }

    @Transactional(readOnly = true)
    public List<Menu> getCollaborationMenus(Store store) {
        return menuRepository.findByStoreAndMenuType(store, Menu.MenuType.COLLABORATION);
    }

    @Transactional(readOnly = true)
    public List<Menu> getSignatureMenus(Store store) {
        return menuRepository.findByStoreAndMenuType(store, Menu.MenuType.SIGNATURE);
    }

    public Menu createMenu(Menu menu) {
        return menuRepository.save(menu);
    }

    public Menu updateMenu(Menu menu) {
        return menuRepository.save(menu);
    }

    public void deleteMenu(UUID menuId) {
        menuRepository.deleteById(menuId);
    }
}