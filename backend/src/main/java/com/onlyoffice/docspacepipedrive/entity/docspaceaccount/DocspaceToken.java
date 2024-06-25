package com.onlyoffice.docspacepipedrive.entity.docspaceaccount;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;


@AllArgsConstructor
@Builder
@Data
@Embeddable
@NoArgsConstructor
public class DocspaceToken {
    private String value;
    @CreationTimestamp
    private Instant issuedAt;
}
