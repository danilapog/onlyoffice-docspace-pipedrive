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

import React, { useState, useEffect, useContext } from "react";
import { useTranslation } from "react-i18next";
import { Command } from "@pipedrive/app-extensions-sdk";
import { DocSpace, TFrameConfig } from "@onlyoffice/docspace-react";

import { OnlyofficeButton } from "@components/button";
import { OnlyofficeInput } from "@components/input";
import { OnlyofficeTitle } from "@components/title";

import { postSettings } from "@services/settings";

import { AppContext } from "@context/AppContext";
import { SettingsResponse } from "src/types/settings";

const DOCSPACE_SYSTEM_FRAME_ID="docspace-system-frame"

export const ConnectionSettings: React.FC= () => {
  const { t } = useTranslation();
  const { settings, setSettings, sdk } = useContext(AppContext);

  const [saving, setSaving] = useState(false);
  const [showValidationMessage, setShowValidationMessage] = useState(false);
  const [address, setAddress] = useState<string | undefined>(settings?.url);

  const handleSettings = async () => {
    if (address) {
      setSaving(true);
    } else {
      setShowValidationMessage(true);
    }
  };

  const onAppReady = async () => {
    if (address) {
      postSettings(sdk, address).then(async (response: SettingsResponse) => {
        setSettings(response);
        await sdk.execute(Command.SHOW_SNACKBAR, {
          message: t(
            "settings.connection.saving.ok",
            "ONLYOFFICE DocSpace settings have been saved"
          ),
        });
      })
      .catch(async (e) => {
        await sdk.execute(Command.SHOW_SNACKBAR, {
          message: t(
            "settings.connection.saving.error",
            "Could not save ONLYOFFICE DocSpace settings"
          ),
        });
      })
      .finally(() => {setSaving(false)});
    }
  }

  const onAppError = async (errorMessage: string) => {
    if ( errorMessage === "The current domain is not set in the Content Security Policy (CSP) settings." ) {
      await sdk.execute(Command.SHOW_SNACKBAR, {
        message: t(
          "docspace.error.csp",
          "The current domain is not set in the Content Security Policy (CSP) settings. Please add it via the Developer Tools section."
        ),
        link: {
          url: `${address}/portal-settings/developer-tools/javascript-sdk`,
          label: t(
            "docspace.link.developer-tools",
            "Developer Tools section"
          )
        }
      });
    } else {
      await sdk.execute(Command.SHOW_SNACKBAR, {
        message: errorMessage
      });
    }

    delete window.DocSpace;
    setSaving(false);
  }

  const onLoadComponentError = async () => {
    await sdk.execute(Command.SHOW_SNACKBAR, {
      message: t(
        "docspace.error.unreached",
        "ONLYOFFICE DocSpace cannot be reached"
      ),
    });
    setSaving(false);
  };

  const stripTrailingSlash = (url: string) => {
    return url.endsWith( '/' )
      ? url.slice( 0, -1 )
      : url;
  };

  return (
    <>
      <div className="flex flex-col items-start pl-5 pr-5 pt-5 pb-3">
        <div className="pb-2">
          <OnlyofficeTitle
            text={t("settings.connection.title", "Configure ONLYOFFICE DocSpace app settings")}
          />
        </div>
        <p className="text-slate-800 font-normal text-base text-left">
          {t(
            "settings.connection.description",
            `
            This plugin allows multiple users to collaborate in real time,
            save back those changes to Pipedrive and enables the users to edit
            office documents from Pipedrive using ONLYOFFICE DocSpace Server.
            `
          )}
        </p>
      </div>
      <div className="max-w-[320px]">
        <div className="pl-5 pr-5 pb-2">
          <OnlyofficeInput
            text={t("settings.connection.inputs.address", "DocSpace Service Address")}
            valid={showValidationMessage ? !!address : true}
            disabled={saving}
            value={address}
            onChange={(e) => setAddress(stripTrailingSlash(e.target.value.trim()))}
          />
        </div>
        <div className="flex justify-start items-center mt-4 ml-5">
          <OnlyofficeButton
            text={t("button.save", "Save")}
            primary
            disabled={saving}
            onClick={handleSettings}
          />
        </div>
      </div>
      {saving && address && (
        <div style={{ display: "none" }}>
          <DocSpace
            url={address}
            config={
              {
                frameId: DOCSPACE_SYSTEM_FRAME_ID,
                mode: "system",
                events: {
                  onAppReady: onAppReady,
                  onAppError: onAppError
                } as unknown
              } as TFrameConfig
            }
            onLoadComponentError={onLoadComponentError}
          />
        </div>
      )}
    </>
  );
};
