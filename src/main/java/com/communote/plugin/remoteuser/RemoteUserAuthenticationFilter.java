package com.communote.plugin.remoteuser;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.communote.plugin.remoteuser.service.RemoteUserService;
import com.communote.server.api.ServiceLocator;
import com.communote.server.core.security.AbstractCommunoteAuthenticationFilter;
import com.communote.server.core.security.AuthenticationManagement;

/**
 * This filter takes the remote user (@see {@link HttpServletRequest#getRemoteUser()} and use it as
 * authenticated user. The remote user will be set as authenticated (if not already set).
 *
 * The user will not be logged in if the url is after a logout or an authentication error.
 *
 * There will be no redirect or anything by this filter. It is silent.
 *
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Component
@Provides
@Instantiate
public class RemoteUserAuthenticationFilter extends AbstractCommunoteAuthenticationFilter {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(RemoteUserAuthenticationFilter.class);

    private static final String DISABLE_REMOTE_USER_FOR_THIS_SESSION = "disableRemoteUserForThisSession";

    @Requires
    private RemoteUserService remoteUserService;

    /**
     * {@inheritDoc}
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
            FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        try {

            if (isRemoteUserAuthActivated() && shouldAttemptAuthentication(request, response)) {
                final String parsedRemoteUserToUser = parseUserFromRemoteUser(request);

                if (parsedRemoteUserToUser != null) {

                    loginUser(parsedRemoteUserToUser);

                }
            }
        } catch (Throwable e) {
            // TODO what about TermsNotExcepted? Authentication was successful in this scenario.
            LOGGER.warn("Error extracting and using remote user: " + e.getMessage(), e);
            // this means something is configured wrong, or the user does not exists in the
            // repo and hence should log on normally.
            request.getSession().setAttribute(DISABLE_REMOTE_USER_FOR_THIS_SESSION, Boolean.TRUE);
        }
        chain.doFilter(request, response);
    }

    @Override
    public int getOrder() {
        return 10;
    }

    private String getRegExToMatchRemoteUserAgainstUserLogin() {
        return remoteUserService.getCachedReadonlyRemoteUserAuthenticationConfiguration()
                .getRegExToMatchRemoteUserAgainstUserLogin();
    }

    /**
     * Checks if the current request is originating after a logout or an authentication failure and
     * hence no sso should be issued because the user wants to logging by username password
     *
     * TODO This is the same logic as in WaffleCommunoteAuthenticationFilter, this should be
     * somewhere generic
     *
     * @param request
     *            the request to exploit the url
     * @return true if the request fits portal/authenticate?logout=
     */
    private boolean isAuthenticationAfterLogoutOrFailure(HttpServletRequest request) {
        if (request.getRequestURI().endsWith("portal/authenticate")) {
            if (request.getParameter("logout") != null
                    || request.getParameter("authenticationFailed") != null
                    || request.getParameter("userLocked") != null
                    || request.getParameter("userPermLocked") != null
                    || request.getParameter("userTempLocked") != null
                    || request.getParameter("userTempDisabled") != null
                    || request.getParameter("userMailNotActivated") != null) {
                return true;
            }
        }
        return false;
    }

    private boolean isRemoteUserAuthActivated() {
        return remoteUserService.getCachedReadonlyRemoteUserAuthenticationConfiguration()
                .isActivated();
    }

    private void loginUser(String userAlias) {

        RemoteUserAuthenticationToken preAuthenticatedAuthenticationToken = new RemoteUserAuthenticationToken(
                userAlias);

        Authentication successAuthentication = getAuthenticationManager().authenticate(
                preAuthenticatedAuthenticationToken);
        ServiceLocator.findService(AuthenticationManagement.class).onSuccessfulAuthentication(
                successAuthentication);
    }

    private String parseUserFromRemoteUser(HttpServletRequest request) {
        // read remote user
        final String remoteUser = request.getRemoteUser();
        String parsedRemoteUser = remoteUser;
        String regEx = getRegExToMatchRemoteUserAgainstUserLogin();
        if (regEx != null && remoteUser != null) {

            parsedRemoteUser = remoteUser.replaceFirst(regEx, "$1");

            // Matcher remoteUserMatcher = regExPatternToMatchRemoteUserAgainstUserLogin
            // .matcher(remoteUser);
            // parsedRemoteUser = remoteUserMatcher.group();
        }
        LOGGER.trace("Remote user extracted from request: parsedRemoteUser={} remoteUser={}",
                parsedRemoteUser, request.getRemoteUser());
        return parsedRemoteUser;
    }

    /**
     * Check if we need authentication, that is it is not disabled for this session and there is no
     * existing authentication.
     *
     * @param request
     *            the request
     * @param response
     *            the response
     * @return true if authentication should be tried
     */
    private boolean shouldAttemptAuthentication(HttpServletRequest request,
            HttpServletResponse response) {
        // don't authenticate if sth went wrong before or if the user just logged out
        if (isAuthenticationAfterLogoutOrFailure(request)
                || Boolean.TRUE.equals(request.getSession().getAttribute(
                        DISABLE_REMOTE_USER_FOR_THIS_SESSION))) {

            return false;
        }
        Authentication existingAuthentication = SecurityContextHolder.getContext()
                .getAuthentication();
        boolean attempt = existingAuthentication == null
                || !existingAuthentication.isAuthenticated()
                || existingAuthentication instanceof AnonymousAuthenticationToken;

        return attempt;
    }

}
