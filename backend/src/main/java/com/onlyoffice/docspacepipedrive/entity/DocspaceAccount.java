package com.onlyoffice.docspacepipedrive.entity;

import com.onlyoffice.docspacepipedrive.entity.docspaceaccount.DocspaceToken;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
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
    @GenericGenerator(name = "generator", strategy = "foreign", parameters = @Parameter(name = "property", value = "user"))
    private Long userId;
    @OneToOne
    @PrimaryKeyJoinColumn(name = "user_id")
    private User user;
    private UUID uuid;
    private String email;
    private String passwordHash;
    @Embedded
    @AttributeOverrides({
            @AttributeOverride( name = "value", column = @Column(name = "token_value")),
            @AttributeOverride( name = "issuedAt", column = @Column(name = "token_issued_at")),
    })
    private DocspaceToken docspaceToken;
}
