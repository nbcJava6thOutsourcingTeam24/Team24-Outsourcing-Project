package com.sparta.outsourcing.domain.store.service;

import com.sparta.outsourcing.domain.store.dto.request.StoreRequestDto;
import com.sparta.outsourcing.domain.store.dto.response.StoreResponseDto;
import com.sparta.outsourcing.domain.store.entity.Store;
import com.sparta.outsourcing.domain.store.repository.StoreRepository;
import com.sparta.outsourcing.domain.user.enums.UserRole;
import java.util.List;
import java.util.stream.Collectors;
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
//
//        User user = userRepository.findById(1L)
//            .orElseThrow(() -> new IllegalArgumentException("User not found"));
//
//        if(user.getUserRole() != UserRole.OWNER)
//        {
//            throw new IllegalArgumentException("Only owner can create store");
//        }
//
//        List<Store> stores = storeRepository.findAllByOwnerId(user.getId());
//        if(stores.size() >= 3)
//        {
//            throw new IllegalArgumentException("store size is too large");
//        }
//
//        storeRepository.save(new Store(storeRequestDto, user));
    }

    public void updateStore(Long storeId, StoreRequestDto storeRequestDto) {
//        User user = User.fromAuthUser(authUser);

        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new IllegalArgumentException("Store not found"));

//        if (store.getOwner().getId() != user.getId()) {
//            throw new IllegalArgumentException("Only owner can update store");
//        }
        store.update(storeRequestDto);
    }


    public List<StoreResponseDto> getStoreList() {
        List<Store> stores = storeRepository.findAll();
        return stores.stream()
           .map(StoreResponseDto::from)
           .collect(Collectors.toList());
    }
}
