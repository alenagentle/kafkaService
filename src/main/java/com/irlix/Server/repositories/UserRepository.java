package com.irlix.Server.repositories;

import com.irlix.Server.entities.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>{

    Optional<User> findByLogin(String login);
    List<User> findAllByLoginContains(String login);
//    List<User> findAllByFioContains(String fio);
    boolean existsUserByLogin(String login);
    List<User> findAllByFioContains(String fio);
}
