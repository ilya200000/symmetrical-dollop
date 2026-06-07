package me.pl.serverplugin;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.Scanner;

public class RegCommand implements CommandExecutor {

    private final ServerPlugin plugin;

    public RegCommand(ServerPlugin plugin) {
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

        if (isRegistered(player.getName())) {
            player.sendMessage(ChatColor.RED + "Этот ник уже зарегистрирован! Используйте /login <пароль>");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + " Использование: /reg <пароль>");
            return true;
        }

        String password = args[0];
        String ip = player.getAddress().getAddress().getHostAddress();

        // Запись в users.txt в формате имя:пароль
        try (FileWriter fw = new FileWriter(ServerPlugin.globalUserFile, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println(player.getName().toLowerCase() + ":" + password);
        } catch (IOException e) {
            player.sendMessage(ChatColor.RED + "Ошибка сохранения на сервере!");
            return true;
        }

        ServerPlugin.authenticatedPlayers.add(player);
        ServerPlugin.createSession(ip); // Создаем сессию на 3 часа
        
        player.sendMessage(ChatColor.GREEN + "Регистрация успешна! Сессия на 3 часа активирована.");
        plugin.logAction(player.getName(), ip, "REGISTER");

        return true;
    }

    // Проверка, есть ли игрок в файле users.txt
    public static boolean isRegistered(String username) {
        try (Scanner scanner = new Scanner(ServerPlugin.globalUserFile)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.startsWith(username.toLowerCase() + ":")) {
                    return true;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }
}
