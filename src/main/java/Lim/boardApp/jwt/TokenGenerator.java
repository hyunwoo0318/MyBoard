package Lim.boardApp.jwt;

import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.security.config.annotation.rsocket.RSocketSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;

import java.security.Key;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class TokenGenerator {
    /*private byte[] key;
    private Date accessTokenExpire;
    private Date refreshTokenExpire;


    public TokenGenerator(@Value("${jwt.secret}") String secretKey){
        key = Base64Utils.decodeFromString(secretKey);
    }

    public TokenDto generateToken(Authentication authentication) {
        authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        long now = new Date().getTime();
        accessTokenExpire = new Date(now + JwtConst.ACCESS_TOKEN_EXPIRED_TIME);
    }*/
}
