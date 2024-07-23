package com.onlyoffice.docspacepipedrive.security.util;

import com.onlyoffice.docspacepipedrive.entity.User;
import com.onlyoffice.docspacepipedrive.security.token.UserAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;


@Component
public final class SecurityUtils {
    public static User getCurrentUser() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();

        if (authentication != null) {
            if (authentication.getPrincipal() instanceof User) {
                return (User) authentication.getPrincipal();
            }
        }

        return null;
    }

    public static <R> R runAs(RunAsWork<R> runAsWork, User user) {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication currentAuthentication = securityContext.getAuthentication();

        final R result;
        try {
            securityContext.setAuthentication(new UserAuthenticationToken(user));

            result = runAsWork.doWork();
            return result;
        } catch (Throwable exception) {
            if (exception instanceof RuntimeException)
            {
                throw (RuntimeException) exception;
            }
            else
            {
                throw new RuntimeException("Error during run as.", exception);
            }
        } finally {
            securityContext.setAuthentication(currentAuthentication);
        }
    }

    public interface RunAsWork<Result> {
        Result doWork() throws Exception;
    }

}
