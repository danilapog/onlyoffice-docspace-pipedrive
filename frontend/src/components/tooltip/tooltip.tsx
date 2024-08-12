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
import Info from "@assets/info.svg";

type TooltipProps = {
  text: string;
};

export const OnlyofficeTooltip: React.FC<TooltipProps> = ({ text }) => (
  <div className="group relative flex flex-col max-w-max p-2 items-center justify-center cursor-pointer">
    <Info />
    <div
      className={`absolute left-3/4 w-max max-w-80 transform invisible
            rounded-md border shadow-md bg-white transition-all
            duration-200 group-hover:translate-x-1 group-hover:visible`}
    >
      <div className="w-full p-4">{text}</div>
    </div>
  </div>
);
