package me.huljak.survivaladdons;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Shulker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main extends JavaPlugin implements Listener {

    final String prefix = ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("prefix"));

    @Override
    public void onEnable() {
        Bukkit.getLogger().info(prefix + " Loaded!");
        this.saveDefaultConfig();
        this.reloadConfig();
        this.getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        Bukkit.getLogger().info(prefix + " Disabled!");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        //Join Message
        List<String> lines = this.getConfig().getStringList("join-message");
        for (String s : lines) {
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', s));
        }
        if (this.getConfig().contains(p.getName())) {
            Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
                Location loc = p.getLocation();

                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§6XYZ:§f " + (int) loc.getX() + " " + (int) loc.getY() + " " + (int) loc.getZ() + "   §6" + getTime(p.getWorld().getTime())));
            }, 0, this.getConfig().getLong("ticks"));
        }
    }

    //Player Head Drops
    @EventHandler
    public void onDeath(PlayerDeathEvent event) {

        ItemStack item = new ItemStack(Material.PLAYER_HEAD, 1, (byte) 1);
        SkullMeta sm = (SkullMeta) item.getItemMeta();
        sm.setOwner((event.getEntity().getName()));
        ArrayList<String> lore = new ArrayList<String>();
        lore.add("Killed by " + event.getEntity().getKiller().getName() );
        if (event.getEntity().getKiller() != null && event.getEntity().getKiller() instanceof Player) {
            sm.setLore(lore);
        }
        item.setItemMeta(sm);

        event.getDrops().add(item);
    }

    //Double Shulker Shells
    @EventHandler
    public void onMobDeath(EntityDeathEvent event) {
        Random rand = new Random();
        int r = rand.nextInt(100) + 1;
        if (r <= 50) {
            if (event.getEntity() instanceof Shulker) {
                event.getEntity().getWorld().dropItem(event.getEntity().getLocation(), new ItemStack(Material.SHULKER_SHELL));
            }
        }

    }

    //MOTD
    @EventHandler
    public void onServerListPing(ServerListPingEvent event) {
        if (this.getConfig().getString("motd") == null) {
            this.getConfig().set("motd", "&fThis server is using &6&lSurvivalAddons");
        } else {
            event.setMotd(ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("motd")));
        }

    }

    //Silence Mobs
    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        Player p = event.getPlayer();
        try {
            if (p.getItemInHand().getType().equals(Material.NAME_TAG)) {
                LivingEntity entity = (LivingEntity) event.getRightClicked();
                entity.setSilent(true);
                entity.setCustomName("Silenced");
                event.setCancelled(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Multiplayer Sleep
    @EventHandler
    public void onPlayerEnterBed(PlayerBedEnterEvent event) {
        Player p = event.getPlayer();
        World w = p.getWorld();
        this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {

            public void run() {
                setDateTime(w);
            }

        }, 10 * 20);

    }

    public void setDateTime(World world) {
        if (world.hasStorm()) {
            world.setStorm(false);
        }
        if (world.isThundering()) {
            world.setThundering(false);
        }
        long Relative_Time = 24000 - world.getTime();
        world.setFullTime(world.getFullTime() + Relative_Time);

    }

    static String getTime(long time) {
        String timeH = Long.toString((time / 1000L + 6L) % 24L);
        String timeM = String.format("%02d", time % 1000L * 60L / 1000L);
        return timeH + ":" + timeM;
    }


    //Coordinates
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!(this.getConfig().contains(player.getName()))) {
                this.getConfig().set(player.getName(), "true");
                this.saveConfig();
                this.reloadConfig();
                Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
                    Location loc = player.getLocation();

                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§6XYZ:§f " + (int) loc.getX() + " " + (int) loc.getY() + " " + (int) loc.getZ() + "   §6" + getTime(player.getWorld().getTime())));
                }, 0, this.getConfig().getLong("ticks"));
            } else {
                this.getConfig().set(player.getName(), null);
                this.saveConfig();
                this.reloadConfig();
                Bukkit.getScheduler().cancelTasks(this);
            }
        }

        return false;
    }

}
