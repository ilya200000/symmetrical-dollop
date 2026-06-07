package me.pl.serverplugin;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public class RegCommand implements CommandExecutor {

    private final ServerPlugin plugin;

    public RegCommand(ServerPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Команда доступна только игрокам!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + " Ошибка! Правильное использование: /reg <пароль>");
            return true;
        }

        if (ServerPlugin.authenticatedPlayers.contains(player)) {
            player.sendMessage(ChatColor.GREEN + "Вы уже авторизованы.");
            return true;
        }

        String password = args[0];
        File dataFile = new File(plugin.getDataFolder(), "users.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(dataFile);

        if (config.contains("users." + player.getName().toLowerCase())) {
            player.sendMessage(ChatColor.RED + "Этот ник уже зарегистрирован!");
            return true;
        }

        config.set("users." + player.getName().toLowerCase(), password);
        try {
            config.save(dataFile);
        } catch (IOException e) {
            player.sendMessage(ChatColor.RED + "Не удалось сохранить пароль в базу данных сервером.");
            e.printStackTrace();
            return true;
        }

        ServerPlugin.authenticatedPlayers.add(player);
        player.sendMessage(ChatColor.GREEN + "Регистрация прошла успешно! Удачной игры на 1.21.11!");

        return true;
    }
}
