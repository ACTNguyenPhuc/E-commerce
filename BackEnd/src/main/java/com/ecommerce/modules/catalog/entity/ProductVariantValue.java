package com.ecommerce.modules.catalog.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "product_variant_values")
@IdClass(ProductVariantValue.PvvId.class)
public class ProductVariantValue {

    @Id
    @Column(name = "variant_id")
    private Long variantId;

    @Id
    @Column(name = "attribute_value_id")
    private Long attributeValueId;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PvvId implements Serializable {
        private Long variantId;
        private Long attributeValueId;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PvvId p)) return false;
            return Objects.equals(variantId, p.variantId)
                    && Objects.equals(attributeValueId, p.attributeValueId);
        }

        @Override
        public int hashCode() { return Objects.hash(variantId, attributeValueId); }
    }
}
