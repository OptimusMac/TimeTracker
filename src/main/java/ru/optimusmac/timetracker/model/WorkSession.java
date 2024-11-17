package ru.optimusmac.TimeTracker.model;

import jakarta.annotation.Nullable;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@Entity
@Table(name = "project")
public class WorkSession {


  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String name;
  @Nullable
  private LocalDateTime start;
  @Nullable
  private LocalDateTime endTime;
  private Duration duration;

  @Enumerated(EnumType.STRING)
  private WorkSessionStatus status = WorkSessionStatus.WAITED;


}
