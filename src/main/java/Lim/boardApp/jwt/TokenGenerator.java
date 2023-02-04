package Lim.boardApp.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
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
    private Key key;
    private Date tokenExpire;


    public TokenGenerator(@Value("${jwt.secret}") String secretKey){
        byte[] bytes = Base64Utils.decodeFromString(secretKey);
        this.key = Keys.hmacShaKeyFor(bytes);
    }

    public TokenDto generateToken(Authentication authentication, String id) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        long now = new Date().getTime();
        tokenExpire = new Date(now + JwtConst.TOKEN_EXPIRED_TIME);

        String accessToken = Jwts.builder()
                .setSubject(authentication.getName())
                .claim("auth", authorities) // claim -> jwt body custom claim
                .setAudience(id)
                .setIssuer(JwtConst.TOKEN_ISSUER)
                .setExpiration(tokenExpire) // 일반적인 claim에 대해서는 setter를 제공함
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        String refreshToken = Jwts.builder()
                .setExpiration(tokenExpire) // 표준에서 refresh token과 access token을 유효기간을 같이 설정하도록 권고
                .signWith(key,SignatureAlgorithm.HS256)
                .compact();

        return TokenDto.builder()
                .type("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
