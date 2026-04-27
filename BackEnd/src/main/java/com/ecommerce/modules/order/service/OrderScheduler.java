package com.ecommerce.modules.order.service;

import com.ecommerce.modules.catalog.repository.ProductVariantRepository;
import com.ecommerce.modules.order.entity.Order;
import com.ecommerce.modules.order.entity.OrderItem;
import com.ecommerce.modules.order.entity.OrderStatus;
import com.ecommerce.modules.order.entity.PaymentMethod;
import com.ecommerce.modules.order.repository.OrderItemRepository;
import com.ecommerce.modules.order.repository.OrderRepository;
import com.ecommerce.modules.order.repository.OrderStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Cron tự động huỷ đơn pending thanh toán online không hoàn tất
 * và tự complete đơn delivered sau N ngày không khiếu nại.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderScheduler {

    private final OrderRepository orderRepo;
    private final OrderItemRepository orderItemRepo;
    private final OrderStatusHistoryRepository historyRepo;
    private final ProductVariantRepository variantRepo;

    @Value("${app.order.auto-cancel-pending-after-minutes:30}")
    private long autoCancelMinutes;

    @Value("${app.order.auto-complete-after-delivered-days:7}")
    private long autoCompleteDays;

    /** Mỗi 5 phút quét đơn pending quá hạn */
    @Scheduled(fixedDelay = 5 * 60 * 1000L)
    @Transactional
    public void autoCancelPending() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(autoCancelMinutes);
        List<Order> orders = orderRepo.findByStatusAndPlacedAtBefore(OrderStatus.pending, threshold);
        for (Order o : orders) {
            if (o.getPaymentMethod() == PaymentMethod.cod) continue; // COD không tự huỷ
            log.info("Auto-cancel order {} (placed {} ago)", o.getOrderCode(), o.getPlacedAt());
            o.setStatus(OrderStatus.cancelled);
            o.setCancelledAt(LocalDateTime.now());
            for (OrderItem oi : orderItemRepo.findByOrderId(o.getId())) {
                if (oi.getVariantId() != null) {
                    variantRepo.restoreStock(oi.getVariantId(), oi.getQuantity());
                }
            }
            historyRepo.save(com.ecommerce.modules.order.entity.OrderStatusHistory.builder()
                    .orderId(o.getId()).fromStatus(OrderStatus.pending.name())
                    .toStatus(OrderStatus.cancelled.name())
                    .note("Tự động huỷ do quá thời gian thanh toán")
                    .build());
        }
    }

    /** Mỗi ngày 1h sáng auto-complete đơn delivered đủ N ngày */
    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void autoCompleteDelivered() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(autoCompleteDays);
        List<Order> orders = orderRepo.findByStatusAndPlacedAtBefore(OrderStatus.delivered, threshold);
        for (Order o : orders) {
            if (o.getDeliveredAt() == null || o.getDeliveredAt().isAfter(threshold)) continue;
            o.setStatus(OrderStatus.completed);
            historyRepo.save(com.ecommerce.modules.order.entity.OrderStatusHistory.builder()
                    .orderId(o.getId()).fromStatus(OrderStatus.delivered.name())
                    .toStatus(OrderStatus.completed.name())
                    .note("Tự động hoàn tất sau " + autoCompleteDays + " ngày")
                    .build());
        }
    }
}
