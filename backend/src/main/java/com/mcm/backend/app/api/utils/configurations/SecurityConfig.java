
package com.mcm.backend.app.api.utils.configurations;

import com.mcm.backend.app.middlewares.jwt.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                // allow everything past this point
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )
                // but still run your JWT filter to set the request attribute
                .addFilterBefore(new JwtFilter(), SecurityContextHolderFilter.class);

        return http.build();
    }
}
