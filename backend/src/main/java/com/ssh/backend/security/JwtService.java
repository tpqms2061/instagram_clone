package com.ssh.backend.security;

import com.ssh.backend.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoder;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.aspectj.weaver.patterns.IToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.*;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    //단일 클레임 추출 => 클레임의 타입(String  , LocalTimeDtate 등등) 종류가 많기 때문에 제네릭을 사용
    //Function: 타입이 다양하니까 추출하는 방법도 다양하니까 타입의 맞게 가져가기 위해서 (String) / get으로 조회하거나 로직이 다양
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    //일반적인 토큰 생성
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> extraClaims = new HashMap<>();

        if (userDetails instanceof User user) {
            extraClaims.put("id", user.getId());
            extraClaims.put("email", user.getEmail());
            extraClaims.put("username", user.getUsername());
            extraClaims.put("fullName", user.getFullName());
            extraClaims.put("profileImageUrl", user.getProfileImageUrl());
            extraClaims.put("bio", user.getBio());

        }

        return generateToken(extraClaims, userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, refreshExpiration);
    }




    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
            ) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();

    }

    public String extractUsername(String token) {
        Claims claims = extractAllClaims(token); //모든 클레임 추출
        if (claims.containsKey("id")) {
            return String.valueOf(claims.get("id"));
        }
        return claims.getSubject();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String identifier = extractUsername(token);

        if (userDetails instanceof User user) {
            boolean isValid = identifier.equals(String.valueOf(user.getId()))
                    || identifier.equals(user.getUsername());

            return isValid && isTokenExpired(token);
        }

        return (identifier.equals(userDetails.getUsername())) && isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) { return extractExpiration(token).after(new Date()); }

    private Date extractExpiration(String token) { return extractClaim(token, Claims::getExpiration); }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder() //JWT 파서(parser)를 생성
                .setSigningKey(getSignInKey()) //토큰의 서명을 검증할 때 사용할 비밀키를 설정
                .build()
                .parseClaimsJws(token) //전달된 JWT를 파싱(해석)해서 서명이 올바른지 확인
                .getBody(); //JWT의 Payload(Claims 부분)만 추출
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey); //Base64 디코딩을 해서 바이트 배열로 변환
        return Keys.hmacShaKeyFor(keyBytes); //HMAC-SHA 알고리즘을 사용할 수 있는 서명 키 객체(Key) 로 변환
    }
}
