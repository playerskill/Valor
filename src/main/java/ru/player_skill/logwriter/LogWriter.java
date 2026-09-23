package ru.playerskill.logwriter;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class LogWriter extends JavaPlugin {
    private LogFileService logFileService;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        File pluginsDirectory = getDataFolder().getParentFile();
        File pluginDirectory = new File(pluginsDirectory, "logwriter");
        File logsDirectory = new File(pluginDirectory, "filelogs");

        try {
            if (!logsDirectory.mkdirs() && !logsDirectory.isDirectory()) {
                throw new IOException("Не удалось создать папку " + logsDirectory);
            }
        } catch (IOException exception) {
            getLogger().log(Level.SEVERE, ChatColor.stripColor(getMessage(
                    "console.directory-error", "%path%", logsDirectory.getPath())), exception);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        logFileService = new LogFileService(logsDirectory.toPath());

        PluginCommand logWrite = getCommand("logwrite");
        PluginCommand logWriter = getCommand("logwriter");
        if (logWrite == null || logWriter == null) {
            getLogger().severe(ChatColor.stripColor(getMessage("console.commands-missing")));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        logWrite.setExecutor(new LogWriteCommand(this, logFileService));
        logWriter.setExecutor(new LogWriterCommand(this));

        getLogger().info(ChatColor.stripColor(getMessage("console.enabled")));
        getLogger().info(ChatColor.stripColor(getMessage(
                "console.logs-folder", "%path%", "plugins/logwriter/filelogs/")));
    }

    @Override
    public void onDisable() {
        getLogger().info(ChatColor.stripColor(getMessage("console.disabled")));
    }

    public String getMessage(String path, String... replacements) {
        String message = getConfig().getString(path, path);
        for (int index = 0; index + 1 < replacements.length; index += 2) {
            message = message.replace(replacements[index], replacements[index + 1]);
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
