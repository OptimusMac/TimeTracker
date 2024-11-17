package ru.optimusmac.TimeTracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.optimusmac.TimeTracker.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

  User findByEmail(String email);

}
