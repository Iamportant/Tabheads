package xyz.destiall.tabheads.bukkit.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.destiall.tabheads.core.PremiumProfile;
import xyz.destiall.tabheads.core.Tabheads;

import java.util.Optional;

public class Unpremium implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) return false;
        Player player = (Player) commandSender;
        Optional<PremiumProfile> profile = Tabheads.get().getPremiumManager().loadProfile(player.getName());
        if (profile.isPresent()) {
            Tabheads.get().getPremiumManager().deleteProfile(player.getName());
            player.kickPlayer(Tabheads.get().getTabConfig().getMessage("kick-unpremium", "&aRejoin the server and you are no longer premium!"));
        } else {
            player.sendMessage(Tabheads.get().getTabConfig().getMessage("not-premium","&cYou are not premium!"));
        }
        return true;
    }
}
