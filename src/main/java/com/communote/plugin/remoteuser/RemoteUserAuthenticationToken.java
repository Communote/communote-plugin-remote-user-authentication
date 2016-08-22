package com.communote.plugin.remoteuser;

import java.util.Collection;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

/**
 * Token holding the remote user name
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
public class RemoteUserAuthenticationToken extends AbstractAuthenticationToken {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private final String remoteUser;

    /**
     * 
     * @param remoteUser
     *            The authenticated remote user login name
     */
    public RemoteUserAuthenticationToken(String remoteUser) {
        super(null);
        this.remoteUser = remoteUser;
    }

    /**
     * 
     * @param remoteUser
     *            The authenticated remote user login name
     * @param anAuthorities
     *            The granted authorities
     */
    public RemoteUserAuthenticationToken(String remoteUser,
            Collection<? extends GrantedAuthority> anAuthorities) {
        super(anAuthorities);
        this.remoteUser = remoteUser;
    }

    /**
     * Get the credentials
     */
    @Override
    public Object getCredentials() {
        return null;
    }

    /**
     * Get the principal
     */
    @Override
    public Object getPrincipal() {
        return this.remoteUser;
    }

}
