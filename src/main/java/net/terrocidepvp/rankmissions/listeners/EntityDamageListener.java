package net.terrocidepvp.rankmissions.listeners;

import net.terrocidepvp.rankmissions.RankMissions;
import net.terrocidepvp.rankmissions.handlers.ActiveMission;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.List;
import java.util.Optional;

public class EntityDamageListener implements Listener {
    private RankMissions plugin;

    public EntityDamageListener(RankMissions plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity)) return;
        LivingEntity livingEntity = (LivingEntity) entity;
        Optional<Player> damager = Optional.empty();
        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent edbeEvent = (EntityDamageByEntityEvent) event;
            Entity damagerEntity = edbeEvent.getDamager();
            if (damagerEntity instanceof Player) {
                damager = Optional.of((Player) edbeEvent.getDamager());
            }
        }
        for (ActiveMission activeMission : plugin.getMissionManager().getActiveMissions()) {
            if (activeMission.getEntity() == livingEntity) {
                if (!activeMission.isStarted()) {
                    event.setCancelled(true);
                }
                if (damager.isPresent()) {
                    // Completed mission check.
                    List<String> completedMissions = plugin.getPluginDatabase().downloadCompletedMissions(damager.get().getUniqueId().toString());
                    List<String> requiredMissions = activeMission.getMission().getSettings().getRequiredMissions();
                    if (!requiredMissions.isEmpty()) {
                        for (String requiredMission : requiredMissions) {
                            if (!completedMissions.contains(requiredMission)) {
                                plugin.getConfigManager().getPluginMessages().getRequiredMissionsNotCompleted().forEach(damager.get()::sendMessage);
                                event.setCancelled(true);
                                return;
                            }
                        }
                    }
                    List<String> blacklistedMissions = activeMission.getMission().getSettings().getBlacklistIfMissionComplete();
                    if (!blacklistedMissions.isEmpty()) {
                        for (String blacklistedMission : blacklistedMissions) {
                            if (completedMissions.contains(blacklistedMission)) {
                                plugin.getConfigManager().getPluginMessages().getCompletedMissionThatBlacklists().forEach(damager.get()::sendMessage);
                                event.setCancelled(true);
                                return;
                            }
                        }
                    }
                    if (!activeMission.getMission().getSettings().isRepeatable()
                            && completedMissions.contains(activeMission.getMission().getMissionName())) {
                        plugin.getConfigManager().getPluginMessages().getNoRepeat().forEach(damager.get()::sendMessage);
                        event.setCancelled(true);
                        return;
                    }
                }
                return;
            }
        }
    }
}
