package ru.optimusmac.timetracker.controllers;

import lombok.AllArgsConstructor;
import org.hibernate.event.spi.SaveOrUpdateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.optimusmac.timetracker.model.WorkSession;
import ru.optimusmac.timetracker.service.UserService;

@Controller
@AllArgsConstructor
public class AuthController {


  private final UserService userService;

  @GetMapping("/")
  public String join(@AuthenticationPrincipal User user){
    if(user == null){
      return "redirect:/login";
    }
    return "redirect:/home";
  }
  @GetMapping("/login")
  public String showLoginPage(@AuthenticationPrincipal User user) {
    if(user != null){
      return "redirect:/home";
    }
    return "login";
  }
  @GetMapping("/home")
  public String home(@AuthenticationPrincipal User user, Model model){
    if (user != null) {
      ru.optimusmac.timetracker.model.User us = userService.findByEmail(user.getUsername());

      String name = us.getEmail();

      if(us.getNickname() != null)
        name = us.getNickname();

      model.addAttribute("identifier", name);
      model.addAttribute("email", us.getEmail());
      model.addAttribute("id", us.getId());
      model.addAttribute("admin", us.getRoles().stream().anyMatch(role -> role.getName().endsWith("ADMIN")));
    }
    return "home";
  }
  @GetMapping("/register")
  public String registerPage(@AuthenticationPrincipal User user){
    if(user != null){
      return "redirect:/home";
    }
    return "register";
  }


  private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);

  @GetMapping("/user/profile")
  public String profile(@AuthenticationPrincipal User user, Model model) {
    ru.optimusmac.timetracker.model.User us = userService.findByEmail(user.getUsername());
    model.addAttribute("id", us.getId());  // Добавляем id в модель
    logger.debug("User ID: {}", us.getId());  // Логирование через логгер
    return "profile"; // Возвращаем имя представления (HTML)
  }

  @GetMapping("/my-trackers")
  public String myTrackers(@AuthenticationPrincipal User user, Model model){
    if (user != null) {
      ru.optimusmac.timetracker.model.User us = userService.findByEmail(user.getUsername());

      String name = us.getEmail();

      if(us.getNickname() != null)
        name = us.getNickname();

      model.addAttribute("identifier", name);
      model.addAttribute("id", us.getId());
    }
    return "my-trackers";
  }

  @GetMapping("/err")
  public String handleError(
      @RequestParam(name = "code", required = false, defaultValue = "Unknown") String errorCode,
      @RequestParam(name = "message", required = false, defaultValue = "Unexpected error occurred") String errorMessage,
      Model model) {
    model.addAttribute("errorCode", errorCode);
    model.addAttribute("errorMessage", errorMessage);
    model.addAttribute("errorDescription", getErrorDescription(errorCode));
    return "err";
  }



  @GetMapping("/create-tracker")
  public String createTracker(@AuthenticationPrincipal User user, Model model){
    ru.optimusmac.timetracker.model.User us = userService.findByEmail(user.getUsername());

    String name = us.getEmail();

    if(us.getNickname() != null)
      name = us.getNickname();

    model.addAttribute("identifier", name);
    model.addAttribute("id", us.getId());
      return "createTracker";
  }

  private String getErrorDescription(String errorCode) {
    return switch (errorCode) {
      case "403" -> "У вас нет прав для доступа к этой странице.";
      case "404" -> "Страница не найдена.";
      case "500" -> "Ошибка на сервере.";
      default -> "Произошла неожиданная ошибка.";
    };
  }



  @GetMapping("/admin/panel")
  public String adminPanel(){
    return "adminPanel";
  }

  @GetMapping("admin/panel/settings")
  public String settingsPanel(@RequestParam Long id, Model model, @AuthenticationPrincipal User user){
    model.addAttribute("id", id);
    model.addAttribute("email", user.getUsername());
    return "settings";
  }
}
