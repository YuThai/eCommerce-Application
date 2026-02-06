package Ecom.SecurityConfig;

import java.util.Arrays;
import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class AppConfig {

    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain springSecurityConfiguration(HttpSecurity http) throws Exception {

        http.sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .cors(cors -> {
                    cors.configurationSource(new CorsConfigurationSource() {
                        @Override
                        public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {

                            CorsConfiguration cfg = new CorsConfiguration();

                            String allowedOrigins = System.getenv("CORS_ORIGINS");
                            if (allowedOrigins != null && !allowedOrigins.isEmpty()) {
                                cfg.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
                            } else {
                                cfg.setAllowedOrigins(Arrays.asList(
                                    "http://localhost:3000",
                                    "http://localhost:3001",
                                    "https://eccomers96.netlify.app"
                                ));
                            }

                            cfg.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
                            cfg.setAllowCredentials(true);
                            cfg.setAllowedHeaders(Collections.singletonList("*"));
                            cfg.setExposedHeaders(Arrays.asList("Authorization"));

                            return cfg;

                        }
                    });
                })
                .authorizeHttpRequests(auth -> {
                    auth
                            .requestMatchers(new AntPathRequestMatcher("/ecom/admin", HttpMethod.POST.name())).permitAll()
                            .requestMatchers(new AntPathRequestMatcher("/ecom/customers", HttpMethod.POST.name())).permitAll()
                            .requestMatchers(new AntPathRequestMatcher("/ecom/orders/users/**", HttpMethod.DELETE.name())).permitAll()
                            .requestMatchers(new AntPathRequestMatcher("/ecom/product-reviews/**", HttpMethod.GET.name())).permitAll()
                            .requestMatchers(new AntPathRequestMatcher("/ecom/products/**", HttpMethod.GET.name())).permitAll()
                            .requestMatchers(new AntPathRequestMatcher("/ecom/signIn", HttpMethod.GET.name())).authenticated()
                            .requestMatchers(new AntPathRequestMatcher("/ecom/auth/login", HttpMethod.POST.name())).permitAll()
                            .requestMatchers(new AntPathRequestMatcher("ecom/products/insert", HttpMethod.POST.name())).permitAll()
                            .requestMatchers(new AntPathRequestMatcher("/ecom/product/**", HttpMethod.POST.name())).hasRole("ADMIN")
                            .requestMatchers(new AntPathRequestMatcher("/ecom/order-shippers/**", HttpMethod.POST.name())).hasRole("ADMIN")
                            .requestMatchers(new AntPathRequestMatcher("/ecom/product-reviews/**", HttpMethod.POST.name())).hasRole("USER")
                            .requestMatchers(new AntPathRequestMatcher("/ecom/customer-addresses/**", HttpMethod.POST.name())).hasRole("USER")
                            .requestMatchers(new AntPathRequestMatcher("/ecom/cart/**", HttpMethod.POST.name())).hasRole("USER")
                            .requestMatchers(new AntPathRequestMatcher("/ecom/orders/**", HttpMethod.POST.name())).hasRole("USER")
                            .requestMatchers(new AntPathRequestMatcher("/ecom/order-shipping/**", HttpMethod.POST.name())).hasRole("USER")
                            .requestMatchers(new AntPathRequestMatcher("/ecom/admin/**", HttpMethod.PUT.name())).hasRole("ADMIN")
                            .requestMatchers(new AntPathRequestMatcher("/ecom/products/**", HttpMethod.PUT.name())).hasRole("ADMIN")
                            .requestMatchers(new AntPathRequestMatcher("/ecom/product-reviews/**", HttpMethod.PUT.name())).hasRole("USER")
                            .requestMatchers(new AntPathRequestMatcher("/ecom/customer-addresses/update/**", HttpMethod.PUT.name())).hasRole("USER")
                            .requestMatchers(new AntPathRequestMatcher("/ecom/cart/**", HttpMethod.PUT.name())).hasRole("USER")
                            .requestMatchers(new AntPathRequestMatcher("/ecom/order-shipping/**", HttpMethod.PUT.name())).hasRole("USER")
                            .requestMatchers(new AntPathRequestMatcher("/ecom/products/**", HttpMethod.DELETE.name())).hasRole("ADMIN")
                            .requestMatchers(new AntPathRequestMatcher("/ecom/product-reviews/**", HttpMethod.DELETE.name())).hasRole("ADMIN")
                            .requestMatchers(new AntPathRequestMatcher("/ecom/customer-addresses/delete/**", HttpMethod.DELETE.name())).hasRole("ADMIN")
                            .requestMatchers(new AntPathRequestMatcher("/ecom/order-shipping/**", HttpMethod.DELETE.name())).hasRole("ADMIN")
                            .requestMatchers(new AntPathRequestMatcher("/ecom/order-shippers/**", HttpMethod.DELETE.name())).hasRole("ADMIN")
                            .requestMatchers(new AntPathRequestMatcher("/ecom/cart/remove-product/**", HttpMethod.DELETE.name())).hasRole("USER")
                            .requestMatchers(new AntPathRequestMatcher("/ecom/customer-addresses/**", HttpMethod.GET.name())).hasAnyRole("ADMIN", "USER")
                            .requestMatchers(new AntPathRequestMatcher("/ecom/cart/products/**", HttpMethod.GET.name())).hasAnyRole("ADMIN", "USER")
                            .requestMatchers(new AntPathRequestMatcher("/ecom/orders/**", HttpMethod.GET.name())).hasAnyRole("ADMIN", "USER")
                            .requestMatchers(new AntPathRequestMatcher("/ecom/order-shippers", HttpMethod.GET.name())).hasAnyRole("ADMIN", "USER")
                            .requestMatchers(new AntPathRequestMatcher("/ecom/order-payments/**", HttpMethod.GET.name())).hasAnyRole("ADMIN", "USER")
                            .requestMatchers(new AntPathRequestMatcher("/")).permitAll()
                            .requestMatchers(new AntPathRequestMatcher("/error")).permitAll()
                            .requestMatchers(new AntPathRequestMatcher("/actuator/**")).permitAll()
                            .requestMatchers(new AntPathRequestMatcher("/actuator/health/**")).permitAll()
                            .requestMatchers(new AntPathRequestMatcher("/swagger-ui*/**")).permitAll()
                            .requestMatchers(new AntPathRequestMatcher("/v3/api-docs/**")).permitAll()
                            .anyRequest().authenticated();
                })
                .authenticationProvider(authenticationProvider())
                .csrf(csrf -> csrf.disable())
                .addFilterAfter(new JwtTokenGeneratorFilter(), BasicAuthenticationFilter.class)
                .addFilterBefore(new JwtTokenValidatorFilter(), BasicAuthenticationFilter.class)
                .httpBasic(Customizer.withDefaults());

        return http.build();

    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}

