package com.bilgeadam.utility;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.bilgeadam.exception.AuthManagerException;
import com.bilgeadam.exception.ErrorType;
import com.bilgeadam.repository.entity.enums.ERole;
import net.bytebuddy.implementation.bytecode.ShiftRight;
import org.apache.tomcat.websocket.AuthenticationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class JwtTokenProvider {
    @Value("${secretkey}")
    String secretkey;
    @Value("${audience}")
    String audience;
    @Value("${issuer}")
    String issuer;

    public Optional<String> createToken(Long id){
        String token = null;
        Date date = new Date(System.currentTimeMillis() + (1000 * 60 * 5));
        try{
            token = JWT.create()
                    .withAudience(audience)
                    .withIssuer(issuer)
                    .withIssuedAt(new Date())
                    .withExpiresAt(date)
                    .withClaim("id",id)
                    .sign(Algorithm.HMAC512(secretkey));
            return Optional.of(token);
        }catch (Exception e){
            System.out.println(e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<String> createToken(Long id, ERole role){
        String token = null;
        Date date = new Date(System.currentTimeMillis() + (1000 * 60 * 5));
        try{
            token = JWT.create()
                    .withAudience(audience)
                    .withIssuer(issuer)
                    .withIssuedAt(new Date())
                    .withExpiresAt(date)
                    .withClaim("id",id)
                    .withClaim("role", role.toString())
                    .sign(Algorithm.HMAC512(secretkey));
            return Optional.of(token);
        }catch (Exception e){
            System.out.println(e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<Long> getIdFromToken(String token){
        try {
            Algorithm algorithm = Algorithm.HMAC512(secretkey);
            JWTVerifier verifier = JWT.require(algorithm).withAudience(audience).withIssuer(issuer).build();
            DecodedJWT decodedJWT = verifier.verify(token);
            if(decodedJWT == null){
                throw new AuthManagerException(ErrorType.INVALID_TOKEN);
            }
            Long id = decodedJWT.getClaim("id").asLong();
            return Optional.of(id);
        }catch (Exception e){
            System.out.println(e.getMessage());
            throw new AuthManagerException(ErrorType.INVALID_TOKEN);
        }
    }

    public Optional<String> getRoleFromToken(String token){
        try {
            Algorithm algorithm = Algorithm.HMAC512(secretkey);
            JWTVerifier verifier = JWT.require(algorithm).withAudience(audience).withIssuer(issuer).build();
            DecodedJWT decodedJWT = verifier.verify(token);
            if(decodedJWT == null){
                throw new AuthManagerException(ErrorType.INVALID_TOKEN);
            }
            String role = decodedJWT.getClaim("role").asString();
            return Optional.of(role);
        }catch (Exception e){
            System.out.println(e.getMessage());
            throw new AuthManagerException(ErrorType.INVALID_TOKEN);
        }
    }


    //----------------------------------------
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
