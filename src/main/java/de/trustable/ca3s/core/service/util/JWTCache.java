package de.trustable.ca3s.core.service.util;

import org.jose4j.jwt.MalformedClaimException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service("jwtCache")
public class JWTCache {

    private static final Logger LOG = LoggerFactory.getLogger(JWTCache.class);

    private static HashMap<String, JWTAndValidity> jwtByKeyMap = new HashMap<>();

    private final JwtUtil jwtUtil;

    public JWTCache(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    public Optional<String> getJwt(final String key) {
        if (jwtByKeyMap.containsKey(key)) {
            JWTAndValidity jwtAndValidity = jwtByKeyMap.get(key);
            if (System.currentTimeMillis() > jwtAndValidity.getExpiresAt()) {
                LOG.info("Expired JWT for key {}", key);
                return Optional.empty();
            }
            LOG.debug("Returning valid JWT for key {}", key);
            return Optional.of(jwtByKeyMap.get(key).getJwt());
        } else {
            LOG.debug("No JWT present for key {}", key);
            return Optional.empty();
        }
    }

    public void putJwt(final String key, final String jwt) {

        try {
            JWTAndValidity jwtAndValidity = new JWTAndValidity(jwt,
                jwtUtil.processCompactJWT(jwt).getJwtClaims().getExpirationTime().getValueInMillis());

            LOG.debug("Put new JWT for key {}", key);

            jwtByKeyMap.put(key, jwtAndValidity);
        } catch (MalformedClaimException e) {
            throw new RuntimeException(e);
        }
    }

    @Scheduled(fixedRateString = "${ca3s.schedule.rate.acmeOrderExpiry:600000}")
    public void runJWTCleanup() {

        long now = System.currentTimeMillis();
        List<Map.Entry<String,JWTAndValidity>> expiredEntryList = jwtByKeyMap.entrySet().stream().
            filter(entry -> now > entry.getValue().getExpiresAt()).collect(Collectors.toList());

        for (Map.Entry<String,JWTAndValidity> entry : expiredEntryList) {
            jwtByKeyMap.remove(entry.getKey(),  entry.getValue());
        }

        LOG.debug("#{} expired entries removed", expiredEntryList.size());
    }
}
class JWTAndValidity{
    final private String jwt;
    final private long expiresAt;


    JWTAndValidity(String jwt, long expiresAt) {
        this.jwt = jwt;
        this.expiresAt = expiresAt;
    }

    public String getJwt() {
        return jwt;
    }

    public long getExpiresAt() {
        return expiresAt;
    }
}
