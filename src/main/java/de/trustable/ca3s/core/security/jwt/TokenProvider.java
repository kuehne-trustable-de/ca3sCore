package de.trustable.ca3s.core.security.jwt;

import de.trustable.ca3s.core.security.SecurityUtils;
import de.trustable.ca3s.core.service.dto.AccountCredentialsType;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import tech.jhipster.config.JHipsterProperties;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Component
public class TokenProvider {

    private final Logger log = LoggerFactory.getLogger(TokenProvider.class);

    public static final String CA3S_JWT_COOKIE_NAME = "ca3sJWT";

    private static final String AUTHORITIES_KEY = "auth";
    public static final String SKI_KEY = "ski";
    private static final String TOKEN_TYPE_KEY = "tokentype";

    private final boolean secureCookie;

    private final Key key;

    private final JwtParser jwtParser;

    private final long tokenValidityInMilliseconds;

    private final long tokenValidityInMillisecondsForRememberMe;

    public TokenProvider(JHipsterProperties jHipsterProperties,
                         @Value("${ca3s.ui.sso.secureCookie:true}") boolean secureCookie) {

        this.secureCookie = secureCookie;

        byte[] keyBytes;
        String secret = jHipsterProperties.getSecurity().getAuthentication().getJwt().getBase64Secret();
        if (!ObjectUtils.isEmpty(secret)) {
            log.debug("Using a Base64-encoded JWT secret key");
            keyBytes = Decoders.BASE64.decode(secret);
        } else {
            log.warn(
                "Warning: the JWT key used is not Base64-encoded. " +
                "We recommend using the `jhipster.security.authentication.jwt.base64-secret` key for optimum security."
            );
            secret = jHipsterProperties.getSecurity().getAuthentication().getJwt().getSecret();
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }
        key = Keys.hmacShaKeyFor(keyBytes);
        jwtParser = Jwts.parserBuilder().setSigningKey(key).build();
        this.tokenValidityInMilliseconds = 1000 * jHipsterProperties.getSecurity().getAuthentication().getJwt().getTokenValidityInSeconds();
        this.tokenValidityInMillisecondsForRememberMe =
            1000 * jHipsterProperties.getSecurity().getAuthentication().getJwt().getTokenValidityInSecondsForRememberMe();
    }

    public String createToken(Authentication authentication, boolean rememberMe) {
        String authorities = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(","));

        long now = (new Date()).getTime();
        Date validity;
        if (rememberMe) {
            validity = new Date(now + this.tokenValidityInMillisecondsForRememberMe);
        } else {
            validity = new Date(now + this.tokenValidityInMilliseconds);
        }

        String principal = SecurityUtils.extractPrincipal(authentication);
        log.debug("building JWT for principal '{}' with authorities {}", principal, authorities);
        return Jwts
            .builder()
            .setSubject(principal)
            .claim(AUTHORITIES_KEY, authorities)
            .signWith(key, SignatureAlgorithm.HS512)
            .setExpiration(validity)
            .compact();
    }
    public String createToken(final String subject, final String b46Ski) {

        long now = (new Date()).getTime();
        long tokenValiditySKIInMilliseconds = 60L * 1000L;
        Date validity = new Date(now + tokenValiditySKIInMilliseconds);

        return Jwts
            .builder()
            .setSubject(subject)
            .claim(SKI_KEY, b46Ski)
            .signWith(key, SignatureAlgorithm.HS512)
            .setExpiration(validity)
            .compact();
    }

    public String createToken(Authentication authentication, AccountCredentialsType credentialType, long validitySeconds) {
        long now = (new Date()).getTime();
        Date validity = new Date(now + (1000L *  validitySeconds));

        return Jwts
            .builder()
            .setSubject(credentialType.name())
            .claim(TOKEN_TYPE_KEY, credentialType.toString())
            .signWith(key, SignatureAlgorithm.HS512)
            .setExpiration(validity)
            .compact();
    }

    public String getSKIClaim(String jwt) {
        Claims claims = jwtParser.parseClaimsJws(jwt).getBody();
        return claims.get(SKI_KEY,String.class);
    }

    public Authentication getAuthentication(String token) {
        Claims claims = jwtParser.parseClaimsJws(token).getBody();

        Collection<? extends GrantedAuthority> authorities = Arrays
            .stream(claims.get(AUTHORITIES_KEY).toString().split(","))
            .filter(auth -> !auth.trim().isEmpty())
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());

        User principal = new User(claims.getSubject(), "", authorities);

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    public boolean validateToken(String authToken) {
        try {
            jwtParser.parseClaimsJws(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.info("Invalid JWT token.");
            log.trace("Invalid JWT token trace.", e);
        }
        return false;
    }

    public void setAuthenticationCookie(HttpServletResponse response,
                                        Authentication authentication,
                                        String targetUrl) {

        if (response.isCommitted()) {
            log.debug("Did not redirect to {} since response already committed.", targetUrl);
        } else {
            this.log.debug("Redirect to {} .", targetUrl);
            Cookie authCookie = new Cookie(CA3S_JWT_COOKIE_NAME, createToken(authentication, false));
            authCookie.setMaxAge(60); // expires in a minute
            authCookie.setSecure(secureCookie);
            authCookie.setHttpOnly(false);
            authCookie.setPath("/"); // global cookie accessible everywhere
            response.addCookie(authCookie);
            this.log.debug("Setting JWT as cookie '{}'", CA3S_JWT_COOKIE_NAME);
        }
    }

}
