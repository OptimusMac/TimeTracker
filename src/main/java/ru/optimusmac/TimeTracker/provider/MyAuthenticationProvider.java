package ru.optimusmac.TimeTracker.provider;

import java.io.Serializable;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.optimusmac.TimeTracker.model.User;
import ru.optimusmac.TimeTracker.service.UserService;

@AllArgsConstructor
public class MyAuthenticationProvider implements AuthenticationProvider, Serializable {

  private final UserService userService;
  private final PasswordEncoder passwordEncoder;

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {

    String username = authentication.getName();
    String password = (String) authentication.getCredentials();
    User user = userService.findByEmail(username);
    if (!passwordEncoder.matches(password, user.getPassword())) {
      throw new BadCredentialsException("Invalid credentials");
    }


    return new UsernamePasswordAuthenticationToken(
        authentication.getPrincipal(),
        password,
        user.getRoles().stream().map(role -> new SimpleGrantedAuthority(role.getName()))
            .collect(Collectors.toSet())
    );
  }

  @Override
  public boolean supports(Class<? extends Object> authentication) {
    return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
  }

}