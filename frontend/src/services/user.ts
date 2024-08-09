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

import axios from "axios";
import axiosRetry from "axios-retry";
import AppExtensionsSDK, { Command } from "@pipedrive/app-extensions-sdk";

import { UserResponse } from "src/types/user";

export const getUser = async (sdk: AppExtensionsSDK) => {
  const pctx = await sdk.execute(Command.GET_SIGNED_TOKEN);
  const client = axios.create({ baseURL: process.env.BACKEND_URL });
  axiosRetry(client, {
    retries: 2,
    retryCondition: (error) => error.status !== 200,
    retryDelay: (count) => count * 50,
    shouldResetTimeout: true,
  });

  const response = await client<UserResponse>({
    method: "GET",
    url: `/api/v1/user`,
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${pctx.token}`,
    },
    timeout: 15000,
  });

  return response.data;
};

export const putDocspaceAccount = async (
  sdk: AppExtensionsSDK,
  userName: string,
  passwordHash: string,
  system: boolean
) => {
  const pctx = await sdk.execute(Command.GET_SIGNED_TOKEN);
  const client = axios.create({ baseURL: process.env.BACKEND_URL });
  axiosRetry(client, {
    retries: 2,
    retryCondition: (error) => error.status === 429,
  });

  await client({
    method: "PUT",
    url: `/api/v1/user/docspace-account?system=${system}`,
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${pctx.token}`,
    },
    data: {
      userName,
      passwordHash,
    },
    timeout: 10000,
  });
};

export const deleteDocspaceAccount = async (sdk: AppExtensionsSDK) => {
  const pctx = await sdk.execute(Command.GET_SIGNED_TOKEN);
  const client = axios.create({ baseURL: process.env.BACKEND_URL });
  axiosRetry(client, {
    retries: 2,
    retryCondition: (error) => error.status === 429,
  });

  await client({
    method: "DELETE",
    url: `/api/v1/user/docspace-account`,
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${pctx.token}`,
    },
    timeout: 4000,
  });
};
