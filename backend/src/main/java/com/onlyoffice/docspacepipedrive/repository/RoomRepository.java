package com.onlyoffice.docspacepipedrive.repository;

import com.onlyoffice.docspacepipedrive.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    Optional<Room> findByDealId(Long dealId);
}
