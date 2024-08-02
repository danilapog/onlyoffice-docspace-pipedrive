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

import { AppContext } from "@context/AppContext";

import { AuthorizationSetting } from "./Authorization";
import { ConnectionSettings } from "./Connection";

type Section = {
  id: string,
  title: string,
  available: boolean,
};

export const SettingsPage: React.FC = () => {
  const { t } = useTranslation();
  const { user, settings } = useContext(AppContext);

  const sections: Array<Section> = [
    {id: "conntection",  title: t("settings.connection.menu-item", "Connection"), available: true},
    {id: "authorization", title: t("settings.authorization.menu-item", "Authorization"), available: true},
  ];

  if (!user?.isAdmin) {
    sections[0].available = false;
  }

  const firstAvailableSection = sections.find((section) => section.available);

  const [selectedSection, setSelectedSection] = useState<string|undefined>(firstAvailableSection?.id);

  return (
    <>
      <div className="w-screen h-screen">
        <div className="flex flex-row">
          <div className="basis-1/5 border-r-2 p-1">
            {sections.filter(section => section.available).map(section => (
              <div
                key={section.id}
                id={section.id}
                tabIndex={0}
                className={`text-left font border-spacing-7 px-10 py-2 m-1 rounded-lg cursor-pointer ${
                  section.id === selectedSection
                  ? "text-blue-600 font-medium bg-sky-100"
                  : "hover:bg-stone-200"
                }`}
                onClick={() => setSelectedSection(section.id)}
                onKeyDown={() => setSelectedSection(section.id)}
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
      </div>
    </>
  );
};
