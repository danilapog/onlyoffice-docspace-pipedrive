/**
 *
 * (c) Copyright Ascensio System SIA 2025
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

package com.onlyoffice.docspacepipedrive.security.util;

import com.onlyoffice.docspacepipedrive.entity.Client;
import com.onlyoffice.docspacepipedrive.entity.User;
import com.onlyoffice.docspacepipedrive.security.oauth.OAuth2PipedriveUser;
import com.onlyoffice.docspacepipedrive.security.token.UserAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;


@Component
public final class SecurityUtils {
    private SecurityUtils() {
    }

    public static Client getCurrentClient() {
        User currentUser = getCurrentUser();

        if (currentUser != null) {
            return currentUser.getClient();
        }

        return null;
    }

    public static User getCurrentUser() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();

        if (authentication != null) {
            if (authentication.getPrincipal() instanceof User) {
                return (User) authentication.getPrincipal();
            }
            if (authentication.getPrincipal() instanceof OAuth2PipedriveUser) {
                return ((OAuth2PipedriveUser) authentication.getPrincipal()).getUser();
            }
        }

        return null;
    }

    public static <R> R runAs(final RunAsWork<R> runAsWork, final User user) {
        Assert.notNull(user, "User must not be null!");
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication currentAuthentication = securityContext.getAuthentication();

        final R result;
        try {
            securityContext.setAuthentication(new UserAuthenticationToken(user));

            result = runAsWork.doWork();
            return result;
        } catch (Throwable exception) {
            if (exception instanceof RuntimeException) {
                throw (RuntimeException) exception;
            } else {
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
