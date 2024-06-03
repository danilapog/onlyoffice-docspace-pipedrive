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

import React, { useContext, useState } from "react";
import { useTranslation } from "react-i18next";
import { ConnectionSettings } from "./Connection";
import { OnlyofficeBackgroundError } from "@layouts/ErrorBackground";

import SettingsError from "@assets/settings-error.svg";
import { AppContext } from "@context/AppContext";
import { AuthorizationSetting } from "./Authorization";

export const SettingsPage: React.FC = () => {
  const { t } = useTranslation();
  const { error } = useContext(AppContext);

  const [selectedSection, setSelectedSection] = useState<string>("conntection");

  const sections = [
    {id: "conntection",  title: t("settings.connection.menu-item", "Connection")},
    {id: "authorization", title: t("settings.authorization.menu-item", "Authorization")},
  ];

  console.log(error);

  return (
    <div className="w-screen h-screen">
      {error && (
        <OnlyofficeBackgroundError
          Icon={<SettingsError />}
          title={t("background.error.title", "Error")}
          subtitle={t(
            error.response?.status !== 401
              ? "background.error.subtitle"
              : "background.error.subtitle.token",
            error.response?.status !== 401
              ? "Could not fetch plugin settings. Something went wrong. Please reload the pipedrive window"
              : "Could not fetch plugin settings. Something went wrong with your access token. Please reinstall the app"
          )}
          button={
            error.response?.status === 401 && t("background.reinstall.button", "Reinstall") ||
            "Reinstall"
          }
          onClick={error.response?.status === 401 ? () => {
            if (error.response?.status === 401)
              window.open(
                // `${getCurrentURL().url}settings/marketplace`,
                "_blank"
              );
          } : undefined}
        />
      )}
      {!error && (
        <div className="flex flex-row">
          <div className="basis-1/5 border-r-2 p-1">
            {sections.map(section => (
              <div
                id={section.id}
                className={`text-left font border-spacing-7 px-10 py-2 m-1 rounded-lg cursor-pointer ${
                  section.id === selectedSection
                  ? "text-blue-600 font-medium bg-sky-100"
                  : "hover:bg-stone-200"
                }`}
                onClick={() => setSelectedSection(section.id)}
              >
                {section.title}
              </div>
            ))}
          </div>
          <div className="basis-4/5 custom-scroll w-screen h-screen overflow-y-scroll overflow-x-hidden p-2">
            {selectedSection === "conntection" &&
              <ConnectionSettings />
            }
            {selectedSection === "authorization" &&
              <AuthorizationSetting />
            }
          </div>
        </div>
      )}
    </div>
  );
};
