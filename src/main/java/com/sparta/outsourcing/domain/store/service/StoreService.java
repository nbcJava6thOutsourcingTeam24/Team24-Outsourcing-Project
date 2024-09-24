package com.sparta.outsourcing.domain.store.service;

import com.sparta.outsourcing.domain.menu.dto.response.UpdateMenuResponseDto;
import com.sparta.outsourcing.domain.menu.entity.Menu;
import com.sparta.outsourcing.domain.menu.repository.MenuRepository;
import com.sparta.outsourcing.exception.ApplicationException;
import com.sparta.outsourcing.exception.ErrorCode;
import com.sparta.outsourcing.domain.store.dto.request.StoreRequestDto;
import com.sparta.outsourcing.domain.store.dto.response.StoreResponseDto;
import com.sparta.outsourcing.domain.store.entity.Store;
import com.sparta.outsourcing.domain.store.repository.StoreRepository;
import com.sparta.outsourcing.domain.user.dto.AuthUser;
import com.sparta.outsourcing.domain.user.entity.User;
import com.sparta.outsourcing.domain.user.enums.UserRole;
import com.sparta.outsourcing.domain.user.repository.UserRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final MenuRepository menuRepository;

    @Transactional
    public void createStore(AuthUser authUser, StoreRequestDto storeRequestDto) {

        User user = userRepository.findById(authUser.getId())
            .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));

        if (authUser.getUserRole() != UserRole.OWNER) {
            throw new ApplicationException(ErrorCode.USER_FORBIDDEN);
        }

        List<Store> stores = storeRepository.findAllByOwnerIdAndStatusFalse(authUser.getId());
        if (stores.size() >= 3) {
            throw new ApplicationException(ErrorCode.INVALID_STORE_SIZE);
        }

        storeRepository.save(new Store(storeRequestDto, user));
    }

    @Transactional
    public void updateStore(AuthUser authUser, Long storeId, StoreRequestDto storeRequestDto) {
        User user = userRepository.findById(authUser.getId())
            .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));

        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new ApplicationException(ErrorCode.STORE_NOT_FOUND));

        if (store.isStatus()) {
            throw new ApplicationException(ErrorCode.STORE_NOT_FOUND);
        }

        if (store.getOwner().getId() != user.getId()) {
            throw new ApplicationException(ErrorCode.USER_FORBIDDEN);
        }
        store.update(storeRequestDto);
    }


    @Transactional(readOnly = true)
    public List<StoreResponseDto> getStoreList(String name) {
        List<Store> stores = storeRepository.findStoreByName(name);

        if (stores.isEmpty()) {
            throw new ApplicationException(ErrorCode.STORE_NOT_FOUND);
        }

        return stores.stream()
            .map(store -> new StoreResponseDto(
                store.getId(),
                store.getName(),
                store.getOpenTime(),
                store.getCloseTime(),
                store.getMinPrice(),
                store.getNotice()
            ))
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StoreResponseDto getStore(Long storeId) {
        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new ApplicationException(ErrorCode.STORE_NOT_FOUND));

        if (store.isStatus()) {
            throw new ApplicationException(ErrorCode.STORE_NOT_FOUND);
        }

        List<Menu> menuList = menuRepository.findAllByStoreId(store.getId());

        List<UpdateMenuResponseDto> menuResponseDtoList = menuList.stream()
            .map(UpdateMenuResponseDto::new)
            .collect(Collectors.toList());

        return new StoreResponseDto(store.getId(), store.getName(),
            store.getOpenTime(), store.getCloseTime(), store.getMinPrice(), store.getNotice(), menuResponseDtoList);
    }

    @Transactional
    public void deleteStore(AuthUser authUser, Long storeId) {
        User user = userRepository.findById(authUser.getId())
            .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));

        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new ApplicationException(ErrorCode.STORE_NOT_FOUND));

        if (store.isStatus()) {
            throw new ApplicationException(ErrorCode.STORE_NOT_FOUND);
        }

        if (!user.getId().equals(store.getOwner().getId())) {
            throw new ApplicationException(ErrorCode.USER_FORBIDDEN);
        }

        store.delete();
        menuRepository.updateMenu(storeId);
    }

    @Transactional
    public void createAdvertisement(AuthUser authUser, Long storeId) {
        User user = userRepository.findById(authUser.getId())
            .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));

        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new ApplicationException(ErrorCode.STORE_NOT_FOUND));

        if (store.isStatus()) {
            throw new ApplicationException(ErrorCode.STORE_NOT_FOUND);
        }

        if (store.getOwner().getId()!= user.getId()) {
            throw new ApplicationException(ErrorCode.USER_FORBIDDEN);
        }

        store.enableAdvertisement();
    }
}
