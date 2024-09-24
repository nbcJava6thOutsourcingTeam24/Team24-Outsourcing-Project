package com.sparta.outsourcing.domain.menu.repository;

import com.sparta.outsourcing.domain.menu.dto.response.UpdateMenuResponseDto;
import com.sparta.outsourcing.domain.menu.entity.Menu;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {
    Optional<Menu> findByIdAndStoreId(Long menuId, Long storeId);

    List<Menu> findAllByStoreId(Long storeId);

    @Query("UPDATE Menu m SET m.)
    int updateMenu(Long menuId, Long storeId, String name, int price);
    void deleteAllByStoreId(Long storeId);
}
