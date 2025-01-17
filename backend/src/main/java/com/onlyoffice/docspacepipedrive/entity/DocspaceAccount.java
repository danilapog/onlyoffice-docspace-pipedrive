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

package com.onlyoffice.docspacepipedrive.entity;

import com.onlyoffice.docspacepipedrive.encryption.EncryptionAttributeConverter;
import com.onlyoffice.docspacepipedrive.entity.docspaceaccount.DocspaceToken;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import java.util.UUID;


@AllArgsConstructor
@Builder
@Data
@Entity
@NoArgsConstructor
@Table(name = "docspace_accounts")
public class DocspaceAccount {
    @Id
    @GeneratedValue(generator = "generator")
    @GenericGenerator(
            name = "generator",
            strategy = "foreign",
            parameters = @Parameter(name = "property", value = "user")
    )
    private Long userId;
    @OneToOne
    @PrimaryKeyJoinColumn(name = "user_id")
    private User user;
    private UUID uuid;
    @Convert(converter = EncryptionAttributeConverter.class)
    private String email;
    @Convert(converter = EncryptionAttributeConverter.class)
    private String passwordHash;
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "token_value", length = 2048)),
            @AttributeOverride(name = "issuedAt", column = @Column(name = "token_issued_at"))
    })
    private DocspaceToken docspaceToken;
}
