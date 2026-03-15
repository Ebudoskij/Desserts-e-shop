package com.ebudoskij.dessert_shop.repository;

import com.ebudoskij.dessert_shop.model.Role;
import com.ebudoskij.dessert_shop.model.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleType name);
}
