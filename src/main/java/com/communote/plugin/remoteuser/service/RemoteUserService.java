package com.communote.plugin.remoteuser.service;

import com.communote.plugin.remoteuser.config.RemoteUserAuthenticationConfiguration;

/**
 * The remote user service stores and gets the configuration for the remote user authentication.
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public interface RemoteUserService {

    /**
     * 
     * @return gets the cached and readonly configuration
     */
    public RemoteUserAuthenticationConfiguration getCachedReadonlyRemoteUserAuthenticationConfiguration();

    /**
     * 
     * @return gets the configuration that can be changed and used for update
     */
    public RemoteUserAuthenticationConfiguration getMutableRemoteUserAuthenticationConfiguration();

    /**
     * Updates the configuration
     * 
     * @param configuration
     *            the configuration to update
     * @throws RemoteUserServiceException
     *             if an error doring storing occurs.
     */
    public void updateRemoteUserAuthenticationConfiguration(
            RemoteUserAuthenticationConfiguration configuration) throws RemoteUserServiceException;

}