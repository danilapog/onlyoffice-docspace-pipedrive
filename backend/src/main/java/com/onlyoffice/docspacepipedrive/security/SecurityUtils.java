package com.onlyoffice.docspacepipedrive.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;


public final class SecurityUtils {
     public static Long getCurrentUserId() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();

        Long userId = null;

        if (authentication != null) {
            if (authentication.getPrincipal() instanceof DefaultOAuth2User) {
                DefaultOAuth2User defaultOAuth2User = (DefaultOAuth2User) authentication.getPrincipal();
                userId = Long.valueOf(defaultOAuth2User.getName());
            }
        }

        return userId;
    }
}
