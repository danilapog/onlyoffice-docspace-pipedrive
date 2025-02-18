package com.onlyoffice.docspacepipedrive.web.dto.fields;

import com.onlyoffice.docspacepipedrive.client.pipedrive.dto.PipedriveField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class FieldsResponse {
    private List<PipedriveField> fields;
}
