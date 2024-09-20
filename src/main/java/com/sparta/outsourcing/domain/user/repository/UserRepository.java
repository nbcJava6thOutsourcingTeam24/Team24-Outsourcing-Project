package com.sparta.outsourcing.domain.user.repository;

import com.sparta.outsourcing.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.NoSuchElementException;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    default User findByIdOrElseThrow(Long id){
        return findById(id).orElseThrow(() ->
                new NoSuchElementException("사용자를 찾을 수 없습니다"));
    }

    default User findByEmailOrElseThrow(String email){
        return findByEmail(email).orElseThrow(() ->
                new NoSuchElementException("사용자를 찾을 수 없습니다"));
    }

    default User findByOrElseThrow(Long id){
        return findById(id).orElseThrow(()->
                new NoSuchElementException("Id가 존재하지 않습니다."));
    }

    Optional<User> findByEmail(String email);
    Optional<User> findByEmailAndDeletedTrue(String email);
    Optional<User> findByEmailAndDeletedFalse(String email);

}

