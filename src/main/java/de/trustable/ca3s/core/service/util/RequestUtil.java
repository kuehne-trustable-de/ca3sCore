package de.trustable.ca3s.core.service.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
public class RequestUtil {

    private final Logger LOG = LoggerFactory.getLogger(RequestUtil.class);

    private final HttpServletRequest request;
    private final String forwardHeaderName;


    public RequestUtil(HttpServletRequest request,
                       @Value("${ca3s.request.forwardHeader:#{null}}") String forwardHeaderName) {
        this.request = request;
        this.forwardHeaderName = forwardHeaderName;
    }

    String getClientIP() {
        if( forwardHeaderName == null){
            return request.getRemoteAddr();
        }

        String addressString;
        String xfHeader = request.getHeader(forwardHeaderName);
        if (xfHeader == null || xfHeader.isEmpty() || !xfHeader.contains(request.getRemoteAddr())) {
            addressString = request.getRemoteAddr();
        }else {
            addressString = xfHeader.split(",")[0];
        }

        return addressString;
    }
}
