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

package com.onlyoffice.docspacepipedrive.encryption;

import jakarta.persistence.AttributeConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class EncryptionAttributeConverter implements AttributeConverter<String, String> {
    private final TextEncryptor textEncryptor;

    @Override
    public String convertToDatabaseColumn(final String s) {
        if (StringUtils.hasText(s)) {
            return textEncryptor.encrypt(s);
        }

        return s;
    }

    @Override
    public String convertToEntityAttribute(final String s) {
        if (StringUtils.hasText(s)) {
            return textEncryptor.decrypt(s);
        }

        return s;
    }
}
