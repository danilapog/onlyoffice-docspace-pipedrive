package com.onlyoffice.docspacepipedrive.web.dto.fields;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class FieldsDataResponse {
    private List<FieldData> fieldsData;
}
