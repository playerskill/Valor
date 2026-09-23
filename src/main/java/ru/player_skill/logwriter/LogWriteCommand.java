package ru.playerskill.logwriter;

import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public final class LogWriteCommand implements CommandExecutor {
    private final LogWriter plugin;
    private final LogFileService logFileService;

    public LogWriteCommand(LogWriter plugin, LogFileService logFileService) {
        this.plugin = plugin;
        this.logFileService = logFileService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("logwriter.write")) {
            sender.sendMessage(plugin.getMessage("messages.no-permission"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(plugin.getMessage("messages.write-usage"));
            return true;
        }

        final String fileName = args[0];
        if (!logFileService.isSafeFileName(fileName)) {
            sender.sendMessage(plugin.getMessage("messages.invalid-file-name"));
            return true;
        }
        if (!logFileService.hasSupportedExtension(fileName)) {
            sender.sendMessage(plugin.getMessage("messages.unsupported-extension"));
            return true;
        }

        final String message = joinArguments(args, 1);
        final String consoleWriteError = ChatColor.stripColor(
                plugin.getMessage("console.write-error", "%file%", fileName));
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                logFileService.append(fileName, message);
            } catch (Exception exception) {
                plugin.getLogger().log(Level.SEVERE, consoleWriteError, exception);
                sendResult(sender, "messages.write-error");
                return;
            }

            sendResult(sender, "messages.write-success", "%file%", fileName);
        });
        return true;
    }

    private String joinArguments(String[] args, int startIndex) {
        StringBuilder message = new StringBuilder(args[startIndex]);
        for (int index = startIndex + 1; index < args.length; index++) {
            message.append(' ').append(args[index]);
        }
        return message.toString();
    }

    private void sendResult(CommandSender sender, String messagePath, String... replacements) {
        if (!plugin.isEnabled()) {
            return;
        }
        plugin.getServer().getScheduler().runTask(plugin, () ->
                sender.sendMessage(plugin.getMessage(messagePath, replacements)));
    }
}
