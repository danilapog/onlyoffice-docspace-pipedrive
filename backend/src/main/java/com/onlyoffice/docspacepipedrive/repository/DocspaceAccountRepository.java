package com.onlyoffice.docspacepipedrive.repository;

import com.onlyoffice.docspacepipedrive.entity.DocspaceAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;


@Repository
public interface DocspaceAccountRepository extends JpaRepository<DocspaceAccount, UUID> {
    Optional<DocspaceAccount> findByUserId(Long userId);
}
