package dev.rvr.precommitmavenplugin.util;

import com.google.common.hash.Hashing;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DownloadUtil {
    private static final String PRE_COMMIT_DOWNLOAD_URL = "https://github.com/pre-commit/pre-commit/releases/download/v{version}/pre-commit-{version}.pyz";
    private static final String PRE_COMMIT_DOWNLOAD_SHA256_URL = "https://github.com/pre-commit/pre-commit/releases/download/v{version}/pre-commit-{version}.pyz.sha256sum";
    private static final Pattern VERSION_PATTERN = Pattern.compile("^\\d+\\.\\d+\\.\\d+$");
    private static final Pattern FILE_PATTERN = Pattern.compile("^pre-commit-(\\d+\\.\\d+\\.\\d+)\\.pyz$");

    /**
     * Get the file name of the pre-commit executable
     * @param version the version of the pre-commit executable
     * @return the file name of the pre-commit executable
     */
    public static String getPrecommitFileName(String version) {
        return "pre-commit-" + version + ".pyz";
    }

    /**
     * Download a specific version of pre-commit and save it to the given folder
     * @param version the version of pre-commit to download
     * @param folder the folder to save the pre-commit executable to
     * @throws DownloadException if the download fails
     */
    public static void downloadPreCommit(String version, Path folder) throws DownloadException {
        Validate.isTrue(VERSION_PATTERN.matcher(version).matches(), "Invalid version: " + version);

        try (BufferedInputStream in = new BufferedInputStream(getDownloadUrl(PRE_COMMIT_DOWNLOAD_URL, version).openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(folder.resolve(getPrecommitFileName(version)).toFile())) {
            byte dataBuffer[] = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
        } catch (IOException e) {
            throw new DownloadException("Failed to download pre-commit", e);
        }
    }

    /**
     * Validate the hash of the pre-commit executable
     * @param version the version of the pre-commit executable
     * @param folder the folder where the pre-commit executable is located
     * @return true if the hash is valid, false otherwise
     * @throws DownloadException if the download of the hash fails
     */
    public static boolean validateHash(String version, Path folder) throws DownloadException {
        Validate.isTrue(VERSION_PATTERN.matcher(version).matches(), "Invalid version: " + version);
        try {
            String hash = IOUtils.toString(getDownloadUrl(PRE_COMMIT_DOWNLOAD_SHA256_URL, version), StandardCharsets.UTF_8);
            File file = folder.resolve(getPrecommitFileName(version)).toFile();
            ByteSource source = Files.asByteSource(file);
            return source.hash(Hashing.sha256()).toString().equals(hash);
        } catch (IOException e) {
            throw new DownloadException("Failed to download pre-commit hash", e);
        }
    }

    /**
     * Check whether a specific version of pre-commit is already downloaded
     * @param version the version of pre-commit to check
     * @param folder the folder where the pre-commit executable is located
     * @return true if the pre-commit executable is already downloaded, false otherwise
     * @throws DownloadException if the download of the hash fails
     */
    public static boolean isDownloaded(String version, Path folder) throws DownloadException {
        Validate.isTrue(VERSION_PATTERN.matcher(version).matches(), "Invalid version: " + version);
        File file = folder.resolve(getPrecommitFileName(version)).toFile();

        return file.exists() && validateHash(version, folder);
    }

    /**
     * Get the latest version of pre-commit
     * @return the latest version of pre-commit
     * @throws IOException if the github api request fails
     */
    public static String getLatestVersion() throws IOException {
        String json = IOUtils.toString(new URL("https://api.github.com/repos/pre-commit/pre-commit/releases/latest"), StandardCharsets.UTF_8);
        JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
        return obj.get("tag_name").getAsString().substring(1);
    }

    /**
     * Get the installed version of pre-commit
     * @param folder the folder where the pre-commit executable is located
     * @return the installed version of pre-commit or null if no version is installed
     */
    public static String getInstalledVersion(Path folder){
        if (!folder.toFile().exists() || !folder.toFile().isDirectory())
            return null;

        for (File file : folder.toFile().listFiles()) {
            Matcher matcher = FILE_PATTERN.matcher(file.getName());
            if (file.isFile() && matcher.matches()) {
                return matcher.group(1);
            }
        }
        return null;
    }

    /**
     * Get the download url for a specific version
     * @param url the url template
     * @param version the version
     * @return the download url
     */
    private static URL getDownloadUrl(String url, String version) {
        Validate.isTrue(VERSION_PATTERN.matcher(version).matches(), "Invalid version: " + version);
        try {
            return new URL(url.replaceAll("\\{version}", version));
        } catch (MalformedURLException e) {

            // Never throw as per assertion of this method
            throw new AssertionError(e);
        }
    }
}
