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

import React, { useState, useContext } from "react";
import { useTranslation } from "react-i18next";
import { Command } from "@pipedrive/app-extensions-sdk";

import { ButtonColor, OnlyofficeButton } from "@components/button";
import { OnlyofficeInput } from "@components/input";
import { OnlyofficeTitle } from "@components/title";

import { deleteSettings, putSettings } from "@services/settings";

import { stripTrailingSlash } from "@utils/url";

import { AppContext } from "@context/AppContext";
import { SettingsResponse } from "src/types/settings";
import { AxiosError } from "axios";
import { ErrorResponse } from "src/types/error";

export const ConnectionSettings: React.FC = () => {
  const { t } = useTranslation();
  const {
    user,
    setUser,
    settings,
    setSettings,
    sdk,
    pipedriveToken,
    reloadAppContext,
  } = useContext(AppContext);

  const [connecting, setConnecting] = useState(false);
  const [disconnecting, setDisconnecting] = useState(false);
  const [changing, setChanging] = useState(false);
  const [showValidationMessage, setShowValidationMessage] = useState(false);
  const [address, setAddress] = useState<string>(settings?.url || "");
  const [apiKey, setApiKey] = useState<string>(settings?.apiKey || "");

  const getSettingsValidationsMessage = (code: string): string => {
    const key = `settings.connection.saving.error.${code}`;
    return t(key, {
      defaultValue: t(
        "settings.connection.saving.error.undefined",
        "Could not save ONLYOFFICE DocSpace settings",
      ),
    });
  };

  const handleConnect = async (event: React.SyntheticEvent) => {
    event.preventDefault();
    if (address && apiKey) {
      setConnecting(true);
      putSettings(pipedriveToken, stripTrailingSlash(address), apiKey)
        .then(async (response: SettingsResponse) => {
          setSettings(response);
          setAddress(response.url);
          setApiKey(response.apiKey);
          await sdk.execute(Command.SHOW_SNACKBAR, {
            message: t(
              "settings.connection.saving.ok",
              "ONLYOFFICE DocSpace settings have been saved",
            ),
          });

          if (changing) {
            reloadAppContext();
          }
        })
        .catch(async (e: AxiosError) => {
          const data = e?.response?.data as ErrorResponse;
          if (
            e?.response?.status === 400 &&
            data?.cause === "SettingsValidationException"
          ) {
            await sdk.execute(Command.SHOW_SNACKBAR, {
              message: getSettingsValidationsMessage(
                data.params.validationError,
              ),
            });
          } else {
            await sdk.execute(Command.SHOW_SNACKBAR, {
              message: t(
                "settings.connection.saving.error.undefined",
                "Could not save ONLYOFFICE DocSpace settings",
              ),
            });
          }
        })
        .finally(() => {
          setConnecting(false);
        });
    } else {
      setShowValidationMessage(true);
    }
  };

  const handleDisconnect = async () => {
    const { confirmed } = await sdk.execute(Command.SHOW_CONFIRMATION, {
      title: t("button.disconnect", "Disconnect"),
      description:
        t(
          "settings.connection.disconnection.description",
          `Are you sure you want to disconnect ONLYOFFICE DocSpace? This will 
        result in all connections between Deals and DocSpace rooms being lost. 
        And also all user authorization data will be deleted.`,
        ) || "",
    });

    if (confirmed) {
      setDisconnecting(true);
      deleteSettings(pipedriveToken)
        .then(async () => {
          await sdk.execute(Command.SHOW_SNACKBAR, {
            message: t(
              "settings.connection.disconnection.ok",
              "ONLYOFFICE DocSpace succesfully disconnected",
            ),
          });
          setSettings(undefined);
          if (user) {
            setUser({ ...user, docspaceAccount: null });
          }
          setAddress("");
          setApiKey("");
        })
        .catch(async () => {
          await sdk.execute(Command.SHOW_SNACKBAR, {
            message: t(
              "settings.connection.disconnection.error",
              "Could not disconnect ONLYOFFICE DocSpace",
            ),
          });
        })
        .finally(() => {
          setDisconnecting(false);
        });
    }
  };

  return (
    <>
      <div className="flex flex-col items-start pl-5 pr-5 pt-5 pb-3">
        <div className="pb-2">
          <OnlyofficeTitle
            text={t(
              "settings.connection.title",
              "Configure connection settings of the ONLYOFFICE DocSpace app",
            )}
          />
        </div>
      </div>
      <div className="max-w-[320px]">
        <form onSubmit={handleConnect}>
          <div className="pl-5 pr-5 pb-2">
            <OnlyofficeInput
              text={t(
                "settings.connection.inputs.address",
                "ONLYOFFICE DocSpace address",
              )}
              placeholder="https://"
              valid={showValidationMessage ? !!address : true}
              disabled={(connecting || !!settings?.url) && !changing}
              value={address}
              onChange={(e) => setAddress(e.target.value.trim())}
            />
          </div>
          <div className="pl-5 pr-5 pb-2">
            <OnlyofficeInput
              text={t(
                "settings.connection.inputs.api-key.title",
                "ONLYOFFICE DocSpace API key",
              )}
              placeholder="***********"
              description={t(
                "settings.connection.inputs.api-key.description",
                "The API key must be created by a DocSpace admin and should include the following access scopes: Profile (Read), Contacts (Write), and Rooms (Write).",
              )}
              valid={showValidationMessage ? !!apiKey : true}
              disabled={(connecting || !!settings?.url) && !changing}
              value={apiKey}
              onChange={(e) => setApiKey(e.target.value.trim())}
            />
          </div>
          <div className="flex justify-start items-center mt-4 ml-5 gap-2">
            {!settings?.url && (
              <OnlyofficeButton
                text={t("button.connect", "Connect")}
                type="submit"
                color={ButtonColor.PRIMARY}
                loading={connecting}
                onClick={handleConnect}
              />
            )}
            {!!settings?.url && !changing && (
              <>
                <OnlyofficeButton
                  text={t("button.change", "Change")}
                  color={ButtonColor.PRIMARY}
                  disabled={disconnecting}
                  onClick={() => {
                    setChanging(true);
                    setApiKey("");
                  }}
                />
                <OnlyofficeButton
                  text={t("button.disconnect", "Disconnect")}
                  color={ButtonColor.NEGATIVE}
                  loading={disconnecting}
                  onClick={handleDisconnect}
                />
              </>
            )}
            {changing && (
              <>
                <OnlyofficeButton
                  text={t("button.cancel", "Cancel")}
                  disabled={connecting}
                  onClick={() => {
                    setChanging(false);
                    setAddress(settings?.url || "");
                    setApiKey(settings?.apiKey || "");
                  }}
                />
                <OnlyofficeButton
                  text={t("button.save", "Save")}
                  type="submit"
                  color={ButtonColor.PRIMARY}
                  loading={connecting}
                  onClick={handleConnect}
                />
              </>
            )}
          </div>
        </form>
      </div>
    </>
  );
};
