package com.sparta.outsourcing.domain.store.service;

import com.sparta.outsourcing.domain.store.dto.request.StoreRequestDto;
import com.sparta.outsourcing.domain.store.entity.Store;
import com.sparta.outsourcing.domain.store.repository.StoreRepository;
import com.sparta.outsourcing.domain.user.entity.User;
import com.sparta.outsourcing.domain.user.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class StoreService {

    private final StoreRepository storeRepository;
//    private final UserRepository userRepository;

    public void createStore(StoreRequestDto storeRequestDto) {
//        User user = User.fromAuthUser(authUser);

//        User user = userRepository.findById(1L).orElseThrow(()->new IllegalArgumentException("User not found"));

//        if(user.getUserRole() != UserRole.OWNER)
//        {
//            throw new IllegalArgumentException("Only owner can create store");
//        }
//
//        storeRepository.findByName(storeRequestDto.getName())
//            .orElseThrow(() -> new IllegalArgumentException("Store not found"));

//        storeRepository.save(new Store(storeRequestDto, user));
    }
}
