package com.ecommerce.modules.order.service;

import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.common.exception.ErrorCode;
import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.common.response.PageResponse;
import com.ecommerce.modules.catalog.entity.Product;
import com.ecommerce.modules.catalog.entity.ProductVariant;
import com.ecommerce.modules.catalog.repository.ProductRepository;
import com.ecommerce.modules.catalog.repository.ProductVariantRepository;
import com.ecommerce.modules.order.dto.*;
import com.ecommerce.modules.order.entity.*;
import com.ecommerce.modules.order.repository.OrderItemRepository;
import com.ecommerce.modules.order.repository.OrderRepository;
import com.ecommerce.modules.order.repository.OrderStatusHistoryRepository;
import com.ecommerce.modules.payment.entity.Payment;
import com.ecommerce.modules.payment.entity.PaymentTxnStatus;
import com.ecommerce.modules.payment.repository.PaymentRepository;
import com.ecommerce.modules.shipping.entity.Shipment;
import com.ecommerce.modules.shipping.entity.ShipmentStatus;
import com.ecommerce.modules.shipping.entity.ShippingMethod;
import com.ecommerce.modules.shipping.repository.ShipmentRepository;
import com.ecommerce.modules.shipping.service.ShippingService;
import com.ecommerce.modules.user.entity.UserAddress;
import com.ecommerce.modules.user.service.AddressService;
import com.ecommerce.modules.voucher.entity.Voucher;
import com.ecommerce.modules.voucher.entity.VoucherUsage;
import com.ecommerce.modules.voucher.repository.VoucherRepository;
import com.ecommerce.modules.voucher.repository.VoucherUsageRepository;
import com.ecommerce.modules.voucher.service.VoucherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepo;
    private final OrderItemRepository orderItemRepo;
    private final OrderStatusHistoryRepository historyRepo;
    private final ProductRepository productRepo;
    private final ProductVariantRepository variantRepo;
    private final ShippingService shippingService;
    private final ShipmentRepository shipmentRepo;
    private final PaymentRepository paymentRepo;
    private final AddressService addressService;
    private final VoucherService voucherService;
    private final VoucherRepository voucherRepo;
    private final VoucherUsageRepository voucherUsageRepo;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    // ---------------- PREVIEW ----------------
    @Transactional(readOnly = true)
    public OrderPreviewResponse preview(Long userId, OrderPreviewRequest req) {
        ShippingMethod sm = shippingService.getMethodOrThrow(req.shippingMethodId());
        List<OrderPreviewResponse.LineItem> lineItems = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        for (OrderPreviewRequest.Item it : req.items()) {
            Product p = productRepo.findById(it.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", it.productId()));
            ProductVariant v = it.variantId() != null
                    ? variantRepo.findById(it.variantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Variant", it.variantId()))
                    : null;
            BigDecimal price = price(p, v);
            BigDecimal lineSub = price.multiply(BigDecimal.valueOf(it.quantity()));
            subtotal = subtotal.add(lineSub);
            lineItems.add(OrderPreviewResponse.LineItem.builder()
                    .productId(p.getId()).variantId(v != null ? v.getId() : null)
                    .productName(p.getName()).variantName(v != null ? v.getSku() : null)
                    .sku(v != null ? v.getSku() : p.getSku())
                    .unitPrice(price).quantity(it.quantity()).subtotal(lineSub)
                    .build());
        }
        BigDecimal shippingFee = shippingService.calculateFee(sm, subtotal);
        BigDecimal discount = BigDecimal.ZERO;
        String voucherCode = null;
        if (req.voucherCode() != null && !req.voucherCode().isBlank()) {
            Voucher v = voucherRepo.findByCode(req.voucherCode())
                    .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_VOUCHER, HttpStatus.NOT_FOUND, "Voucher không tồn tại"));
            discount = voucherService.validateAndCalc(v, subtotal, userId).discountAmount();
            voucherCode = v.getCode();
        }
        BigDecimal total = subtotal.add(shippingFee).subtract(discount);
        return OrderPreviewResponse.builder()
                .subtotal(subtotal).shippingFee(shippingFee).discountAmount(discount).totalAmount(total)
                .voucherCode(voucherCode).items(lineItems).build();
    }

    // ---------------- PLACE ORDER ----------------
    @Transactional
    public OrderDetailResponse placeOrder(Long userId, PlaceOrderRequest req) {
        UserAddress address = addressService.getOwnedOrThrow(userId, req.addressId());
        ShippingMethod sm = shippingService.getMethodOrThrow(req.shippingMethodId());

        // 1. Validate stock + tính subtotal (lock variants)
        List<OrderItem> items = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        Map<Long, Integer> variantQtyMap = new LinkedHashMap<>();
        for (PlaceOrderRequest.Item it : req.items()) {
            Product p = productRepo.findById(it.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", it.productId()));
            ProductVariant v = null;
            BigDecimal price = price(p, null);
            String sku = p.getSku();
            String variantName = null;
            if (it.variantId() != null) {
                v = variantRepo.findByIdForUpdate(it.variantId())
                        .orElseThrow(() -> new ResourceNotFoundException("Variant", it.variantId()));
                if (!v.getProductId().equals(p.getId())) {
                    throw new BusinessException(ErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST, "Variant không thuộc sản phẩm");
                }
                if (v.getStockQuantity() < it.quantity()) {
                    throw new BusinessException(ErrorCode.OUT_OF_STOCK, HttpStatus.UNPROCESSABLE_ENTITY,
                            "Sản phẩm '" + p.getName() + "' chỉ còn " + v.getStockQuantity());
                }
                price = price(p, v);
                sku = v.getSku();
                variantName = v.getSku();
                variantQtyMap.put(v.getId(), it.quantity());
            }
            BigDecimal lineSub = price.multiply(BigDecimal.valueOf(it.quantity()));
            subtotal = subtotal.add(lineSub);
            OrderItem oi = OrderItem.builder()
                    .productId(p.getId()).variantId(it.variantId())
                    .productName(p.getName()).variantName(variantName).sku(sku)
                    .unitPrice(price).quantity(it.quantity()).subtotal(lineSub)
                    .build();
            items.add(oi);
        }

        // 2. Voucher
        Voucher voucher = null;
        BigDecimal discount = BigDecimal.ZERO;
        if (req.voucherCode() != null && !req.voucherCode().isBlank()) {
            voucher = voucherRepo.findByCode(req.voucherCode())
                    .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_VOUCHER, HttpStatus.NOT_FOUND, "Voucher không tồn tại"));
            discount = voucherService.validateAndCalc(voucher, subtotal, userId).discountAmount();
        }

        // 3. Phí ship + tổng tiền
        BigDecimal shippingFee = shippingService.calculateFee(sm, subtotal);
        BigDecimal total = subtotal.add(shippingFee).subtract(discount);

        // 4. Tạo Order
        Order order = Order.builder()
                .orderCode(generateOrderCode())
                .userId(userId)
                .voucherId(voucher != null ? voucher.getId() : null)
                .shippingMethodId(sm.getId())
                .recipientName(address.getRecipientName())
                .recipientPhone(address.getRecipientPhone())
                .shippingAddress(formatAddress(address))
                .subtotal(subtotal)
                .shippingFee(shippingFee)
                .discountAmount(discount)
                .totalAmount(total)
                .paymentMethod(req.paymentMethod())
                .paymentStatus(PaymentStatus.pending)
                .status(OrderStatus.pending)
                .note(req.note())
                .placedAt(LocalDateTime.now())
                .build();
        order = orderRepo.save(order);

        // 5. Save order items
        for (OrderItem oi : items) {
            oi.setOrderId(order.getId());
            orderItemRepo.save(oi);
        }

        // 6. Trừ kho
        for (Map.Entry<Long, Integer> e : variantQtyMap.entrySet()) {
            int rows = variantRepo.decreaseStock(e.getKey(), e.getValue());
            if (rows == 0) {
                throw new BusinessException(ErrorCode.OUT_OF_STOCK, HttpStatus.UNPROCESSABLE_ENTITY,
                        "Variant id=" + e.getKey() + " không đủ tồn kho");
            }
        }
        // Tăng sold_count cho từng product
        for (OrderItem oi : items) {
            productRepo.incrementSoldCount(oi.getProductId(), oi.getQuantity());
        }

        // 7. Status history
        addStatusHistory(order.getId(), null, OrderStatus.pending.name(), "Khách đặt đơn", userId);

        // 8. Tạo Payment + Shipment
        paymentRepo.save(Payment.builder()
                .orderId(order.getId())
                .paymentMethod(req.paymentMethod())
                .amount(total)
                .status(PaymentTxnStatus.pending)
                .build());
        shipmentRepo.save(Shipment.builder()
                .orderId(order.getId())
                .shippingMethodId(sm.getId())
                .shippingFee(shippingFee)
                .status(ShipmentStatus.preparing)
                .build());

        // 9. Voucher usage
        if (voucher != null) {
            voucherUsageRepo.save(VoucherUsage.builder()
                    .voucherId(voucher.getId()).userId(userId).orderId(order.getId())
                    .discountAmount(discount).build());
            voucherRepo.incrementUsedCount(voucher.getId());
        }

        return getOrderDetail(userId, order.getOrderCode(), false);
    }

    // ---------------- LIST / DETAIL ----------------
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> myOrders(Long userId, OrderStatus status, Pageable pageable) {
        Page<Order> page = orderRepo.search(userId, status, pageable);
        return PageResponse.from(page, OrderResponse::from);
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> adminOrders(OrderStatus status, Pageable pageable) {
        Page<Order> page = orderRepo.search(null, status, pageable);
        return PageResponse.from(page, OrderResponse::from);
    }

    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderDetail(Long userId, String orderCode, boolean isAdmin) {
        Order o = isAdmin
                ? orderRepo.findByOrderCode(orderCode).orElseThrow(() -> new ResourceNotFoundException("Order " + orderCode))
                : orderRepo.findByOrderCodeAndUserId(orderCode, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order " + orderCode));
        List<OrderDetailResponse.Item> items = new ArrayList<>();
        for (OrderItem oi : orderItemRepo.findByOrderId(o.getId())) {
            Product p = productRepo.findById(oi.getProductId()).orElse(null);
            ProductVariant v = oi.getVariantId() != null ? variantRepo.findById(oi.getVariantId()).orElse(null) : null;
            boolean thumbFromVariant = v != null && v.getImageUrl() != null;
            String thumb = thumbFromVariant ? v.getImageUrl() : (p != null ? p.getThumbnailUrl() : null);
            Boolean useFileUpload = thumbFromVariant ? v.getUseFileUpload() : (p != null ? p.getUseFileUpload() : null);
            items.add(OrderDetailResponse.Item.from(oi, thumb, useFileUpload));
        }
        List<OrderDetailResponse.History> history = historyRepo.findByOrderIdOrderByCreatedAtAsc(o.getId())
                .stream().map(OrderDetailResponse.History::from).toList();
        return OrderDetailResponse.builder()
                .id(o.getId()).orderCode(o.getOrderCode())
                .recipientName(o.getRecipientName()).recipientPhone(o.getRecipientPhone())
                .shippingAddress(o.getShippingAddress())
                .subtotal(o.getSubtotal()).shippingFee(o.getShippingFee())
                .discountAmount(o.getDiscountAmount()).totalAmount(o.getTotalAmount())
                .paymentMethod(o.getPaymentMethod()).paymentStatus(o.getPaymentStatus())
                .status(o.getStatus()).note(o.getNote())
                .placedAt(o.getPlacedAt()).confirmedAt(o.getConfirmedAt())
                .deliveredAt(o.getDeliveredAt()).cancelledAt(o.getCancelledAt())
                .items(items).history(history).build();
    }

    // ---------------- CANCEL ----------------
    @Transactional
    public OrderDetailResponse cancelByCustomer(Long userId, String orderCode) {
        Order o = orderRepo.findByOrderCodeAndUserId(orderCode, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order " + orderCode));
        if (o.getStatus() != OrderStatus.pending && o.getStatus() != OrderStatus.confirmed) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS, HttpStatus.UNPROCESSABLE_ENTITY,
                    "Đơn ở trạng thái '" + o.getStatus() + "' không thể huỷ");
        }
        return doCancel(o, userId, "Khách huỷ đơn");
    }

    @Transactional
    public OrderDetailResponse cancelByAdmin(Long staffId, String orderCode, String note) {
        Order o = orderRepo.findByOrderCode(orderCode)
                .orElseThrow(() -> new ResourceNotFoundException("Order " + orderCode));
        if (o.getStatus() == OrderStatus.completed || o.getStatus() == OrderStatus.cancelled) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS, HttpStatus.UNPROCESSABLE_ENTITY,
                    "Không thể huỷ đơn đã " + o.getStatus());
        }
        return doCancel(o, staffId, note != null ? note : "Admin huỷ đơn");
    }

    private OrderDetailResponse doCancel(Order o, Long actorId, String note) {
        OrderStatus from = o.getStatus();
        o.setStatus(OrderStatus.cancelled);
        o.setCancelledAt(LocalDateTime.now());
        // hoàn kho
        for (OrderItem oi : orderItemRepo.findByOrderId(o.getId())) {
            if (oi.getVariantId() != null) {
                variantRepo.restoreStock(oi.getVariantId(), oi.getQuantity());
            }
        }
        addStatusHistory(o.getId(), from.name(), OrderStatus.cancelled.name(), note, actorId);
        return getOrderDetail(o.getUserId(), o.getOrderCode(), true);
    }

    // ---------------- ADMIN: UPDATE STATUS ----------------
    @Transactional
    public OrderDetailResponse updateStatus(Long staffId, String orderCode, UpdateOrderStatusRequest req) {
        Order o = orderRepo.findByOrderCode(orderCode)
                .orElseThrow(() -> new ResourceNotFoundException("Order " + orderCode));
        if (req.status() == OrderStatus.cancelled) {
            return doCancel(o, staffId, req.note());
        }
        OrderStatus from = o.getStatus();
        validateTransition(from, req.status());
        o.setStatus(req.status());
        if (req.status() == OrderStatus.confirmed && o.getConfirmedAt() == null) {
            o.setConfirmedAt(LocalDateTime.now());
        }
        if (req.status() == OrderStatus.delivered && o.getDeliveredAt() == null) {
            o.setDeliveredAt(LocalDateTime.now());
            // COD khi giao xong → đánh dấu paid
            if (o.getPaymentMethod() == PaymentMethod.cod) {
                o.setPaymentStatus(PaymentStatus.paid);
            }
        }
        addStatusHistory(o.getId(), from.name(), req.status().name(), req.note(), staffId);
        return getOrderDetail(o.getUserId(), o.getOrderCode(), true);
    }

    private void validateTransition(OrderStatus from, OrderStatus to) {
        Map<OrderStatus, Set<OrderStatus>> allowed = Map.of(
                OrderStatus.pending,    Set.of(OrderStatus.confirmed, OrderStatus.cancelled),
                OrderStatus.confirmed,  Set.of(OrderStatus.processing, OrderStatus.cancelled),
                OrderStatus.processing, Set.of(OrderStatus.shipping, OrderStatus.cancelled),
                OrderStatus.shipping,   Set.of(OrderStatus.delivered, OrderStatus.returned),
                OrderStatus.delivered,  Set.of(OrderStatus.completed, OrderStatus.returned),
                OrderStatus.completed,  Set.of(),
                OrderStatus.cancelled,  Set.of(),
                OrderStatus.returned,   Set.of()
        );
        Set<OrderStatus> next = allowed.getOrDefault(from, Set.of());
        if (!next.contains(to)) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS, HttpStatus.UNPROCESSABLE_ENTITY,
                    "Không thể chuyển từ '" + from + "' sang '" + to + "'");
        }
    }

    // ---------------- HELPERS ----------------
    private void addStatusHistory(Long orderId, String from, String to, String note, Long actor) {
        historyRepo.save(OrderStatusHistory.builder()
                .orderId(orderId).fromStatus(from).toStatus(to).note(note).changedBy(actor)
                .build());
    }

    private BigDecimal price(Product p, ProductVariant v) {
        if (v != null) {
            return v.getSalePrice() != null ? v.getSalePrice() : v.getPrice();
        }
        return p.getSalePrice() != null ? p.getSalePrice() : p.getBasePrice();
    }

    private String formatAddress(UserAddress a) {
        return java.util.stream.Stream.of(
                        a.getStreetAddress(),
                        a.getWard(),
                        a.getDistrict(),
                        a.getProvince()
                )
                .filter(s -> s != null && !s.isBlank())
                .collect(java.util.stream.Collectors.joining(", "));
    }

    private String generateOrderCode() {
        String date = LocalDateTime.now().format(DATE_FMT);
        int rand = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "ORD-" + date + "-" + rand;
    }
}
