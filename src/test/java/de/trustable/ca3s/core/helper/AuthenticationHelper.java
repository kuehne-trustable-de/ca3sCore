package de.trustable.ca3s.core.helper;

import de.trustable.ca3s.core.security.AuthoritiesConstants;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.Collection;

public class AuthenticationHelper {

    public static void setAuthenticationUser(){
        setAuthentication("user", AuthoritiesConstants.USER);
    }

    public static void setAuthenticationAdmin(){
        setAuthentication("admin", AuthoritiesConstants.ADMIN);
    }

    public static void setAuthentication(String name, String role){

        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(role));
        Authentication authentication = new UsernamePasswordAuthenticationToken(name, name, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);

    }

}
