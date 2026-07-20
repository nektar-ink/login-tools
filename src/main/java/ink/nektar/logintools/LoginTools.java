package ink.nektar.logintools;

import ink.nektar.logintools.config.LoginToolsConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

public final class LoginTools {
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("login-tools.json");
    private static LoginToolsConfig config = LoginToolsConfig.load(CONFIG_PATH);

    private LoginTools() {
    }

    public static LoginToolsConfig getConfig() {
        return config;
    }

    public static void setConfig(LoginToolsConfig newConfig) {
        config = newConfig;
    }

    public static void saveConfig() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
        } catch (IOException e) {
            throw new RuntimeException("Failed to create config directory", e);
        }

        config.save(CONFIG_PATH);
    }

    public static boolean shouldMaskLoginCommand(String text) {
        return config.hidePassword() && text != null && text.toLowerCase().startsWith("/login");
    }

    public static String maskedLoginText(String text) {
        if (text == null) {
            return "";
        }

        String trimmed = text.trim();
        if (!trimmed.toLowerCase().startsWith("/login")) {
            return text;
        }

        int commandLength = "/login".length();
        if (trimmed.length() <= commandLength) {
            return "/login";
        }

        String password = trimmed.substring(commandLength).trim();
        if (password.isEmpty()) {
            return "/login";
        }

        return "/login " + "*".repeat(password.length());
    }

    public static String maskPassword(String password) {
        if (password == null || password.isEmpty()) {
            return "";
        }

        return "*".repeat(password.length());
    }
}
