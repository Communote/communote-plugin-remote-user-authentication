package com.communote.plugin.remoteuser;

import java.util.List;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import com.communote.plugin.remoteuser.service.RemoteUserService;
import com.communote.server.api.ServiceLocator;
import com.communote.server.api.core.user.UserNotFoundException;
import com.communote.server.api.core.user.UserVO;
import com.communote.server.core.security.CommunoteAuthenticationProvider;
import com.communote.server.core.security.UserDetails;
import com.communote.server.core.security.authentication.BaseCommunoteAuthenticationProvider;
import com.communote.server.model.user.User;
import com.communote.server.persistence.user.InvitationField;
import com.communote.server.service.UserService;

/**
 * AuthenticationProvider that takes the {@link RemoteUserAuthenticationToken} and extracts the
 * username to forward it to the {@link UserService}
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
@Component
@Provides
@Instantiate
public class RemoteUserAuthenticationProvider extends BaseCommunoteAuthenticationProvider
        implements CommunoteAuthenticationProvider {

    private final static Logger LOGGER = LoggerFactory
            .getLogger(RemoteUserAuthenticationProvider.class);

    @Requires
    private RemoteUserService remoteUserService;

    /**
     * {@inheritDoc}
     * 
     * Actually nothing serious is done, since if we have a token it means the SSO process went
     * successful. We just encapsulate the token into {@link CommunoteWindowsAuthenticationToken}
     * that also holds the principal and authorities for further decision making.
     */
    @Override
    protected Authentication createSuccessAuthentication(UserDetails details,
            Authentication authentication) {

        return new PreAuthenticatedAuthenticationToken(details, null, details.getAuthorities());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getIdentifier() {
        return this.getClass().getSimpleName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<InvitationField> getInvitationFields() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getOrder() {
        return 100;
    }

    /**
     * 
     * @return get the user service
     */
    private UserService getUserService() {
        return ServiceLocator.instance().getService(UserService.class);
    }

    /**
     * {@inheritDoc}
     * 
     * Retrieve the user details, that is extract the fields from the remote user token and forward
     * it to the {@link UserService}
     */
    @Override
    protected UserDetails handleRetrieveUserDetails(Authentication authentication)
            throws AuthenticationException {

        RemoteUserAuthenticationToken remoteUserToken = (RemoteUserAuthenticationToken) authentication;

        String onlyUseExternalSystemId = this.remoteUserService
                .getCachedReadonlyRemoteUserAuthenticationConfiguration()
                .getOnlyUseExternalSystemId();
        final String remoteUserLogin = (String) remoteUserToken.getPrincipal();

        User user;
        try {
            if (onlyUseExternalSystemId == null) {
                user = getUserService().getUser(
                        remoteUserLogin);
            } else {
                user = getUserService().getUser(
                        remoteUserLogin, onlyUseExternalSystemId);
            }
        } catch (UserNotFoundException e) {
            LOGGER.trace("User not found for " + remoteUserLogin);
            return null;
        }

        return new UserDetails(user, user.getAlias());

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserVO queryUser(Map<InvitationField, String> queryData) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supports(Class<?> authentication) {
        return RemoteUserAuthenticationToken.class.isAssignableFrom(authentication);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supportsUserQuerying() {
        return false;
    }

}
