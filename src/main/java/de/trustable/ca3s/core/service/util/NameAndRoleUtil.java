package de.trustable.ca3s.core.service.util;

import de.trustable.ca3s.core.security.AuthoritiesConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class NameAndRoleUtil {

    private final Logger log = LoggerFactory.getLogger(NameAndRoleUtil.class);

    public NameAndRole getNameAndRole(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if( auth != null ){
            String role = getRole(auth);
            return new NameAndRole(auth.getName(), role);
        }
        return new NameAndRole("System","System");
    }

    String getRole(Authentication auth){

        log.debug( "Authorities #{} present", auth.getAuthorities().size());

        if( auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(AuthoritiesConstants.ADMIN))){
            return "ADMIN";
        }

        if( auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(AuthoritiesConstants.RA_OFFICER))){
            return "RA";
        }

        if( auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(AuthoritiesConstants.USER))){
            return "USER";
        }

        for( GrantedAuthority ga: auth.getAuthorities()){
            log.debug( "Authority: {}", ga.getAuthority());
        }
        return "ANON";
    }


}
