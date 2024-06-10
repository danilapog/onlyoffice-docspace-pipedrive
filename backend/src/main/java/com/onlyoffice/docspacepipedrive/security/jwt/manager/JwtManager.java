package com.onlyoffice.docspacepipedrive.security.jwt.manager;

import java.util.Map;

public interface JwtManager {
    Map<String,Object> getBody(String key, String token);
}
