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
import AppExtensionsSDK, { Command } from "@pipedrive/app-extensions-sdk";

import { RoomResponse } from "src/types/room";

export const getRoom = async (sdk: AppExtensionsSDK, dealId: number) => {
  const pctx = await sdk.execute(Command.GET_SIGNED_TOKEN);
  const client = axios.create({ baseURL: process.env.BACKEND_URL });
  axiosRetry(client as any, {
    retries: 2,
    retryCondition: (error) => error.status !== 200,
    retryDelay: (count) => count * 50,
    shouldResetTimeout: true,
  });

  const response = await client<RoomResponse>({
    method: "GET",
    url: `/api/v1/room/${dealId}`,
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${pctx.token}`,
    },
    timeout: 10000,
  });

  return response.data;
};

export const createRoom = async (
  sdk: AppExtensionsSDK,
  dealId: number,
  roomType: number,
) => {
  const pctx = await sdk.execute(Command.GET_SIGNED_TOKEN);
  const client = axios.create({ baseURL: process.env.BACKEND_URL });

  const response = await client<RoomResponse>({
    method: "POST",
    url: `/api/v1/room/${dealId}`,
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${pctx.token}`,
    },
    data: {
      roomType,
    },
    timeout: 30000,
  });

  return response.data;
};

export const requestAccessToRoom = async (
  sdk: AppExtensionsSDK,
  dealId: number,
) => {
  const pctx = await sdk.execute(Command.GET_SIGNED_TOKEN);
  const client = axios.create({ baseURL: process.env.BACKEND_URL });
  axiosRetry(client as any, {
    retries: 1,
    retryCondition: (error) => error.status === 429,
    retryDelay: (count) => count * 50,
    shouldResetTimeout: true,
  });

  const response = await client<RoomResponse>({
    method: "POST",
    url: `/api/v1/room/${dealId}/request-access`,
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${pctx.token}`,
    },
    timeout: 15000,
  });

  return response.data;
};
