package co.judisa.config;

import co.judisa.security.CambioPasswordObligatorioFilter;
import co.judisa.security.JwtAuthFilter;
import co.judisa.security.JwtService;
import co.judisa.security.RestAuthenticationEntryPoint;
import co.judisa.security.WebLoginFailureHandler;
import tools.jackson.databind.ObjectMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.Duration;
import java.util.List;

/**
 * Dos cadenas de seguridad:
 * <ol>
 *   <li>/api/** — sin estado, JWT en la cabecera Authorization, sin CSRF (no usa cookies), CORS restringido.</li>
 *   <li>Resto (web Thymeleaf) — formulario de login con sesión y CSRF activo.</li>
 * </ol>
 */
@Configuration
public class SecurityConfig {

    private static final String[] RECURSOS_PUBLICOS = {
            "/login", "/webjars/**", "/css/**", "/js/**", "/img/**", "/favicon.ico", "/error",
            "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/actuator/health"
    };

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // RNF4: BCrypt (factor 10, validación < 2 s)
    }

    /** Un único AuthenticationManager para web y API, que publica eventos (bloqueo por intentos fallidos). */
    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService,
                                                       PasswordEncoder passwordEncoder,
                                                       ApplicationEventPublisher publisher) {
        DaoAuthenticationProvider proveedor = new DaoAuthenticationProvider(userDetailsService);
        proveedor.setPasswordEncoder(passwordEncoder);
        ProviderManager manager = new ProviderManager(proveedor);
        manager.setAuthenticationEventPublisher(new DefaultAuthenticationEventPublisher(publisher));
        return manager;
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain apiFilterChain(HttpSecurity http, JwtService jwtService,
                                              UserDetailsService userDetailsService, ObjectMapper objectMapper,
                                              CorsConfigurationSource corsConfigurationSource) throws Exception {
        RestAuthenticationEntryPoint entryPoint = new RestAuthenticationEntryPoint(objectMapper);
        http.securityMatcher("/api/**")
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(b -> b.disable())
                .formLogin(f -> f.disable())
                .logout(l -> l.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/api/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login", "/api/v1/auth/refresh",
                                "/api/v1/auth/logout").permitAll()
                        .anyRequest().hasRole("ADMIN"))
                .exceptionHandling(e -> e.authenticationEntryPoint(entryPoint).accessDeniedHandler(entryPoint))
                .addFilterBefore(new JwtAuthFilter(jwtService, userDetailsService), UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(new CambioPasswordObligatorioFilter(true, List.of("/api/v1/auth/"), objectMapper),
                        BasicAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain webFilterChain(HttpSecurity http, AuthenticationManager authenticationManager,
                                              SecurityContextRepository securityContextRepository,
                                              ObjectMapper objectMapper) throws Exception {
        http.authenticationManager(authenticationManager)
                .securityContext(sc -> sc.securityContextRepository(securityContextRepository))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(RECURSOS_PUBLICOS).permitAll()
                        .anyRequest().hasRole("ADMIN"))
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/", true)
                        .failureHandler(new WebLoginFailureHandler())
                        .permitAll())
                // RF-3: cerrar sesión invalida la sesión y borra la cookie
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?salir")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID"))
                .sessionManagement(s -> s.sessionFixation(f -> f.migrateSession()))
                .addFilterAfter(new CambioPasswordObligatorioFilter(false,
                                List.of("/cuenta/password", "/logout", "/login", "/webjars/", "/css/", "/js/", "/img/", "/error"),
                                objectMapper),
                        BasicAuthenticationFilter.class);
        return http.build();
    }

    /** RNF4: CORS restringido a los orígenes configurados en CORS_ALLOWED_ORIGINS. */
    @Bean
    public CorsConfigurationSource corsConfigurationSource(JudisaProperties props) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(props.cors().allowedOrigins());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        config.setExposedHeaders(List.of("Content-Disposition"));
        config.setMaxAge(Duration.ofHours(1));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
