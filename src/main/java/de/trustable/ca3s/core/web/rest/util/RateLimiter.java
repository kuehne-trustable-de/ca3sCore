package de.trustable.ca3s.core.web.rest.util;

import de.trustable.ca3s.core.service.dto.acme.problem.AcmeProblemException;
import de.trustable.ca3s.core.service.dto.acme.problem.ProblemDetail;
import de.trustable.ca3s.core.service.util.AcmeUtil;
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

public class RateLimiter {

    Logger LOG = LoggerFactory.getLogger(RateLimiter.class);

    int maxEntries = 100;
    private int rateSec = 0;
    private int rateMin = 20;
    private int rateHour = 0;

    LinkedHashMap<Long, Bucket> lruMapOrder;

    private final String endpointName;

    public RateLimiter(final String endpointName){
        this.endpointName = endpointName;

        lruMapOrder = new LinkedHashMap<>(maxEntries * 10 / 7, 0.7f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Long, Bucket> eldest) {
                return size() > maxEntries;
            }
        };
    }

    public RateLimiter(final String endpointName,
                   int rateSec,
        int rateMin,
        int rateHour){

        this( endpointName);

        this.rateSec = rateSec;
        this.rateMin = rateMin;
        this.rateHour = rateHour;
    }

    public void checkRateLimit(long id, String realm) {
        Bucket bucket = getBucket(id);
        LOG.debug("Current bucket : {} ", bucket);
        if(bucket.tryConsume(1)) {
            LOG.debug("rate limitation bucket has {} tokens left", bucket.getAvailableTokens());
        }else{
            LOG.warn("rate limit applies to '{}/{}/{}'", realm, endpointName, id);
            final ProblemDetail problem = new ProblemDetail(AcmeUtil.RATE_LIMITED, "Rate limit applies",
                BAD_REQUEST, "Too many requests for ACME object", AcmeController.NO_INSTANCE);
            throw new AcmeProblemException(problem);
        }
    }

    private Bucket getBucket(final long id){

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
