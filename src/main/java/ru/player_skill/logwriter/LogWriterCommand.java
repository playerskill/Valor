package ru.player_skill.logwriter;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public final class LogWriterCommand implements CommandExecutor {
    private final LogWriter plugin;

    public LogWriterCommand(LogWriter plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("logwriter.reload")) {
            sender.sendMessage(plugin.getMessage("messages.no-permission"));
            return true;
        }

        if (args.length != 1 || !args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage(plugin.getMessage("messages.reload-usage"));
            return true;
        }

        plugin.reloadConfig();
        sender.sendMessage(plugin.getMessage("messages.reload-success"));
        return true;
    }
}
