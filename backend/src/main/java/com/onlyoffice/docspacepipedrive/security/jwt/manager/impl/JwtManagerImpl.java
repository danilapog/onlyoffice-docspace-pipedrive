package com.onlyoffice.docspacepipedrive.security.jwt.manager.impl;

import com.onlyoffice.docspacepipedrive.security.jwt.manager.JwtManager;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class JwtManagerImpl implements JwtManager {

    public Map<String,Object> getBody(String key, String token) {
        return Jwts.parser().setSigningKey(key.getBytes())
                .parseClaimsJws(token).getBody();
    }
}
