package me.pl.serverplugin;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ServerPlugin extends JavaPlugin implements Listener {

    // Список авторизованных в данный момент игроков
    public static final List<Player> authenticatedPlayers = new ArrayList<>();
    
    // Карта сессий: IP-адрес -> Время истечения сессии (в миллисекундах)
    private static final Map<String, Long> sessionCache = new HashMap<>();
    private static final long SESSION_DURATION = 3 * 60 * 60 * 1000; // 3 часа в миллисекундах

    public static File globalUserFile;
    private File logFile;

    @Override
    public void onEnable() {
        // Путь к файлам в КОРНЕВОЙ папке сервера
        File serverRootDir = getDataFolder().getParentFile().getParentFile();
        globalUserFile = new File(serverRootDir, "users.txt");
        logFile = new File(serverRootDir, "auth_logs.txt");

        // Создаем файлы, если их нет
        try {
            if (!globalUserFile.exists()) globalUserFile.createNewFile();
            if (!logFile.exists()) logFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Регистрация команд и ивентов
        this.getCommand("reg").setExecutor(new RegCommand(this));
        this.getCommand("login").setExecutor(new LoginCommand(this));
        getServer().getPluginManager().registerEvents(this, this);

        getLogger().info("SureLandAuth успешно запущен! База данных и логи созданы в корне сервера.");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String ip = player.getAddress().getAddress().getHostAddress();

        // Проверяем, есть ли активная сессия для этого IP
        if (sessionCache.containsKey(ip) && System.currentTimeMillis() < sessionCache.get(ip)) {
            // Если сессия жива, автоматически авторизуем игрока
            authenticatedPlayers.add(player);
            player.sendMessage(ChatColor.GREEN + " [BustMC] Автоматический вход по IP-сессии. Добро пожаловать!");
            logAction(player.getName(), ip, "AUTO_LOGIN_SESSION");
            return;
        }

        // Если сессии нет, просим войти или зарегистрироваться
        if (RegCommand.isRegistered(player.getName())) {
            player.sendMessage(ChatColor.YELLOW + " [BustMC] Пожалуйста, войдите: /login <пароль>");
        } else {
            player.sendMessage(ChatColor.RED + " [BustMC] Вы не зарегистрированы! Введите: /reg <пароль>");
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!authenticatedPlayers.contains(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        authenticatedPlayers.remove(player);
    }

    // Метод для создания/обновления сессии после успешного ввода пароля
    public static void createSession(String ip) {
        sessionCache.put(ip, System.currentTimeMillis() + SESSION_DURATION);
    }

    // Метод для записи логов в файл auth_logs.txt
    public void logAction(String username, String ip, String action) {
        try (FileWriter fw = new FileWriter(logFile, true);
             PrintWriter pw = new PrintWriter(fw)) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            pw.println("[" + dtf.format(LocalDateTime.now()) + "] Игрок: " + username + " | IP: " + ip + " | Действие: " + action);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
