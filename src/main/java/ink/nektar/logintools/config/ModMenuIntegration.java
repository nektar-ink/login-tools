package ink.nektar.logintools.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import ink.nektar.logintools.LoginTools;
import ink.nektar.logintools.screen.LoginToolsConfigScreen;

public final class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new LoginToolsConfigScreen(parent, LoginTools.getConfig().copy());
    }
}
