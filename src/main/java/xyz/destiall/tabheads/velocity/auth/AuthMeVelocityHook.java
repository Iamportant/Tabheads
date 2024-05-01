package xyz.destiall.tabheads.velocity.auth;

import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import io.github._4drian3d.authmevelocity.api.velocity.AuthMeVelocityAPI;

public class AuthMeVelocityHook {
    private static AuthMeVelocityAPI manager;

    private AuthMeVelocityHook() {}

    public static void setup(ProxyServer server) {
        PluginContainer container = server.getPluginManager().getPlugin("authmevelocity").orElse(null);
        if (container == null)
            return;

        manager = (AuthMeVelocityAPI) container.getInstance().orElse(null);
    }

    public static boolean isAuthed(Player player) {
        return manager != null && manager.isLogged(player);
    }

    public static void logIn(Player player) {
        if (manager == null)
            return;

        manager.addPlayer(player);
    }
}
