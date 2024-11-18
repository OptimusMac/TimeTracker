package ru.optimusmac.timetracker.service;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.optimusmac.timetracker.model.WorkSession;
import ru.optimusmac.timetracker.repository.TrackerRepository;

@Service
@AllArgsConstructor
public class TrackerService {


  private final TrackerRepository trackerRepository;


  public WorkSession createSession(WorkSession workSession) {
    return trackerRepository.save(workSession);
  }

  public Collection<WorkSession> findAll() {
    return trackerRepository.findAll();
  }

  public Collection<WorkSession> findAllByName(String name) {
    return trackerRepository.findAll()
        .stream()
        .filter(workSession -> workSession.getName().equals(name))
        .collect(Collectors.toSet());
  }

  public WorkSession getSession(String name) {
    return trackerRepository.findByName(name);
  }

  public Collection<WorkSession> getAllById(Long id) {
    return trackerRepository.findAll()
        .stream()
        .filter(workSession -> Objects.equals(workSession.getId(), id))
        .collect(Collectors.toSet());
  }

  public WorkSession getById(Long id) {
    return trackerRepository.findById(id)
        .orElse(null);
  }

  public WorkSession save(WorkSession workSession){
    return trackerRepository.save(workSession);
  }

}
