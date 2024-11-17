package ru.optimusmac.TimeTracker.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

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
      String email = user.getUsername();
      model.addAttribute("email", email); // Передаем в модель
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

}
