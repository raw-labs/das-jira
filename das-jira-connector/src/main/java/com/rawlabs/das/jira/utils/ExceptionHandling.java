package com.rawlabs.das.jira.utils;

import com.rawlabs.das.sdk.DASSdkInvalidArgumentException;
import com.rawlabs.das.sdk.DASSdkPermissionDeniedException;
import com.rawlabs.das.sdk.DASSdkUnauthenticatedException;

public final class ExceptionHandling {

    private ExceptionHandling() {
        // Prevent instantiation
    }

    /**
     * Helper method to create a DASSdk RuntimeException from an ApiException.
     *
     * <p>When the SDK exposes more exceptions (e.g. authentication errors), this method can
     * investigate the ApiException and create the appropriate SDK exception.
     *
     * @param e the ApiException to convert
     * @return the RuntimeException to throw
     */
    public static RuntimeException makeSdkException(com.rawlabs.das.jira.rest.platform.ApiException e) {
        if (e.getCode() == 0 && e.getCause() instanceof java.net.UnknownHostException) {
            // That happens if the host is not found.
            return new DASSdkInvalidArgumentException(e.getCause().getMessage(), e);
        }
        return switch (e.getCode()) {
            case 400 -> new DASSdkInvalidArgumentException(mkMessage(e.getResponseBody()), e);
            case 401 -> new DASSdkUnauthenticatedException("unauthorized", e);
            case 403 -> new DASSdkPermissionDeniedException("permission denied", e);
            default ->
                // For now, just create a generic SDK exception with the body of the ApiException
                    new DASSdkInvalidArgumentException(mkMessage(e.getResponseBody()), e);
        };
    }

    /**
     * Helper method to create a DASSdk RuntimeException from an ApiException.
     *
     * <p>When the SDK exposes more exceptions (e.g. authentication errors), this method can
     * investigate the ApiException and create the appropriate SDK exception.
     *
     * @param e the ApiException to convert
     * @return the RuntimeException to throw
     */
    public static RuntimeException makeSdkException(com.rawlabs.das.jira.rest.software.ApiException e) {
        if (e.getCode() == 0 && e.getCause() instanceof java.net.UnknownHostException) {
            // That happens if the host is not found.
            return new DASSdkInvalidArgumentException(e.getCause().getMessage(), e);
        }
        return switch (e.getCode()) {
            case 400 -> new DASSdkInvalidArgumentException(mkMessage(e.getResponseBody()), e);
            case 401 -> new DASSdkUnauthenticatedException("unauthorized", e);
            case 403 -> new DASSdkPermissionDeniedException("permission denied", e);
            default ->
                // For now, just create a generic SDK exception with the body of the ApiException
                    new DASSdkInvalidArgumentException(mkMessage(e.getResponseBody()), e);
        };
    }

    // A helper to fallback to a default message if ever the body would be empty or null.
    private static String mkMessage(String msg) {
        if (msg == null || msg.trim().isEmpty()) {
            return "Unknown JIRA API error";
        }
        return msg;
    }

}