package com.onlyoffice.docspacepipedrive.web.controller;

import com.onlyoffice.docspacepipedrive.entity.User;
import com.onlyoffice.docspacepipedrive.security.SecurityUtils;
import com.onlyoffice.docspacepipedrive.web.dto.status.AppStatusResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/app-status")
@RequiredArgsConstructor
public class AppStatusController {

    @GetMapping
    public ResponseEntity<AppStatusResponse> getAppStatus() {
        User currentUser = SecurityUtils.getCurrentUser();

        Boolean isActive = false;

        if (currentUser.getClient().getSystemUser() != null) {
            isActive = true;
        }

        return ResponseEntity.ok(new AppStatusResponse(isActive));
    }
}
