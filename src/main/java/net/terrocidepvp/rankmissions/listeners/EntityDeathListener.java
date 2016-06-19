package net.terrocidepvp.rankmissions.listeners;

import net.terrocidepvp.rankmissions.RankMissions;
import net.terrocidepvp.rankmissions.handlers.ActiveMission;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.ArrayList;
import java.util.List;

public class EntityDeathListener implements Listener {
    private RankMissions plugin;

    public EntityDeathListener(RankMissions plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        for (ActiveMission activeMission : plugin.getMissionManager().getActiveMissions()) {
            if (activeMission.getEntity() == entity) {
                Location location = entity.getLocation();
                // Run commands on mission complete.
                if (entity.getKiller() != null) {
                    activeMission.getMission().getActions().getOnComplete().forEach(str -> plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(),
                            str.replace("%killer%", entity.getKiller().getName())
                                    .replace("%player%", activeMission.getDeployedBy().getName())
                                    .replace("%mission%", activeMission.getMission().getMissionName())
                                    .replace("%x%", Integer.toString(location.getBlockX()))
                                    .replace("%y%", Integer.toString(location.getBlockY()))
                                    .replace("%z%", Integer.toString(location.getBlockZ()))));
                } else {
                    plugin.getConfigManager().getPluginMessages().getDiedByNaturalCauses().forEach(str ->
                            plugin.getServer().broadcastMessage(str
                                    .replace("%player%", activeMission.getDeployedBy().getName())
                                    .replace("%mission%", activeMission.getMission().getMissionName())
                                    .replace("%x%", Integer.toString(location.getBlockX()))
                                    .replace("%y%", Integer.toString(location.getBlockY()))
                                    .replace("%z%", Integer.toString(location.getBlockZ()))));
                }
                // Update player info in database.
                List<String> completedMissions = plugin.getPluginDatabase().downloadCompletedMissions(entity.getKiller().getUniqueId().toString());
                if (completedMissions == null) {
                    completedMissions = new ArrayList<>();
                }
                if (!completedMissions.contains(activeMission.getMission().getMissionName())) {
                    completedMissions.add(activeMission.getMission().getMissionName());
                }
                plugin.getPluginDatabase().updatePlayerData(entity.getKiller().getUniqueId().toString(), completedMissions);
                // Remove from mission manager.
                plugin.getMissionManager().getActiveMissions().remove(activeMission);
                return;
            }
        }
    }

}
