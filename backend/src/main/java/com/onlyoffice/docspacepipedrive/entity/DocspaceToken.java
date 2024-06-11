package com.onlyoffice.docspacepipedrive.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;


@AllArgsConstructor
@Builder
@Data
@Entity
@NoArgsConstructor
@Table(name = "docspace_tokens")
public class DocspaceToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String value;
    @UpdateTimestamp
    private Instant issuedAt;
    @ManyToOne
    @JoinColumn(name = "client_id")
    @ToString.Exclude
    private Client client;
}
