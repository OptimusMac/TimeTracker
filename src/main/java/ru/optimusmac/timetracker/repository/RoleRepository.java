package ru.optimusmac.TimeTracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.optimusmac.TimeTracker.model.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
  Role findByName(String name);

}
