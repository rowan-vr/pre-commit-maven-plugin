package dev.rvr.precommitmavenplugin.util;

/**
 * Download exception
 */
public class DownloadException extends Exception {
    public DownloadException(String message, Exception cause) {
        super(message, cause);
    }
}
