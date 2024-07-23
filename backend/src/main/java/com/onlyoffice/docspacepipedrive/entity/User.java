package com.onlyoffice.docspacepipedrive.entity;

import com.onlyoffice.docspacepipedrive.entity.user.AccessToken;
import com.onlyoffice.docspacepipedrive.entity.user.RefreshToken;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;


@AllArgsConstructor
@Builder
@Data
@Entity
@NoArgsConstructor
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"userId", "client_id" })
        })
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    @Embedded
    @AttributeOverrides({
            @AttributeOverride( name = "value", column = @Column(name = "access_token_value", length = 1024)),
            @AttributeOverride( name = "issuedAt", column = @Column(name = "access_token_issued_at")),
            @AttributeOverride( name = "expiresAt", column = @Column(name = "access_token_expires_at"))
    })
    private AccessToken accessToken;
    @Embedded
    @AttributeOverrides({
            @AttributeOverride( name = "value", column = @Column(name = "refresh_token_value")),
            @AttributeOverride( name = "issuedAt", column = @Column(name = "refresh_token_issued_at")),
    })
    private RefreshToken refreshToken;
    @OneToOne(mappedBy = "user", cascade = CascadeType.REMOVE)
    @ToString.Exclude
    private DocspaceAccount docspaceAccount;
    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    @ToString.Exclude
    private List<Webhook> webhook;
    @ManyToOne
    @JoinColumn(name = "client_id")
    @ToString.Exclude
    private Client client;

    public boolean isSystemUser() {
        User systemUser = this.client.getSystemUser();

        return systemUser != null && systemUser.getId().equals(this.id);
    }
}
