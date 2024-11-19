package ru.optimusmac.timetracker.configs;

import static org.springframework.security.config.Customizer.withDefaults;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import ru.optimusmac.timetracker.model.User;
import ru.optimusmac.timetracker.response.AccessDeniedHandler403;
import ru.optimusmac.timetracker.service.UserService;

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
            .requestMatchers("/login", "/register").permitAll()

            .requestMatchers("/user/admin/**", "/role/**", "/tracker/session/admin/**", "/tracker/admin/info", "admin/panel").hasRole("ADMIN")
            .requestMatchers("/tracker/session/create", "/tracker/session/end", "/tracker/session/start", "/tracker/session/{id}", "/session/profile", "/session/{id}/action", "/user/profile", "/profile/upload", "/user/change/nick", "/tracker/create-tracker").authenticated()
            .requestMatchers("/user/{email}/tracks", "/home").authenticated()

            .anyRequest().permitAll()
        )
        .formLogin(form -> form
            .loginPage("/login")
            .defaultSuccessUrl("/home", true)
            .permitAll()
        )
        .logout(logout -> logout
            .logoutUrl("/logout")
            .logoutSuccessUrl("/login?logout")
            .invalidateHttpSession(true)
            .deleteCookies("JSESSIONID")
        )
        .exceptionHandling(exc -> exc
            .accessDeniedHandler(new AccessDeniedHandler403()))
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
  public BCryptPasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

}
