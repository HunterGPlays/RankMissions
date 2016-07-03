package net.terrocidepvp.rankmissions.listeners;

import net.terrocidepvp.rankmissions.RankMissions;
import net.terrocidepvp.rankmissions.configuration.Item;
import net.terrocidepvp.rankmissions.configuration.Mission;
import net.terrocidepvp.rankmissions.handlers.ActiveMission;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class InteractListener implements Listener {
    private RankMissions plugin;
    private List<UUID> cooldowns = new LinkedList<>();

    public InteractListener(RankMissions plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.isAsynchronous()) {
            return;
        }

        if (!event.hasItem() ||
                event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack itemStack = event.getItem();
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null
                || !itemMeta.hasDisplayName()
                || !itemMeta.hasLore()) {
            return;
        }
        String displayName = itemMeta.getDisplayName();
        List<String> lore = itemMeta.getLore();

        Player player = event.getPlayer();
        for (Mission mission : plugin.getConfigManager().getMissions()) {
            // Item checks.
            Item item = mission.getItem();
            if (item.getType() != itemStack.getType()
                    || item.getData() != itemStack.getDurability()) continue;
            if (!(item.getName().equals(displayName)
                    && item.getLore().equals(lore))) continue;

            // Interact cooldown check.
            if (cooldowns.contains(player.getUniqueId())) {
                event.setCancelled(true);
                //noinspection deprecation
                player.updateInventory();
                return;
            } else {
                cooldowns.add(player.getUniqueId());
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> cooldowns.remove(player.getUniqueId()), 5L);
            }

            // Perm check.
            if (mission.getSettings().isUsePermission()
                    && !player.hasPermission("rankmissions." + mission.getMissionName())) {
                plugin.getConfigManager().getPluginMessages().getNoPermission().forEach(player::sendMessage);
                event.setCancelled(true);
                //noinspection deprecation
                player.updateInventory();
                return;
            }

            // Region check.
            if (plugin.getWorldGuardHook() != null
                    && !mission.getItem().getActivateInRegion().isEmpty()) {
                boolean inRegion = false;
                for (String regionToCheckAgainst : mission.getItem().getActivateInRegion()) {
                    if (plugin.getWorldGuardHook().checkIfPlayerInRegion(player, regionToCheckAgainst, plugin.getConfigManager().isRegionNeedsMobSpawningAllow())) {
                        inRegion = true;
                        break;
                    }
                }
                if (!inRegion) {
                    plugin.getConfigManager().getPluginMessages().getNotInRegion().forEach(player::sendMessage);
                    event.setCancelled(true);
                    //noinspection deprecation
                    player.updateInventory();
                    return;
                }
            }

            // Completed mission check.
            List<String> completedMissions = plugin.getPluginDatabase().downloadCompletedMissions(player.getUniqueId().toString());
            List<String> requiredMissions = mission.getSettings().getRequiredMissions();
            if (!requiredMissions.isEmpty()) {
                for (String requiredMission : requiredMissions) {
                    if (!completedMissions.contains(requiredMission)) {
                        plugin.getConfigManager().getPluginMessages().getRequiredMissionsNotCompleted().forEach(player::sendMessage);
                        event.setCancelled(true);
                        //noinspection deprecation
                        player.updateInventory();
                        return;
                    }
                }
            }
            List<String> blacklistedMissions = mission.getSettings().getBlacklistIfMissionComplete();
            if (!blacklistedMissions.isEmpty()) {
                for (String blacklistedMission : blacklistedMissions) {
                    if (completedMissions.contains(blacklistedMission)) {
                        plugin.getConfigManager().getPluginMessages().getCompletedMissionThatBlacklists().forEach(player::sendMessage);
                        event.setCancelled(true);
                        //noinspection deprecation
                        player.updateInventory();
                        return;
                    }
                }
            }
            if (!mission.getSettings().isRepeatable()
                    && completedMissions.contains(mission.getMissionName())) {
                plugin.getConfigManager().getPluginMessages().getNoRepeat().forEach(player::sendMessage);
                event.setCancelled(true);
                //noinspection deprecation
                player.updateInventory();
                return;
            }
            for (ActiveMission activeMission : plugin.getMissionManager().getActiveMissions()) {
                if (player == activeMission.getDeployedBy()) {
                    plugin.getConfigManager().getPluginMessages().getAlreadyCompletingMission().forEach(player::sendMessage);
                    event.setCancelled(true);
                    //noinspection deprecation
                    player.updateInventory();
                    return;
                }
            }

            Location location = event.getClickedBlock().getLocation();
            while (location.getBlock().getType().isSolid()) {
                location.add(0d, 1d, 0d);
            }
            // Spawn the entity.
            Entity entity = player.getWorld().spawnEntity(location, mission.getEntity().getMob());
            LivingEntity livingEntity;
            if (entity != null
                    && entity instanceof LivingEntity) {
                livingEntity = (LivingEntity) entity;
            } else {
                plugin.getConfigManager().getPluginMessages().getFailedToSpawnEntity().forEach(player::sendMessage);
                event.setCancelled(true);
                //noinspection deprecation
                player.updateInventory();
                return;
            }
            // Set name.
            livingEntity.setCustomName(mission.getEntity().getName()
                    .replace("%player%", player.getName())
                    .replace("%mission%", mission.getMissionName())
                    .replace("%x%", Integer.toString(location.getBlockX()))
                    .replace("%y%", Integer.toString(location.getBlockY()))
                    .replace("%z%", Integer.toString(location.getBlockZ())));
            // Apply potion effects.
            for (Map.Entry<String, Integer> stringIntegerEntry : mission.getEntity().getEffects().entrySet()) {
                String effectName = stringIntegerEntry.getKey();
                int effectAmplifier = stringIntegerEntry.getValue();
                livingEntity.addPotionEffect(new PotionEffect(
                        PotionEffectType.getByName(effectName),
                        Integer.MAX_VALUE,
                        effectAmplifier - 1), true);
            }
            // Set health.
            livingEntity.setMaxHealth(mission.getEntity().getHealth());
            livingEntity.setHealth(mission.getEntity().getHealth());
            // Deduct item.
            if (itemStack.getAmount() > 1) {
                itemStack.setAmount(itemStack.getAmount() - 1);
            } else {
                if (plugin.getServerVersion()[0] >= 1
                        && plugin.getServerVersion()[1] >= 9) {
                    boolean itemInMainHand = true;
                    ItemStack itemStackMain = player.getInventory().getItemInMainHand();
                    if (itemStackMain == null) {
                        itemInMainHand = false;
                    } else if (itemStackMain.getType() != item.getType()
                            || itemStackMain.getDurability() != item.getData()) {
                        itemInMainHand = false;
                    }
                    if (itemInMainHand) {
                        player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                    } else {
                        player.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
                    }
                } else {
                    //noinspection deprecation
                    player.getInventory().setItemInHand(new ItemStack(Material.AIR));
                }
            }
            mission.getActions().getOnClick().forEach(str -> plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(),
                    str.replace("%player%", player.getName())
                            .replace("%mission%", mission.getMissionName())
                            .replace("%x%", Integer.toString(location.getBlockX()))
                            .replace("%y%", Integer.toString(location.getBlockY()))
                            .replace("%z%", Integer.toString(location.getBlockZ()))));
            plugin.getMissionManager().addActiveMission(new ActiveMission(mission, player, livingEntity, false, System.currentTimeMillis()));
        }
    }
}
