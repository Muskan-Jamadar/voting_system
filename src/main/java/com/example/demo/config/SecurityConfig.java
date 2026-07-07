//package com.example.demo.config;
//
//import org.springframework.context.annotation.*;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//
//@Configuration
//public class SecurityConfig {
//
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//
//        http
//            .csrf(csrf -> csrf.disable())
//
//            .authorizeHttpRequests(auth -> auth
//                .requestMatchers(
//                        "/", "/landing",
//                        "/register", "/login",
//                        "/send-otp", "/login-otp", "/verify-login-otp",
//                        "/css/**", "/js/**", "/images/**"
//                ).permitAll()
//
//                .requestMatchers("/admin/**").hasRole("ADMIN")
//
//                .anyRequest().permitAll()   // 🔥 IMPORTANT FOR DEBUG
//            )
//
//            .formLogin(form -> form.disable());
//
//        return http.build();
//    }
//
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//}

package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/", "/landing",
                    "/register", "/login",
                    "/login-otp", "/send-otp", "/verify-login-otp",
                    "/css/**", "/js/**", "/images/**"
                ).permitAll()

                .requestMatchers("/admin/**").hasAuthority("ADMIN")

                .anyRequest().authenticated()
            )
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}