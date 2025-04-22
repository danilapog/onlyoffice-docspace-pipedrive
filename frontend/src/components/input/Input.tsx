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

/* eslint-disable jsx-a11y/label-has-associated-control */
import React from "react";
import cx from "classnames";

type InputProps = {
  text: string;
  value?: string;
  description?: string;
  placeholder?: string;
  type?: "text" | "password";
  errorText?: string;
  valid?: boolean;
  disabled?: boolean;
  autocomplete?: boolean;
  onChange?: React.ChangeEventHandler<HTMLInputElement>;
};

export const OnlyofficeInput: React.FC<InputProps> = ({
  text,
  value,
  description,
  placeholder,
  type = "text",
  errorText = "Please fill out this field",
  valid = true,
  disabled = false,
  autocomplete = false,
  onChange,
}) => {
  const istyle = cx({
    "appearance-none block select-auto h-8 mt-1 px-2 py-1": true,
    "dark:bg-pipedrive-color-dark-neutral-100": true,
    "w-full border rounded": true,
    "border-pipedrive-color-light-divider-strong dark:border-pipedrive-color-dark-divider-strong":
      valid,
    "border-red-600": !valid,
    "text-pipedrive-color-light-neutral-500 dark:text-pipedrive-color-dark-neutral-500 cursor-not-allowed":
      disabled,
  });

  const pstyle = cx({
    hidden: valid,
  });

  return (
    <div>
      <label className="py-2">{text}</label>
      <input
        value={value}
        placeholder={placeholder}
        className={istyle}
        required
        autoCorrect={autocomplete ? undefined : "off"}
        autoComplete={autocomplete ? undefined : "off"}
        type={type}
        onChange={onChange}
        disabled={disabled}
      />
      <p className={`text-red-600 text-xs ${pstyle}`}>{errorText}</p>
      <div className="mt-1 text-xs text-pipedrive-color-light-neutral-700 dark:text-pipedrive-color-dark-neutral-700">
        {description}
      </div>
    </div>
  );
};
