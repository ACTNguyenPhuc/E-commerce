package com.ecommerce.modules.user.service;

import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.modules.user.dto.AddressRequest;
import com.ecommerce.modules.user.dto.AddressResponse;
import com.ecommerce.modules.user.entity.AddressType;
import com.ecommerce.modules.user.entity.UserAddress;
import com.ecommerce.modules.user.repository.UserAddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final UserAddressRepository repo;

    @Transactional(readOnly = true)
    public List<AddressResponse> list(Long userId) {
        return repo.findByUserIdOrderByIsDefaultDescIdAsc(userId).stream()
                .map(AddressResponse::from).toList();
    }

    @Transactional
    public AddressResponse create(Long userId, AddressRequest req) {
        boolean isDefault = Boolean.TRUE.equals(req.isDefault())
                || repo.findFirstByUserIdAndIsDefaultTrue(userId).isEmpty();
        if (isDefault) {
            repo.clearDefault(userId);
        }
        UserAddress a = UserAddress.builder()
                .userId(userId)
                .recipientName(req.recipientName())
                .recipientPhone(req.recipientPhone())
                .province(req.province())
                .district(req.district())
                .ward(req.ward())
                .streetAddress(req.streetAddress())
                .addressType(req.addressType() != null ? req.addressType() : AddressType.home)
                .isDefault(isDefault)
                .build();
        return AddressResponse.from(repo.save(a));
    }

    @Transactional
    public AddressResponse update(Long userId, Long id, AddressRequest req) {
        UserAddress a = repo.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", id));
        a.setRecipientName(req.recipientName());
        a.setRecipientPhone(req.recipientPhone());
        a.setProvince(req.province());
        a.setDistrict(req.district());
        a.setWard(req.ward());
        a.setStreetAddress(req.streetAddress());
        if (req.addressType() != null) a.setAddressType(req.addressType());
        if (Boolean.TRUE.equals(req.isDefault())) {
            repo.clearDefault(userId);
            a.setIsDefault(true);
        }
        return AddressResponse.from(a);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        UserAddress a = repo.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", id));
        repo.delete(a);
    }

    @Transactional
    public AddressResponse setDefault(Long userId, Long id) {
        UserAddress a = repo.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", id));
        repo.clearDefault(userId);
        a.setIsDefault(true);
        return AddressResponse.from(a);
    }

    public UserAddress getOwnedOrThrow(Long userId, Long id) {
        return repo.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", id));
    }
}
