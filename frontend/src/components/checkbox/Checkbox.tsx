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
    "select-auto": true,
    "bg-slate-200": disabled,
  });

  return (
    <div>
      <input
        checked={checked}
        className={istyle}
        type="checkbox"
        onChange={onChange}
        disabled={disabled}
      />
      <label className={`font-semibold text-${labelSize} text-gray-700 py-2 ml-1`}>
        {text}
      </label>
    </div>
  );
};
