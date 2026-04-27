package com.ecommerce.modules.user.dto;

import com.ecommerce.modules.user.entity.AddressType;
import com.ecommerce.modules.user.entity.UserAddress;
import lombok.Builder;

@Builder
public record AddressResponse(
        Long id,
        String recipientName,
        String recipientPhone,
        String province,
        String district,
        String ward,
        String streetAddress,
        AddressType addressType,
        Boolean isDefault
) {
    public static AddressResponse from(UserAddress a) {
        return AddressResponse.builder()
                .id(a.getId())
                .recipientName(a.getRecipientName())
                .recipientPhone(a.getRecipientPhone())
                .province(a.getProvince())
                .district(a.getDistrict())
                .ward(a.getWard())
                .streetAddress(a.getStreetAddress())
                .addressType(a.getAddressType())
                .isDefault(a.getIsDefault())
                .build();
    }
}
