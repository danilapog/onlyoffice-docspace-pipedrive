package com.onlyoffice.docspacepipedrive.repository;

import com.onlyoffice.docspacepipedrive.entity.DocspaceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface DocspaceTokenRepository extends JpaRepository<DocspaceToken, Long> {
    Optional<DocspaceToken> findByClientId(Long clientId);
    void deleteByClientId(Long clientId);
}
