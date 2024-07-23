package com.onlyoffice.docspacepipedrive.repository;

import com.onlyoffice.docspacepipedrive.entity.Webhook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;


@Repository
public interface WebhookRepository extends JpaRepository<Webhook, UUID> {
    List<Webhook> findAllByUserId(Long userId);
}
