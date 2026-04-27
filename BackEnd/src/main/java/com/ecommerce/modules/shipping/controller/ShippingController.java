package com.ecommerce.modules.shipping.controller;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.modules.shipping.dto.ShippingMethodResponse;
import com.ecommerce.modules.shipping.service.ShippingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/shipping")
@RequiredArgsConstructor
@Tag(name = "Shipping", description = "Phương thức vận chuyển")
public class ShippingController {

    private final ShippingService shippingService;

    @GetMapping("/methods")
    public ApiResponse<List<ShippingMethodResponse>> methods() {
        return ApiResponse.ok(shippingService.listMethods());
    }
}
