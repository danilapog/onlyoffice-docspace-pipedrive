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

import React, { useState, useContext } from "react";
import { useTranslation, Trans } from "react-i18next";
import { Command } from "@pipedrive/app-extensions-sdk";

import { ButtonType, OnlyofficeButton } from "@components/button";
import { OnlyofficeInput } from "@components/input";
import { OnlyofficeTitle } from "@components/title";
import { OnlyofficeHint } from "@components/hint";

import { deleteSettings, postSettings } from "@services/settings";

import { getCurrentURL, stripTrailingSlash } from "@utils/url";

import { AppContext } from "@context/AppContext";
import { SettingsResponse } from "src/types/settings";
import { getCSPSettings } from "@services/docspace";

export const ConnectionSettings: React.FC = () => {
  const { t } = useTranslation();
  const { user, setUser, settings, setSettings, sdk } = useContext(AppContext);
  const { url } = getCurrentURL();

  const [connecting, setConnecting] = useState(false);
  const [disconnecting, setDisconnecting] = useState(false);
  const [changing, setChanging] = useState(false);
  const [showValidationMessage, setShowValidationMessage] = useState(false);
  const [address, setAddress] = useState<string>(settings?.url || "");

  const handleConnect = async () => {
    if (address) {
      setConnecting(true);
      getCSPSettings(stripTrailingSlash(address))
        .then(async (cspSettings) => {
          const allowedDomains = cspSettings.domains.filter(
            (domain) =>
              stripTrailingSlash(domain) === stripTrailingSlash(url) ||
              stripTrailingSlash(domain) ===
                new URL(document.location.href).origin,
          );

          if (allowedDomains.length < 2) {
            await sdk.execute(Command.SHOW_SNACKBAR, {
              message: t(
                "docspace.error.csp",
                "The current domain is not set in the Content Security Policy (CSP) settings. Please add it via the Developer Tools section.",
              ),
              link: {
                url: `${stripTrailingSlash(
                  address || "",
                )}/portal-settings/developer-tools/javascript-sdk`,
                label: t(
                  "docspace.link.developer-tools",
                  "Developer Tools section",
                ),
              },
            });
            setConnecting(false);
            return;
          }

          postSettings(sdk, stripTrailingSlash(address))
            .then(async (response: SettingsResponse) => {
              setSettings(response);
              await sdk.execute(Command.SHOW_SNACKBAR, {
                message: t(
                  "settings.connection.saving.ok",
                  "ONLYOFFICE DocSpace settings have been saved",
                ),
              });
            })
            .catch(async () => {
              await sdk.execute(Command.SHOW_SNACKBAR, {
                message: t(
                  "settings.connection.saving.error.undefined",
                  "Could not save ONLYOFFICE DocSpace settings",
                ),
              });
            })
            .finally(() => {
              setConnecting(false);
              setChanging(false);
            });
        })
        .catch(async () => {
          await sdk.execute(Command.SHOW_SNACKBAR, {
            message: t(
              "docspace.error.unreached",
              "ONLYOFFICE DocSpace cannot be reached",
            ),
          });
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
      deleteSettings(sdk)
        .then(async () => {
          await sdk.execute(Command.SHOW_SNACKBAR, {
            message: t(
              "settings.connection.disconnection.ok",
              "ONLYOFFICE DocSpace succesfully disconnected",
            ),
          });
          setSettings(undefined);
          if (user) {
            setUser({ ...user, docspaceAccount: null, isSystem: false });
          }
          setAddress("");
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
        {(!settings?.url || changing) && (
          <OnlyofficeHint>
            <div>
              <p className="font-semibold">
                {t(
                  "settings.connection.hint.csp.title",
                  "Check the CSP settings",
                )}
              </p>
              <p>
                <Trans
                  i18nKey="settings.connection.hint.csp.message"
                  defaults="Before connecting the app, please go to the <semibold>{{path}}</semibold> and add the following credentials to the allow list"
                  values={{
                    path: t(
                      "settings.connection.hint.csp.path",
                      "DocSpace Settings - Developer tools - JavaScript SDK",
                    ),
                  }}
                  components={{ semibold: <span className="font-semibold" /> }}
                />
                :
              </p>
              <br />
              <p className="font-semibold">
                {t(
                  "settings.connection.hint.csp.pipedrive-adress",
                  "Pipedrive portal address",
                )}
                :{" "}
                <span className="text-green-700">
                  {stripTrailingSlash(url)}
                </span>
              </p>
              <p className="font-semibold">
                {t(
                  "settings.connection.hint.csp.docspace-adress",
                  "ONLYOFFICE DocSpace app",
                )}
                :{" "}
                <span className="text-green-700">
                  {new URL(document.location.href).origin}
                </span>
              </p>
            </div>
          </OnlyofficeHint>
        )}
      </div>
      <div className="max-w-[320px]">
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
        <div className="flex justify-start items-center mt-4 ml-5 gap-2">
          {!settings?.url && (
            <OnlyofficeButton
              text={t("button.connect", "Connect")}
              type={ButtonType.Primary}
              loading={connecting}
              onClick={handleConnect}
            />
          )}
          {!!settings?.url && !changing && (
            <>
              <OnlyofficeButton
                text={t("button.change", "Change")}
                type={ButtonType.Primary}
                disabled={disconnecting}
                onClick={() => {
                  setChanging(true);
                }}
              />
              <OnlyofficeButton
                text={t("button.disconnect", "Disconnect")}
                type={ButtonType.Danger}
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
                }}
              />
              <OnlyofficeButton
                text={t("button.save", "Save")}
                type={ButtonType.Primary}
                loading={connecting}
                onClick={handleConnect}
              />
            </>
          )}
        </div>
      </div>
    </>
  );
};
