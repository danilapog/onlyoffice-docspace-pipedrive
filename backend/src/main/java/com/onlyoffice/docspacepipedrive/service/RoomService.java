package com.onlyoffice.docspacepipedrive.service;

import com.onlyoffice.docspacepipedrive.entity.Room;

public interface RoomService {
    Room findByDealId(Long dealId);
    Room create(Long clientId, Room room);
}
