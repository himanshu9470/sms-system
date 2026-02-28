// SecurityConfig.java
package com.studentmanagement.config;

import com.studentmanagement.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(
                auth -> auth.requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**", "/uploads/**")
                        .permitAll().requestMatchers("/", "/login", "/register", "/about").permitAll()
                        .requestMatchers("/forgot-password", "/verify-otp", "/reset-password").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN").requestMatchers("/faculty/**")
                        .hasAnyRole("ADMIN", "FACULTY").requestMatchers("/parent/**").hasRole("PARENT")
                        .requestMatchers("/student/**").hasAnyRole("ADMIN", "FACULTY", "STUDENT")
                        .requestMatchers("/dashboard").authenticated().anyRequest().authenticated())
                .formLogin(form -> form.loginPage("/login").loginProcessingUrl("/login")
                        .defaultSuccessUrl("/dashboard", true).failureUrl("/login?error=true").permitAll())
                .logout(logout -> logout.logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .logoutSuccessUrl("/login?logout=true").invalidateHttpSession(true).deleteCookies("JSESSIONID")
                        .permitAll())
                .userDetailsService(customUserDetailsService)
                .exceptionHandling(ex -> ex.accessDeniedPage("/access-denied"));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}