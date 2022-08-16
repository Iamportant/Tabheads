package xyz.destiall.tabheads.bukkit.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import xyz.destiall.tabheads.core.Tabheads;

public class Reload implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (sender.hasPermission("tabheads.reload") || sender.hasPermission("tabheads.*")) {
            Tabheads.get().getTabConfig().reload();
            sender.sendMessage("Reloaded Tabheads");
        }
        return false;
    }
}
