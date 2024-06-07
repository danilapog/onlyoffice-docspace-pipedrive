package com.onlyoffice.docspacepipedrive.entity.user;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;


@AllArgsConstructor
@Builder
@Data
@Embeddable
@NoArgsConstructor
public class AccessToken {
    private String value;
    private Instant issuedAt;
    private Instant expiresAt;
}
