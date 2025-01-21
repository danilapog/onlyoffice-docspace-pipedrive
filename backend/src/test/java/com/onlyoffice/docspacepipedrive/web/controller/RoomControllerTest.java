/**
 *
 * (c) Copyright Ascensio System SIA 2025
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.onlyoffice.docspacepipedrive.web.controller;

import com.onlyoffice.docspacepipedrive.AbstractControllerTest;
import com.onlyoffice.docspacepipedrive.client.docspace.dto.DocspaceRoomType;
import com.onlyoffice.docspacepipedrive.entity.Room;
import com.onlyoffice.docspacepipedrive.exceptions.DocspaceUrlNotFoundException;
import com.onlyoffice.docspacepipedrive.exceptions.RoomNotFoundException;
import com.onlyoffice.docspacepipedrive.web.dto.room.RoomRequest;
import com.onlyoffice.docspacepipedrive.web.dto.room.RoomResponse;
import net.javacrumbs.jsonunit.JsonAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.text.MessageFormat;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class RoomControllerTest extends AbstractControllerTest {

    private static Room testRoom;

    @BeforeEach
    public void setupTestRoom() {
        testRoom = roomService.create(
                testClient.getId(),
                Room.builder()
                        .dealId(1L)
                        .roomId(10000L)
                        .build()
        );
    }

    @Test
    public void whenGetRoom_thenReturnOk() throws Exception {
        String actualResponse =  mockMvc.perform(get("/api/v1/room/" + testRoom.getDealId().toString())
                        .header("Authorization",
                                getAuthorizationHeaderForUser(testUserSalesAdmin)
                        )
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String expectedResponse = objectMapper.writeValueAsString(
                new RoomResponse(String.valueOf(testRoom.getRoomId()))
        );

        JsonAssert.assertJsonEquals(expectedResponse, actualResponse);
    }

    @Test
    public void whenGetNotSavedRoom_thenReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/room/2")
                        .header("Authorization",
                                getAuthorizationHeaderForUser(testUserSalesAdmin)
                        )
                )
                .andExpect(status().isNotFound());
    }

    @Test
    public void whenGetRoomForNotAvailableDeal_thenReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/room/3")
                        .header("Authorization",
                                getAuthorizationHeaderForUser(testUserSalesAdmin)
                        )
                )
                .andExpect(status().isForbidden());
    }

    @Test
    public void whenPostRoom_thenReturnOk() throws Exception {
        assertThrows(
                RoomNotFoundException.class,
                () -> roomService.findByClientIdAndDealId(testUserSalesAdmin.getClient().getId(), 4L)
        );

        String actualResponse = mockMvc.perform(post("/api/v1/room/4")
                        .header("Authorization",
                                getAuthorizationHeaderForUser(testUserSalesAdmin)
                        )
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(new RoomRequest(DocspaceRoomType.EDITING_ROOM)))
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String expectedResponse = objectMapper.writeValueAsString(
                new RoomResponse("10001")
        );

        JsonAssert.assertJsonEquals(expectedResponse, actualResponse);

        Room room = roomService.findByClientIdAndDealId(testUserSalesAdmin.getClient().getId(), 4L);

        assertEquals(room.getRoomId(), 10001);
    }

    @Test
    public void whenPostRoomWithEmptySettings_thenReturnForbidden() throws Exception {
        settingsService.clear(testUserSalesAdmin.getClient().getId());

        String response = mockMvc.perform(post("/api/v1/room/4")
                        .header("Authorization",
                                getAuthorizationHeaderForUser(testUserSalesAdmin)
                        )
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(new RoomRequest(DocspaceRoomType.EDITING_ROOM)))
                )
                .andExpect(status().isForbidden())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        assertEquals(
                responseMap.get("message"),
                new DocspaceUrlNotFoundException(testUserSalesAdmin.getClient().getId()).getMessage()
        );
    }

    @Test
    public void whenPostRoomWithoutDocspaceAccount_thenReturnForbidden() throws Exception {
        docspaceAccountService.deleteById(testUserSalesAdmin.getId());

        String response = mockMvc.perform(post("/api/v1/room/4")
                        .header("Authorization",
                                getAuthorizationHeaderForUser(testUserSalesAdmin)
                        )
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(new RoomRequest(DocspaceRoomType.EDITING_ROOM)))
                )
                .andExpect(status().isForbidden())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        assertEquals(
                responseMap.get("message"),
                MessageFormat.format(
                        "DocspaceAccount for User with USER_ID({0}) and CLIENT_ID({1}) not found.",
                        testUserSalesAdmin.getUserId(),
                        testUserSalesAdmin.getClient().getId()
                )
        );
    }
}
