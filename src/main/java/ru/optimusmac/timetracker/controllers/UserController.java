package ru.optimusmac.TimeTracker.controllers;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.optimusmac.TimeTracker.model.Role;
import ru.optimusmac.TimeTracker.model.User;
import ru.optimusmac.TimeTracker.service.RoleService;
import ru.optimusmac.TimeTracker.service.UserService;
import ru.optimusmac.TimeTracker.utils.Validate;
import ru.optimusmac.response.ErrorResponse;

@RestController
@RequestMapping("/user")
@AllArgsConstructor
public class UserController{


  public final UserService userService;
  public final RoleService roleService;
  private final BCryptPasswordEncoder passwordEncoder;



  @GetMapping("/all")
  public Collection<User> findAll() {
    return userService.findAll();
  }

  @PostMapping("/create")
  public ResponseEntity<?> create(@RequestBody User user) {
    User checkUser = userService.findByEmail(user.getEmail());
    if (checkUser != null) {
      return ResponseEntity.badRequest().body(new ErrorResponse("Email is already in use.", "email"));

    }

    user.setPassword(passwordEncoder.encode(user.getPassword()));

    User created = userService.create(user);
    return ResponseEntity.ok(created);
  }


  @GetMapping("/find")
  public ResponseEntity<?> find(@RequestParam(required = false) Long id,
      @RequestParam(required = false) String email) {
    Predicate<User> userPredicate = Objects::nonNull;
    User user;

    if (id == null) {
      user = userService.findByEmail(email);
    } else {
      user = userService.findById(id);
    }

    if (!userPredicate.test(user)) {
      return ResponseEntity.status(404)
          .body(Map.of("not_found", "user is null"));
    }
    return ResponseEntity
        .ok(user);
  }

  @DeleteMapping("/delete")
  @Transactional
  public ResponseEntity<?> delete(@RequestParam(required = false) Long id,
      @RequestParam(required = false) String email) {
    Object obj = find(id, email).getBody();
    if (obj instanceof User user) {
      userService.delete(user);
      return ResponseEntity.ok(user);
    }

    return ResponseEntity.notFound().build();
  }


  @GetMapping("/roles/{email}/find")
  public ResponseEntity<?> findRoles(@PathVariable String email) {
    User user = userService.findByEmail(email);
    if (user == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(user.getRoles());
  }

  @PutMapping("/roles/{email}/remove")
  public ResponseEntity<?> removeRole(@PathVariable String email,
      @RequestParam String role, @RequestParam(defaultValue = "true", required = false) Boolean validate){
    return roleToUser(email, role, false, validate);

  }

  @PutMapping("/roles/{email}")
  public ResponseEntity<?> setRole(@PathVariable String email, @RequestParam String role) {
    return roleToUser(email, role, true, true);
  }

  @Transactional
  public ResponseEntity<?> roleToUser(String email, String roleName, boolean give, boolean validate) {
    User user = userService.findByEmail(email);
    if (user == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(Map.of("not_found", "user not found!"));
    }
    roleName = Validate.validateFormRole(roleName, validate);
    Role role = roleService.findByName(roleName);
    if (role == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(Map.of("not_found", "role not found!"));
    }
    if(give) {
      user.getRoles().add(role);
    }else{
      user.getRoles().remove(role);
    }
    user = userService.save(user);
    return ResponseEntity.ok(user);
  }

}
