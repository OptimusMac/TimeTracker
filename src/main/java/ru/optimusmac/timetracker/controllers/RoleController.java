package ru.optimusmac.timetracker.controllers;


import java.util.Collection;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.optimusmac.timetracker.model.Role;
import ru.optimusmac.timetracker.service.RoleService;
import ru.optimusmac.timetracker.utils.Validate;

@RestController
@RequestMapping("/role")
@AllArgsConstructor
public class RoleController{


  private final RoleService roleService;


  @PostMapping("/create")
  public Role create(@RequestBody Role role){
    return roleService.create(role);
  }

  @GetMapping("/findAll")
  public ResponseEntity<Collection<Role>> findAll(){
    return ResponseEntity.ok(roleService.findAll());
  }

  @DeleteMapping("/delete")
  @Transactional
  public ResponseEntity<?> delete(@RequestParam String name, @RequestParam(defaultValue = "true") boolean validate){
    name = Validate.validateFormRole(name, validate);
    return roleService.delete(name);
  }

}
