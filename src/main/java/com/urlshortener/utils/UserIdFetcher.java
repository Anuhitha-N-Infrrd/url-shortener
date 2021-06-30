package com.urlshortener.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

//This method extracts user_id from the token
@Component
public class UserIdFetcher {

    private static final Logger logger = LoggerFactory.getLogger(UserIdFetcher.class);

    public String getUserId(HttpServletRequest httpServletRequest) {

        String authorizationHeader = httpServletRequest.getHeader("Authorization");
        String token = null;
        String secret="taeganger";
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            logger.info("-----> Token identified");
            token = authorizationHeader.substring(7);
            try {
                Claims body = Jwts.parser()
                        .setSigningKey(secret)
                        .parseClaimsJws(token)
                        .getBody();
                String userId=(String) body.get("sub");
                logger.info("-----> User id extracted");
                return userId;
            } catch (JwtException | ClassCastException e) {
                logger.info("-----> Could not extract user_id");
                return "JWT exception";
            }
        }
        return "No email_id found";
    }
}
