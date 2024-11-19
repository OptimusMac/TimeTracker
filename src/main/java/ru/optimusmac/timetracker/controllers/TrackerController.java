package ru.optimusmac.timetracker.controllers;


import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
import ru.optimusmac.timetracker.model.Role;
import ru.optimusmac.timetracker.model.User;
import ru.optimusmac.timetracker.model.WorkSession;
import ru.optimusmac.timetracker.model.WorkSessionStatus;
import ru.optimusmac.timetracker.requests.InfoRequest;
import ru.optimusmac.timetracker.service.TrackerService;
import ru.optimusmac.timetracker.service.UserService;

@RestController
@RequestMapping("/tracker")
@AllArgsConstructor
public class TrackerController {

  private final TrackerService service;
  private final UserService userService;

  @PostMapping("/session/create")
  public WorkSession create(@RequestBody WorkSession workSession,
      @AuthenticationPrincipal org.springframework.security.core.userdetails.User user) {
    User us = userService.findByEmail(user.getUsername());

    workSession = service.createSession(workSession);  // Сохраняем сессию в базе данных

    us.getActiveSessions().add(workSession);

    userService.save(us);

    return workSession;
  }


  @GetMapping("/info")
  public ResponseEntity<InfoRequest> getInfo(
      @AuthenticationPrincipal org.springframework.security.core.userdetails.User user) {
    if (user == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }
    User us = userService.findByEmail(user.getUsername());
    return ResponseEntity.ok(buildInfo(us));
  }

  @GetMapping("/admin/info")
  public ResponseEntity<InfoRequest> getAdminableUser(
      @RequestParam Long id) {

    User user = userService.findById(id);
    if (user == null) {
      return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok(buildInfo(user));
  }

  @Transactional
  public InfoRequest buildInfo(User user) {
    int completing = task(user, WorkSessionStatus.COMPLETED).size();
    int activeTasks = task(user, WorkSessionStatus.IN_PROGRESS).size();
    int totalTasks = user.getActiveSessions().size();
    Duration duration = Duration.ofNanos(avgTime(user.getActiveSessions()
        .stream()
        .map(WorkSession::getDuration)
        .collect(Collectors.toSet())));
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    String registrationDate = user.getRegister().format(formatter);
    String email = user.getEmail();
    String nickname = user.getNickname();
    List<String> roles = user.getRoles().stream().map(Role::getName).toList();

    return new InfoRequest(completing, activeTasks, totalTasks, formatedDuration(duration),
        registrationDate, email, nickname, roles);
  }

  @Transactional
  private String formatedDuration(Duration duration) {
    long hours = duration.toHours();
    long minutes = duration.toMinutes() % 60;
    long seconds = duration.getSeconds() % 60;

    return String.format("%02d ч %02d мин %02d с", hours, minutes, seconds);
  }

  @Transactional
  private Collection<WorkSession> task(User user, WorkSessionStatus status) {
    return user.getActiveSessions()
        .stream()
        .filter(workSession -> workSession.getStatus() == status)
        .collect(Collectors.toSet());
  }

  @Transactional
  public long avgTime(Collection<Duration> durations) {
    if (durations == null || durations.isEmpty()) {
      return 0;
    }
    Duration totalDuration = Duration.ZERO;

    for (Duration duration : durations) {
      if (duration == null) {
        continue;
      }
      totalDuration = totalDuration.plus(duration);
    }
    return totalDuration.toNanos() / durations.size();
  }


  @GetMapping("/session/admin/find")
  public WorkSession getSession(@RequestParam(required = false) Long id,
      @RequestParam(required = false) String name) {
    if (id != null) {
      return service.getById(id);
    } else if (name != null) {
      return service.getSession(name);
    } else {
      throw new IllegalArgumentException("Either id or name must be provided");
    }
  }

  @GetMapping("/session/profile")
  public Map<String, Object> getProfile(
      @AuthenticationPrincipal org.springframework.security.core.userdetails.User user) {
    User us = userService.findByEmail(user.getUsername());
    Collection<WorkSession> activeSessions = us.getActiveSessions();
    Map<String, Object> response = new HashMap<>();
    response.put("trackers", activeSessions);
    return response;
  }

  @GetMapping("session/find")
  public ResponseEntity<WorkSession> findSession(@RequestParam Long id,
      @AuthenticationPrincipal org.springframework.security.core.userdetails.User user) {
    User us = userService.findByEmail(user.getUsername());
    WorkSession session =
        us.getActiveSessions()
            .stream()
            .filter(s -> Objects.equals(s.getId(), id))
            .findFirst()
            .orElse(null);

    if (session == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }
    return ResponseEntity.ok(session);
  }


  @GetMapping("/session/action")
  public ResponseEntity<Boolean> checkStage(@RequestParam Long id,
      @RequestParam String stage,
      @AuthenticationPrincipal org.springframework.security.core.userdetails.User user) {

    User us = userService.findByEmail(user.getUsername());

    WorkSession workSession =
        us.getActiveSessions()
            .stream()
            .filter(session -> Objects.equals(session.getId(), id))
            .findFirst()
            .orElse(null);

    if (workSession == null) {
      return new ResponseEntity<>(false, HttpStatus.NOT_FOUND);
    }

    return new ResponseEntity<>(workSession.getStatus().name().equals(stage), HttpStatus.ACCEPTED);
  }

  @DeleteMapping("/session/delete")
  @Transactional
  public ResponseEntity<?> safeDelete(@RequestParam Long id,
      @AuthenticationPrincipal org.springframework.security.core.userdetails.User user) {
    User us = userService.findByEmail(user.getUsername());
    WorkSession workSession =
        us.getActiveSessions()
            .stream()
            .filter(session -> Objects.equals(session.getId(), id))
            .findFirst()
            .orElse(null);

    us.getActiveSessions().remove(workSession);
    userService.save(us);
    return ResponseEntity.ok().body(us);
  }


  @PutMapping("/session/{id}")
  public ResponseEntity<WorkSession> updateSession(@PathVariable Long id,
      @RequestBody WorkSession updatedSession) {
    WorkSession existingSession = service.getById(id);

    existingSession.setName(updatedSession.getName());
    existingSession.setStart(updatedSession.getStart());
    existingSession.setEndTime(updatedSession.getEndTime());
    existingSession.setDuration(updatedSession.getDuration());
    existingSession.setStatus(updatedSession.getStatus());

    WorkSession savedSession = service.save(existingSession);

    return ResponseEntity.ok(savedSession);
  }

  @GetMapping("/session/status")
  public ResponseEntity<?> setStatus(@RequestParam(required = false) Long id,
      @RequestParam(required = false) String name,
      @RequestParam String status) {
    WorkSession session = getSession(id, name);

    if (session == null) {
      return ResponseEntity.status(404).body(null);
    }

    if (session.getStatus() == WorkSessionStatus.IN_PROGRESS) {
      session.setEndTime(LocalDateTime.now());
      Duration duration = Duration.between(session.getStart(), session.getEndTime());
      session.setDuration(duration);
    } else if (session.getStatus() == WorkSessionStatus.WAITED) {
      session.setStart(LocalDateTime.now());
    }
    session.setStatus(WorkSessionStatus.valueOf(status.toUpperCase(Locale.ROOT)));
    return ResponseEntity.ok(service.save(session));
  }


  @GetMapping("/session")
  public ResponseEntity<Collection<WorkSession>> getSession(@RequestParam Long id){
    User user = userService.findById(id);
    return ResponseEntity.ok(user.getActiveSessions());
  }


  @DeleteMapping("/admin/delete")
  @Transactional
  public ResponseEntity<?> adminDelete(@RequestParam Long id, @RequestParam Long userID){
    User user = userService.findById(userID);

    user.getActiveSessions().removeIf(workSession -> workSession.getId().equals(id));
    userService.save(user);
    service.deleteById(id);
    return ResponseEntity.ok().build();
  }
}
