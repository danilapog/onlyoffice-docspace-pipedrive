package com.onlyoffice.docspacepipedrive.web.aop.docspace;

import com.onlyoffice.docspacepipedrive.client.pipedrive.PipedriveClient;
import com.onlyoffice.docspacepipedrive.client.pipedrive.response.PipedriveDealFollower;
import com.onlyoffice.docspacepipedrive.entity.DocspaceAccount;
import com.onlyoffice.docspacepipedrive.entity.Room;
import com.onlyoffice.docspacepipedrive.entity.User;
import com.onlyoffice.docspacepipedrive.exceptions.UserNotFoundException;
import com.onlyoffice.docspacepipedrive.manager.DocspaceActionManager;
import com.onlyoffice.docspacepipedrive.security.util.SecurityUtils;
import com.onlyoffice.docspacepipedrive.service.RoomService;
import com.onlyoffice.docspacepipedrive.service.UserService;
import com.onlyoffice.docspacepipedrive.web.aop.Execution;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Aspect
@Component
@RequiredArgsConstructor
public class ExecuteDocspaceActionAspect {
    private final DocspaceActionManager docspaceActionManager;
    private final RoomService roomService;
    private final PipedriveClient pipedriveClient;
    private final UserService userService;

    private boolean afterThrowing = false;


    @Before("@annotation(executeDocspaceAction)")
    public void before(JoinPoint joinPoint, ExecuteDocspaceAction executeDocspaceAction) {
        if (executeDocspaceAction.execution().equals(Execution.BEFORE)) {
            execute(executeDocspaceAction.action(), joinPoint);
        }
    }

    @After("@annotation(executeDocspaceAction)")
    public void after(JoinPoint joinPoint, ExecuteDocspaceAction executeDocspaceAction) {
        if (executeDocspaceAction.execution().equals(Execution.AFTER) && !afterThrowing) {
            execute(executeDocspaceAction.action(), joinPoint);
        }
    }

    @AfterThrowing(pointcut = "@annotation(ExecuteDocspaceAction)", throwing = "exception")
    public void afterThrowing(JoinPoint joinPoint, Exception exception) {
        this.afterThrowing = true;
    }

    private void execute(DocspaceAction docspaceAction, JoinPoint joinPoint) {
        User currentUser = SecurityUtils.getCurrentUser();

        switch (docspaceAction){
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
                        users.add(userService.findByUserIdAndClientId(dealFollower.getUserId(), currentUser.getClient().getId()));
                    } catch (UserNotFoundException e) {}
                }

                List<DocspaceAccount> docspaceAccounts = users.stream()
                        .filter(user -> user.getDocspaceAccount() != null)
                        .map(user -> user.getDocspaceAccount())
                        .toList();

                docspaceActionManager.inviteListDocspaceAccountsToRoom(room.getRoomId(), docspaceAccounts);
                break;
        }
    }
}
