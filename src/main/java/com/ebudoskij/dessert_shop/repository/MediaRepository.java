package com.ebudoskij.dessert_shop.repository;

import com.ebudoskij.dessert_shop.model.Media;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MediaRepository extends JpaRepository<Media, Long> {
    List<Media> findByEntityTypeAndEntityIdOrderByPriorityAsc(String entityType, Long entityId);
}
