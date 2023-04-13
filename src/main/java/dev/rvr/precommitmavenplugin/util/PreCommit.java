package dev.rvr.precommitmavenplugin.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class PreCommit {
    private final String pythonPath;
    private final String fileName;
    private final File cacheDir;

    public PreCommit(String pythonPath, String version, Path preCommitPath) {
        this.pythonPath = pythonPath;

        if (pythonPath == null)
            throw new IllegalArgumentException("pythonPath cannot be null!");
        if (version == null)
            throw new IllegalArgumentException("version cannot be null!");
        if (preCommitPath == null)
            throw new IllegalArgumentException("preCommitPath cannot be null!");
        if (!preCommitPath.toFile().isDirectory())
            throw new IllegalArgumentException("preCommitPath is not a directory!");
        if (!preCommitPath.resolve(DownloadUtil.getPrecommitFileName(version)).toFile().exists())
            throw new IllegalArgumentException("pre-commit is not downloaded!");

        this.cacheDir = preCommitPath.toFile();
        this.fileName = DownloadUtil.getPrecommitFileName(version);
    }

    public void install() throws PreCommitException {
        try {
            Process process = Runtime.getRuntime().exec("python " + fileName + " install", null, cacheDir);
            if (process.waitFor() != 0) {
                throw new RuntimeException("Install process failed with exit code: " + process.exitValue());
            }
        } catch (IOException | InterruptedException | RuntimeException e) {
            throw new PreCommitException("Failed to install pre-commit hooks!", e);
        }
    }
}
