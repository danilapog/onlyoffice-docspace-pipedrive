/**
 *
 * (c) Copyright Ascensio System SIA 2024
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

import com.onlyoffice.docspacepipedrive.exceptions.DocspaceAccessDeniedException;
import com.onlyoffice.docspacepipedrive.exceptions.DocspaceWebClientResponseException;
import com.onlyoffice.docspacepipedrive.exceptions.PipedriveAccessDeniedException;
import com.onlyoffice.docspacepipedrive.exceptions.PipedriveOAuth2AuthorizationException;
import com.onlyoffice.docspacepipedrive.exceptions.PipedriveWebClientResponseException;
import com.onlyoffice.docspacepipedrive.exceptions.RoomNotFoundException;
import com.onlyoffice.docspacepipedrive.web.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class ExceptionHandlerController {
    @ExceptionHandler(RoomNotFoundException.class)
    public ResponseEntity<ErrorResponse> entityNotFound(RoomNotFoundException e) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(
                        new ErrorResponse(
                                HttpStatus.NOT_FOUND.value(),
                                e.getLocalizedMessage(),
                                ErrorResponse.Provider.INTEGRATION_APP
                        )
                );
    }

    @ExceptionHandler(PipedriveWebClientResponseException.class)
    public ResponseEntity<ErrorResponse> pipedriveWebClientResponseException(PipedriveWebClientResponseException e) {
        return ResponseEntity.status(e.getStatusCode())
                .body(
                        new ErrorResponse(
                                e.getStatusCode().value(),
                                e.getLocalizedMessage(),
                                ErrorResponse.Provider.PIPEDRIVE
                        )
                );
    }

    @ExceptionHandler(PipedriveAccessDeniedException.class)
    public ResponseEntity<ErrorResponse> pipedriveAccessDeniedException(PipedriveAccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(
                        new ErrorResponse(
                                HttpStatus.FORBIDDEN.value(),
                                e.getLocalizedMessage(),
                                ErrorResponse.Provider.PIPEDRIVE
                        )
                );
    }

    @ExceptionHandler(PipedriveOAuth2AuthorizationException.class)
    public ResponseEntity<ErrorResponse> pipedriveOAuth2AuthorizationException(PipedriveOAuth2AuthorizationException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(
                        new ErrorResponse(
                                HttpStatus.UNAUTHORIZED.value(),
                                e.getLocalizedMessage(),
                                ErrorResponse.Provider.PIPEDRIVE
                        )
                );
    }

    @ExceptionHandler(DocspaceWebClientResponseException.class)
    public ResponseEntity<ErrorResponse> docspaceWebClientResponseException(DocspaceWebClientResponseException e) {
        return ResponseEntity.status(e.getStatusCode())
                .body(
                        new ErrorResponse(
                                e.getStatusCode().value(),
                                e.getLocalizedMessage(),
                                ErrorResponse.Provider.DOCSPACE
                        )
                );
    }

    @ExceptionHandler(DocspaceAccessDeniedException.class)
    public ResponseEntity<ErrorResponse> docspaceAccessDeniedException(DocspaceAccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(
                        new ErrorResponse(
                                HttpStatus.FORBIDDEN.value(),
                                e.getLocalizedMessage(),
                                ErrorResponse.Provider.DOCSPACE
                        )
                );
    }
}
