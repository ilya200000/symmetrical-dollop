package me.pl.serverplugin;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.util.Scanner;

public class LoginCommand implements CommandExecutor {

    private final ServerPlugin plugin;

    public LoginCommand(ServerPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;

        if (ServerPlugin.authenticatedPlayers.contains(player)) {
            player.sendMessage(ChatColor.GREEN + "Вы уже вошли.");
            return true;
        }

        if (!RegCommand.isRegistered(player.getName())) {
            player.sendMessage(ChatColor.RED + "Вы еще не зарегистрированы! Используйте /reg <пароль>");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + " Использование: /login <пароль>");
            return true;
        }

        String inputPassword = args[0];
        String ip = player.getAddress().getAddress().getHostAddress();

        // Проверяем пароль по файлу users.txt
        if (checkPassword(player.getName(), inputPassword)) {
            ServerPlugin.authenticatedPlayers.add(player);
            ServerPlugin.createSession(ip); // Создаем сессию на 3 часа
            
            player.sendMessage(ChatColor.GREEN + "Вы успешно вошли! Приятной игры.");
            plugin.logAction(player.getName(), ip, "LOGIN_SUCCESS");
        } else {
            player.sendMessage(ChatColor.RED + "Неверный пароль!");
            plugin.logAction(player.getName(), ip, "LOGIN_FAILED (Wrong Password)");
        }

        return true;
    }

    private boolean checkPassword(String username, String password) {
        try (Scanner scanner = new Scanner(ServerPlugin.globalUserFile)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(":", 2);
                if (parts.length == 2 && parts[0].equals(username.toLowerCase()) && parts[1].equals(password)) {
                    return true;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }
}
