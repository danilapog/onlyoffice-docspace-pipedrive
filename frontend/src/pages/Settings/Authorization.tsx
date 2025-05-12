import React, { useContext, useState } from "react";
import { useTranslation, Trans } from "react-i18next";
import { Command } from "@pipedrive/app-extensions-sdk";
import { DocSpace } from "@onlyoffice/docspace-react";
import { TFrameConfig } from "@onlyoffice/docspace-sdk-js/dist/types/types";
import { SDKInstance } from "@onlyoffice/docspace-sdk-js/dist/types/instance";

import { ButtonColor, OnlyofficeButton } from "@components/button";
import { OnlyofficeInput } from "@components/input";
import { OnlyofficeTitle } from "@components/title";
import { OnlyofficeBackgroundError } from "@layouts/ErrorBackground";

import { AppContext, AppErrorType } from "@context/AppContext";

import { putDocspaceAccount, deleteDocspaceAccount } from "@services/user";

import Authorized from "@assets/authorized.svg";
import NotAvailable from "@assets/not-available.svg";
import { OnlyofficeCheckbox } from "@components/checkbox";
import { OnlyofficeTooltip } from "@components/tooltip";
import { OnlyofficeHint } from "@components/hint";

const DOCSPACE_SYSTEM_FRAME_ID = "authorization-docspace-system-frame";

export const AuthorizationSetting: React.FC = () => {
  const { t } = useTranslation();
  const {
    user,
    settings,
    setUser,
    setSettings,
    setAppError,
    sdk,
    pipedriveToken,
    reloadAppContext,
  } = useContext(AppContext);

  const [saving, setSaving] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [showValidationMessage, setShowValidationMessage] = useState(false);
  const [email, setEmail] = useState<string | undefined>("");
  const [password, setPassword] = useState<string | undefined>("");
  const [isSystem, setIsSystem] = useState<boolean>(!settings?.existSystemUser);

  let docspaceInstance: SDKInstance;

  const handleLogin = async (event: React.SyntheticEvent) => {
    event.preventDefault();
    if (email && password) {
      if (isSystem) {
        const { confirmed } = await sdk.execute(Command.SHOW_CONFIRMATION, {
          title: t("button.login", "Login"),
          description:
            t(
              "settings.authorization.login.system.confirm",
              "Do you agree to connect your DocSpace account? The app will use it to perform actions.",
            ) || "",
        });
        if (!confirmed) {
          return;
        }
      }

      setSaving(true);
    } else {
      setShowValidationMessage(true);
    }
  };

  const handleLogout = async () => {
    let executelogout = true;
    if (user?.isSystem) {
      const { confirmed } = await sdk.execute(Command.SHOW_CONFIRMATION, {
        title: t("button.logout", "Log out"),
        description:
          t(
            "settings.authorization.deleting.system.confirm",
            `Are you sure you want to log out? You are a System User and this action will
          result in the ONLYOFFICE DocSpace functionality being limited.`,
          ) || "",
      });
      executelogout = confirmed;
    }

    if (executelogout) {
      setDeleting(true);
      deleteDocspaceAccount(pipedriveToken)
        .then(async () => {
          setEmail("");
          setPassword("");
          setIsSystem(!!user?.isSystem || !settings?.existSystemUser);
          setShowValidationMessage(false);
          if (user && settings) {
            if (user?.isSystem) {
              setSettings({ ...settings, existSystemUser: false });
            }
            setUser({ ...user, docspaceAccount: null, isSystem: false });

            if (!user.isAdmin && !settings?.existSystemUser) {
              setAppError(AppErrorType.PLUGIN_NOT_AVAILABLE);
            }
          }
          await sdk.execute(Command.SHOW_SNACKBAR, {
            message: t(
              "settings.authorization.deleting.ok",
              "ONLYOFFICE DocSpace authorization has been successfully deleted",
            ),
          });
        })
        .catch(async () => {
          await sdk.execute(Command.SHOW_SNACKBAR, {
            message: t(
              "error.common",
              "Something went wrong. Please reload the app.",
            ),
          });
        })
        .finally(() => setDeleting(false));
    }
  };

  const onAppReady = async () => {
    if (email && password && docspaceInstance) {
      const loginTimeout = setTimeout(async () => {
        await sdk.execute(Command.SHOW_SNACKBAR, {
          message: `${t("docspace.error.login", "User authentication failed")} (Timeout)`,
        });
        setSaving(false);
      }, 15000);

      const hashSettings = await docspaceInstance.getHashSettings();
      const passwordHash = (await docspaceInstance.createHash(
        password,
        hashSettings,
      )) as unknown as string;

      const login = (await docspaceInstance.login(email, passwordHash)) as {
        status: number;
      };

      clearTimeout(loginTimeout);

      if (login.status && login.status !== 200) {
        await sdk.execute(Command.SHOW_SNACKBAR, {
          message: t("docspace.error.login", "User authentication failed"),
        });
        setSaving(false);
      } else {
        putDocspaceAccount(pipedriveToken, email, passwordHash, isSystem)
          .then(async () => {
            if (user) {
              setUser({
                ...user,
                docspaceAccount: {
                  userName: email,
                  passwordHash: "",
                  canCreateRoom: false,
                },
                isSystem,
              });
            }

            await sdk.execute(Command.SHOW_SNACKBAR, {
              message: t(
                "settings.authorization.saving.ok",
                "ONLYOFFICE DocSpace authorization has been successfully saved",
              ),
            });
          })
          .catch(async (e) => {
            if (
              e.response?.status === 403 &&
              e.response?.data?.provider === "DOCSPACE"
            ) {
              let message = t(
                "settings.connection.saving.error.forbidden.guest",
                "The specified user should be not Guest",
              );

              if (isSystem) {
                message = t(
                  "settings.connection.saving.error.forbidden.not-admin",
                  "The specified user is not a ONLYOFFICE DocSpace administrator",
                );
              }

              await sdk.execute(Command.SHOW_SNACKBAR, {
                message,
              });
              return;
            }

            await sdk.execute(Command.SHOW_SNACKBAR, {
              message: t(
                "settings.authorization.saving.error",
                "Could not save ONLYOFFICE DocSpace authorization",
              ),
            });
          })
          .finally(() => setSaving(false));
      }
    }
  };

  const onAppError = async () => {
    await sdk.execute(Command.SHOW_SNACKBAR, {
      message: t("docspace.error.loading", "Error loading ONLYOFFICE DocSpace"),
    });

    if (docspaceInstance) {
      docspaceInstance.destroyFrame();
    }

    setSaving(false);
  };

  return (
    <>
      {!settings?.url && (
        <OnlyofficeBackgroundError
          Icon={<NotAvailable />}
          title={t("background.error.title.not-available", "Not yet available")}
          subtitle={
            user?.isAdmin
              ? `${t(
                  "background.error.subtitle.docspace-connection",
                  "You are not connected to ONLYOFFICE DocSpace",
                )} ${t(
                  "background.error.hint.admin.docspace-connection",
                  "Please go to the Connection Setting to configure ONLYOFFICE DocSpace app settings.",
                )}`
              : `${t(
                  "background.error.subtitle.plugin.not-active.message",
                  "ONLYOFFICE DocSpace App is not yet available.",
                )} ${t(
                  "background.error.subtitle.plugin.not-active.help",
                  "Please wait until a Pipedrive Administrator configures the app settings.",
                )}`
          }
          button={
            !user?.isAdmin
              ? {
                  text: t("button.reload", "Reload"),
                  onClick: () => reloadAppContext(),
                }
              : undefined
          }
        />
      )}
      {settings?.url && (
        <>
          <div className="flex flex-col items-start pl-5 pr-5 pt-5 pb-3">
            <div className="pb-2">
              <OnlyofficeTitle
                text={t(
                  "settings.authorization.title",
                  "Login to ONLYOFFICE DocSpace account",
                )}
              />
            </div>
            {!user?.docspaceAccount && (
              <OnlyofficeHint>
                <p>
                  <Trans
                    i18nKey="settings.authorization.hint.login.message"
                    defaults="DocSpace users with the <semibold>{{role}}</semibold> type can't log into Pipedrive"
                    values={{
                      role: t("docspace.roles.guest", "Guest"),
                    }}
                    components={{
                      semibold: <span className="font-semibold" />,
                    }}
                  />
                </p>
              </OnlyofficeHint>
            )}
          </div>
          {user?.docspaceAccount && (
            <>
              <div className="inline-flex pl-5 pr-5">
                <div className="p-1">
                  <Authorized />
                </div>
                <span className="pl-3">
                  {t(
                    "settings.authorization.status.authorized",
                    "You have successfully logged in to your ONLYOFFICE DocSpace account",
                  )}
                  {user.isSystem && (
                    <>
                      <br />
                      <Trans
                        i18nKey="settings.authorization.status.system"
                        defaults="Current user is <semibold>System Admin</semibold>"
                        components={{
                          semibold: <span className="font-semibold" />,
                        }}
                      />
                    </>
                  )}
                </span>
              </div>
              <div className="flex justify-start items-center mt-4 ml-5">
                <OnlyofficeButton
                  text={t("button.logout", "Log out")}
                  color={ButtonColor.PRIMARY}
                  loading={deleting}
                  onClick={handleLogout}
                />
              </div>
            </>
          )}
          {!user?.docspaceAccount && (
            <div className="max-w-[320px]">
              <form onSubmit={handleLogin}>
                <div className="pl-5 pr-5 pb-2">
                  <OnlyofficeInput
                    text={t("settings.authorization.inputs.email", "Email")}
                    valid={showValidationMessage ? !!email : true}
                    value={email}
                    disabled={saving}
                    onChange={(e) => setEmail(e.target.value.trim())}
                  />
                </div>
                <div className="pl-5 pr-5 pb-2">
                  <OnlyofficeInput
                    text={t(
                      "settings.authorization.inputs.password",
                      "Password",
                    )}
                    valid={showValidationMessage ? !!password : true}
                    value={password}
                    type="password"
                    disabled={saving}
                    onChange={(e) => setPassword(e.target.value)}
                  />
                </div>
                {user?.isAdmin && (
                  <div className="pl-5 pr-5">
                    <div className="flex">
                      <OnlyofficeCheckbox
                        id="isSystem"
                        checked={isSystem}
                        text={t(
                          "settings.authorization.inputs.system.title",
                          "Use this account with System user (DocSpace Admin role required)",
                        )}
                        disabled={!settings?.existSystemUser}
                        onChange={() => setIsSystem(!isSystem)}
                      />
                      <OnlyofficeTooltip
                        text={t(
                          "settings.authorization.inputs.system.help",
                          "If you click on this switch, the plugin will perform actions from your DocSpace account",
                        )}
                      />
                    </div>
                  </div>
                )}
                <div className="flex justify-start items-center mt-4 ml-5">
                  <OnlyofficeButton
                    text={t("button.login", "Login")}
                    type="submit"
                    color={ButtonColor.PRIMARY}
                    loading={saving}
                    onClick={handleLogin}
                  />
                </div>
              </form>
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
                  onAppReady,
                  onAppError,
                } as unknown,
              } as TFrameConfig
            }
            onSetDocspaceInstance={(instance) => {
              docspaceInstance = instance;
            }}
          />
        </div>
      )}
    </>
  );
};
