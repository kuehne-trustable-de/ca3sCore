package de.trustable.ca3s.core.service.util;

import de.trustable.ca3s.core.security.IPBlockedException;
import de.trustable.ca3s.core.service.dto.acme.problem.AcmeProblemException;
import de.trustable.ca3s.core.service.dto.acme.problem.ProblemDetail;
import de.trustable.ca3s.core.web.rest.acme.AcmeController;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.local.LocalBucketBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

public class RateLimiterService {

    Logger LOG = LoggerFactory.getLogger(RateLimiterService.class);

    int maxEntries = 100;
    private int rateSec = 0;
    private int rateMin = 20;
    private int rateHour = 0;

    LinkedHashMap<Long, Bucket> lruMapOrder;

    public String getEndpointName() {
        return endpointName;
    }

    private final String endpointName;

    public RateLimiterService(final String endpointName){
        this.endpointName = endpointName;

        lruMapOrder = new LinkedHashMap<>(maxEntries * 10 / 7, 0.7f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Long, Bucket> eldest) {
                return size() > maxEntries;
            }
        };
    }

    public RateLimiterService(final String endpointName,
                              int rateSec,
                              int rateMin,
                              int rateHour){

        this( endpointName);

        this.rateSec = rateSec;
        this.rateMin = rateMin;
        this.rateHour = rateHour;
    }

    public void checkSprayingRateLimit(final Long clientIP, String clientIPAsString) {
        Bucket bucket = getBucket(clientIP);
        long availableLoginTokens = bucket.getAvailableTokens();
        if(availableLoginTokens > 0L){
            LOG.debug("login per IP rate limitation bucket has {} tokens left", availableLoginTokens);
        }else{
            throw new IPBlockedException("no token left in login bucket for " + clientIPAsString);
        }
    }
    public void consumeSprayingRateLimit(final Long clientIP, String clientIPAsString) {
        Bucket bucket = getBucket(clientIP);
        if(bucket.tryConsume(1)) {
            LOG.debug("login per IP rate limitation bucket has {} tokens left", bucket.getAvailableTokens());
        }else{
            throw new IPBlockedException("no token left in login bucket for " + clientIPAsString);
        }
    }

    public Bucket getBucket(final long id){

        // no need for synchronisation, in the worst case surplus buckets were created.
        if( lruMapOrder.containsKey(id) ){
            return lruMapOrder.get(id);
        }else {
            LOG.info("creating new bucket for '{}' '{}'", endpointName, id);
            LocalBucketBuilder bucketBuilder = Bucket.builder();
            if( rateSec > 0 ){
                bucketBuilder.addLimit(Bandwidth.simple(rateSec, Duration.ofSeconds(1)));
            }
            if( rateMin > 0 ){
                bucketBuilder.addLimit(Bandwidth.simple(rateMin, Duration.ofMinutes(1)));
            }
            if( rateHour > 0 ){
                bucketBuilder.addLimit(Bandwidth.simple(rateHour, Duration.ofHours(1)));
            }

            Bucket bucket = bucketBuilder.build();

            lruMapOrder.put(id, bucket);
            return bucket;
        }
    }

}
