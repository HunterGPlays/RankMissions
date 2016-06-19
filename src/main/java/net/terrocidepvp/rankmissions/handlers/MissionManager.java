package net.terrocidepvp.rankmissions.handlers;

import net.terrocidepvp.rankmissions.RankMissions;
import org.bukkit.Location;

import java.util.LinkedList;
import java.util.List;

public class MissionManager {
    private List<ActiveMission> activeMissions = new LinkedList<>();

    public MissionManager(RankMissions plugin) {
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> activeMissions.forEach(activeMission -> {
            // Manage entity deployment.
            if (!activeMission.isStarted()) {
                if (System.currentTimeMillis() - activeMission.getClickedAt() <= (activeMission.getMission().getSettings().getStartDelay() * 1000)) return;
                Location location = activeMission.getEntity().getLocation();
                activeMission.getMission().getActions().getOnStart().forEach(str -> plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(),
                        str.replace("%player%", activeMission.getDeployedBy().getName())
                                .replace("%mission%", activeMission.getMission().getMissionName())
                                .replace("%x%", Integer.toString(location.getBlockX()))
                                .replace("%y%", Integer.toString(location.getBlockY()))
                                .replace("%z%", Integer.toString(location.getBlockZ()))));
                activeMission.setStarted(true);
            }
        }), 20L, 20L);
    }

    public List<ActiveMission> getActiveMissions() {
        return activeMissions;
    }

    public void addActiveMission(ActiveMission activeMission) {
        activeMissions.add(activeMission);
    }
}
