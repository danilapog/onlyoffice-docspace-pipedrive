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
public class JoinToSharedGroupAspect {
    private final DocspaceClient docspaceClient;
    private boolean afterThrowing = false;
    @Before("@annotation(joinToSharedGroup)")
    public void before(JoinPoint joinPoint, JoinToSharedGroup joinToSharedGroup) {
        if (joinToSharedGroup.execution().equals(Execution.BEFORE)) {
            joinToSharedGroup();
        }
    }

    @After("@annotation(joinToSharedGroup)")
    public void after(JoinPoint joinPoint, JoinToSharedGroup joinToSharedGroup) {
        if (joinToSharedGroup.execution().equals(Execution.AFTER) && !afterThrowing) {
            joinToSharedGroup();
        }
    }

    @AfterThrowing(pointcut = "@annotation(JoinToSharedGroup)", throwing = "exception")
    public void afterThrowing(JoinPoint joinPoint, Exception exception) {
        this.afterThrowing = true;
    }

    private void joinToSharedGroup() {
        User currentUser = SecurityUtils.getCurrentUser();

        if (currentUser.getClient().getSettings().getSharedGroupId() != null) {
            docspaceClient.updateGroup(
                    currentUser.getClient().getSettings().getSharedGroupId(),
                    null,
                    null,
                    Collections.singletonList(currentUser.getDocspaceAccount().getUuid()),
                    null
            );
        }
    }
}
