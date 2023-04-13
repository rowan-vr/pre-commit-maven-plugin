package dev.rvr.precommitmavenplugin.util;

import org.apache.maven.plugin.logging.Log;

import java.io.*;
import java.nio.file.Path;

public class PreCommit {
    private final String pythonPath;
    private final String fileName;
    private final File cacheDir;
    private final Log log;

    public PreCommit(String pythonPath, String version, Path preCommitPath, Log log) {
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
        this.log = log;
    }

    public void install() throws PreCommitException {
        try {
            this.run("install");
        } catch (RuntimeException e) {
            throw new PreCommitException("Failed to install pre-commit hooks!", e);
        }
    }

    public void run(String arguments) throws PreCommitException {
        try {
            Process process = Runtime.getRuntime().exec(pythonPath + " " + fileName + " " + arguments, null, cacheDir);

            if (log != null) {
                InputStream stdIn = process.getInputStream();
                InputStreamReader isr = new InputStreamReader(stdIn);
                BufferedReader br = new BufferedReader(isr);

                String line;
                while ((line = br.readLine()) != null)
                    log.info(line);
            }

            if (process.waitFor() != 0) {
                throw new RuntimeException("Run process failed with exit code: " + process.exitValue());
            }
        } catch (IOException | InterruptedException | RuntimeException e) {
            throw new PreCommitException("Failed to run pre-commit!", e);
        }
    }
}
