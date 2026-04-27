package com.ecommerce.modules.catalog.service;

import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.common.exception.ErrorCode;
import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.common.util.SlugUtil;
import com.ecommerce.modules.catalog.dto.ProductAttributeRequest;
import com.ecommerce.modules.catalog.dto.ProductAttributeResponse;
import com.ecommerce.modules.catalog.dto.ProductAttributeValueRequest;
import com.ecommerce.modules.catalog.dto.AttributeValueResponse;
import com.ecommerce.modules.catalog.entity.ProductAttribute;
import com.ecommerce.modules.catalog.entity.ProductAttributeValue;
import com.ecommerce.modules.catalog.repository.ProductAttributeRepository;
import com.ecommerce.modules.catalog.repository.ProductAttributeValueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttributeService {

    private final ProductAttributeRepository attrRepo;
    private final ProductAttributeValueRepository valueRepo;

    @Transactional(readOnly = true)
    public List<ProductAttributeResponse> listAttributes() {
        return attrRepo.findAll().stream().map(ProductAttributeResponse::from).toList();
    }

    @Transactional
    public ProductAttributeResponse createAttribute(ProductAttributeRequest req) {
        String slug = req.slug() != null && !req.slug().isBlank() ? req.slug() : SlugUtil.toSlug(req.name());
        if (attrRepo.existsBySlug(slug)) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, HttpStatus.CONFLICT, "Slug đã tồn tại");
        }
        ProductAttribute a = ProductAttribute.builder().name(req.name()).slug(slug).build();
        return ProductAttributeResponse.from(attrRepo.save(a));
    }

    @Transactional
    public ProductAttributeResponse updateAttribute(Long id, ProductAttributeRequest req) {
        ProductAttribute a = attrRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Attribute", id));
        a.setName(req.name());
        if (req.slug() != null && !req.slug().isBlank()) a.setSlug(req.slug());
        return ProductAttributeResponse.from(a);
    }

    @Transactional
    public void deleteAttribute(Long id) {
        ProductAttribute a = attrRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Attribute", id));
        attrRepo.delete(a);
    }

    @Transactional(readOnly = true)
    public List<AttributeValueResponse> listValues(Long attributeId) {
        ProductAttribute a = attrRepo.findById(attributeId)
                .orElseThrow(() -> new ResourceNotFoundException("Attribute", attributeId));
        return valueRepo.findByAttributeIdOrderByDisplayOrderAscIdAsc(attributeId)
                .stream()
                .map(v -> AttributeValueResponse.from(v, a.getName()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AttributeValueResponse> listAllValues() {
        List<ProductAttribute> attrs = attrRepo.findAll();
        Map<Long, String> nameMap = attrs.stream().collect(Collectors.toMap(ProductAttribute::getId, ProductAttribute::getName));
        return valueRepo.findAll().stream()
                .map(v -> AttributeValueResponse.from(v, nameMap.get(v.getAttributeId())))
                .toList();
    }

    @Transactional
    public AttributeValueResponse createValue(ProductAttributeValueRequest req) {
        ProductAttribute a = attrRepo.findById(req.attributeId())
                .orElseThrow(() -> new ResourceNotFoundException("Attribute", req.attributeId()));
        ProductAttributeValue v = ProductAttributeValue.builder()
                .attributeId(req.attributeId())
                .value(req.value())
                .colorCode(req.colorCode())
                .displayOrder(req.displayOrder() != null ? req.displayOrder() : 0)
                .build();
        return AttributeValueResponse.from(valueRepo.save(v), a.getName());
    }

    @Transactional
    public void deleteValue(Long id) {
        ProductAttributeValue v = valueRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AttributeValue", id));
        valueRepo.delete(v);
    }
}

