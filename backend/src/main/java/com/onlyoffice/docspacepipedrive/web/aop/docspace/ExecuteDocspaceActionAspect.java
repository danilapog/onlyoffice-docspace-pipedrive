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

package com.onlyoffice.docspacepipedrive.web.aop.docspace;

import com.onlyoffice.docspacepipedrive.client.pipedrive.PipedriveClient;
import com.onlyoffice.docspacepipedrive.client.pipedrive.dto.PipedriveDealFollower;
import com.onlyoffice.docspacepipedrive.entity.DocspaceAccount;
import com.onlyoffice.docspacepipedrive.entity.Room;
import com.onlyoffice.docspacepipedrive.entity.User;
import com.onlyoffice.docspacepipedrive.exceptions.UserNotFoundException;
import com.onlyoffice.docspacepipedrive.manager.DocspaceActionManager;
import com.onlyoffice.docspacepipedrive.security.util.SecurityUtils;
import com.onlyoffice.docspacepipedrive.service.RoomService;
import com.onlyoffice.docspacepipedrive.service.UserService;
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
import java.util.ArrayList;
import java.util.List;


@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class ExecuteDocspaceActionAspect {
    private final DocspaceActionManager docspaceActionManager;
    private final RoomService roomService;
    private final PipedriveClient pipedriveClient;
    private final UserService userService;

    @Around("@annotation(executeDocspaceAction)")
    public Object run(final ProceedingJoinPoint joinPoint,
                      final ExecuteDocspaceAction executeDocspaceAction) throws Throwable {
        if (executeDocspaceAction.execution().equals(Execution.BEFORE)) {
            execute(executeDocspaceAction.action(), executeDocspaceAction.mode(), joinPoint);
        }

        Object result = joinPoint.proceed();

        if (executeDocspaceAction.execution().equals(Execution.AFTER)) {
            execute(executeDocspaceAction.action(), executeDocspaceAction.mode(), joinPoint);
        }

        return result;
    }

    private void execute(final DocspaceAction docspaceAction, final Mode mode, final JoinPoint joinPoint) {
        User currentUser = SecurityUtils.getCurrentUser();

        try {
            switch (docspaceAction) {
                case INIT_SHARED_GROUP:
                    docspaceActionManager.initSharedGroup();
                    break;
                case INVITE_CURRENT_USER_TO_SHARED_GROUP:
                    docspaceActionManager.inviteCurrentUserToSharedGroup();
                    break;
                case REMOVE_CURRENT_USER_FROM_SHARED_GROUP:
                    docspaceActionManager.removeCurrentUserFromSharedGroup();
                    break;
                case INVITE_DEAL_FOLLOWERS_TO_ROOM:
                    Long dealId = (Long) joinPoint.getArgs()[0];

                    Room room = roomService.findByDealId(dealId);
                    List<PipedriveDealFollower> dealFollowers = pipedriveClient.getDealFollowers(dealId);

                    List<User> users = new ArrayList<>();
                    for (PipedriveDealFollower dealFollower : dealFollowers) {
                        try {
                            users.add(
                                    userService.findByUserIdAndClientId(
                                            dealFollower.getUserId(),
                                            currentUser.getClient().getId()
                                    )
                            );
                        } catch (UserNotFoundException e) {
                        }
                    }

                    List<DocspaceAccount> docspaceAccounts = users.stream()
                            .filter(user -> user.getDocspaceAccount() != null)
                            .map(user -> user.getDocspaceAccount())
                            .toList();

                    docspaceActionManager.inviteListDocspaceAccountsToRoom(room.getRoomId(), docspaceAccounts);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            if (mode.equals(Mode.ATTEMPT)) {
                log.warn(
                        MessageFormat.format(
                                "An attempt execute action {0} failed with the error: {1}",
                                docspaceAction.name(),
                                e.getMessage()
                        )
                );
            } else {
                throw e;
            }
        }
    }
}
