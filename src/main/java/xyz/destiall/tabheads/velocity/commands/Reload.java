package xyz.destiall.tabheads.velocity.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import xyz.destiall.tabheads.core.Tabheads;
import xyz.destiall.tabheads.velocity.misc.BungeeChatColor;

public class Reload implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        CommandSource sender = invocation.source();

        if (sender.hasPermission("tabheads.reload") || sender.hasPermission("tabheads.*")) {
            Tabheads.get().getTabConfig().reload();
            sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(BungeeChatColor.GREEN + "Reloaded Tabheads"));
        }
    }
}
