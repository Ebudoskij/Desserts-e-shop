package com.ebudoskij.dessert_shop.repository;

import com.ebudoskij.dessert_shop.model.AdditionalItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AdditionalItem, Long> {

}
