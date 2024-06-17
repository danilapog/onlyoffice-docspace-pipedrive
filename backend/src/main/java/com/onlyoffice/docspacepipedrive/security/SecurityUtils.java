package com.onlyoffice.docspacepipedrive.security;

import com.onlyoffice.docspacepipedrive.entity.User;
import com.onlyoffice.docspacepipedrive.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;


@Component
public final class SecurityUtils {
    private static UserService userService;

    @Autowired
    public void setUserService(UserService userService) {
        SecurityUtils.userService = userService;
    }

    public static User getCurrentUser() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();

        if (authentication != null) {
            if (authentication.getPrincipal() instanceof OAuth2User) { //ToDo user in principal
                OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
                Long userId = oAuth2User.getAttribute("id");
                Long companyId = oAuth2User.getAttribute("company_id");

                return userService.findByUserIdAndClientId(userId, companyId);
            }
        }

        return null;
    }
}
