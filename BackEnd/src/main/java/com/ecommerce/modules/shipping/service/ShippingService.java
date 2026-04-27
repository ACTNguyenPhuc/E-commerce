package com.ecommerce.modules.shipping.service;

import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.modules.catalog.entity.CommonStatus;
import com.ecommerce.modules.shipping.dto.ShippingMethodResponse;
import com.ecommerce.modules.shipping.entity.ShippingMethod;
import com.ecommerce.modules.shipping.repository.ShippingMethodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShippingService {

    private final ShippingMethodRepository methodRepo;

    @Transactional(readOnly = true)
    public List<ShippingMethodResponse> listMethods() {
        return methodRepo.findByStatusOrderByBaseFeeAsc(CommonStatus.active)
                .stream().map(ShippingMethodResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ShippingMethod getMethodOrThrow(Long id) {
        return methodRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ShippingMethod", id));
    }

    /** Tính phí ship đơn giản: lấy base_fee. Có thể mở rộng: theo địa lý, khối lượng. */
    public BigDecimal calculateFee(ShippingMethod method, BigDecimal subtotal) {
        return method.getBaseFee();
    }
}
