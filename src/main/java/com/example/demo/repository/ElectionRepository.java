package com.example.demo.repository;

import com.example.demo.model.Election;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ElectionRepository extends JpaRepository<Election, Long> {

	List<Election> findByActiveTrue();

    Election findByTypeIgnoreCase(String type);
}