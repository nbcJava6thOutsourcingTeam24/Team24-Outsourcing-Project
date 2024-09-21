package com.sparta.outsourcing.domain.store.repository;

import com.sparta.outsourcing.domain.store.entity.Store;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository extends JpaRepository<Store, Long> {

    Optional<Store> findByName(String name);

    List<Store> findAllByOwnerIdAndStatusFalse(Long ownerId);

    List<Store> findAllByStatusFalse();
}
