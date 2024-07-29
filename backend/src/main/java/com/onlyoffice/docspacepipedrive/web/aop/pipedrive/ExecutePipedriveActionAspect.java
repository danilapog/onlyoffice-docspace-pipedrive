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
