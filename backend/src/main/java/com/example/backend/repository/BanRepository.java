package com.example.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.entity.Ban;

public interface BanRepository extends JpaRepository<Ban, Long> {
    
}
