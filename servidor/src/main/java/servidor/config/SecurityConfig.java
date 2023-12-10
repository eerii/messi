package servidor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@EnableWebSecurity
@Configuration
public class SecurityConfig{

    @Bean
    protected DefaultSecurityFilterChain configure(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(auth -> auth.requestMatchers(
                AntPathRequestMatcher.antMatcher("/h2-console/**"),
                AntPathRequestMatcher.antMatcher("/api/**"))
                .permitAll()
                .anyRequest()
                .authenticated())
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions().disable()).build();
    }
}
