package ru.optimusmac.timetracker.service;

import java.util.Collection;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.optimusmac.timetracker.model.User;
import ru.optimusmac.timetracker.repository.UserRepository;

@Service
@AllArgsConstructor
public class UserService {


  private final UserRepository repository;

  public User create(User user){
    return repository.save(user);
  }

  public User findById(Long id){
    return repository.findById(id)
        .orElse(null);
  }

  public Collection<User> findAll(){
    return repository.findAll();
  }

  public User findByEmail(String email){
    return repository.findByEmail(email);
  }

  public void delete(User user){
    repository.delete(user);
  }

  public User save(User user){
    return repository.save(user);
  }
}
