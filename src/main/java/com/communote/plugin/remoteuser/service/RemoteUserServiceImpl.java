package com.communote.plugin.remoteuser.service;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.communote.plugin.remoteuser.config.RemoteUserAuthenticationConfiguration;
import com.communote.plugins.core.services.PluginPropertyService;
import com.communote.plugins.core.services.PluginPropertyServiceException;

/**
 * Implementation of {@link RemoteUserService} which stores and gets the configuration from
 * communote plugin properties.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
@Provides
@Component
@Instantiate
public class RemoteUserServiceImpl implements RemoteUserService {

    private static final String KEY_REMOTE_USER_CONFIGURATION = "com.communote.plugin.remoteuser.configuration";

    private RemoteUserAuthenticationConfiguration cachedRemoteUserAuthenticationConfiguration;

    private final static Logger LOGGER = LoggerFactory.getLogger(RemoteUserServiceImpl.class);

    @Requires
    private PluginPropertyService pluginPropertyService;

    /**
     * {@inheritDoc}
     */
    @Override
    public RemoteUserAuthenticationConfiguration getCachedReadonlyRemoteUserAuthenticationConfiguration() {

        if (cachedRemoteUserAuthenticationConfiguration == null) {
            setCachedConfiguration();
        }
        return cachedRemoteUserAuthenticationConfiguration;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RemoteUserAuthenticationConfiguration getMutableRemoteUserAuthenticationConfiguration() {

        RemoteUserAuthenticationConfiguration remoteUserConfiguration = new RemoteUserAuthenticationConfiguration();
        try {
            remoteUserConfiguration = pluginPropertyService
                    .getClientPropertyAsObject(
                            KEY_REMOTE_USER_CONFIGURATION,
                            RemoteUserAuthenticationConfiguration.class);
        } catch (Exception e) {
            LOGGER.error("Error getting remote user configuration with key "
                    + KEY_REMOTE_USER_CONFIGURATION
                    + ". Will use an empty one.", e);
        }
        if (remoteUserConfiguration == null) {
            remoteUserConfiguration = new RemoteUserAuthenticationConfiguration();
        }
        return remoteUserConfiguration;

    }

    private synchronized void resetCachedConfiguration() {
        cachedRemoteUserAuthenticationConfiguration = null;
        setCachedConfiguration();
    }

    private synchronized void setCachedConfiguration() {

        if (cachedRemoteUserAuthenticationConfiguration == null) {
            RemoteUserAuthenticationConfiguration config = getMutableRemoteUserAuthenticationConfiguration();
            config = config.clone();
            config.setReadonly();
            cachedRemoteUserAuthenticationConfiguration = config;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateRemoteUserAuthenticationConfiguration(
            RemoteUserAuthenticationConfiguration configuration) throws RemoteUserServiceException {
        try {
            this.pluginPropertyService.setClientPropertyAsObject(
                    KEY_REMOTE_USER_CONFIGURATION, configuration);
        } catch (PluginPropertyServiceException e) {
            LOGGER.error("Error storing configuration.", e);
            throw new RemoteUserServiceException("Error storing configuration");
        }

        resetCachedConfiguration();

    }

}
