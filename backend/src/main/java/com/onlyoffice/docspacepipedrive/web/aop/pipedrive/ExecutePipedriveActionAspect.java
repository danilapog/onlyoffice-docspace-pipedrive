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

import com.onlyoffice.docspacepipedrive.manager.PipedriveActionManager;
import com.onlyoffice.docspacepipedrive.web.aop.Execution;
import com.onlyoffice.docspacepipedrive.web.aop.Mode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;


@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class ExecutePipedriveActionAspect {
    private final PipedriveActionManager pipedriveActionManager;

    @Around("@annotation(executePipedriveAction)")
    public Object run(final ProceedingJoinPoint joinPoint,
                      final ExecutePipedriveAction executePipedriveAction) throws Throwable {
        if (executePipedriveAction.execution().equals(Execution.BEFORE)) {
            execute(executePipedriveAction.action(), executePipedriveAction.mode(), joinPoint);
        }

        Object result = joinPoint.proceed();

        if (executePipedriveAction.execution().equals(Execution.AFTER)) {
            execute(executePipedriveAction.action(), executePipedriveAction.mode(), joinPoint);
        }

        return result;
    }

    private void execute(final PipedriveAction pipedriveAction, final Mode mode, final JoinPoint joinPoint) {
        try {
            switch (pipedriveAction) {
                case INIT_WEBHOOKS:
                    pipedriveActionManager.initWebhooks();
                    break;
                case REMOVE_WEBHOOKS:
                    pipedriveActionManager.removeWebhooks();
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            if (mode.equals(Mode.ATTEMPT)) {
                log.warn(
                        MessageFormat.format(
                                "An attempt execute action {0} failed with the error: {1}",
                                pipedriveAction.name(),
                                e.getMessage()
                        )
                );
            } else {
                throw e;
            }
        }
    }
}
