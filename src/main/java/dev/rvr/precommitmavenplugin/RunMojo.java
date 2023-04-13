package dev.rvr.precommitmavenplugin;

import dev.rvr.precommitmavenplugin.util.DownloadUtil;
import dev.rvr.precommitmavenplugin.util.PreCommit;
import dev.rvr.precommitmavenplugin.util.PythonUtil;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.nio.file.Path;

@Mojo(name = "run")
public class RunMojo extends AbstractMojo {

    /**
     * The {@link MavenProject}
     */
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    /**
     * The path to the pre-commit cache directory
     */
    @Parameter(defaultValue = ".pre-commit-cache", property = "pre-commit-cache-path", required = false)
    private String preCommitCachePath;

    /**
     * The pre-commit command to run
     */
    @Parameter(property = "command", required = true)
    private String command;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().debug("Checking if pre-commit hooks are installed...");

        Path preCommitPath = project.getBasedir().toPath().resolve(preCommitCachePath);
        String version = DownloadUtil.getInstalledVersion(preCommitPath);

        if (version == null) {
            getLog().error("Pre-commit hooks are not installed!");
            throw new MojoExecutionException("Pre-commit hooks are not installed!");
        } else {
            getLog().debug("Pre-commit hooks are installed! Found version: " + version);
            getLog().info("Found pre-commit hooks! Version: " + version);
        }

        getLog().info("Running pre-commit command: " + command);
        try {
            PreCommit preCommit = new PreCommit(PythonUtil.findPythonPath(), version, preCommitPath, getLog());
            preCommit.run(command);
        } catch (Exception e) {
            getLog().error("Failed to run pre-commit command!");
            throw new MojoExecutionException("Failed to run pre-commit command!", e);
        }
    }
}
