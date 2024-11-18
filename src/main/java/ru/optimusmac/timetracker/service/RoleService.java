package ru.optimusmac.timetracker.service;

import java.util.Collection;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.optimusmac.timetracker.model.Role;
import ru.optimusmac.timetracker.repository.RoleRepository;

@Service
@AllArgsConstructor
public class RoleService {

  private final RoleRepository repository;


  public Role findByName(String name) {
    return repository.findByName(name);
  }

  public Role create(Role role) {
    return repository.save(role);
  }

  public Collection<Role> findAll() {
    return repository.findAll();
  }

  public ResponseEntity<?> delete(String name) {
    Role role = findByName(name);
    if (role == null) {
      return ResponseEntity.notFound().build();
    }
    repository.delete(role);
    return ResponseEntity.ok(role);
  }

}
