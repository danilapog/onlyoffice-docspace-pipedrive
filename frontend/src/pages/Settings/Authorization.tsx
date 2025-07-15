import React, { useContext, useState } from "react";
import { useTranslation } from "react-i18next";
import { Command, View } from "@pipedrive/app-extensions-sdk";
import { DocSpace } from "@onlyoffice/docspace-react";
import { TFrameConfig } from "@onlyoffice/docspace-sdk-js/dist/types/types";
import { SDKInstance } from "@onlyoffice/docspace-sdk-js/dist/types/instance";

import { ButtonColor, OnlyofficeButton } from "@components/button";
import { OnlyofficeInput } from "@components/input";
import { OnlyofficeTitle } from "@components/title";
import { OnlyofficeBackgroundError } from "@layouts/ErrorBackground";

import { AppContext } from "@context/AppContext";

import { putDocspaceAccount, deleteDocspaceAccount } from "@services/user";

import Authorized from "@assets/authorized.svg";
import NotAvailable from "@assets/not-available.svg";
import Welcome from "@assets/welcome.svg";

import { ErrorResponse } from "src/types/error";

const DOCSPACE_SYSTEM_FRAME_ID = "authorization-docspace-system-frame";

export type AuthorizationSettingProps = {
  showUserGuide(): void;
};

export const AuthorizationSetting: React.FC<AuthorizationSettingProps> = ({
  showUserGuide,
}) => {
  const { t } = useTranslation();
  const {
    user,
    settings,
    setSettings,
    setUser,
    sdk,
    pipedriveToken,
    reloadAppContext,
  } = useContext(AppContext);

  const [saving, setSaving] = useState(false);
  const [deleting, setDeleting] = useState(false);

  const [email, setEmail] = useState<string | undefined>("");
  const [isInvalidEmail, setIsInvalidEmail] = useState(false);
  const [errorTextInvalidEmail, setErrorTextInvalidEmail] = useState("");

  const [password, setPassword] = useState<string | undefined>("");
  const [isInvalidPassword, setIsInvalidPassword] = useState(false);
  const [errorTextInvalidPassword, setErrorTextInvalidPassword] = useState("");

  let docspaceInstance: SDKInstance;

  const handleLogin = async (event: React.SyntheticEvent) => {
    event.preventDefault();

    if (!email || !password) {
      if (!email) {
        setIsInvalidEmail(true);
        setErrorTextInvalidEmail(
          t("error.empty-field", "This field is required"),
        );
      } else {
        setIsInvalidEmail(false);
      }

      if (!password) {
        setIsInvalidPassword(true);
        setErrorTextInvalidPassword(
          t("error.empty-field", "This field is required"),
        );
      } else {
        setIsInvalidPassword(false);
      }

      return;
    }

    setIsInvalidEmail(false);
    setIsInvalidPassword(false);

    setSaving(true);
  };

  const handleLogout = async () => {
    setDeleting(true);
    deleteDocspaceAccount(pipedriveToken)
      .then(async () => {
        setEmail("");
        setPassword("");
        if (user && settings) {
          setUser({ ...user, docspaceAccount: null });
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
        setIsInvalidEmail(true);
        setErrorTextInvalidEmail(
          t("docspace.error.login", "User authentication failed"),
        );

        await sdk.execute(Command.SHOW_SNACKBAR, {
          message: t(
            "settings.authorization.unsuccessful",
            "Invalid authorization credentials. Please check your Email and password and try to log in again.",
          ),
        });
        setSaving(false);
      } else {
        const userInfo = (await docspaceInstance.getUserInfo()) as {
          id: string;
        };

        putDocspaceAccount(pipedriveToken, userInfo.id, email, passwordHash)
          .then(async () => {
            if (user) {
              setUser({
                ...user,
                docspaceAccount: {
                  userName: email,
                  passwordHash: "",
                },
              });
            }

            await sdk.execute(Command.SHOW_SNACKBAR, {
              message: t(
                "settings.authorization.saving.ok",
                "ONLYOFFICE DocSpace authorization has been successfully saved",
              ),
            });
            showUserGuide();
          })
          .catch(async (e) => {
            const data = e?.response?.data as ErrorResponse;
            if (
              e?.response?.status === 503 &&
              data?.cause === "DocspaceUrlNotFoundException"
            ) {
              if (settings) {
                setSettings({
                  ...settings,
                  url: "",
                });
              }
              return;
            }

            if (
              e?.response?.status === 503 &&
              data?.cause === "DocspaceApiKeyNotFoundException"
            ) {
              if (settings) {
                setSettings({
                  ...settings,
                  apiKey: "",
                  isApiKeyValid: false,
                });
              }
              return;
            }

            if (
              e?.response?.status === 503 &&
              data?.cause === "DocspaceApiKeyInvalidException"
            ) {
              if (settings) {
                setSettings({
                  ...settings,
                  isApiKeyValid: false,
                });
              }
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
      {(!settings?.url || !settings?.apiKey) && (
        <OnlyofficeBackgroundError
          Icon={<NotAvailable />}
          title={t("background.error.title.not-available", "Not yet available")}
          subtitle={
            user?.isAdmin
              ? `${t(
                  "background.error.subtitle.docspace-connection",
                  "You are not connected to ONLYOFFICE DocSpace.",
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
      {settings?.apiKey && !settings?.isApiKeyValid && (
        <OnlyofficeBackgroundError
          Icon={<NotAvailable />}
          title={t("background.error.title.not-available", "Not yet available")}
          subtitle={`${t(
            "background.error.title.docspace-invalid-api-key",
            "The ONLYOFFICE DocSpace API Key is invalid.",
          )} ${
            user?.isAdmin
              ? t(
                  "background.error.hint.admin.docspace-connection",
                  "Please go to the Connection Setting to configure ONLYOFFICE DocSpace app settings.",
                )
              : t(
                  "background.error.hint.docspace-connection",
                  "Please contact the administrator.",
                )
          }`}
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
      {settings?.url && settings?.apiKey && settings?.isApiKeyValid && (
        <>
          <div className="flex flex-col items-start pl-5 pr-5 pt-5 pb-3">
            <div className="pb-2">
              <OnlyofficeTitle
                text={t(
                  "settings.authorization.title",
                  "Log into your connected ONLYOFFICE DocSpace to start using it within Pipedrive",
                )}
              />
            </div>
            {!user?.docspaceAccount && (
              <div className="pt-3 pb-2">
                {t(
                  "settings.authorization.subtitle.address",
                  "Your connected DocSpace address is",
                )}{" "}
                <span className="font-semibold text-pipedrive-color-light-green-600 dark:text-pipedrive-color-dark-green-600">
                  {settings.url}
                </span>
              </div>
            )}
          </div>
          {!user?.docspaceAccount && (
            <div className="max-w-[390px]">
              <form onSubmit={handleLogin}>
                <div className="pl-5 pr-5 pb-2">
                  <OnlyofficeInput
                    text={t("settings.authorization.inputs.email", "Email")}
                    placeholder={t(
                      "settings.authorization.inputs.email",
                      "Email",
                    )}
                    required
                    valid={!isInvalidEmail}
                    errorText={errorTextInvalidEmail}
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
                    placeholder={t(
                      "settings.authorization.inputs.password",
                      "Password",
                    )}
                    required
                    valid={!isInvalidPassword}
                    errorText={errorTextInvalidPassword}
                    value={password}
                    type="password"
                    disabled={saving}
                    link={{
                      text: t("button.forgot-password", "Forgot password?"),
                      href: `${settings.url}/login`,
                    }}
                    onChange={(e) => setPassword(e.target.value)}
                  />
                </div>
                <div className="flex justify-start items-center mt-4 ml-5">
                  <OnlyofficeButton
                    text={t("button.login", "Log in")}
                    type="submit"
                    color={ButtonColor.PRIMARY}
                    loading={saving}
                    onClick={handleLogin}
                  />
                </div>
              </form>
            </div>
          )}
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
              <div className="pt-4">
                <OnlyofficeBackgroundError
                  Icon={<Welcome />}
                  title={t(
                    "settings.authorization.welcome.title",
                    "Welcome to DocSpace!",
                  )}
                  options={[
                    t(
                      "settings.authorization.welcome.option1",
                      "Create a room in the deal",
                    ),
                    t(
                      "settings.authorization.welcome.option2",
                      "Upload a document to the room",
                    ),
                  ]}
                  button={{
                    text: t("button.deals", "Go to Deals"),
                    onClick: async () => {
                      await sdk.execute(Command.REDIRECT_TO, {
                        view: View.DEALS,
                      });
                    },
                  }}
                  link={{
                    text: t(
                      "settings.authorization.welcome.open-guide",
                      "Open Guide",
                    ),
                    onClick: showUserGuide,
                  }}
                />
              </div>
            </>
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
