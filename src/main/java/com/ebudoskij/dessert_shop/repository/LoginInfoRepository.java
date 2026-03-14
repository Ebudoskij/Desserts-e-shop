package com.ebudoskij.dessert_shop.repository;

import com.ebudoskij.dessert_shop.model.LoginInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoginInfoRepository extends JpaRepository<LoginInfo, Long> {
}
