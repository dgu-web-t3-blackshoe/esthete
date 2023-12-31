package com.example.estheteadminservice.repository;

import com.example.estheteadminservice.dto.UserDto;
import com.example.estheteadminservice.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    @Query("SELECT new com.example.estheteadminservice.dto.UserDto$ManagerDto(u) " +
            "FROM User u " +
            "WHERE u.role = Role.MANAGER")
    Page<UserDto.ManagerDto> findAllManager(Pageable pageable);
}
