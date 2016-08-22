package com.communote.plugin.remoteuser.admin;

import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.mvc.Controller;

import com.communote.common.util.ParameterHelper;
import com.communote.plugin.remoteuser.config.RemoteUserAuthenticationConfiguration;
import com.communote.plugin.remoteuser.service.RemoteUserService;
import com.communote.plugin.remoteuser.service.RemoteUserServiceException;
import com.communote.plugins.core.views.AdministrationViewController;
import com.communote.plugins.core.views.ViewControllerException;
import com.communote.plugins.core.views.annotations.Page;
import com.communote.plugins.core.views.annotations.UrlMapping;
import com.communote.server.api.ServiceLocator;
import com.communote.server.core.external.ExternalUserRepository;
import com.communote.server.service.UserService;
import com.communote.server.web.commons.MessageHelper;

/**
 * Controlelr to configure the remote user authentication.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 */
@Component
@Provides
@Instantiate
@UrlMapping(value = "/*/admin/remoteuser")
@Page(menu = "extensions", submenu = "remoteuser", 
    menuMessageKey = "plugins.remoteuser.administration.menu.title", jsCategories = {
        "communote-core", "admin" })
public class RemoteUserAdminConfigurationController extends AdministrationViewController implements
        Controller {

    private static final String PARAM_CONF_REG_EX = "conf.regExToMatchRemoteUserAgainstUserLogin";

    private static final String PARAM_CONF_ONLY_USE_EXTERNAL_SYSTEM_ID = "conf.onlyUseExternalSystemId";

    private static final String PARAM_CONF_ACTIVATED = "conf.activated";

    private static final String MODEL_ACTIVE_REPOS = "activeRepos";

    @Requires
    private RemoteUserService remoteUserService;

    private final static Logger LOGGER = LoggerFactory
            .getLogger(RemoteUserAuthenticationConfiguration.class);

    private final static String MODEL_REMOTE_USER_CONFIGURATION = "remoteUserConfiguration";

    /**
     * @param bundleContext
     *            OSGi bundle context
     */
    public RemoteUserAdminConfigurationController(BundleContext bundleContext) {
        super(bundleContext);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response,
            Map<String, Object> model) throws ViewControllerException {

        Collection<ExternalUserRepository> activeRepos = ServiceLocator
                .findService(UserService.class).getActiveUserRepositories();

        model.put(MODEL_REMOTE_USER_CONFIGURATION,
                remoteUserService.getCachedReadonlyRemoteUserAuthenticationConfiguration());
        model.put(MODEL_ACTIVE_REPOS, activeRepos);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doPost(HttpServletRequest request,
            HttpServletResponse response, Map<String, Object> model)
            throws ViewControllerException {

        RemoteUserAuthenticationConfiguration configuration = remoteUserService
                .getMutableRemoteUserAuthenticationConfiguration();

        // get the parameters
        String regEx = request.getParameter(PARAM_CONF_REG_EX);
        String extSystem = request.getParameter(PARAM_CONF_ONLY_USE_EXTERNAL_SYSTEM_ID);
        boolean activated = ParameterHelper.getParameterAsBoolean(request.getParameterMap(),
                PARAM_CONF_ACTIVATED, false);
        if (!activated) {
            activated = StringUtils.equalsIgnoreCase("on",
                    request.getParameter(PARAM_CONF_ACTIVATED));
        }

        regEx = StringUtils.trimToNull(regEx);
        if (extSystem != null) {
            extSystem = extSystem.replace("none", "");
        }
        extSystem = StringUtils.trimToNull(extSystem);

        // validate the parameters
        int errors = 0;
        if (regEx != null) {
            try {
                Pattern.compile(regEx);
            } catch (Exception e) {
                // add error
                MessageHelper.saveErrorMessageFromKey(request,
                        "plugins.remoteuser.administration.configuration.error.parsing.regex");
                errors++;
            }
        }
        if (extSystem != null) {
            ExternalUserRepository extRepo = ServiceLocator
                    .findService(UserService.class).getAvailableUserRepository(extSystem);
            if (extRepo == null) {
                // add error that extRepoId is invalid
                MessageHelper.saveErrorMessageFromKey(request,
                        "plugins.remoteuser.administration.configuration.error.external.system");
                errors++;
            }
        }

        if (errors == 0) {
            // if no error occurred store the parameters
            configuration.setActivated(activated);
            configuration.setRegExToMatchRemoteUserAgainstUserLogin(regEx);
            configuration.setOnlyUseExternalSystemId(extSystem);

            try {
                remoteUserService.updateRemoteUserAuthenticationConfiguration(configuration);

                MessageHelper.saveMessageFromKey(request,
                        "plugins.remoteuser.administration.configuration.success");
            } catch (RemoteUserServiceException e) {
                LOGGER.error(e.getMessage(), e);
                MessageHelper.saveErrorMessageFromKey(request,
                        "plugins.remoteuser.administration.configuration.error");
            }
        }

        doGet(request, response, model);
    }

    @Override
    public String getContentTemplate() {
        return "/vm/remote-user-admin-configuration.html.vm";
    }

}
