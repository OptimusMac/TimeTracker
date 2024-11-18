package ru.optimusmac.timetracker.model;

import jakarta.annotation.Nullable;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
    private LocalDateTime start;
    private LocalDateTime endTime;
    private Duration duration = Duration.ZERO;
    private String description;


    @Enumerated(EnumType.STRING)
    private WorkSessionStatus status = WorkSessionStatus.WAITED;
  }
