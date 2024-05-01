package xyz.destiall.tabheads.velocity.commands;

import com.github.games647.craftapi.model.Profile;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import xyz.destiall.tabheads.bungee.TabheadsBungee;
import xyz.destiall.tabheads.bungee.auth.AuthMeBungeeHook;
import xyz.destiall.tabheads.core.PremiumProfile;
import xyz.destiall.tabheads.core.Tabheads;
import xyz.destiall.tabheads.velocity.TabheadsVelocity;
import xyz.destiall.tabheads.velocity.auth.AuthMeVelocityHook;
import xyz.destiall.tabheads.velocity.misc.BungeeChatColor;

import java.util.Optional;

public class Premium implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        CommandSource sender = invocation.source();
        String[] args = invocation.arguments();

        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length > 0) {
                String name = args[0];
                Player target = TabheadsVelocity.INSTANCE.getServer().getPlayer(name).orElse(null);
                Optional<PremiumProfile> profile;
                if (target == null) {
                    profile = Tabheads.get().getPremiumManager().loadProfile(name);
                } else {
                    profile = Tabheads.get().getPremiumManager().loadProfile(target.getUsername());
                }
                if (profile.isPresent()) {
                    PremiumProfile p = profile.get();
                    sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(BungeeChatColor.GREEN + p.getName() + " is using premium!"));
                } else {
                    sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(BungeeChatColor.RED + name + " is not using premium!"));
                }
                return;
            }
            if (TabheadsVelocity.INSTANCE.AMB && !AuthMeVelocityHook.isAuthed(player)) return;

            Optional<PremiumProfile> premium = Tabheads.get().getPremiumManager().loadProfile(player.getUsername());
            if (premium.isPresent()) {
                sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Tabheads.get().getTabConfig().getMessage("already-premium", "&cYou are already registered as premium!")));
                return;
            }
            if (Tabheads.get().getPremiumManager().isPending(((Player) sender).getUsername())) {
                sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Tabheads.get().getTabConfig().getMessage("already-pending", "&cYou are already pending to confirm your account!")));
                return;
            }
            try {
                Optional<Profile> profile = Tabheads.get().getResolver().findProfile(player.getUsername());
                if (!profile.isPresent()) {
                    sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Tabheads.get().getTabConfig().getMessage("name-not-registered", "&cThis name is not registered to a premium account!")));
                    return;
                }
            } catch (Exception e) {
                sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Tabheads.get().getTabConfig().getMessage("server-overloaded", "&cThe server is being overloaded! Please wait a while to authenticate!")));
                return;
            }
            player.disconnect(LegacyComponentSerializer.legacyAmpersand().deserialize(Tabheads.get().getTabConfig().getMessage("kick-premium", "&aRejoin the server within 1 minute. If you are able to join successfully, then it means you are a premium player ;D")));
            Tabheads.get().getPremiumManager().addPending(player.getUsername());
        }
    }
}
