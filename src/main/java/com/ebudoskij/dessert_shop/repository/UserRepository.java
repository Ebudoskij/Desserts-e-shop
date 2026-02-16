package com.ebudoskij.dessert_shop.repository;

import com.ebudoskij.dessert_shop.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
