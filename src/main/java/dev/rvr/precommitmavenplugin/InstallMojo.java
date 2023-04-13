package dev.rvr.precommitmavenplugin;

import dev.rvr.precommitmavenplugin.util.DownloadUtil;
import dev.rvr.precommitmavenplugin.util.PreCommit;
import dev.rvr.precommitmavenplugin.util.PythonUtil;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;

import java.nio.file.Files;
import java.nio.file.Path;


@Mojo(name = "install")
public class InstallMojo extends AbstractMojo {

    /**
     * The {@link MavenProject}
     */
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    /**
     * The version of pre-commit to install
     */
    @Parameter(property = "version", required = false)
    private String version;

    /**
     * The path to the pre-commit cache directory
     */
    @Parameter(defaultValue = ".pre-commit-cache", property = "pre-commit-cache-path", required = false)
    private String preCommitCachePath;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("Installing pre-commit hooks...");
        getLog().debug("Testing python version...");
        String pythonPath = PythonUtil.findPythonPath();
        String pythonVersion = PythonUtil.getPythonVersion(pythonPath);
        if (pythonVersion == null) {
            getLog().error("Python is not installed!");
            throw new MojoExecutionException("Python is not installed!");
        }
        getLog().debug("Python version: " + pythonVersion);
        getLog().debug("Python path: " + pythonPath);

        getLog().debug("Checking for existing pre-commit download...");
        Path preCommitPath = project.getBasedir().toPath().resolve(preCommitCachePath);

        if (!Files.exists(preCommitPath)) {
            getLog().debug("Pre-commit cache directory does not exist! Creating...");
            try {
                Files.createDirectory(preCommitPath);
            } catch (Exception e) {
                getLog().error("Failed to create pre-commit cache directory!");
                throw new MojoExecutionException("Failed to create pre-commit cache directory!", e);
            }
        } else {
            getLog().debug("Pre-commit cache directory exists!");
        }

        if (version == null){
            getLog().debug("Version not specified! Using latest version...");
            try {
                version = DownloadUtil.getLatestVersion();
            } catch (Exception e) {
                getLog().error("Failed to get latest version!");
                throw new MojoExecutionException("Failed to get latest version!", e);
            }
            getLog().debug("Latest version: " + version);
            getLog().info("Version not specified! Using latest version: v" + version);
        }

        boolean downloaded;
        try {
            downloaded = DownloadUtil.isDownloaded(version, preCommitPath);
        } catch (Exception e) {
            getLog().error("Failed to check for pre-commit download!");
            throw new MojoExecutionException("Failed to check for pre-commit download!", e);
        }

        if (!downloaded) {
            getLog().info("Pre-commit not found! Downloading pre-commit...");
            try {
                DownloadUtil.downloadPreCommit(version, preCommitPath);
            } catch (Exception e) {
                getLog().error("Failed to download pre-commit!");
                throw new MojoExecutionException("Failed to download pre-commit!", e);
            }
            getLog().info("Pre-commit downloaded! Verifying download...");
            try {
                DownloadUtil.validateHash(version, preCommitPath);
            } catch (Exception e) {
                getLog().error("Failed to verify pre-commit download!");
                throw new MojoExecutionException("Failed to verify pre-commit download!", e);
            }
            getLog().info("Pre-commit successfully downloaded!");
        }

        getLog().info("Installing pre-commit hooks...");
        PreCommit preCommit = new PreCommit(pythonPath, version, preCommitPath, getLog());
        try {
            preCommit.install();
        } catch (Exception e) {
            getLog().error("Failed to install pre-commit hooks!");
            throw new MojoExecutionException("Failed to install pre-commit hooks!", e);
        }
        getLog().info("Pre-commit hooks successfully installed!");
    }
}
