package ru.optimusmac.timetracker.controllers;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;
import ru.optimusmac.timetracker.model.Role;
import ru.optimusmac.timetracker.model.User;
import ru.optimusmac.timetracker.response.ErrorResponse;
import ru.optimusmac.timetracker.service.RoleService;
import ru.optimusmac.timetracker.service.UserService;
import ru.optimusmac.timetracker.utils.Validate;

@RestController
@RequestMapping("/user")
@AllArgsConstructor
public class UserController {


  public final UserService userService;
  public final RoleService roleService;
  private final BCryptPasswordEncoder passwordEncoder;


  @GetMapping("/admin/all")
  public Collection<User> findAll() {
    return userService.findAll();
  }


  @PostMapping("/create")
  public ResponseEntity<?> create(@RequestBody User user) {
    User checkUser = userService.findByEmail(user.getEmail());
    if (checkUser != null) {
      return ResponseEntity.badRequest()
          .body(new ErrorResponse("Email is already in use.", "email"));

    }

    user.setPassword(passwordEncoder.encode(user.getPassword()));
    user.setRegister(LocalDateTime.now());
    user.getRoles().add(roleService.findByName("ROLE_USER"));
    User created = userService.create(user);
    return ResponseEntity.ok(created);
  }

  @PutMapping("/change/nick")
  public ResponseEntity<?> changeNickname(@RequestParam String nickname,
      @AuthenticationPrincipal org.springframework.security.core.userdetails.User user){
    User us = userService.findByEmail(user.getUsername());
    us.setNickname(nickname);
    return ResponseEntity.ok().body(userService.save(us));
  }


  @GetMapping("/admin/find")
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

  @DeleteMapping("/admin/delete")
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

  @GetMapping("{email}/tracks")
  public ResponseEntity<Void> userTrackers(@PathVariable String email, @AuthenticationPrincipal org.springframework.security.core.userdetails.User user) {
    HttpHeaders headers = new HttpHeaders();
    if (!email.equals(user.getUsername())) {
      headers.add("Location", "/err?code=403&message=Access Denied");
      return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    headers.add("Location", "/my-trackers");
    return new ResponseEntity<>(headers, HttpStatus.FOUND);
  }


  @GetMapping("/admin/roles/{email}/find")
  public ResponseEntity<?> findRoles(@PathVariable String email) {
    User user = userService.findByEmail(email);
    if (user == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(user.getRoles());
  }

  @PutMapping("/admin/roles/{id}/remove")
  public ResponseEntity<?> removeRole(@PathVariable Long id,
      @RequestParam String role,
      @RequestParam(defaultValue = "true", required = false) Boolean validate) {
    return roleToUser(id, role, false, validate);

  }

  @PutMapping("/admin/roles/{id}")
  public ResponseEntity<?> setRole(@PathVariable Long id, @RequestParam String role) {
    return roleToUser(id, role, true, true);
  }

  @Transactional
  public ResponseEntity<?> roleToUser(Long id, String roleName, boolean give,
      boolean validate) {
    User user = userService.findById(id);
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
    if (give) {
      user.getRoles().add(role);
    } else {
      user.getRoles().remove(role);
    }
    user = userService.save(user);
    return ResponseEntity.ok(user);
  }

}
