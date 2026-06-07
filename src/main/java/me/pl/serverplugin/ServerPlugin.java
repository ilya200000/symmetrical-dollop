package me.pl.serverplugin;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class ServerPlugin extends JavaPlugin implements Listener {

    public static final List<Player> authenticatedPlayers = new ArrayList<>();

    @Override
    public void onEnable() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }

        this.getCommand("reg").setExecutor(new RegCommand(this));
        getServer().getPluginManager().registerEvents(this, this);

        getLogger().info("Плагин SureLandAuth для 1.21.11 успешно запущен!");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.sendMessage(ChatColor.RED + " [Auth] Зарегистрируйтесь! Введите команду: /reg <пароль>");
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (!authenticatedPlayers.contains(player)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + " [Auth] Движение заблокировано. Напишите: /reg <пароль>");
        }
    }
}
