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

package com.onlyoffice.docspacepipedrive.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlyoffice.docspacepipedrive.client.pipedrive.PipedriveClient;
import com.onlyoffice.docspacepipedrive.client.pipedrive.dto.PipedriveField;
import com.onlyoffice.docspacepipedrive.entity.Client;

import com.onlyoffice.docspacepipedrive.web.dto.fields.FieldData;
import com.onlyoffice.docspacepipedrive.web.dto.fields.FieldsDataResponse;
import com.onlyoffice.docspacepipedrive.web.dto.fields.FieldsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/v1/fields")
@RequiredArgsConstructor
@Slf4j
public class FieldsController {
    private static final String[] TYPES = {"int", "varchar", "monetary"};

    private static final Map<String, List<String>> FIELDS_TYPES_FOR_REPLACE = new HashMap<>() {{
       put("user", Arrays.asList("name", "email"));
    }};

    private final PipedriveClient pipedriveClient;

    @GetMapping("/{entityName}")
    public ResponseEntity<FieldsResponse> getPipedriveFields(
            @AuthenticationPrincipal(expression = "client") Client currentClient,
            @PathVariable String entityName
    ) {

        List<PipedriveField> pipedriveFields = pipedriveClient.getFields(entityName);

        pipedriveFields = replaceFields(pipedriveFields);

        return ResponseEntity.ok(
                new FieldsResponse(
                        pipedriveFields
                )
        );
    }


    private List<PipedriveField> replaceFields(final List<PipedriveField> pipedriveFields) {
        Map<Integer, List<PipedriveField>> pipedriveFieldsForReplaceMap = new HashMap<>();

        for (int i = 0; i < pipedriveFields.size(); i++) {
            PipedriveField pipedriveField = pipedriveFields.get(i);
            if (FIELDS_TYPES_FOR_REPLACE.containsKey(pipedriveField.getFieldType())) {
                List<String> keys = FIELDS_TYPES_FOR_REPLACE.get(pipedriveField.getFieldType());

                List<PipedriveField> pipedriveFieldsForReplace = new ArrayList<>();
                for (String key : keys) {
                    pipedriveFieldsForReplace.add(new PipedriveField(
                            null,
                            pipedriveField.getKey() + "_" + key,
                            pipedriveField.getName() + " (" + key + ")",
                            "varchar"
                    ));
                }
                pipedriveFieldsForReplaceMap.put(i, pipedriveFieldsForReplace);
            }
        }

        int offset = 0;
        for (Map.Entry<Integer, List<PipedriveField>> pipedriveFieldsForReplaceMapEntry
                : pipedriveFieldsForReplaceMap.entrySet()) {
            pipedriveFields.remove(pipedriveFieldsForReplaceMapEntry.getKey().intValue() + offset);
            pipedriveFields.addAll(
                    pipedriveFieldsForReplaceMapEntry.getKey().intValue() + offset,
                    pipedriveFieldsForReplaceMapEntry.getValue()
            );

            offset = offset + pipedriveFieldsForReplaceMapEntry.getValue().size() - 1;
        }

        return pipedriveFields;
    }

    @GetMapping("/values/{dealId}")
    public ResponseEntity<FieldsDataResponse> getPipedriveFieldsValues(
            @AuthenticationPrincipal(expression = "client") Client currentClient,
            @PathVariable Long dealId
    ) {
        Map<String, Object> dealObject = pipedriveClient.getDealAsJson(dealId);

        return ResponseEntity.ok(
                new FieldsDataResponse(convertDealToFieldsData(dealObject, "pipedrive_deal"))

        );
    }

    private List<FieldData> convertDealToFieldsData(Map<String, Object> object, String prefix) {
        List<FieldData> result = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();

        if (object == null) {
            return result;
        }

        for (Map.Entry<String, Object> field : object.entrySet()) {
            String key = prefix.length() > 0 ? prefix + "_" + field.getKey() : field.getKey();
            Object value = field.getValue();

            if (value instanceof List) {
                if (((List<?>) value).size() > 0) {
                    value = ((List<?>) value).get(0);
                } else {
                    value = null;
                }
            }

            if (isPrimitiveOrString(value)) {
                result.add(new FieldData(key, value.toString()));
            } else {
                Map<String, Object> t = objectMapper.convertValue(value, Map.class);
                result.addAll(convertDealToFieldsData(t, key));
            }
        }

        return result;
    }

    private Map<String, Object> covertToSimpleObject(Map<String, Object> object, String prefix) {
        Map<String, Object> result = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();

        if (object == null) {
            return result;
        }

        for (Map.Entry<String, Object> field : object.entrySet()) {
            String key = prefix.length() > 0 ? prefix + "_" + field.getKey() : field.getKey();
            Object value = field.getValue();

            if (value instanceof List) {
                if (((List<?>) value).size() > 0) {
                    value = ((List<?>) value).get(0);
                } else {
                    value = null;
                }
            }

            if (isPrimitiveOrString(value)) {
                result.put(key, value);
            } else {
                Map<String, Object> t = objectMapper.convertValue(value, Map.class);
                result.putAll(covertToSimpleObject(t, key));
            }
        }

        return result;
    }


    private static boolean isPrimitiveOrString(Object obj) {
        return obj instanceof String || obj instanceof Number || obj instanceof Boolean || obj instanceof Character;
    }
}
