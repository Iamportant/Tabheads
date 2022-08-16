package xyz.destiall.tabheads.bungee.auth;

import fr.xephi.authmebungee.AuthMeBungee;
import fr.xephi.authmebungee.data.AuthPlayer;
import fr.xephi.authmebungee.services.AuthPlayerManager;
import net.md_5.bungee.api.ProxyServer;

import java.lang.reflect.Field;

public class AuthMeBungeeHook {
    private static AuthPlayerManager manager;

    private AuthMeBungeeHook() {}

    public static void setup() {
        AuthMeBungee plugin = ((AuthMeBungee) ProxyServer.getInstance().getPluginManager().getPlugin("AuthMeBungee"));
        try {
            Field field = plugin.getClass().getDeclaredField("authPlayerManager");
            field.setAccessible(true);
            manager = (AuthPlayerManager) field.get(plugin);
            field.setAccessible(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isAuthed(String name) {
        AuthPlayer player = manager.getAuthPlayer(name);
        return player != null && player.isLogged();
    }
}
