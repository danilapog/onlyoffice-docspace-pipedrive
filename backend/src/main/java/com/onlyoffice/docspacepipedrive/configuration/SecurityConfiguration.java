package com.onlyoffice.docspacepipedrive.configuration;

import com.onlyoffice.docspacepipedrive.security.AuthenticationEntryPointImpl;
import com.onlyoffice.docspacepipedrive.security.AuthenticationSuccessHandlerImpl;
import com.onlyoffice.docspacepipedrive.security.jwt.JwtLoginAuthenticationFilter;
import com.onlyoffice.docspacepipedrive.security.jwt.JwtLogoutAuthenticationFilter;
import com.onlyoffice.docspacepipedrive.security.jwt.manager.JwtManager;
import com.onlyoffice.docspacepipedrive.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationEntryPointFailureHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;


@Configuration
@EnableWebSecurity
@Slf4j
@RequiredArgsConstructor
public class SecurityConfiguration {

    @Value("${spring.security.oauth2.client.registration.pipedrive.client-secret}")
    private String jwtSecretKey;
    @Value("${spring.security.oauth2.client.provider.pipedrive.nested-user-name-attribute}")
    private String oauthUserNameAttribute;
    @Value("${spring.security.jwt.user-name-attribute}")
    private String jwtUserNameAttribute;
    @Value("${spring.security.jwt.header}")
    private String jwtHeader;
    @Value("${spring.security.jwt.prefix}")
    private String jwtPrefix;

    private final JwtManager jwtManager;
    private final UserService userService;
    private final AuthenticationEntryPointImpl authenticationEntryPoint;

    private final AuthenticationSuccessHandlerImpl authenticationSuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        AuthenticationManager authenticationManager = (AuthenticationManager)http.getSharedObject(AuthenticationManager.class);

        AuthenticationFailureHandler authenticationFailureHandler = getAuthenticationFailureHandler();

        JwtLoginAuthenticationFilter jwtLoginAuthenticationFilter = createJwtLoginAuthenticationFilter(
                authenticationManager,
                userService,
                jwtManager,
                authenticationFailureHandler
        );

        JwtLoginAuthenticationFilter jwtLogoutAuthenticationFilter = createJwtLogoutAuthenticationFilter(
                authenticationManager,
                userService,
                jwtManager,
                authenticationFailureHandler
        );

        http
                .authorizeHttpRequests(auth -> {
                    auth
                            .anyRequest().authenticated();
                })

                // OAuth security configuration
                .oauth2Login(httpSecurityOAuth2LoginConfigurer -> {
                    httpSecurityOAuth2LoginConfigurer
                        .failureHandler(authenticationFailureHandler)
                        .successHandler(authenticationSuccessHandler);
                })

                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .cors(httpSecurityCorsConfigurer -> {
                    httpSecurityCorsConfigurer
                            .configurationSource(corsConfigurationSource());
                })
                .sessionManagement(httpSecuritySessionManagementConfigurer -> {
                        httpSecuritySessionManagementConfigurer
                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                })
                .addFilterBefore(jwtLogoutAuthenticationFilter, OAuth2LoginAuthenticationFilter.class)
                .addFilterAfter(jwtLoginAuthenticationFilter, OAuth2LoginAuthenticationFilter.class);

        return http.build();
    }

    //ToDo: Modify Cors settings
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.addAllowedOrigin("*");
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    private JwtLoginAuthenticationFilter createJwtLoginAuthenticationFilter(
            AuthenticationManager authenticationManager,
            UserService userService,
            JwtManager jwtManager,
            AuthenticationFailureHandler authenticationFailureHandler) {
        JwtLoginAuthenticationFilter jwtLoginAuthenticationFilter = new JwtLoginAuthenticationFilter(
                authenticationManager,
                userService,
                jwtManager
        );

        jwtLoginAuthenticationFilter.setFilterProcessesUrl("/**");
        jwtLoginAuthenticationFilter.setSecretKey(jwtSecretKey);
        jwtLoginAuthenticationFilter.setOauthUserNameAttribute(oauthUserNameAttribute);
        jwtLoginAuthenticationFilter.setUserNameAttribute(jwtUserNameAttribute);
        jwtLoginAuthenticationFilter.setAuthorizationHeader(jwtHeader);
        jwtLoginAuthenticationFilter.setAuthorizationPrefix(jwtPrefix);
        jwtLoginAuthenticationFilter.setAuthenticationFailureHandler(authenticationFailureHandler);

        return jwtLoginAuthenticationFilter;
    }

    private JwtLogoutAuthenticationFilter createJwtLogoutAuthenticationFilter(AuthenticationManager authenticationManager,
                                                                              UserService userService,
                                                                              JwtManager jwtManager,
                                                                              AuthenticationFailureHandler authenticationFailureHandler) {
        JwtLogoutAuthenticationFilter jwtLogoutAuthenticationFilter = new JwtLogoutAuthenticationFilter(
                authenticationManager,
                userService,
                jwtManager
        );

        jwtLogoutAuthenticationFilter.setFilterProcessesUrl("/login/oauth2/code/pipedrive", "DELETE");
        jwtLogoutAuthenticationFilter.setSecretKey(jwtSecretKey);
        jwtLogoutAuthenticationFilter.setUserNameAttribute(jwtUserNameAttribute);
        jwtLogoutAuthenticationFilter.setAuthorizationHeader(jwtHeader);
        jwtLogoutAuthenticationFilter.setAuthorizationPrefix(jwtPrefix);
        jwtLogoutAuthenticationFilter.setAuthenticationFailureHandler(authenticationFailureHandler);

        return jwtLogoutAuthenticationFilter;
    }

    private AuthenticationFailureHandler getAuthenticationFailureHandler() {
        return new AuthenticationEntryPointFailureHandler(authenticationEntryPoint);
    }
}
