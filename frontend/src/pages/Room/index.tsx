/**
 *
 * (c) Copyright Ascensio System SIA 2025
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
import { DocSpace } from "@onlyoffice/docspace-react";
import {
  TFrameConfig,
  TFrameEvents,
} from "@onlyoffice/docspace-sdk-js/dist/types/types";

import { AppContext, AppErrorType } from "@context/AppContext";

import { createRoom, getRoom } from "@services/room";
import { getCurrentURL, stripTrailingSlash } from "@utils/url";

import { OnlyofficeSpinner } from "@components/spinner";

import { OnlyofficeSnackbar } from "@components/snackbar";
import { getLocaleForDocspace } from "@utils/locale";
import { AxiosError } from "axios";
import {
  DropdownButtonColor,
  OnlyofficeDropdownButton,
} from "@components/dropdownButton";
import { DropdownButtonOptions } from "@components/dropdownButton/DropdownButton";

const DOCSPACE_FRAME_ID = "docspace-frame";
const DOCSPACE_ROOM_TYPES = [
  {
    id: 2,
    name: "collaboration",
  },
  {
    id: 6,
    name: "public",
  },
  {
    id: 8,
    name: "vdr",
  },
  {
    id: 5,
    name: "custom",
  },
];

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
    showHeader: false,
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
        setAppError(AppErrorType.DOCSPACE_ROOM_NO_ACCESS);
        setLoading(false);
      },
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      onNotFound: (e: Event) => {
        if (user?.docspaceAccount?.canCreateRoom) {
          setConfig({
            ...config,
            id: null,
          });
        } else {
          setAppError(AppErrorType.DOCSPACE_ROOM_NOT_FOUND);
        }

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

          setLoading(false);
        } else if (e?.response?.status === 401) {
          setAppError(AppErrorType.TOKEN_ERROR);
        } else {
          setAppError(AppErrorType.COMMON_ERROR);
        }
      });
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  const handleCreateRoom = (roomType: string) => {
    setCreating(true);

    createRoom(sdk, Number(parameters.get("selectedIds")), Number(roomType))
      .then(async (response) => {
        setConfig({
          ...config,
          id: response.roomId,
          locale: getLocaleForDocspace(i18next.language),
        });
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

  const onUnsuccessLogin = () => {
    setAppError(AppErrorType.DOCSPACE_AUTHORIZATION);
    setLoading(false);
  };

  const getCreateRoomOptions = () =>
    DOCSPACE_ROOM_TYPES.reduce((createRoomOptions, roomType) => {
      createRoomOptions.push({
        id: String(roomType.id),
        label: t(`docspace.room.type.${roomType.name}`, roomType.name),
      });
      return createRoomOptions;
    }, new Array<DropdownButtonOptions>());

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
        <div className="h-full flex flex-row custom-scroll overflow-y-scroll overflow-x-hidden">
          <div className="p-5">
            <div className="w-full pb-4">
              {t(
                "room.create.description",
                "Create ONLYOFFICE DocSpace room to work with documents related to this deal. You will not be able to change the room type.",
              )}
              <a
                className="font-semibold
                  text-pipedrive-color-light-blue-600
                  dark:text-pipedrive-color-dark-blue-600"
                target="_blank"
                href="https://helpcenter.onlyoffice.com/userguides/docspace-creating-rooms.aspx"
                rel="noreferrer"
              >
                {` ${t("t", "Learn more")}.`}
              </a>
            </div>
            <OnlyofficeDropdownButton
              text={t("button.create.room", "Create room")}
              color={DropdownButtonColor.PRIMARY}
              loading={creating}
              options={getCreateRoomOptions()}
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
            onRequestPasswordHash={onRequestPasswordHash}
            onUnsuccessLogin={onUnsuccessLogin}
          />
        </div>
      )}
    </div>
  );
};
export default RoomPage;
