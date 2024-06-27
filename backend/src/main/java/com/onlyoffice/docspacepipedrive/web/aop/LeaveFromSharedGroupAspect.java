package com.onlyoffice.docspacepipedrive.web.aop;

import com.onlyoffice.docspacepipedrive.client.docspace.DocspaceClient;
import com.onlyoffice.docspacepipedrive.entity.User;
import com.onlyoffice.docspacepipedrive.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Aspect
@Component
@RequiredArgsConstructor
public class LeaveFromSharedGroupAspect {
    private final DocspaceClient docspaceClient;
    private boolean afterThrowing = false;

    @Before("@annotation(leaveFromSharedGroup)")
    public void before(JoinPoint joinPoint, LeaveFromSharedGroup leaveFromSharedGroup) {
        if (leaveFromSharedGroup.execution().equals(Execution.BEFORE)) {
            leaveFromSharedGroup();
        }
    }

    @After("@annotation(leaveFromSharedGroup)")
    public void after(JoinPoint joinPoint, LeaveFromSharedGroup leaveFromSharedGroup) {
        if (leaveFromSharedGroup.execution().equals(Execution.AFTER) && !afterThrowing) {
            leaveFromSharedGroup();
        }
    }

    @AfterThrowing(pointcut = "@annotation(LeaveFromSharedGroup)", throwing = "exception")
    public void afterThrowing(JoinPoint joinPoint, Exception exception) {
        this.afterThrowing = true;
    }

    private void leaveFromSharedGroup() {
        User currentUser = SecurityUtils.getCurrentUser();

        if (currentUser.getClient().getSettings().getSharedGroupId() != null) {
            docspaceClient.updateGroup(
                    currentUser.getClient().getSettings().getSharedGroupId(),
                    null,
                    null,
                    null,
                    Collections.singletonList(currentUser.getDocspaceAccount().getUuid())
            );
        }
    }
}
