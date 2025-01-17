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
import React, { useState } from "react";

type SnackbarProps = {
  header: string;
  text: string;
  isClosed?: boolean;
};

export const OnlyofficeSnackbar: React.FC<SnackbarProps> = ({
  header,
  text,
  isClosed = true,
}) => {
  const [show, setShow] = useState(true);

  return (
    <div>
      {show && (
        <div className="flex w-full px-5 py-3 bg-amber-100 font-normal text-xs items-start">
          <div className="flex flex-col w-full gap-2">
            <div className="font-semibold leading-3">{header}</div>
            <div className="leading-3">{text}</div>
          </div>
          {isClosed && (
            <button
              type="button"
              className="inline-block"
              aria-label="Close"
              onClick={() => setShow(false)}
            >
              <svg
                width="12"
                height="12"
                viewBox="0 0 12 12"
                fill="none"
                xmlns="http://www.w3.org/2000/svg"
                className="sc-fvwjDU hmHauk"
              >
                <path
                  fillRule="evenodd"
                  clipRule="evenodd"
                  d="M7.763 6.359a.5.5 0 010-.707l3.186-3.187a.5.5 0 000-.707l-.707-.707a.5.5 0 00-.707 0L6.349 4.237a.5.5 0 01-.707 0L2.46 1.056a.5.5 0 00-.707 0l-.707.707a.5.5 0 000 .707l3.182 3.182a.5.5 0 010 .707L1.05 9.536a.5.5 0 000 .707l.707.708a.5.5 0 00.707 0l3.178-3.178a.5.5 0 01.707 0l3.182 3.182a.5.5 0 00.707 0l.707-.707a.5.5 0 000-.707L7.763 6.36z"
                  fill="#657077"
                />
              </svg>
            </button>
          )}
        </div>
      )}
    </div>
  );
};
