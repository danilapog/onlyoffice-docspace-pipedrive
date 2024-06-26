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

import React, { useEffect, useState } from "react";
import AppExtensionsSDK from "@pipedrive/app-extensions-sdk";
import i18next from "i18next";
import { useTranslation } from "react-i18next";

import { AxiosError } from "axios";

import { OnlyofficeSpinner } from "@components/spinner";
import { OnlyofficeBackgroundError } from "@layouts/ErrorBackground";

import { getUser } from "@services/user";
import { getAppStatus } from "@services/appStatus";

import { UserResponse } from "src/types/user";
import { AppStatusResponse } from "src/types/appStatus";

import CommonError from "@assets/common-error.svg";
import TokenError from "@assets/token-error.svg";


type AppContextProps = {
  children?: JSX.Element | JSX.Element[];
};

export interface IAppContext {
  sdk: AppExtensionsSDK;
  user: UserResponse | undefined;
  setUser: (value: UserResponse) => void;
  appStatus: AppStatusResponse | undefined;
  setAppStatus: (value: AppStatusResponse) => void;
  error: AxiosError | undefined;
  setError: (value: AxiosError) => void;
}

export const AppContext = React.createContext<IAppContext>({} as IAppContext);

export const AppContextProvider: React.FC<AppContextProps> = ({ children }) => {
  const [sdk, setSDK] = useState<AppExtensionsSDK>();
  const [user, setUser] = useState<UserResponse>();
  const [appStatus, setAppStatus] = useState<AppStatusResponse>();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<any>();

  const { t } = useTranslation();

  useEffect(() => {
    new AppExtensionsSDK()
      .initialize()
      .then(async (s) => {
        setSDK(s);
        try {
          const user = await getUser(s);
          const appStatus = await getAppStatus(s);  

          await i18next.changeLanguage(`${user.language.language_code}-${user.language.country_code}`);
          setUser(user);
          setAppStatus(appStatus);
        } catch (e) {
          setError(e);
        } finally {
          setLoading(false);
        }
      })
      .catch((e) => console.error(e));
  }, []);

  return(
    <>
      {loading && (
        <OnlyofficeSpinner />
      )}
      {!loading && error && (
        <OnlyofficeBackgroundError
          Icon={
            error?.response?.status === 401
              ? <TokenError className="mb-5" />
              : <CommonError className="mb-5" />
          }
          title={t(
            error?.response?.status === 401 ? "background.error.title.token-expired" : "background.error.title.common",
            error?.response?.status === 401 ? "The document security token has expired" : "Error"
          )}
          subtitle={t(
            error?.response?.status === 401 ? "background.error.subtitle.token-expired" : "background.error.subtitle.common",
            error?.response?.status === 401
              ? "Something went wrong. Please re-authorize the app."
              : "Something went wrong. Please reload the app."
          )}
          button={t("button.reauthorize", "Re-authorize") || "Re-authorize"}
          onClick={
            error?.response?.status === 401
              ? () =>
                  window.open(
                    `${process.env.BACKEND_URL}/oauth2/authorization/pipedrive`,
                    "_blank"
                  )
              : undefined
          }
        />
      )}
      {!loading && !error && sdk &&(
        <AppContext.Provider value={{sdk, user, setUser, appStatus, setAppStatus, error, setError}}>{children}</AppContext.Provider>
      )}
    </>
  );
};
