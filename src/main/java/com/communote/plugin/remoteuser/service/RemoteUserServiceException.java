package com.communote.plugin.remoteuser.service;

/**
 * Indicates an error during a @see RemoteUserService operation
 * 
 * @author Communote GmbH - <a href="http://www.communote.com/">http://www.communote.com/</a>
 * 
 */
public class RemoteUserServiceException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public RemoteUserServiceException(String message) {
        super(message);
    }

    public RemoteUserServiceException(String message, Throwable cause) {
        super(message, cause);
    }

}
