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

/* eslint-disable jsx-a11y/label-has-associated-control */
import React from "react";
import cx from "classnames";

type CheckboxProps = {
  text: string;
  checked?: boolean;
  disabled?: boolean;
  labelSize?: "sm" | "xs";
  onChange?: React.ChangeEventHandler<HTMLInputElement>;
};

export const OnlyofficeCheckbox: React.FC<CheckboxProps> = ({
  text,
  checked,
  disabled = false,
  labelSize = "xs",
  onChange,
}) => {
  const istyle = cx({
    "relative w-8 h-4 shrink-0 rounded-full": true,
    "peer peer-checked:after:translate-x-4": true,
    "rtl:peer-checked:after:-translate-x-full": true,
    "after:absolute after:top-[2px] after:start-[2px]": true,
    "after:rounded-full after:h-3 after:w-3 after:transition-all": true,
    "bg-zinc-300": !disabled,
    "bg-zinc-300/50": disabled,
    "after:bg-white": !disabled,
    "after:bg-white/50": disabled,
    "peer-checked:bg-green-700": !disabled,
    "peer-checked:bg-green-700/50": disabled,
    "cursor-not-allowed": disabled,
  });

  return (
    <label
      className={`inline-flex items-center ${
        disabled ? "cursor-not-allowed" : "cursor-pointer"
      } `}
    >
      <input
        type="checkbox"
        className="sr-only peer"
        checked={checked}
        onChange={onChange}
        disabled={disabled}
      />
      <div className={istyle} />
      <span className={`font-semibold text-${labelSize} py-2 ml-2`}>
        {text}
      </span>
    </label>
  );
};
