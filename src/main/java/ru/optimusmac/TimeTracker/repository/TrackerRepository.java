package ru.optimusmac.TimeTracker.repository;

import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.optimusmac.TimeTracker.model.WorkSession;

public interface TrackerRepository extends JpaRepository<WorkSession, Long> {

  WorkSession findByName(String name);

}
