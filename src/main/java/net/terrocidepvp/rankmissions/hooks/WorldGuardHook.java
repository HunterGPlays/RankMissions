package net.terrocidepvp.rankmissions.hooks;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import net.terrocidepvp.rankmissions.RankMissions;
import org.bukkit.entity.Player;

public class WorldGuardHook {
    private WorldGuardPlugin wgPlugin;
    public WorldGuardHook(RankMissions plugin) {
        wgPlugin = (WorldGuardPlugin) plugin.getServer().getPluginManager().getPlugin("WorldGuard");
        plugin.getLogger().info("Hooked into WorldGuard for region checks.");
    }

    public boolean checkIfPlayerInRegion(Player player, String region) {
        final com.sk89q.worldedit.Vector v = new com.sk89q.worldedit.Vector(
                player.getLocation().getX(),
                player.getLocation().getBlockY(),
                player.getLocation().getZ());
        for (String s : wgPlugin.getRegionManager(player.getLocation().getWorld()).getApplicableRegionsIDs(v)) {
            if (s.equalsIgnoreCase(region)) return true;
        }
        return false;
    }
}
