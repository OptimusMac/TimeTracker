package ru.optimusmac.timetracker.requests;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InfoRequest {

  int completedTasks;
  int activeTasks;
  int totalTasks;
  String averageTime;
  String registrationDate;
  String email;
  String nickname;
  List<String> roles;

}
