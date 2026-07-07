//package com.example.demo.repository;
//
//import org.springframework.data.jpa.repository.JpaRepository;
//import com.example.demo.model.User;
//
//public interface UserRepository extends JpaRepository<User, Long> {
//
//    User findByUsername(String username);
//    boolean existsByUsername(String username);
//
//    User findByPhone(String phone);
//    boolean existsByPhone(String phone);
//
//    User findByVoterId(String voterId);
//    boolean existsByVoterId(String voterId);
//}

package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);
}