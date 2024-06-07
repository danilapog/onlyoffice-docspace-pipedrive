package com.onlyoffice.docspacepipedrive.repository;

import com.onlyoffice.docspacepipedrive.entity.Settings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface SettingsRepository extends JpaRepository<Settings, Long> {
    Optional<Settings> findByClientId(Long id);
    boolean existsByClientId(Long id);
}
