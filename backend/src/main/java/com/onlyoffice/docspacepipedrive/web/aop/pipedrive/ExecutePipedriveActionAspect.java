package com.onlyoffice.docspacepipedrive.web.aop.pipedrive;

import com.onlyoffice.docspacepipedrive.entity.User;
import com.onlyoffice.docspacepipedrive.manager.PipedriveActionManager;
import com.onlyoffice.docspacepipedrive.security.util.SecurityUtils;
import com.onlyoffice.docspacepipedrive.web.aop.Execution;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;


@Aspect
@Component
@RequiredArgsConstructor
public class ExecutePipedriveActionAspect {
    private final PipedriveActionManager pipedriveActionManager;

    private boolean afterThrowing = false;


    @Before("@annotation(executePipedriveAction)")
    public void before(JoinPoint joinPoint, ExecutePipedriveAction executePipedriveAction) {
        if (executePipedriveAction.execution().equals(Execution.BEFORE)) {
            execute(executePipedriveAction.action(), joinPoint);
        }
    }

    @After("@annotation(executePipedriveAction)")
    public void after(JoinPoint joinPoint, ExecutePipedriveAction executePipedriveAction) {
        if (executePipedriveAction.execution().equals(Execution.AFTER) && !afterThrowing) {
            execute(executePipedriveAction.action(), joinPoint);
        }
    }

    @AfterThrowing(pointcut = "@annotation(ExecutePipedriveAction)", throwing = "exception")
    public void afterThrowing(JoinPoint joinPoint, Exception exception) {
        this.afterThrowing = true;
    }

    private void execute(PipedriveAction pipedriveAction, JoinPoint joinPoint) {
        User currentUser = SecurityUtils.getCurrentUser();

        switch (pipedriveAction){
            case INIT_WEBHOOKS:
                pipedriveActionManager.initWebhooks();
                break;
            case REMOVE_WEBHOOKS:
                pipedriveActionManager.removeWebhooks();
                break;
        }
    }
}
