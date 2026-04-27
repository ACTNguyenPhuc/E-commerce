package com.ecommerce.modules.banner.repository;

import com.ecommerce.modules.banner.entity.Banner;
import com.ecommerce.modules.catalog.entity.CommonStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BannerRepository extends JpaRepository<Banner, Long> {
    List<Banner> findByStatusOrderByDisplayOrderAscIdAsc(CommonStatus status);
}

