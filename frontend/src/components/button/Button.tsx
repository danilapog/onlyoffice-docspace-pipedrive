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

import React from "react";
import cx from "classnames";

export enum ButtonType {
  Normal,
  Primary,
  Danger,
}

type ButtonProps = {
  text: string;
  disabled?: boolean;
  type?: ButtonType;
  fullWidth?: boolean;
  Icon?: React.ReactElement;
  onClick?: React.MouseEventHandler<HTMLButtonElement>;
};

export const OnlyofficeButton: React.FC<ButtonProps> = ({
  text,
  disabled = false,
  type = ButtonType.Normal,
  fullWidth = false,
  Icon,
  onClick,
}) => {
  const classes = cx({
    "hover:shadow-lg duration-200": !disabled,
    "bg-pipedrive-color-dark-green-600 hover:bg-pipedrive-color-light-green-700 dark:hover:bg-pipedrive-color-dark-green-700 text-white":
      type === ButtonType.Primary,
    "bg-pipedrive-color-dark-red-600 hover:bg-pipedrive-color-light-red-700 dark:hover:bg-pipedrive-color-dark-red-700 text-white":
      type === ButtonType.Danger,
    "bg-white text-black dark:bg-pipedrive-color-dark-neutral-100 dark:hover:bg-pipedrive-color-dark-neutral-200 dark:text-pipedrive-color-dark-neutral-1000 border dark:border-pipedrive-color-dark-divider-strong border-solid":
      type === ButtonType.Normal,
    "min-w-[62px] h-[32px]": true,
    "w-full": fullWidth,
    "bg-opacity-50 cursor-not-allowed": disabled,
  });

  return (
    <button
      type="button"
      disabled={disabled}
      className={`flex justify-center items-center p-3 tracking-wide text-base font-bold rounded-md cursor-pointer ${classes} truncate text-ellipsis`}
      onClick={onClick}
    >
      {text}
      {Icon ? <div className="pl-1">{Icon}</div> : null}
    </button>
  );
};
