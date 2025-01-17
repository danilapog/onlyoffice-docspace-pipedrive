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

package com.onlyoffice.docspacepipedrive.security.token;

import com.onlyoffice.docspacepipedrive.entity.User;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.Collection;


public class UserAuthenticationToken extends AbstractAuthenticationToken {
    private final User principal;

    public UserAuthenticationToken(final User principal) {
        super((Collection) null);
        this.principal = principal;
        this.setAuthenticated(true);
    }

    @Override
    public String getName() {
        return this.principal.getId().toString();
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return this.principal;
    }
}
