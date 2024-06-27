package com.onlyoffice.docspacepipedrive.web.aop;

import com.onlyoffice.docspacepipedrive.client.docspace.DocspaceClient;
import com.onlyoffice.docspacepipedrive.client.docspace.response.DocspaceGroup;
import com.onlyoffice.docspacepipedrive.entity.User;
import com.onlyoffice.docspacepipedrive.security.SecurityUtils;
import com.onlyoffice.docspacepipedrive.service.SettingsService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;

@Aspect
@Component
@RequiredArgsConstructor
public class InitSharedGroupAspect {
    private final SettingsService settingsService;
    private final DocspaceClient docspaceClient;
    private boolean afterThrowing = false;

    @Before("@annotation(initSharedGroup)")
    public void before(JoinPoint joinPoint, InitSharedGroup initSharedGroup) {
        if (initSharedGroup.execution().equals(Execution.BEFORE)) {
            initSharedGroup();
        }
    }

    @After("@annotation(initSharedGroup)")
    public void after(JoinPoint joinPoint, InitSharedGroup initSharedGroup) {
        if (initSharedGroup.execution().equals(Execution.AFTER) && !afterThrowing) {
            initSharedGroup();
        }
    }

    @AfterThrowing(pointcut = "@annotation(InitSharedGroup)", throwing = "exception")
    public void afterThrowing(JoinPoint joinPoint, Exception exception) {
        this.afterThrowing = true;
    }


    private void initSharedGroup() {
        User currentUser = SecurityUtils.getCurrentUser();

        if (currentUser.getClient().getSettings().getSharedGroupId() == null) {
            DocspaceGroup docspaceGroup = docspaceClient.createGroup(
                    MessageFormat.format("Pipedrive Users ({0})", currentUser.getClient().getUrl()),
                    currentUser.getDocspaceAccount().getUuid(),
                    null
            );

            settingsService.saveSharedGroup(currentUser.getClient().getId(), docspaceGroup.getId());
        } else {
            docspaceClient.updateGroup(
                    currentUser.getClient().getSettings().getSharedGroupId(),
                    null,
                    currentUser.getDocspaceAccount().getUuid(),
                    null,
                    null
            );
        }
    }
}
