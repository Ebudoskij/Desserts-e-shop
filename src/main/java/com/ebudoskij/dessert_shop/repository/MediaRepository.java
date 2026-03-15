package com.ebudoskij.dessert_shop.repository;

import com.ebudoskij.dessert_shop.model.Media;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MediaRepository extends JpaRepository<Media, Long> {
    List<Media> findByEntityTypeAndEntityIdOrderByPriorityAsc(String entityType, Long entityId);

    @Modifying
    @Query("UPDATE Media m SET m.priority = " +
            "CASE " +
            "  WHEN m.id = :newMainId THEN 0 " +
            "  WHEN m.priority = 0 THEN 1 " +
            "  ELSE m.priority " +
            "END " +
            "WHERE m.entityType = :type AND m.entityId = :id " +
            "AND (m.id = :newMainId OR m.priority = 0)")
    void swapMainImage(@Param("type") String type, @Param("id") Long id, @Param("newMainId") Long newMainId);

    @Modifying
    @Query("UPDATE Media m SET m.priority = 1 WHERE m.entityType = :type AND m.entityId = :id AND m.priority = 0")
    void demoteCurrentMain(@Param("type") String type, @Param("id") Long id);
}
