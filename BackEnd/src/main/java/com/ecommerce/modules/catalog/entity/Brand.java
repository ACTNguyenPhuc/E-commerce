package com.ecommerce.modules.catalog.entity;

import com.ecommerce.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "brands")
public class Brand extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 180, unique = true)
    private String slug;


    @Column(name = "logo_url", length = 2048)
    private String logoUrl;

    @Column(name = "use_file_upload", nullable = false)
    @Builder.Default
    private Boolean useFileUpload = false;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('active','inactive')")
    @Builder.Default
    private CommonStatus status = CommonStatus.active;
}
