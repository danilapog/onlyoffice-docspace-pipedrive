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
import { Command } from "@pipedrive/app-extensions-sdk";
import {
  DocSpace,
  TFrameConfig,
  TFrameEvents,
} from "@onlyoffice/docspace-react";

import { AppContext, AppErrorType } from "@context/AppContext";

import { createRoom, getRoom } from "@services/room";
import { getCurrentURL, stripTrailingSlash } from "@utils/url";

import { ButtonType, OnlyofficeButton } from "@components/button";
import { OnlyofficeSpinner } from "@components/spinner";

import { OnlyofficeSnackbar } from "@components/snackbar";
import { getLocaleForDocspace } from "@utils/locale";
import { AxiosError } from "axios";

const DOCSPACE_FRAME_ID = "docspace-frame";

const RoomPage: React.FC = () => {
  const { sdk, user, settings, setAppError } = useContext(AppContext);

  const [loading, setLoading] = useState(true);
  const [creating, setCreating] = useState(false);
  const [showDocspaceWindow, setShowDocspaceWindow] = useState(false);
  const [config, setConfig] = useState<TFrameConfig>({
    frameId: DOCSPACE_FRAME_ID,
    mode: "manager",
    width: "100%",
    height: "100%",
    theme: sdk.userSettings.theme === "dark" ? "Dark" : "Base",
    events: {
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      onContentReady: (e: string | Event) => {
        setShowDocspaceWindow(true);
        setLoading(false);
        sdk.execute(Command.RESIZE, { height: 680 });
      },
      onAppError: (e: string | Event) => {
        setAppError(AppErrorType.COMMON_ERROR);
        setLoading(false);

        // eslint-disable-next-line no-console
        console.error(e);
      },
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      onNoAccess: (e: string | Event) => {
        sdk.execute(Command.RESIZE, { height: 350 });
        setAppError(AppErrorType.DOCSPACE_ROOM_NO_ACCESS);
        setLoading(false);
      },
    } as TFrameEvents,
  } as TFrameConfig);

  const { t } = useTranslation();
  const { parameters } = getCurrentURL();

  useEffect(() => {
    if (!settings?.url) {
      setAppError(AppErrorType.DOCSPACE_CONNECTION);
      setLoading(false);
      return;
    }

    if (!user?.docspaceAccount) {
      setAppError(AppErrorType.DOCSPACE_AUTHORIZATION);
      setLoading(false);
      return;
    }

    getRoom(sdk, Number(parameters.get("selectedIds")))
      .then(async (response) => {
        setConfig({
          ...config,
          id: response.roomId,
          locale: getLocaleForDocspace(i18next.language),
        });
      })
      .catch(async (e) => {
        if (e?.response?.status === 404) {
          if (!user?.docspaceAccount?.canCreateRoom) {
            setAppError(AppErrorType.DOCSPACE_ROOM_NOT_FOUND);
            setLoading(false);
            return;
          }

          await sdk.execute(Command.RESIZE, { height: 150 });
          setLoading(false);
        } else if (e?.response?.status === 401) {
          setAppError(AppErrorType.TOKEN_ERROR);
        } else {
          setAppError(AppErrorType.COMMON_ERROR);
        }
      });
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  const handleCreateRoom = () => {
    setCreating(true);

    createRoom(sdk, Number(parameters.get("selectedIds")))
      .then(async (response) => {
        setConfig({
          ...config,
          id: response.roomId,
          locale: getLocaleForDocspace(i18next.language),
        });
        sdk.execute(Command.RESIZE, { height: 350 });
        setLoading(true);

        await sdk.execute(Command.SHOW_SNACKBAR, {
          message: t(
            "room.creating.ok",
            "ONLYOFFICE DocSpace room was successfully created!",
          ),
        });
      })
      .catch(async (e) => {
        let message = t(
          "room.creating.error",
          "Failed to create ONLYOFFICE DocSpace room!",
        );
        let link;

        if (e instanceof AxiosError && e?.response?.status === 402) {
          message = t(
            "docspace.error.payment",
            "Creating this room is not possible since the limit is reached for the number of rooms included in your current plan.",
          );
          link = {
            url: `${stripTrailingSlash(settings?.url || "")}/portal-settings/payments/portal-payments`,
            label: t("docspace.link.payments", "Upgrade plan"),
          };
        }

        await sdk.execute(Command.SHOW_SNACKBAR, {
          message,
          link,
        });
        setCreating(false);
      });
  };

  const onRequestPasswordHash = () => user?.docspaceAccount?.passwordHash || "";

  const onLoadComponentError = () => {
    setAppError(AppErrorType.DOCSPACE_UNREACHABLE);
  };

  const onUnSuccessLogin = () => {
    setAppError(AppErrorType.DOCSPACE_AUTHORIZATION);
    setLoading(false);
  };

  return (
    <div className="w-full h-full flex flex-col">
      {loading && (
        <div className="h-full w-full flex justify-center items-center">
          <OnlyofficeSpinner />
        </div>
      )}
      {!loading && !settings?.existSystemUser && (
        <div className="w-full">
          <OnlyofficeSnackbar
            header={t(
              "notification.system-user.not-found",
              "System User is not set.",
            )}
            text={`${
              user?.isAdmin
                ? t(
                    "notification.plugin.set-system-user",
                    "Please go to Settings and set yourself as a System User.",
                  )
                : t(
                    "notification.plugin.functionality-is-limited",
                    "Plugin functionality is limited.",
                  )
            }`}
          />
        </div>
      )}
      {!loading && !config.id && user?.docspaceAccount?.canCreateRoom && (
        <div className="h-full flex flex-row">
          <div className="p-5">
            <div className="w-full pb-4">
              {t(
                "room.create.description",
                "Create ONLYOFFICE DocSpace room to easily collaborate on documents in this deal",
              )}
            </div>
            <OnlyofficeButton
              text={t("button.create.room", "Create room")}
              type={ButtonType.Primary}
              loading={creating}
              onClick={handleCreateRoom}
            />
          </div>
        </div>
      )}
      {config.id && user && settings?.url && (
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
export default RoomPage;
