package com.sparta.outsourcing.domain.store.repository;

import com.sparta.outsourcing.domain.store.entity.Store;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface StoreRepository extends JpaRepository<Store, Long> {

    List<Store> findAllByOwnerIdAndStatusFalse(Long ownerId);

    @Query("SELECT s From Store s Where s.name = :storeName AND s.status = false ORDER BY s.isAdvertised DESC")
    List<Store> findStoreByName(String storeName);
}
