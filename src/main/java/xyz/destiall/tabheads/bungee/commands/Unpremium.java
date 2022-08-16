package xyz.destiall.tabheads.bungee.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import xyz.destiall.tabheads.core.PremiumProfile;
import xyz.destiall.tabheads.core.Tabheads;

import java.util.Optional;

public class Unpremium extends Command {
    public Unpremium() {
        super("unpremium", null, "cracked");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) sender;
            Optional<PremiumProfile> profile = Tabheads.get().getPremiumManager().loadProfile(player.getName());
            if (profile.isPresent()) {
                Tabheads.get().getPremiumManager().deleteProfile(player.getName());
                player.disconnect(TextComponent.fromLegacyText(Tabheads.get().getTabConfig().getMessage("kick-unpremium", "&aRejoin the server and you are no longer premium!")));
            } else {
                player.sendMessage(TextComponent.fromLegacyText(Tabheads.get().getTabConfig().getMessage("not-premium","&cYou are not premium!")));
            }
        }
    }
}
