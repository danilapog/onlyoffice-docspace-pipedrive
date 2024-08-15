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

import React, { useContext, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";

import {
  ErrorProps,
  OnlyofficeBackgroundError,
} from "@layouts/ErrorBackground";

import { AppContext, AppErrorType } from "@context/AppContext";

import CommonError from "@assets/common-error.svg";
import TokenError from "@assets/token-error.svg";
import DenniedError from "@assets/dennied-error.svg";
import UnreachableError from "@assets/unreachable-error.svg";

import { Command, View } from "@pipedrive/app-extensions-sdk";

type ErrorPageProps = {
  children?: JSX.Element | JSX.Element[];
};

export const ErrorPage: React.FC<ErrorPageProps> = ({ children }) => {
  const [errorProps, setErrorProps] = useState<ErrorProps>();

  const { t } = useTranslation();
  const { sdk, user, appError } = useContext(AppContext);

  useEffect(() => {
    switch (appError) {
      case AppErrorType.COMMON_ERROR: {
        setErrorProps({
          Icon: <CommonError className="mb-5" />,
          title: t("background.error.title.common", "Error"),
          subtitle: t(
            "background.error.subtitle.common",
            "Something went wrong. Please reload the app.",
          ),
        });
        break;
      }
      case AppErrorType.TOKEN_ERROR: {
        setErrorProps({
          Icon: <TokenError className="mb-5" />,
          title: t(
            "background.error.title.token-expired",
            "The document security token has expired",
          ),
          subtitle: t(
            "background.error.subtitle.token-expired",
            "Something went wrong. Please re-authorize the app.",
          ),
          button: t("button.reauthorize", "Re-authorize") || "Re-authorize",
          onClick: () =>
            window.open(
              `${process.env.BACKEND_URL}/oauth2/authorization/pipedrive`,
              "_blank",
            ),
        });
        break;
      }
      case AppErrorType.PLUGIN_NOT_AVAILABLE: {
        setErrorProps({
          Icon: <CommonError className="mb-5" />,
          title: t(
            "background.error.subtitle.plugin.not-active.message",
            "ONLYOFFICE DocSpace App is not yet available",
          ),
          subtitle: t(
            "background.error.subtitle.plugin.not-active.help",
            "Please wait until a Pipedrive Administrator configures the app settings",
          ),
        });
        break;
      }
      case AppErrorType.DOCSPACE_CONNECTION: {
        setErrorProps({
          Icon: <CommonError className="mb-5" />,
          title: t(
            "background.error.subtitle.docspace-connection",
            "You are not connected to ONLYOFFICE DocSpace",
          ),
          subtitle: `${
            user?.isAdmin
              ? t(
                  "background.error.hint.admin.docspace-connection",
                  "Please go to the Connection Setting to configure ONLYOFFICE DocSpace app settings.",
                )
              : t(
                  "background.error.hint.docspace-connection",
                  "Please contact the administrator.",
                )
          }`,
          button: t("button.settings", "Settings") || "Settings",
          onClick: user?.isAdmin
            ? () => sdk.execute(Command.REDIRECT_TO, { view: View.SETTINGS })
            : undefined,
        });
        break;
      }
      case AppErrorType.DOCSPACE_AUTHORIZATION: {
        setErrorProps({
          Icon: <CommonError className="mb-5" />,
          title: t(
            "background.error.subtitle.docspace-authorization.message",
            "Can not get authorize in ONLYOFFICE DocSpace",
          ),
          subtitle: t(
            "background.error.subtitle.docspace-authorization.help",
            "Please go to the Authorization Setting to configure ONLYOFFICE DocSpace app settings",
          ),
          button: t("button.settings", "Settings") || "Settings",
          onClick: () =>
            sdk.execute(Command.REDIRECT_TO, { view: View.SETTINGS }),
        });
        break;
      }
      case AppErrorType.DOCSPACE_ROOM_NOT_FOUND: {
        setErrorProps({
          Icon: <DenniedError className="mb-5" />,
          title: t(
            "background.error.title.create-room",
            "Sorry, you don't have a permission to create rooms",
          ),
          subtitle: t(
            "background.error.subtitle.create-room",
            "Please ask a Pipedrive Administrator to create a room or contact a DocSpace admin to upgrade your role.",
          ),
        });
        break;
      }
      case AppErrorType.DOCSPACE_UNREACHABLE: {
        setErrorProps({
          Icon: <UnreachableError className="mb-5" />,
          title: t(
            "docspace.error.unreached",
            "ONLYOFFICE DocSpace cannot be reached",
          ),
          subtitle: `${t(
            "docspace.error.unreached",
            "ONLYOFFICE DocSpace cannot be reached",
          )}. 
              ${
                user?.isAdmin
                  ? t(
                      "background.error.hint.admin.docspace-connection",
                      "Please go to the Connection Setting to configure ONLYOFFICE DocSpace app settings.",
                    )
                  : t(
                      "background.error.hint.docspace-connection",
                      "Please contact the administrator.",
                    )
              }`,
          button: t("button.settings", "Settings") || "Settings",
          onClick: user?.isAdmin
            ? () => sdk.execute(Command.REDIRECT_TO, { view: View.SETTINGS })
            : undefined,
        });
        break;
      }
      default: {
        break;
      }
    }
  }, [sdk, appError, t, user]);

  return (
    <>
      {errorProps && (
        <OnlyofficeBackgroundError
          Icon={errorProps.Icon}
          title={errorProps.title || ""}
          subtitle={errorProps.subtitle || ""}
          button={errorProps.button}
          onClick={errorProps.onClick}
        />
      )}
      {!errorProps && children}
    </>
  );
};
