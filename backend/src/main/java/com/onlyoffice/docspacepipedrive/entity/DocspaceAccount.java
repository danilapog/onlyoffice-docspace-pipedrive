package com.onlyoffice.docspacepipedrive.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.UUID;


@AllArgsConstructor
@Builder
@Data
@Entity
@NoArgsConstructor
@Table(name = "docspace_accounts")
public class DocspaceAccount {
    @Id
    private UUID id;
    private String passwordHash;
    @OneToOne
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    private User user;
}
