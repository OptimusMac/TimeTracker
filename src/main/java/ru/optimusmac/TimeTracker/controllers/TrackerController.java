package ru.optimusmac.TimeTracker.controllers;


import java.security.Principal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.optimusmac.TimeTracker.model.WorkSession;
import ru.optimusmac.TimeTracker.model.WorkSessionStatus;
import ru.optimusmac.TimeTracker.service.TrackerService;

@RestController
@RequestMapping("/tracker")
@AllArgsConstructor
public class TrackerController {

  private final TrackerService service;

  @PostMapping("/session/create")
  public WorkSession create(@RequestBody WorkSession workSession) {
    return service.createSession(workSession);
  }


  @GetMapping("/sessions")
  public Collection<WorkSession> findAll() {
    return service.findAll();
  }

  @GetMapping("/session/find")
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

  @PostMapping("/session/start")
  public ResponseEntity<?> setStarted(@RequestParam(required = false) Long id,
      @RequestParam(required = false) String name) {
    WorkSession session = getSession(id, name);

    if (session == null) {
      return ResponseEntity.status(404).body(null);
    }

    session.setStart(LocalDateTime.now());
    session.setStatus(WorkSessionStatus.IN_PROGRESS);
    return updateSession(session.getId(), session);
  }

  @PostMapping("/session/end")
  public ResponseEntity<?> setEnd(@RequestParam(required = false) Long id,
      @RequestParam(required = false) String name) {
    WorkSession session = getSession(id, name);

    if (session == null) {
      return ResponseEntity.status(404).body(null);
    }
    if(session.getStatus() != WorkSessionStatus.IN_PROGRESS){
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(Map.of("status_exception", "Session cannot continue because it is not in progress, current status: " + session.getStatus().name()));
    }

    session.setEndTime(LocalDateTime.now());
    Duration duration = Duration.between(session.getStart(), session.getEndTime());
    session.setDuration(duration);
    session.setStatus(WorkSessionStatus.COMPLETED);
    return updateSession(session.getId(), session);
  }
}
