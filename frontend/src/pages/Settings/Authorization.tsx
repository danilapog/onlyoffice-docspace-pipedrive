import { useContext, useState } from "react";
import { useTranslation } from "react-i18next";
import { Command } from "@pipedrive/app-extensions-sdk";
import { DocSpace, TFrameConfig } from "@onlyoffice/docspace-react";

import { ButtonType, OnlyofficeButton } from "@components/button";
import { OnlyofficeInput } from "@components/input";
import { OnlyofficeTitle } from "@components/title";
import { OnlyofficeBackgroundError } from "@layouts/ErrorBackground";

import { AppContext } from "@context/AppContext";

import { putDocspaceAccount, deleteDocspaceAccount } from "@services/user";

import Authorized from "@assets/authorized.svg"
import CommonError from "@assets/common-error.svg";
import { OnlyofficeCheckbox } from "@components/checkbox";

const DOCSPACE_SYSTEM_FRAME_ID="docspace-system-frame"

export const AuthorizationSetting: React.FC = () => {
  const { t } = useTranslation();
  const { user, settings, setUser, setSettings, sdk } = useContext(AppContext);

  const [saving, setSaving] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [showValidationMessage, setShowValidationMessage] = useState(false);
  const [email, setEmail] = useState<string | undefined>("");
  const [password, setPassword] = useState<string | undefined>("");
  const [isSystem, setIsSystem] = useState<boolean>(!settings?.existSystemUser);

  const handleLogin = async () => {
    if (email && password) {
      setSaving(true);
    } else {
      setShowValidationMessage(true);
    }
  };

  const handleLogout = async () => {
    setDeleting(true);
    deleteDocspaceAccount(sdk).then(async () => {
      setEmail("");
      setPassword("");
      setIsSystem(!!user?.isSystem || !settings?.existSystemUser);
      setShowValidationMessage(false);
      if (user && settings) {
        if (!!user?.isSystem) {
          setSettings({...settings, existSystemUser: false});
        }
        setUser({...user, docspaceAccount: null, isSystem: false});
      }
      await sdk.execute(Command.SHOW_SNACKBAR, {
        message: t(
          "settings.authorization.deleting.ok",
          "ONLYOFFICE DocSpace authorization has been successfully deleted"
        ),
      });
    })
    .catch(async (e) => {
      await sdk.execute(Command.SHOW_SNACKBAR, {
        message: t(
          "background.error.subtitle.common",
          "Something went wrong. Please reload the app."
        ),
      });
    })
    .finally(() => setDeleting(false));
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
        setSaving(false);
      } else {
        putDocspaceAccount(sdk, email, passwordHash, isSystem).then(async () => {
          if (user) {
            setUser({...user, docspaceAccount: {userName: email, passwordHash: "", canCreateRoom: false}, isSystem: isSystem});
          }

          await sdk.execute(Command.SHOW_SNACKBAR, {
            message: t(
              "settings.authorization.saving.ok",
              "ONLYOFFICE DocSpace authorization has been successfully saved"
            ),
          });
        })
        .catch(async (e)=> {
          if (e.response?.status === 403 && e.response?.data?.provider === "DOCSPACE") {
            await sdk.execute(Command.SHOW_SNACKBAR, {
              message: t(
                "settings.connection.saving.error.forbidden",
                "The specified user is not a ONLYOFFICE DocSpace administrator"
              ),
            });
            return;
          }

          await sdk.execute(Command.SHOW_SNACKBAR, {
            message: t(
              "settings.authorization.saving.error",
              "Could not save ONLYOFFICE DocSpace authorization"
            ),
          });
        })
        .finally(() => setSaving(false));
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
    setSaving(false);
  };

  const onLoadComponentError = async () => {
    await sdk.execute(Command.SHOW_SNACKBAR, {
      message: t(
        "docspace.error.unreached",
        "ONLYOFFICE DocSpace cannot be reached"
      ),
    });
    setSaving(false);
  };

  return (
    <>
      {!settings?.url && (
        <OnlyofficeBackgroundError
          Icon={<CommonError />}
          title={t("background.error.subtitle.docspace-connection", "You are not connected to ONLYOFFICE DocSpace")}
          subtitle={
              `${user?.isAdmin
                  ? t("background.error.hint.admin.docspace-connection", "Please go to the Connection tab to configure the DocSpace app settings.")
                  : t("background.error.hint.docspace-connection", "Please contact the administrator.")
                }`
          }
        />
      )}
      {settings?.url && (
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
                  {user.isSystem && (
                    <>
                      <br/>
                      {t("settings.authorization.status.system", "Current user is System Admin")}
                    </>
                  )}
                </span>
              </div>
              <div className="flex justify-start items-center mt-4 ml-5">
                <OnlyofficeButton
                  text={t("button.logout", "Logout")}
                  type={ButtonType.Primary}
                  disabled={deleting}
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
                  disabled={saving}
                  onChange={(e) => setEmail(e.target.value)}
                />
              </div>
              <div className="pl-5 pr-5 pb-2">
                <OnlyofficeInput
                  text={t("settings.authorization.inputs.password", "Password")}
                  valid={showValidationMessage ? !!password : true}
                  value={password}
                  type="password"
                  disabled={saving}
                  onChange={(e) => setPassword(e.target.value)}
                />
              </div>
              {user?.isAdmin && (
                <div className="pl-5 pr-5">
                  <OnlyofficeCheckbox
                    checked={isSystem}
                    text={t("settings.authorization.inputs.system", "System User (DocSpace administrator role required)")}
                    disabled={!settings?.existSystemUser}
                    onChange={(e) => setIsSystem(!isSystem)}
                  />
                </div>
              )}
              <div className="flex justify-start items-center mt-4 ml-5">
                <OnlyofficeButton
                  text={t("button.login", "Login")}
                  type={ButtonType.Primary}
                  disabled={saving}
                  onClick={handleLogin}
                />
              </div>
            </div>
          )}
        </>
      )}
      {saving && settings?.url && (
        <div hidden>
        <DocSpace
          url={settings.url}
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
