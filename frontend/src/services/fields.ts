/**
 *
 * (c) Copyright Ascensio System SIA 2024
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

import axios, { AxiosResponseTransformer } from "axios";
import axiosRetry from "axios-retry";
import AppExtensionsSDK, { Command } from "@pipedrive/app-extensions-sdk";

import { FieldsResponse, FieldsDataRespose } from "src/types/fields";

export const getFields = async (sdk: AppExtensionsSDK, entityName: string) => {
  const pctx = await sdk.execute(Command.GET_SIGNED_TOKEN);
  const client = axios.create({
    baseURL: process.env.BACKEND_URL,
    transformResponse: [
      (data: any) => {
        try {
          const parsedData = JSON.parse(data);
          return toCamelCase(parsedData);
        } catch (error) {
          return data;
        }
      },
    ],
  });
  // axiosRetry(client, {
  //   retries: 2,
  //   retryCondition: (error) => error.status !== 200,
  //   retryDelay: (count) => count * 50,
  //   shouldResetTimeout: true,
  // });

  const response = await client<FieldsResponse>({
    method: "GET",
    url: `/api/v1/fields/${entityName}`,
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${pctx.token}`,
    },
    timeout: 10000,
  });

  return response.data;
};

export const getFieldsData = async (sdk: AppExtensionsSDK, dealId: number) => {
  const pctx = await sdk.execute(Command.GET_SIGNED_TOKEN);
  const client = axios.create({
    baseURL: process.env.BACKEND_URL
  });
  // axiosRetry(client, {
  //   retries: 2,
  //   retryCondition: (error) => error.status !== 200,
  //   retryDelay: (count) => count * 50,
  //   shouldResetTimeout: true,
  // });

  const response = await client<FieldsDataRespose>({
    method: "GET",
    url: `/api/v1/fields/values/${dealId}`,
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${pctx.token}`,
    },
    timeout: 10000,
  });

  return response.data;
};

const toCamelCase = (obj: any): any => {
  if (Array.isArray(obj)) {
    return obj.map((v) => toCamelCase(v));
  } else if (obj !== null && obj.constructor === Object) {
    return Object.keys(obj).reduce((result: any, key: string) => {
      const camelKey = key.replace(/_([a-z])/g, (_, letter) => letter.toUpperCase());
      result[camelKey] = toCamelCase(obj[key]);
      return result;
    }, {});
  }
  return obj;
};