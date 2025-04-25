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

import axios from "axios";
import axiosRetry from "axios-retry";

import { UserResponse } from "src/types/user";
import { PipedriveToken } from "@context/PipedriveToken";

export const getUser = async (pipedriveToken: PipedriveToken) => {
  const token = await pipedriveToken.getValue();
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
      Authorization: `Bearer ${token}`,
    },
    timeout: 15000,
  });

  return response.data;
};

export const putDocspaceAccount = async (
  pipedriveToken: PipedriveToken,
  userName: string,
  passwordHash: string,
) => {
  const token = await pipedriveToken.getValue();
  const client = axios.create({ baseURL: process.env.BACKEND_URL });
  axiosRetry(client, {
    retries: 1,
    retryCondition: (error) => error.status === 429,
    retryDelay: (count) => count * 50,
    shouldResetTimeout: true,
  });

  await client({
    method: "PUT",
    url: `/api/v1/user/docspace-account`,
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    data: {
      userName,
      passwordHash,
    },
    timeout: 20000,
  });
};

export const deleteDocspaceAccount = async (pipedriveToken: PipedriveToken) => {
  const token = await pipedriveToken.getValue();
  const client = axios.create({ baseURL: process.env.BACKEND_URL });
  axiosRetry(client, {
    retries: 1,
    retryCondition: (error) => error.status === 429,
    retryDelay: (count) => count * 50,
    shouldResetTimeout: true,
  });

  await client({
    method: "DELETE",
    url: `/api/v1/user/docspace-account`,
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    timeout: 10000,
  });
};
