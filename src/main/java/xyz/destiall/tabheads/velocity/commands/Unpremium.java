package xyz.destiall.tabheads.velocity.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import xyz.destiall.tabheads.core.PremiumProfile;
import xyz.destiall.tabheads.core.Tabheads;

import java.util.Optional;

public class Unpremium implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        CommandSource sender = invocation.source();

        if (sender instanceof Player) {
            Player player = (Player) sender;
            Optional<PremiumProfile> profile = Tabheads.get().getPremiumManager().loadProfile(player.getUsername());
            if (profile.isPresent()) {
                Tabheads.get().getPremiumManager().deleteProfile(player.getUsername());
                player.disconnect(LegacyComponentSerializer.legacyAmpersand().deserialize(Tabheads.get().getTabConfig().getMessage("kick-unpremium", "&aRejoin the server and you are no longer premium!")));
            } else {
                player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Tabheads.get().getTabConfig().getMessage("not-premium","&cYou are not premium!")));
            }
        }
    }
}
