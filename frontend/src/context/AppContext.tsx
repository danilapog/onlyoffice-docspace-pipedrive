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

import { OnlyofficeSpinner } from "@components/spinner";
import AppExtensionsSDK from "@pipedrive/app-extensions-sdk";
import { AxiosError } from "axios";
import React, { useEffect, useState } from "react";

type AppContextProps = {
  children?: JSX.Element | JSX.Element[];
};

export interface IAppContext {
  error: AxiosError | undefined;
  setError: (value: AxiosError) => void;
  sdk: AppExtensionsSDK;
}

export const AppContext = React.createContext<IAppContext>({} as IAppContext);

export const AppContextProvider: React.FC<AppContextProps> = ({ children }) => {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<AxiosError>();
  const [sdk, setSDK] = useState<AppExtensionsSDK>();

  useEffect(() => {
    try {
      new AppExtensionsSDK()
        .initialize()
        .then((s) => {
          setSDK(s);
          setLoading(false);
        })
        .catch((e) => console.error(e));
    } catch (e) {
      console.error(e);
    }
  }, []);

  return(
    <>
      {loading && !sdk && (
        <OnlyofficeSpinner />
      )}
      {!loading && sdk && (
        <AppContext.Provider value={{error, setError, sdk}}>{children}</AppContext.Provider>
      )}
    </>
  );
};
