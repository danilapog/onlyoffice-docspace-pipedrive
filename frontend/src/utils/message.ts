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

import { useTranslation } from "react-i18next";

export function useErrorMessage() {
  const { t } = useTranslation();

  const getSettingsErrorMessage = (code: string): string => {
    const key = `settings.connection.saving.error.${code}`;
    return t(key, {
      defaultValue: t(
        "settings.connection.saving.error.undefined",
        "Could not save ONLYOFFICE DocSpace settings",
      ),
    });
  };

  return getSettingsErrorMessage;
}
