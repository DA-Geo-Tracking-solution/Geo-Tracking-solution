package at.htlhl.geo_tracking_solution.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Controller;

@Controller
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests((authz) -> authz
                    .requestMatchers("/", "/public/**").permitAll()
                    .requestMatchers("/ws", "/ws/geo-tracking-solution/**").permitAll()
                    .requestMatchers("/member/**").hasRole("member")
                    .requestMatchers("/squadmaster/**").hasRole("squadmaster")
                    .requestMatchers("/groupmaster/**").hasRole("groupmaster")
                    .requestMatchers("/v3/**", "/swagger-ui/**").denyAll()
                    .anyRequest().authenticated()
            )
            .oauth2ResourceServer(auth ->
                    auth.jwt(token -> token.jwtAuthenticationConverter(new KeycloakJwtAuthenticationConverter())));

        return http.build();
    }
}
