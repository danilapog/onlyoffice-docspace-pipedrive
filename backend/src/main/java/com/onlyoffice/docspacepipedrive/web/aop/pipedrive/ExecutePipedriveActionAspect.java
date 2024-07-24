package com.onlyoffice.docspacepipedrive.web.aop.pipedrive;

import com.onlyoffice.docspacepipedrive.entity.User;
import com.onlyoffice.docspacepipedrive.manager.PipedriveActionManager;
import com.onlyoffice.docspacepipedrive.security.util.SecurityUtils;
import com.onlyoffice.docspacepipedrive.web.aop.Execution;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;


@Aspect
@Component
@RequiredArgsConstructor
public class ExecutePipedriveActionAspect {
    private final PipedriveActionManager pipedriveActionManager;

    @Around("@annotation(executePipedriveAction)")
    public Object run(ProceedingJoinPoint joinPoint, ExecutePipedriveAction executePipedriveAction) throws Throwable {
        if (executePipedriveAction.execution().equals(Execution.BEFORE)) {
            execute(executePipedriveAction.action(), joinPoint);
        }

        Object result = joinPoint.proceed();

        if (executePipedriveAction.execution().equals(Execution.AFTER)) {
            execute(executePipedriveAction.action(), joinPoint);
        }

        return result;
    }

    private void execute(PipedriveAction pipedriveAction, JoinPoint joinPoint) {
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
