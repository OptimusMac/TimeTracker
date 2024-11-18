package ru.optimusmac.timetracker.utils;


import lombok.experimental.UtilityClass;

@UtilityClass
public class Validate {
  private static final String prefix = "ROLE_";


  public static String validateFormRole(String role, boolean validate){
    if(!role.startsWith(prefix) && validate){
      return prefix + role;
    }
    return role;
  }

}
