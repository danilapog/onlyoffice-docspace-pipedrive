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
import { CSPSettings, DocspaceResponse } from "src/types/docspace";

export const getCSPSettings = async (url: string) => {
  const client = axios.create({ baseURL: url });
  axiosRetry(client, {
    retries: 2,
    retryCondition: (error) => error.status !== 200,
    retryDelay: (count) => count * 50,
    shouldResetTimeout: true,
  });

  const response = await client<DocspaceResponse<CSPSettings>>({
    method: "GET",
    url: `/api/2.0/security/csp`,
    headers: {
      "Content-Type": "application/json",
    },
    timeout: 10000,
  });

  return response.data.response;
};
