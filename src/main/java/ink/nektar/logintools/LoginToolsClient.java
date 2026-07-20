package ink.nektar.logintools;

import ink.nektar.logintools.client.AutoLoginController;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public final class LoginToolsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> AutoLoginController.handleJoin(client));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> AutoLoginController.clearPending());
        ClientTickEvents.END_CLIENT_TICK.register(AutoLoginController::tick);
    }
}
