package com.ecommerce.modules.cart.service;

import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.common.exception.ErrorCode;
import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.modules.cart.dto.*;
import com.ecommerce.modules.cart.entity.Cart;
import com.ecommerce.modules.cart.entity.CartItem;
import com.ecommerce.modules.cart.entity.CartStatus;
import com.ecommerce.modules.cart.repository.CartItemRepository;
import com.ecommerce.modules.cart.repository.CartRepository;
import com.ecommerce.modules.catalog.dto.AttributeValueResponse;
import com.ecommerce.modules.catalog.entity.Product;
import com.ecommerce.modules.catalog.entity.ProductAttribute;
import com.ecommerce.modules.catalog.entity.ProductAttributeValue;
import com.ecommerce.modules.catalog.entity.ProductVariantValue;
import com.ecommerce.modules.catalog.entity.ProductVariant;
import com.ecommerce.modules.catalog.repository.ProductAttributeRepository;
import com.ecommerce.modules.catalog.repository.ProductAttributeValueRepository;
import com.ecommerce.modules.catalog.repository.ProductRepository;
import com.ecommerce.modules.catalog.repository.ProductVariantValueRepository;
import com.ecommerce.modules.catalog.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepo;
    private final CartItemRepository cartItemRepo;
    private final ProductRepository productRepo;
    private final ProductVariantRepository variantRepo;
    private final ProductVariantValueRepository pvvRepo;
    private final ProductAttributeValueRepository attrValueRepo;
    private final ProductAttributeRepository attrRepo;

    private List<AttributeValueResponse> loadVariantAttributes(Long variantId, Map<Long, String> attrNameMap) {
        if (variantId == null) return List.of();

        List<ProductVariantValue> pvvs = pvvRepo.findByVariantId(variantId);
        if (pvvs == null || pvvs.isEmpty()) return List.of();

        List<Long> avIds = pvvs.stream()
                .map(ProductVariantValue::getAttributeValueId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<Long, ProductAttributeValue> avMap = avIds.isEmpty() ? Map.of()
                : attrValueRepo.findByIdIn(avIds).stream()
                .collect(Collectors.toMap(ProductAttributeValue::getId, x -> x));

        return pvvs.stream()
                .map(pvv -> {
                    ProductAttributeValue av = avMap.get(pvv.getAttributeValueId());
                    if (av == null) return null;
                    return AttributeValueResponse.from(av, attrNameMap.get(av.getAttributeId()));
                })
                .filter(Objects::nonNull)
                .toList();
    }

    @Transactional
    public CartResponse getOrCreate(Long userId, String sessionId) {
        Cart cart = resolve(userId, sessionId);
        if (cart == null) {
            if (userId == null && (sessionId == null || sessionId.isBlank())) {
                // Guest chưa có session: trả về giỏ rỗng, không tạo bản ghi DB
                return CartResponse.builder()
                        .items(java.util.List.of())
                        .totalQuantity(0)
                        .subtotal(BigDecimal.ZERO)
                        .build();
            }
            cart = Cart.builder().userId(userId).sessionId(sessionId).status(CartStatus.active).build();
            cart = cartRepo.save(cart);
        }
        return buildResponse(cart);
    }

    @Transactional
    public CartResponse addItem(Long userId, String sessionId, AddCartItemRequest req) {
        Product product = productRepo.findById(req.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", req.productId()));
        BigDecimal price = product.getSalePrice() != null ? product.getSalePrice() : product.getBasePrice();
        ProductVariant variant = null;
        if (req.variantId() != null) {
            variant = variantRepo.findById(req.variantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Variant", req.variantId()));
            if (!variant.getProductId().equals(product.getId())) {
                throw new BusinessException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Variant không thuộc sản phẩm");
            }
            price = variant.getSalePrice() != null ? variant.getSalePrice() : variant.getPrice();
            if (variant.getStockQuantity() < req.quantity()) {
                throw new BusinessException(ErrorCode.OUT_OF_STOCK, HttpStatus.UNPROCESSABLE_ENTITY,
                        "Variant chỉ còn " + variant.getStockQuantity() + " sản phẩm");
            }
        }
        Cart cart = resolveOrCreate(userId, sessionId);
        CartItem item = cartItemRepo
                .findByCartIdAndProductIdAndVariantId(cart.getId(), product.getId(), req.variantId())
                .orElse(null);
        if (item == null) {
            item = CartItem.builder()
                    .cartId(cart.getId()).productId(product.getId()).variantId(req.variantId())
                    .quantity(req.quantity()).unitPrice(price)
                    .build();
        } else {
            item.setQuantity(item.getQuantity() + req.quantity());
            item.setUnitPrice(price);
        }
        cartItemRepo.save(item);
        return buildResponse(cart);
    }

    @Transactional
    public CartResponse updateItem(Long userId, String sessionId, Long itemId, UpdateCartItemRequest req) {
        Cart cart = requireCart(userId, sessionId);
        CartItem item = cartItemRepo.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", itemId));
        if (!item.getCartId().equals(cart.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, HttpStatus.FORBIDDEN, "Item không thuộc giỏ của bạn");
        }
        item.setQuantity(req.quantity());
        return buildResponse(cart);
    }

    @Transactional
    public CartResponse removeItem(Long userId, String sessionId, Long itemId) {
        Cart cart = requireCart(userId, sessionId);
        CartItem item = cartItemRepo.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", itemId));
        if (!item.getCartId().equals(cart.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, HttpStatus.FORBIDDEN, "Item không thuộc giỏ của bạn");
        }
        cartItemRepo.delete(item);
        return buildResponse(cart);
    }

    @Transactional
    public void clear(Long userId, String sessionId) {
        Cart cart = resolve(userId, sessionId);
        if (cart != null) cartItemRepo.deleteByCartId(cart.getId());
    }

    @Transactional
    public CartResponse merge(Long userId, String guestSessionId) {
        Cart guest = cartRepo.findFirstBySessionIdAndStatusOrderByIdDesc(guestSessionId, CartStatus.active).orElse(null);
        if (guest == null) return getOrCreate(userId, null);
        Cart user = cartRepo.findFirstByUserIdAndStatusOrderByIdDesc(userId, CartStatus.active).orElseGet(() ->
                cartRepo.save(Cart.builder().userId(userId).status(CartStatus.active).build()));
        for (CartItem gi : cartItemRepo.findByCartId(guest.getId())) {
            CartItem existing = cartItemRepo.findByCartIdAndProductIdAndVariantId(
                    user.getId(), gi.getProductId(), gi.getVariantId()).orElse(null);
            if (existing == null) {
                gi.setCartId(user.getId());
                cartItemRepo.save(gi);
            } else {
                existing.setQuantity(existing.getQuantity() + gi.getQuantity());
                cartItemRepo.delete(gi);
            }
        }
        guest.setStatus(CartStatus.abandoned);
        return buildResponse(user);
    }

    public Cart requireCartOrThrow(Long userId, String sessionId) {
        return requireCart(userId, sessionId);
    }

    private Cart requireCart(Long userId, String sessionId) {
        Cart c = resolve(userId, sessionId);
        if (c == null) throw new ResourceNotFoundException("Giỏ hàng không tồn tại");
        return c;
    }

    private Cart resolve(Long userId, String sessionId) {
        if (userId != null) {
            return cartRepo.findFirstByUserIdAndStatusOrderByIdDesc(userId, CartStatus.active).orElse(null);
        }
        if (sessionId != null && !sessionId.isBlank()) {
            return cartRepo.findFirstBySessionIdAndStatusOrderByIdDesc(sessionId, CartStatus.active).orElse(null);
        }
        return null;
    }

    private Cart resolveOrCreate(Long userId, String sessionId) {
        Cart c = resolve(userId, sessionId);
        if (c != null) return c;
        if (userId == null && (sessionId == null || sessionId.isBlank())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST,
                    "Cần đăng nhập hoặc cung cấp X-Session-Id cho khách");
        }
        return cartRepo.save(Cart.builder().userId(userId).sessionId(sessionId).status(CartStatus.active).build());
    }

    private CartResponse buildResponse(Cart cart) {
        List<CartItem> items = cartItemRepo.findByCartId(cart.getId());
        BigDecimal subtotal = BigDecimal.ZERO;
        int totalQty = 0;
        List<CartItemResponse> itemResponses = new java.util.ArrayList<>();
        Map<Long, String> attrNameMap = attrRepo.findAll().stream()
                .collect(Collectors.toMap(ProductAttribute::getId, ProductAttribute::getName));
        for (CartItem it : items) {
            BigDecimal lineTotal = it.getUnitPrice().multiply(BigDecimal.valueOf(it.getQuantity()));
            subtotal = subtotal.add(lineTotal);
            totalQty += it.getQuantity();
            Product p = productRepo.findById(it.getProductId()).orElse(null);
            ProductVariant v = it.getVariantId() != null ? variantRepo.findById(it.getVariantId()).orElse(null) : null;
            boolean thumbFromVariant = v != null && v.getImageUrl() != null;

            List<AttributeValueResponse> attrs = loadVariantAttributes(it.getVariantId(), attrNameMap);
            itemResponses.add(CartItemResponse.builder()
                    .id(it.getId())
                    .productId(it.getProductId())
                    .variantId(it.getVariantId())
                    .productName(p != null ? p.getName() : null)
                    .variantName(v != null ? v.getSku() : null)
                    .attributes(attrs)
                    .thumbnailUrl(thumbFromVariant ? v.getImageUrl()
                            : (p != null ? p.getThumbnailUrl() : null))
                    .useFileUpload(thumbFromVariant ? v.getUseFileUpload() : (p != null ? p.getUseFileUpload() : null))
                    .sku(v != null ? v.getSku() : (p != null ? p.getSku() : null))
                    .quantity(it.getQuantity())
                    .unitPrice(it.getUnitPrice())
                    .subtotal(lineTotal)
                    .stockQuantity(v != null ? v.getStockQuantity() : null)
                    .build());
        }
        return CartResponse.builder()
                .cartId(cart.getId())
                .userId(cart.getUserId())
                .sessionId(cart.getSessionId())
                .items(itemResponses)
                .totalQuantity(totalQty)
                .subtotal(subtotal)
                .build();
    }
}
