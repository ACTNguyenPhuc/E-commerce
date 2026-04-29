package com.ecommerce.modules.banner.entity;

import com.ecommerce.common.entity.BaseEntity;
import com.ecommerce.modules.catalog.entity.CommonStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "banners")
public class Banner extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 200)
    private String title;

    @Column(length = 2048)
    private String subtitle;

    @Column(name = "image_url", nullable = false, length = 2048)
    private String imageUrl;

    @Column(name = "use_file_upload", nullable = false)
    @Builder.Default
    private Boolean useFileUpload = false;

    @Column(name = "link_url", length = 2048)
    private String linkUrl;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('active','inactive')")
    @Builder.Default
    private CommonStatus status = CommonStatus.active;

    @Column(name = "start_at")
    private LocalDateTime startAt;

    @Column(name = "end_at")
    private LocalDateTime endAt;
}

