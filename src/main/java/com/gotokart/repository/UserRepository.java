package com.gotokart.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gotokart.model.User;

public interface UserRepository extends JpaRepository<User, Long>{

}