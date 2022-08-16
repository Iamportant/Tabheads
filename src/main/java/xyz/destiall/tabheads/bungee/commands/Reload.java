package xyz.destiall.tabheads.bungee.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import xyz.destiall.tabheads.core.Tabheads;

public class Reload extends Command {
    public Reload() {
        super("tabheadsreload");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender.hasPermission("tabheads.reload") || sender.hasPermission("tabheads.*")) {
            Tabheads.get().getTabConfig().reload();
            sender.sendMessage(TextComponent.fromLegacyText("Reloaded Tabheads"));
        }
    }
}
