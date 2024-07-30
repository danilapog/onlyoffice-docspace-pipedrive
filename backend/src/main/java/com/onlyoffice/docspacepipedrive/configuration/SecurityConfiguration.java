/**
 *
 * (c) Copyright Ascensio System SIA 2024
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.onlyoffice.docspacepipedrive.configuration;

import com.onlyoffice.docspacepipedrive.security.AuthenticationEntryPointImpl;
import com.onlyoffice.docspacepipedrive.security.provider.ClientRegistrationAuthenticationProvider;
import com.onlyoffice.docspacepipedrive.security.provider.WebhookAuthenticationProvider;
import com.onlyoffice.docspacepipedrive.security.provider.JwtAuthenticationProvider;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationCodeGrantFilter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.context.request.async.WebAsyncManagerIntegrationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.ForwardedHeaderFilter;

import java.util.Arrays;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {
    private final AuthenticationEntryPointImpl authenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(final HttpSecurity http,
                                                   final BasicAuthenticationFilter clientRegistrationAuthenticationFilter,
                                                   final BearerTokenAuthenticationFilter jwtAuthenticationFilter,
                                                   final BasicAuthenticationFilter webhookAuthenticationFilter) throws Exception {
        http
                .authorizeHttpRequests(auth -> {
                    auth
                            .requestMatchers("/api/**").authenticated()
                            .requestMatchers(HttpMethod.DELETE, "/login/oauth2/code/{registrationId}").authenticated()
                            .anyRequest().permitAll();
                })
                .oauth2Client(httpSecurityOAuth2ClientConfigurer -> {
                    httpSecurityOAuth2ClientConfigurer.init(http);
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
                .exceptionHandling(httpSecurityExceptionHandlingConfigurer ->
                        httpSecurityExceptionHandlingConfigurer.authenticationEntryPoint(authenticationEntryPoint))
                .addFilterAfter(new ForwardedHeaderFilter(), WebAsyncManagerIntegrationFilter.class)
                .addFilterBefore(clientRegistrationAuthenticationFilter, OAuth2AuthorizationCodeGrantFilter.class)
                .addFilterAfter(jwtAuthenticationFilter, OAuth2AuthorizationCodeGrantFilter.class)
                .addFilterAfter(webhookAuthenticationFilter, OAuth2AuthorizationCodeGrantFilter.class);

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

    @Bean
    BasicAuthenticationFilter clientRegistrationAuthenticationFilter(final HttpSecurity http,
                                                                     final ClientRegistrationAuthenticationProvider clientRegistrationAuthenticationProvider) throws Exception {
        var authManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);

        authManagerBuilder.authenticationProvider(clientRegistrationAuthenticationProvider);

        AuthenticationManager authenticationManager = authManagerBuilder.build();
        return new BasicAuthenticationFilter(authenticationManager, authenticationEntryPoint) {
            @Override
            protected boolean shouldNotFilter(final HttpServletRequest request) {
                return new NegatedRequestMatcher(
                        new AntPathRequestMatcher("/login/oauth2/code/{registrationId}", "DELETE")
                ).matches(request);
            }
        };
    }

    @Bean
    BearerTokenAuthenticationFilter jwtAuthenticationFilter(final HttpSecurity http,
                                                            final JwtAuthenticationProvider jwtAuthenticationProvider) throws Exception {
        var authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);

        authenticationManagerBuilder.authenticationProvider(jwtAuthenticationProvider);

        AuthenticationManager authenticationManager = authenticationManagerBuilder.build();

        BearerTokenAuthenticationFilter jwtAuthenticationFilter =
                new BearerTokenAuthenticationFilter(authenticationManager) {
            @Override
            protected boolean shouldNotFilter(final HttpServletRequest request) {
                return new AntPathRequestMatcher("/api/v1/webhook/**").matches(request);
            }
        };

        jwtAuthenticationFilter.setAuthenticationEntryPoint(authenticationEntryPoint);

        return jwtAuthenticationFilter;
    }

    @Bean
    BasicAuthenticationFilter webhookAuthenticationFilter(final HttpSecurity http,
                                                          final WebhookAuthenticationProvider webhookAuthenticationProvider) throws Exception {
        var authManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);

        authManagerBuilder.authenticationProvider(webhookAuthenticationProvider);

        AuthenticationManager authenticationManager = authManagerBuilder.build();

        return new BasicAuthenticationFilter(authenticationManager, authenticationEntryPoint) {
            @Override
            protected boolean shouldNotFilter(final HttpServletRequest request) {
                return new NegatedRequestMatcher(new AntPathRequestMatcher("/api/v1/webhook/**")).matches(request);
            }
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
