package com.onlyoffice.docspacepipedrive.constants;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface PipedriveFieldsModel {
    public static final Map<String, List<String>> PIPEDRIVE_FIELDS_KEYS = new HashMap<>(){{
        put("deal", Arrays.asList(
                ""
        ));

        put("person", Arrays.asList(
                "id",
                "name",
                "phone",
                "email"

        ));

        put("organization", Arrays.asList(
                ""
        ));

        put("product", Arrays.asList(

        ));
    }};
}
