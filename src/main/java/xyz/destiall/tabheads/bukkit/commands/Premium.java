package xyz.destiall.tabheads.bukkit.commands;

import com.github.games647.craftapi.model.Profile;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.destiall.tabheads.core.PremiumProfile;
import xyz.destiall.tabheads.core.Tabheads;

import java.util.Optional;

public class Premium implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (!(commandSender instanceof Player)) return false;
        Player sender = (Player) commandSender;
        if (args.length > 0) {
            String name = args[0];
            Player target = Bukkit.getPlayer(name);
            Optional<PremiumProfile> profile;
            if (target == null) {
                profile = Tabheads.get().getPremiumManager().loadProfile(name);
            } else {
                profile = Tabheads.get().getPremiumManager().loadProfile(target.getName());
            }
            if (profile.isPresent()) {
                PremiumProfile p = profile.get();
                sender.sendMessage(ChatColor.GREEN + p.getName() + " is using premium!");
            } else {
                sender.sendMessage(ChatColor.RED + name + " is not using premium!");
            }
            return true;
        }
        Optional<PremiumProfile> premium = Tabheads.get().getPremiumManager().loadProfile(sender.getName());
        if (premium.isPresent()) {
            sender.sendMessage(Tabheads.get().getTabConfig().getMessage("already-premium", "&cYou are already registered as premium!"));
            return true;
        }
        if (Tabheads.get().getPremiumManager().isPending(sender.getName())) {
            sender.sendMessage(Tabheads.get().getTabConfig().getMessage("already-pending", "&cYou are already pending to confirm your account!"));
            return true;
        }
        try {
            Optional<Profile> profile = Tabheads.get().getResolver().findProfile(sender.getName());
            if (!profile.isPresent()) {
                sender.sendMessage(Tabheads.get().getTabConfig().getMessage("name-not-registered", "&cThis name is not registered to a premium account!"));
                return true;
            }
        } catch (Exception e) {
            sender.sendMessage(Tabheads.get().getTabConfig().getMessage("server-overloaded", "&cThe server is being overloaded! Please wait a while to authenticate!"));
            return true;
        }
        sender.kickPlayer(Tabheads.get().getTabConfig().getMessage("kick-premium", "&aRejoin the server within 1 minute. If you are able to join successfully, then it means you are a premium player ;D"));
        Tabheads.get().getPremiumManager().addPending(sender.getName());
        return true;
    }
}
