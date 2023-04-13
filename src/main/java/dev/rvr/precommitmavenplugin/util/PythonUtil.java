package dev.rvr.precommitmavenplugin.util;

import java.io.IOException;

public class PythonUtil {

    public static String findPythonPath() {
        if (getPythonVersion("python3") != null)
            return "python3";
        else if (getPythonVersion("python") != null)
            return "python";
        else return null;
    }

    public static String getPythonVersion(String pythonPath) {
        try {
            Process process = Runtime.getRuntime().exec(pythonPath + " --version");
            if (process.waitFor() == 0)
                return new String(process.getInputStream().readAllBytes());
            else return null;
        } catch (IOException | InterruptedException e) {
            return null;
        }
    }
}
