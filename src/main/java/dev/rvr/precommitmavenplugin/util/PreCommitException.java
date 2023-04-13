package dev.rvr.precommitmavenplugin.util;

/**
 * Pre-commit exception
 */
public class PreCommitException extends Exception{
    public PreCommitException(String message, Exception cause) {
        super(message, cause);
    }
}
