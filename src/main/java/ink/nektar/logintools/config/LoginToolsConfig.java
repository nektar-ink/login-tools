package ink.nektar.logintools.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class LoginToolsConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private AutoLoginMode autoLoginMode = AutoLoginMode.NO;
    private boolean hidePassword = true;
    private PasswordScope passwordScope = PasswordScope.GLOBAL;
    private String globalPassword = "";
    private List<ServerPasswordEntry> serverPasswords = new ArrayList<>();

    public static LoginToolsConfig load(Path path) {
        if (!Files.exists(path)) {
            return new LoginToolsConfig();
        }

        try (Reader reader = Files.newBufferedReader(path)) {
            LoginToolsConfig config = GSON.fromJson(reader, LoginToolsConfig.class);
            return config != null ? config.sanitized() : new LoginToolsConfig();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read config", e);
        }
    }

    public void save(Path path) {
        try (Writer writer = Files.newBufferedWriter(path)) {
            GSON.toJson(this.sanitized(), writer);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write config", e);
        }
    }

    public LoginToolsConfig copy() {
        return this.sanitized();
    }

    public String findPasswordForServer(String address) {
        if (this.passwordScope == PasswordScope.GLOBAL) {
            return this.globalPassword.isBlank() ? null : this.globalPassword;
        }

        String normalized = normalizeAddress(address);
        for (ServerPasswordEntry entry : this.serverPasswords) {
            if (normalizeAddress(entry.address()).equals(normalized) && !entry.password().isBlank()) {
                return entry.password();
            }
        }

        int portSeparator = normalized.indexOf(':');
        if (portSeparator > 0) {
            String hostOnly = normalized.substring(0, portSeparator);
            for (ServerPasswordEntry entry : this.serverPasswords) {
                if (normalizeAddress(entry.address()).equals(hostOnly) && !entry.password().isBlank()) {
                    return entry.password();
                }
            }
        }

        return null;
    }

    public AutoLoginMode autoLoginMode() {
        return this.autoLoginMode;
    }

    public void setAutoLoginMode(AutoLoginMode autoLoginMode) {
        this.autoLoginMode = autoLoginMode;
    }

    public boolean hidePassword() {
        return this.hidePassword;
    }

    public void setHidePassword(boolean hidePassword) {
        this.hidePassword = hidePassword;
    }

    public PasswordScope passwordScope() {
        return this.passwordScope;
    }

    public void setPasswordScope(PasswordScope passwordScope) {
        this.passwordScope = passwordScope;
    }

    public String globalPassword() {
        return this.globalPassword;
    }

    public void setGlobalPassword(String globalPassword) {
        this.globalPassword = globalPassword;
    }

    public List<ServerPasswordEntry> serverPasswords() {
        return this.serverPasswords;
    }

    private LoginToolsConfig sanitized() {
        LoginToolsConfig copy = new LoginToolsConfig();
        copy.autoLoginMode = this.autoLoginMode == null ? AutoLoginMode.NO : this.autoLoginMode;
        copy.hidePassword = this.hidePassword;
        copy.passwordScope = this.passwordScope == null ? PasswordScope.GLOBAL : this.passwordScope;
        copy.globalPassword = this.globalPassword == null ? "" : this.globalPassword;
        copy.serverPasswords = new ArrayList<>();

        if (this.serverPasswords != null) {
            for (ServerPasswordEntry entry : this.serverPasswords) {
                if (entry != null) {
                    copy.serverPasswords.add(new ServerPasswordEntry(entry.address(), entry.password()));
                }
            }
        }

        return copy;
    }

    private static String normalizeAddress(String address) {
        return address == null ? "" : address.trim().toLowerCase(Locale.ROOT);
    }

    public enum AutoLoginMode {
        YES,
        ASK,
        NO;

        public String displayText() {
            return switch (this) {
                case YES -> "Yes";
                case ASK -> "Ask";
                case NO -> "No";
            };
        }

        public AutoLoginMode next() {
            return switch (this) {
                case YES -> ASK;
                case ASK -> NO;
                case NO -> YES;
            };
        }
    }

    public enum PasswordScope {
        GLOBAL,
        CERTAIN;

        public String displayText() {
            return switch (this) {
                case GLOBAL -> "Global";
                case CERTAIN -> "Certain";
            };
        }

        public PasswordScope next() {
            return switch (this) {
                case GLOBAL -> CERTAIN;
                case CERTAIN -> GLOBAL;
            };
        }
    }

    public record ServerPasswordEntry(String address, String password) {
        public ServerPasswordEntry {
            address = address == null ? "" : address;
            password = password == null ? "" : password;
        }
    }
}
