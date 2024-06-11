package com.onlyoffice.docspacepipedrive.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


@AllArgsConstructor
@Builder
@Data
@Entity
@NoArgsConstructor
@Table(name = "clients")
public class Client {
    @Id
    private Long id;
    private String url;
    @CreationTimestamp
    private Instant installationDate;
    @OneToOne(mappedBy = "client", cascade = CascadeType.REMOVE)
    @ToString.Exclude
    private Settings settings;
    @OneToOne(mappedBy = "client", cascade = CascadeType.REMOVE)
    @ToString.Exclude
    private DocspaceToken docspaceToken;
    @OneToMany(mappedBy = "client", cascade = CascadeType.REMOVE)
    @ToString.Exclude
    @Builder.Default
    private List<User> users = new ArrayList<>();
    @OneToMany(mappedBy = "client", cascade = CascadeType.REMOVE)
    @ToString.Exclude
    @Builder.Default
    private List<Room> rooms = new ArrayList<>();
}
