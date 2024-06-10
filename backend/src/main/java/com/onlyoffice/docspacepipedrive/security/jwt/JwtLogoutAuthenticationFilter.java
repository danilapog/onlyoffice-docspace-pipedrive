package com.onlyoffice.docspacepipedrive.security.jwt;

import com.onlyoffice.docspacepipedrive.security.jwt.manager.JwtManager;
import com.onlyoffice.docspacepipedrive.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.IOException;


@Slf4j
public class JwtLogoutAuthenticationFilter extends JwtLoginAuthenticationFilter {
    public JwtLogoutAuthenticationFilter(final AuthenticationManager authenticationManager,
                                         final UserService userService,
                                         final JwtManager jwtManager) {
        super(authenticationManager, userService, jwtManager);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {
        Long userId = Long.valueOf(authResult.getName());

        getUserService().delete(userId);

        response.setStatus(HttpStatus.NO_CONTENT.value());
    }

    public void setFilterProcessesUrl(String filterProcessesUrl, String method) {
        this.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher(filterProcessesUrl, method));
    }
}
