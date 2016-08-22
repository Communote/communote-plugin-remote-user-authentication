package com.communote.plugin.remoteuser.config;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.communote.server.service.UserService;

/**
 * The configuration for the remote user pre-authentication
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class RemoteUserAuthenticationConfiguration implements Serializable, Cloneable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private String onlyUseExternalSystemId;

    private String regExToMatchRemoteUserAgainstUserLogin;

    private boolean readonly;

    private boolean activated;

    @Override
    public RemoteUserAuthenticationConfiguration clone() {
        try {
            return (RemoteUserAuthenticationConfiguration) super.clone();
        } catch (CloneNotSupportedException e) {
            // should never occur
            throw new InternalError(e.getMessage());
        }
    }

    /**
     * @see #setOnlyUseExternalSystemId(String)
     * 
     * @return null to use users from all systems.
     */
    public String getOnlyUseExternalSystemId() {
        return onlyUseExternalSystemId;
    }

    public String getRegExToMatchRemoteUserAgainstUserLogin() {
        return regExToMatchRemoteUserAgainstUserLogin;
    }

    public boolean isActivated() {
        return activated;
    }

    @JsonIgnore
    public boolean isReadonly() {
        return readonly;
    }

    public void setActivated(boolean activated) {
        if (readonly) {
            throw new IllegalStateException("Cannot change value, since configuration is readonly.");
        }
        this.activated = activated;
    }

    /**
     * if set the remote user will be treated as external login with the external system id given.
     * Hence only user from the given system are used. The {@link UserService} must of course be
     * configured appropriately.
     * 
     * @param onlyUseExternalSystemId
     *            null to use users from all systems
     */
    public void setOnlyUseExternalSystemId(String onlyUseExternalSystemId) {
        if (readonly) {
            throw new IllegalStateException("Cannot change value, since configuration is readonly.");
        }
        this.onlyUseExternalSystemId = onlyUseExternalSystemId;
    }

    @JsonIgnore
    public void setReadonly() {
        this.readonly = true;
    }

    /**
     * The reg ex to use to extract the use login (alias) from the remote user
     * 
     * @param regExToMatchRemoteUserAgainstUserLogin
     *            null if the remote user should be taken as it is
     */
    public void setRegExToMatchRemoteUserAgainstUserLogin(
            String regExToMatchRemoteUserAgainstUserLogin) {
        if (readonly) {
            throw new IllegalStateException("Cannot change value, since configuration is readonly.");
        }
        this.regExToMatchRemoteUserAgainstUserLogin = regExToMatchRemoteUserAgainstUserLogin;
    }

}
