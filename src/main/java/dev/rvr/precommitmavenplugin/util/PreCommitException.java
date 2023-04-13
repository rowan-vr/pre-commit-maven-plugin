package dev.rvr.precommitmavenplugin.util;

public class PreCommitException extends Exception{
    public PreCommitException(String message, Exception cause) {
        super(message, cause);
    }
}
