package ru.optimusmac.timetracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.optimusmac.timetracker.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

  User findByEmail(String email);

}
