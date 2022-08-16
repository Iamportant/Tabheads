package xyz.destiall.tabheads.bungee.commands;

import com.github.games647.craftapi.model.Profile;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import xyz.destiall.tabheads.bungee.TabheadsBungee;
import xyz.destiall.tabheads.bungee.auth.AuthMeBungeeHook;
import xyz.destiall.tabheads.core.PremiumProfile;
import xyz.destiall.tabheads.core.Tabheads;

import java.util.Optional;

public class Premium extends Command {
    public Premium() {
        super("premium", null, "legit");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) sender;
            if (args.length > 0) {
                String name = args[0];
                ProxiedPlayer target = ProxyServer.getInstance().getPlayer(name);
                Optional<PremiumProfile> profile;
                if (target == null) {
                    profile = Tabheads.get().getPremiumManager().loadProfile(name);
                } else {
                    profile = Tabheads.get().getPremiumManager().loadProfile(target.getName());
                }
                if (profile.isPresent()) {
                    PremiumProfile p = profile.get();
                    sender.sendMessage(TextComponent.fromLegacyText(ChatColor.GREEN + p.getName() + " is using premium!"));
                } else {
                    sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + name + " is not using premium!"));
                }
                return;
            }
            if (TabheadsBungee.INSTANCE.AMB && !AuthMeBungeeHook.isAuthed(player.getName())) return;

            Optional<PremiumProfile> premium = Tabheads.get().getPremiumManager().loadProfile(sender.getName());
            if (premium.isPresent()) {
                sender.sendMessage(TextComponent.fromLegacyText(Tabheads.get().getTabConfig().getMessage("already-premium", "&cYou are already registered as premium!")));
                return;
            }
            if (Tabheads.get().getPremiumManager().isPending(sender.getName())) {
                sender.sendMessage(TextComponent.fromLegacyText(Tabheads.get().getTabConfig().getMessage("already-pending", "&cYou are already pending to confirm your account!")));
                return;
            }
            try {
                Optional<Profile> profile = Tabheads.get().getResolver().findProfile(sender.getName());
                if (!profile.isPresent()) {
                    sender.sendMessage(TextComponent.fromLegacyText(Tabheads.get().getTabConfig().getMessage("name-not-registered", "&cThis name is not registered to a premium account!")));
                    return;
                }
            } catch (Exception e) {
                sender.sendMessage(TextComponent.fromLegacyText(Tabheads.get().getTabConfig().getMessage("server-overloaded", "&cThe server is being overloaded! Please wait a while to authenticate!")));
                return;
            }
            player.disconnect(TextComponent.fromLegacyText(Tabheads.get().getTabConfig().getMessage("kick-premium", "&aRejoin the server within 1 minute. If you are able to join successfully, then it means you are a premium player ;D")));
            Tabheads.get().getPremiumManager().addPending(sender.getName());
        }
    }
}
