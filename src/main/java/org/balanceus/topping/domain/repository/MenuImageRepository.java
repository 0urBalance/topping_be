package org.balanceus.topping.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.balanceus.topping.domain.model.Menu;
import org.balanceus.topping.domain.model.MenuImage;

public interface MenuImageRepository {
    
    MenuImage save(MenuImage menuImage);
    
    Optional<MenuImage> findByUuid(UUID uuid);
    
    List<MenuImage> findByMenu(Menu menu);
    
    List<MenuImage> findByMenuOrderByDisplayOrderAscCreatedAtAsc(Menu menu);
    
    List<MenuImage> findByMenuAndImageType(Menu menu, MenuImage.ImageType imageType);
    
    void deleteByUuid(UUID uuid);
    
    void deleteByMenu(Menu menu);
    
    long countByMenu(Menu menu);
}