package ru.optimusmac.TimeTracker.configs;

import static org.springframework.security.config.Customizer.withDefaults;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import ru.optimusmac.TimeTracker.model.User;
import ru.optimusmac.TimeTracker.service.UserService;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfig {


  private UserService userService;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .userDetailsService(this::loadUserByUsername)
        .authorizeHttpRequests(authorize -> authorize
            .requestMatchers("/login", "/register").anonymous()
            .requestMatchers("/home").authenticated()
            .requestMatchers("/user/**", "/role/**", "/tracker/**").hasRole("ADMIN")
            .anyRequest().permitAll()
        )
        .formLogin(form -> form
            .loginPage("/login")
            .failureHandler(authenticationFailureHandler())
            .defaultSuccessUrl("/home", true)
            .permitAll()
        )
        .logout(logout -> logout
            .logoutUrl("/logout")
            .logoutSuccessUrl("/login?logout")
            .invalidateHttpSession(true)
            .deleteCookies("JSESSIONID")
        )
        .httpBasic(withDefaults());
    return http.build();
  }


  public UserDetails loadUserByUsername(String email)
      throws UsernameNotFoundException {

    User user = userService.findByEmail(email);

    if (user == null) {
      throw new UsernameNotFoundException("Bad credentials");
    }

    return new org.springframework.security.core.userdetails.User(
        user.getEmail(),
        user.getPassword(),
        user.getRoles().stream().map(role -> new SimpleGrantedAuthority(role.getName())).toList());
  }

  @Bean
  public AuthenticationFailureHandler authenticationFailureHandler() {
    return (request, response, exception) -> {
      response.sendRedirect("/login?error=true");
    };
  }

  @Bean
  public BCryptPasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

}
