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
import { DocSpace } from "@onlyoffice/docspace-react";
import axios, { AxiosError } from "axios";

import { OnlyofficeButton } from "@components/button";
import { OnlyofficeInput } from "@components/input";
import { OnlyofficeTitle } from "@components/title";
import { OnlyofficeSpinner } from "@components/spinner";

import { getSettings, postSettings } from "@services/settings";

import { AppContext } from "@context/AppContext";

const DOCSPACE_SYSTEM_FRAME_ID="docspace-system-frame"

export const ConnectionSettings: React.FC= () => {
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [address, setAddress] = useState<string | undefined>(undefined);
  const [login, setLogin] = useState<string | undefined>(undefined);
  const [password, setPassword] = useState<string | undefined>(undefined);

  const { t } = useTranslation();
  const { sdk, setError } = useContext(AppContext);

  useEffect(() => {
    getSettings(sdk).then(response => {
      setAddress(response.url);
      setLogin(response.userName);
      setLoading(false);
    }).catch(e => {
      if (axios.isAxiosError(e) && !axios.isCancel(e)) {
        setError(e as AxiosError);
      }
    });
  }, []);

  const handleSettings = async () => {
    if (address && login && password) {
      setSaving(true);
    }
  };

  const onAppReady = async () => {
    if (address && login && password) {
      try {
        const hashSettings = await window.DocSpace.SDK.frames[DOCSPACE_SYSTEM_FRAME_ID].getHashSettings();
        const passwordHash = await window.DocSpace.SDK.frames[DOCSPACE_SYSTEM_FRAME_ID].createHash(password, hashSettings);

        await postSettings(sdk, address, login, passwordHash);

        await sdk.execute(Command.SHOW_SNACKBAR, {
          message: t(
            "settings.connection.saving.ok",
            "ONLYOFFICE settings have been saved"
          ),
        });
      } catch (e) {
        await sdk.execute(Command.SHOW_SNACKBAR, {
          message: t(
            "settings.connection.saving.error",
            "Could not save ONLYOFFICE settings"
          ),
        });
      } finally {
        setSaving(false);
      }
    }
  }

  const onAppError = async (errorMessage: string) => {
    if ( errorMessage === "The current domain is not set in the Content Security Policy (CSP) settings." ) {
      await sdk.execute(Command.SHOW_SNACKBAR, {
        message: t(
          "settings.connection.saving.error.docspace.csp",
          "The current domain is not set in the Content Security Policy (CSP) settings. Please add it via the Developer Tools section."
        ),
        link: {
          url: `${address}/portal-settings/developer-tools/javascript-sdk`,
          label: t(
            "settings.connection.link.docspace.developer-tools",
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
        "settings.connection.saving.error.docspace.unreached",
        "ONLYOFFICE DocSpace cannot be reached."
      ),
    });
    setSaving(false);
  }

  const stripTrailingSlash = (url: string) => {
    return url.endsWith( '/' )
      ? url.slice( 0, -1 )
      : url;
  };

  return (
    <>
      {loading && (
        <div className="h-full w-full flex justify-center items-center">
          <OnlyofficeSpinner />
        </div>
      )}
      {!loading && (
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
                valid={!!address}
                disabled={saving}
                value={address}
                onChange={(e) => setAddress(stripTrailingSlash(e.target.value.trim()))}
              />
            </div>
            <div className="pl-5 pr-5 pb-2">
              <OnlyofficeInput
                text={t("settings.connection.inputs.login", "DocSpace Login")}
                valid={!!login}
                disabled={saving}
                value={login}
                onChange={(e) => setLogin(e.target.value.trim())}
              />
            </div>
            <div className="pl-5 pr-5">
              <OnlyofficeInput
                text={t("settings.connection.inputs.password", "DocSpace Password")}
                type="password"
                valid={!!password}
                disabled={saving}
                value={password}
                onChange={(e) => setPassword(e.target.value.trim())}
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
        </>
      )}
      {!loading && saving && address && (
        <div style={{ display: "none" }}>
          <DocSpace
            id={DOCSPACE_SYSTEM_FRAME_ID}
            url={address}
            mode="system"
            events={{
              onAppReady: onAppReady,
              onAppError: onAppError
            }}
            onLoadComponentError={onLoadComponentError}
          />
        </div>
      )}
    </>
  );
};
