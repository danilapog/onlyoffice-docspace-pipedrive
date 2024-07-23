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

import java.util.UUID;


@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "webhooks")
public class Webhook {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private Long webhookId;
    private String name;
    private String password;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
