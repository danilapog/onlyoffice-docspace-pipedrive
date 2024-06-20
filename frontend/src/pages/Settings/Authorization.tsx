import { useContext, useState } from "react";
import { useTranslation } from "react-i18next";
import { Command } from "@pipedrive/app-extensions-sdk";
import { DocSpace, TFrameConfig } from "@onlyoffice/docspace-react";

import { OnlyofficeButton } from "@components/button";
import { OnlyofficeInput } from "@components/input";
import { OnlyofficeTitle } from "@components/title";
import { OnlyofficeSpinner } from "@components/spinner";
import { OnlyofficeBackgroundError } from "@layouts/ErrorBackground";

import { AppContext } from "@context/AppContext";

import { postDocspaceAccount, deleteDocspaceAccount } from "@services/user";

import Authorized from "@assets/authorized.svg"
import CommonError from "@assets/common-error.svg";

const DOCSPACE_SYSTEM_FRAME_ID="docspace-system-frame"

export const AuthorizationSetting: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [showValidationMessage, setShowValidationMessage] = useState(false);
  const [email, setEmail] = useState<string | undefined>(undefined);
  const [password, setPassword] = useState<string | undefined>(undefined);

  const { t } = useTranslation();
  const { user, setUser, sdk } = useContext(AppContext);

  const handleLogin = async () => {
    setShowValidationMessage(true);
    if (email && password) {
      setLoading(true);
    }
  };

  const handleLogout = async () => {
      setLoading(true);
      
      deleteDocspaceAccount(sdk).then(() => {
        if (user) {
          setUser({...user, docspaceAccount: null});
        }
      })
      .catch(async (e) => {
        await sdk.execute(Command.SHOW_SNACKBAR, {
          message: t(
            "background.error.subtitle.common",
            "Something went wrong. Please reload the app."
          ),
        });
      })
      setLoading(false);
  };

  const onAppReady = async () => {
    if (email && password) {
      const hashSettings = await window.DocSpace.SDK.frames[DOCSPACE_SYSTEM_FRAME_ID].getHashSettings();
      const passwordHash = await window.DocSpace.SDK.frames[DOCSPACE_SYSTEM_FRAME_ID].createHash(password, hashSettings);
      
      const login = await window.DocSpace.SDK.frames[DOCSPACE_SYSTEM_FRAME_ID].login(email, passwordHash)
      
      if (login.status && login.status !== 200) {
        await sdk.execute(Command.SHOW_SNACKBAR, {
          message: t(
            "docspace.error.login",
            "User authentication failed"
          ),
        });
        setLoading(false);
      } else {
        postDocspaceAccount(sdk, email, passwordHash).then(async () => {
          if (user) {
            setUser({...user, docspaceAccount: {userName: email, passwordHash: ""}});
          }

          await sdk.execute(Command.SHOW_SNACKBAR, {
            message: t(
              "settings.authorization.saving.ok",
              "ONLYOFFICE DocSpace authorization has been successfully saved"
            ),
          });
        })
        .catch(async (e)=> {
          await sdk.execute(Command.SHOW_SNACKBAR, {
            message: t(
              "settings.authorization.saving.error",
              "Could not save ONLYOFFICE DocSpace authorization"
            ),
          });
        })
        .finally(() => setLoading(false));
      }
    }
  };

  const onAppError = async () => {
    await sdk.execute(Command.SHOW_SNACKBAR, {
      message: t(
        "docspace.error.loading",
        "Error loading ONLYOFFICE DocSpace"
      ),
    });
    delete window.DocSpace;
    setLoading(false);
  };

  const onLoadComponentError = async () => {
    await sdk.execute(Command.SHOW_SNACKBAR, {
      message: t(
        "docspace.error.unreached",
        "ONLYOFFICE DocSpace cannot be reached"
      ),
    });
    setLoading(false);
  };

  return (
    <>
      {loading && user?.docspaceSettings.url && (
        <>
          <div className="h-full w-full flex justify-center items-center">
            <OnlyofficeSpinner />
          </div>
          <div hidden>
            <DocSpace
              url={user?.docspaceSettings.url}
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
        </>
      )}
      {!loading && (!user?.docspaceSettings || !user?.docspaceSettings.url) && (
        <OnlyofficeBackgroundError
          Icon={<CommonError />}
          title={t("background.error.title", "Error")}
          subtitle={
              `${t("background.error.subtitle.docspace-connection", "You are not connected to ONLYOFFICE DocSpace app.")} 
                ${(user?.is_admin && user.access.find((a) => a.app === "global" && a.admin))
                  ? t("background.error.hint.admin.docspace-connection", "Please, go to the Connection Setting to configure ONLYOFFICE DocSpace app settings.")
                  : t("background.error.hint.docspace-connection", "Please contact the administrator.")
                }`
          }
        />
      )}
      {!loading && user?.docspaceSettings && user?.docspaceSettings.url && (
        <>
          <div className="flex flex-col items-start pl-5 pr-5 pt-5 pb-3">
            <div className="pb-2">
              <OnlyofficeTitle
                text={t("settings.authorization.title", "Login to ONLYOFFICE DocSpace account")}
              />
            </div>
          </div>
          {user?.docspaceAccount && (
            <>
              <div 
                className="inline-flex pl-5 pr-5"
              >
                <Authorized />
                <span
                  className="pl-3"
                >
                  {t("settings.authorization.status.authorized", "You have successfully logged in to your ONLYOFFICE DocSpace account")}
                </span>
              </div>
              <div className="flex justify-start items-center mt-4 ml-5">
                <OnlyofficeButton
                  text={t("button.logout", "Logout")}
                  primary
                  onClick={handleLogout}
                />
              </div>
            </>
          )}
          {!user?.docspaceAccount && (
            <div className="max-w-[320px]">
              <div className="pl-5 pr-5 pb-2">
                <OnlyofficeInput
                  text={t("settings.authorization.inputs.email", "Email")}
                  valid={showValidationMessage ? !!email : true}
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                />
              </div>
              <div className="pl-5 pr-5">
                <OnlyofficeInput
                  text={t("settings.authorization.inputs.password", "Password")}
                  valid={showValidationMessage ? !!password : true}
                  value={password}
                  type="password"
                  onChange={(e) => setPassword(e.target.value)}
                />
              </div>
              <div className="flex justify-start items-center mt-4 ml-5">
                <OnlyofficeButton
                  text={t("button.login", "Login")}
                  primary
                  onClick={handleLogin}
                />
              </div>
            </div>
          )}
        </>
      )}
    </>
  );
};
