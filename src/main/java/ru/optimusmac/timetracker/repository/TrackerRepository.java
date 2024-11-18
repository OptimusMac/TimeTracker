package ru.optimusmac.timetracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.optimusmac.timetracker.model.WorkSession;

public interface TrackerRepository extends JpaRepository<WorkSession, Long> {

  WorkSession findByName(String name);

}
