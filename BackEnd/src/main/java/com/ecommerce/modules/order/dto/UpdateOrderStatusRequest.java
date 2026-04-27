package com.ecommerce.modules.order.dto;

import com.ecommerce.modules.order.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(@NotNull OrderStatus status, String note) {}
