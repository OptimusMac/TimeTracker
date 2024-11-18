package ru.optimusmac.timetracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.optimusmac.timetracker.model.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
  Role findByName(String name);

}
