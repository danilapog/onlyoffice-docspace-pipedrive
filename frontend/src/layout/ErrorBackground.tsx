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

import React from "react";

import { ButtonColor, OnlyofficeButton, ButtonProps } from "@components/button";
import { OnlyofficeSubtitle } from "@components/title";
import { OnlyofficeError } from "@components/error/Error";

import DocspaceLogo from "@assets/docspace-logo.svg";
import DocspaceLogoDark from "@assets/docspace-logo-dark.svg";

export type ErrorProps = {
  Icon: JSX.Element | JSX.Element[];
  title: string;
  subtitle: string;
  button?: ButtonProps;
};

export const OnlyofficeBackgroundError: React.FC<ErrorProps> = ({
  Icon,
  title,
  subtitle,
  button,
}) => (
  <div className="w-full h-full flex justify-center flex-col items-center">
    <div className="max-w-[526px] flex justify-between flex-col items-center">
      <div className="hidden sm:flex justify-center items-center mb-8">
        <DocspaceLogo className="dark:hidden" />
        <DocspaceLogoDark className="hidden dark:block" />
      </div>
      <div className="max-w-[314px] md:max-w-[470px]">
        <div className="flex justify-center items-center min-h-40">{Icon}</div>
        <div className="flex flex-col justify-center items-center">
          <div className="flex flex-col gap-2">
            <OnlyofficeError text={title} />
            <OnlyofficeSubtitle text={subtitle} />
          </div>
          {button && (
            <div className="pt-4 z-[100]">
              <OnlyofficeButton color={ButtonColor.PRIMARY} {...button} />
            </div>
          )}
        </div>
      </div>
    </div>
  </div>
);
