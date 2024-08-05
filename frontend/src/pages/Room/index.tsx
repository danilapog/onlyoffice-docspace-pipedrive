/**
 *
 * (c) Copyright Ascensio System SIA 2023
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

import React, { useContext, useEffect, useState } from "react";
import i18next from "i18next";
import { useTranslation } from "react-i18next";
import { Command, View } from "@pipedrive/app-extensions-sdk";
import { DocSpace, TFrameConfig, TFrameEvents } from "@onlyoffice/docspace-react";

import { AppContext } from "@context/AppContext";

import { createRoom, getRoom } from "@services/room";
import { getCurrentURL } from "@utils/url";

import { ButtonType, OnlyofficeButton } from "@components/button";
import { OnlyofficeSpinner } from "@components/spinner";
import { OnlyofficeBackgroundError } from "@layouts/ErrorBackground";

import CommonError from "@assets/common-error.svg";
import DenniedError from "@assets/dennied-error.svg";
import UnreachableError from "@assets/unreachable-error.svg";
import { OnlyofficeSnackbar } from "@components/snackbar";


const DOCSPACE_FRAME_ID="docspace-frame";

type iError = {
  Icon: any;
  title: string;
  message: string;
  button?: string;
  onClick?: React.MouseEventHandler<HTMLButtonElement> | undefined;
}

export const RoomPage: React.FC = () => {
  const [loading, setLoading] = useState(true);
  const [showDocspaceWindow, setShowDocspaceWindow] = useState(false);
  const [iError, setIError] = useState<iError>();
  const [config, setConfig] = useState<TFrameConfig>({
    frameId: DOCSPACE_FRAME_ID,
    mode: "manager",
    width: "100%",
    height: "100%",
    theme: "Base",
    events: {
      onAppReady: (e: string | Event) => {
        window.DocSpace.SDK.frames[DOCSPACE_FRAME_ID].setIsLoaded(false); //ToDo: need add to sdk event onContentReady()
        setShowDocspaceWindow(true);
        setLoading(false);
      },
      onAppError: (e: string | Event) => {
        setIError({
          Icon: <CommonError />,
          title: t("background.error.title", "Error"),
          message: t("background.error.subtitle.common", "Something went wrong. Please reload the app.")
        });
        setLoading(false);
        console.error(e);
      }
    } as TFrameEvents
  } as TFrameConfig);

  const { t } = useTranslation();
  const { sdk, user, settings, setError } = useContext(AppContext);
  const { parameters } = getCurrentURL();

  useEffect(() => {
    if(!settings?.url) {
      setIError({
        Icon: <CommonError />,
        title: t("background.error.subtitle.docspace-connection", "You are not connected to ONLYOFFICE DocSpace"),
        message: `${user?.isAdmin
                    ? t("background.error.hint.admin.docspace-connection", "Please, go to the Connection Setting to configure ONLYOFFICE DocSpace app settings.")
                    : t("background.error.hint.docspace-connection", "Please contact the administrator.")
                  }`,
        button: t("button.settings", "Settings") || "Settings",
        onClick: user?.isAdmin
              ? async () => await sdk.execute(Command.REDIRECT_TO, { view: View.SETTINGS})
              : undefined
      });
      setLoading(false);
      return;
    }

    if (!user?.docspaceAccount) {
      setIError({
        Icon: <CommonError />,
        title: t("background.error.subtitle.docspace-authorization.message", "Can not get authorize in ONLYOFFICE DocSpace"),
        message: t("background.error.subtitle.docspace-authorization.help", "Please, go to the Authorization Setting to configure ONLYOFFICE DocSpace app settings"),
        button: t("button.settings", "Settings") || "Settings",
        onClick: async () => await sdk.execute(Command.REDIRECT_TO, { view: View.SETTINGS})
      });
      setLoading(false);
      return;
    }

    getRoom(sdk, Number(parameters.get("selectedIds"))).then(response => {
      setConfig({...config, id: response.roomId, locale: i18next.language.split('-')[0]});
    }).catch(async (e) => {
      if (e?.response?.status == 404) {
        if(!user?.docspaceAccount?.canCreateRoom) {
          setIError({
            Icon: <DenniedError />,
            title: t("background.error.title.create-room", "Sorry, you don't have a permission to create rooms"),
            message: t("background.error.subtitle.create-room", "Please ask a Pipedrive Administrator to create a room or contact a DocSpace admin to upgrade your role.")
          });
          setLoading(false);
          return;
        }

        await sdk.execute(Command.RESIZE, { height: 150 });
        setLoading(false);
      } else {
        setError(e);
      }
    });
  }, []);

  const handleCreateRoom = () => {
    setLoading(true);

    createRoom(sdk, Number(parameters.get("selectedIds"))).then(async (response) => {
      setConfig({...config, id: response.roomId, locale: i18next.language.split('-')[0]});

      await sdk.execute(Command.RESIZE, { height: 350 });
      await sdk.execute(Command.SHOW_SNACKBAR, {
        message: t(
          "room.creating.ok",
          "ONLYOFFICE DocSpace room was successfully created!"
        ),
      });
    }).catch(async (e) => {
      await sdk.execute(Command.SHOW_SNACKBAR, {
        message: t(
          "room.creating.error",
          "Failed to create ONLYOFFICE DocSpace room!"
        ),
      });
      setLoading(false);
    });
  }

  const onRequestPasswordHash = () => {
    return user?.docspaceAccount?.passwordHash || "";
  }

  const onLoadComponentError = () => {
    setIError({
      Icon: <UnreachableError />,
      title: t("background.error.title", "Error"),
      message: `${t("docspace.error.unreached", "ONLYOFFICE DocSpace cannot be reached")}. 
                  ${(user?.isAdmin)
                    ? t("background.error.hint.admin.docspace-connection", "Please, go to the Connection Setting to configure ONLYOFFICE DocSpace app settings.")
                    : t("background.error.hint.docspace-connection", "Please contact the administrator.")
                  }`,
      button: t("button.settings", "Settings") || "Settings",
      onClick: user?.isAdmin
        ? async () => await sdk.execute(Command.REDIRECT_TO, { view: View.SETTINGS})
        : undefined
    });
  }

  const onUnSuccessLogin = () => {
    setIError({
      Icon: <CommonError />,
      title: t("background.error.subtitle.docspace-authorization.message", "Can not get authorize in ONLYOFFICE DocSpace"),
      message: t("background.error.subtitle.docspace-authorization.help", "Please, go to the Authorization Setting to configure ONLYOFFICE DocSpace app settings"),
      button: t("button.settings", "Settings") || "Settings",
      onClick: async () => await sdk.execute(Command.REDIRECT_TO, { view: View.SETTINGS})
    });
    setLoading(false);
    return;
  }

  return (
    <div className="w-full h-full flex flex-col">
      {loading && (
        <div className="h-full w-full flex justify-center items-center">
          <OnlyofficeSpinner />
        </div>
      )}
      {!loading && iError && (
        <OnlyofficeBackgroundError
          Icon={iError.Icon}
          title={iError.title}
          subtitle={iError.message}
          button={iError.button}
          onClick={iError.onClick}
        />
      )}
      {!loading && !iError && !settings?.existSystemUser && (
        <div className="w-full">
          <OnlyofficeSnackbar
            header={t("notification.system-user.not-found", "System User is not set.")}
            text={`${(user?.isAdmin)
              ? t("notification.plugin.set-system-user", "Please go to Settings and set yourself as a System User.")
              : t("notification.plugin.functionality-is-limited", "Plugin functionality is limited.")
            }`}
          />
        </div>
      )}
      {!loading && !config.id && !iError && user?.docspaceAccount?.canCreateRoom && (
        <div className="h-full flex flex-row">
          <div className="p-5">
            <div
              className="w-full pb-4"
            >
              {t("room.create.description", "Create ONLYOFFICE DocSpace room to easily collaborate on documents in this deal")}
            </div>
            <OnlyofficeButton
              text={t("button.create.room", "Create room")}
              type={ButtonType.Primary}
              onClick={handleCreateRoom}
            />
          </div>
        </div>
      )}
      {config.id && user && settings?.url && !iError && (
        <div
          className={`w-full h-full flex flex-row
            ${!showDocspaceWindow ? "hidden" : ""}
          `}
        >
          <DocSpace
            url={settings.url}
            config={config}
            email={user?.docspaceAccount?.userName || "undefined"}
            onLoadComponentError={onLoadComponentError}
            onRequestPasswordHash={onRequestPasswordHash}
            onUnSuccessLogin={onUnSuccessLogin}
          />
        </div>
      )}
    </div>
  );
};
