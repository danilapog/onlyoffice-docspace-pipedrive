package com.onlyoffice.docspacepipedrive.service.impl;

import com.onlyoffice.docspacepipedrive.entity.Client;
import com.onlyoffice.docspacepipedrive.entity.Room;
import com.onlyoffice.docspacepipedrive.exceptions.RoomNotFoundException;
import com.onlyoffice.docspacepipedrive.repository.RoomRepository;
import com.onlyoffice.docspacepipedrive.service.ClientService;
import com.onlyoffice.docspacepipedrive.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {
    private final ClientService clientService;
    private final RoomRepository roomRepository;

    @Override
    public Room findByDealId(Long dealId) {
        return roomRepository.findByDealId(dealId)
                .orElseThrow(() -> new RoomNotFoundException(dealId));
    }

    @Override
    public Room create(Long clientId, Room room) {
        Client client = clientService.findById(clientId);

        room.setClient(client);

        return roomRepository.save(room);
    }
}
