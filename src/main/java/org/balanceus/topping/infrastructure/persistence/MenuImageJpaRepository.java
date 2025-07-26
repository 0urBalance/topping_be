package org.balanceus.topping.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.balanceus.topping.domain.model.Menu;
import org.balanceus.topping.domain.model.MenuImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MenuImageJpaRepository extends JpaRepository<MenuImage, UUID> {
    
    List<MenuImage> findByMenuOrderByDisplayOrderAscCreatedAtAsc(Menu menu);
    
    List<MenuImage> findByMenuAndImageType(Menu menu, MenuImage.ImageType imageType);
    
    void deleteByMenu(Menu menu);
    
    long countByMenu(Menu menu);
}