package ink.nektar.logintools.client;

import ink.nektar.logintools.LoginTools;
import ink.nektar.logintools.config.LoginToolsConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class AutoLoginController {
    private static final int LOGIN_DELAY_TICKS = 40;

    private static int pendingTicks = -1;
    private static String pendingPassword;
    private static String pendingAddress;
    private static LoginToolsConfig.AutoLoginMode pendingMode;
    private static boolean awaitingAskDecision;
    private static boolean yWasDown;
    private static boolean nWasDown;

    private AutoLoginController() {
    }

    public static void handleJoin(MinecraftClient client) {
        clearPending();

        ServerInfo server = client.getCurrentServerEntry();
        if (server == null) {
            return;
        }

        LoginToolsConfig config = LoginTools.getConfig();
        if (config.autoLoginMode() == LoginToolsConfig.AutoLoginMode.NO) {
            return;
        }

        String password = config.findPasswordForServer(server.address);
        if (password == null || password.isBlank()) {
            return;
        }

        pendingTicks = LOGIN_DELAY_TICKS;
        pendingPassword = password;
        pendingAddress = server.address;
        pendingMode = config.autoLoginMode();
    }

    public static void tick(MinecraftClient client) {
        if (pendingTicks < 0 || client.player == null) {
            return;
        }

        if (pendingTicks > 0) {
            pendingTicks--;
            return;
        }

        if (pendingMode == LoginToolsConfig.AutoLoginMode.YES) {
            sendLogin(client);
            clearPending();
            return;
        }

        if (!awaitingAskDecision) {
            if (client.player != null) {
                client.player.sendMessage(Text.literal("Use password (" + LoginTools.maskPassword(pendingPassword) + ")? Y/N"), false);
            }
            awaitingAskDecision = true;
        }

        if (client.currentScreen != null) {
            return;
        }

        boolean yDown = InputUtil.isKeyPressed(client.getWindow(), GLFW.GLFW_KEY_Y);
        boolean nDown = InputUtil.isKeyPressed(client.getWindow(), GLFW.GLFW_KEY_N);

        if (yDown && !yWasDown) {
            sendLogin(client);
            clearPending();
            return;
        }

        if (nDown && !nWasDown) {
            if (client.player != null) {
                client.player.sendMessage(Text.literal("Auto login cancelled for " + pendingAddress + "."), false);
            }
            clearPending();
            return;
        }

        yWasDown = yDown;
        nWasDown = nDown;
    }

    public static void clearPending() {
        pendingTicks = -1;
        pendingPassword = null;
        pendingAddress = null;
        pendingMode = null;
        awaitingAskDecision = false;
        yWasDown = false;
        nWasDown = false;
    }

    private static void sendLogin(MinecraftClient client) {
        sendLogin(client, pendingPassword);
    }

    private static void sendLogin(MinecraftClient client, String password) {
        if (client.player != null && password != null && !password.isBlank()) {
            client.player.networkHandler.sendChatCommand("login " + password);
        }
    }
}
