package net.terrocidepvp.rankmissions.hooks;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.terrocidepvp.rankmissions.RankMissions;
import org.bukkit.entity.Player;

public class WorldGuardHook {
    private WorldGuardPlugin wgPlugin;
    public WorldGuardHook(RankMissions plugin) {
        wgPlugin = (WorldGuardPlugin) plugin.getServer().getPluginManager().getPlugin("WorldGuard");
        plugin.getLogger().info("Hooked into WorldGuard for region checks.");
    }

    public boolean checkIfPlayerInRegion(Player player, String region, boolean mobSpawningCheck) {
        final com.sk89q.worldedit.Vector v = new com.sk89q.worldedit.Vector(
                player.getLocation().getX(),
                player.getLocation().getBlockY(),
                player.getLocation().getZ());
        ApplicableRegionSet applicableRegions = wgPlugin.getRegionManager(player.getLocation().getWorld()).getApplicableRegions(v);
        // Check if mob spawning is allowed.
        if (!mobSpawningCheck
                || applicableRegions.testState(wgPlugin.wrapPlayer(player), DefaultFlag.MOB_SPAWNING)) {
            // Check if the region matches.
            for (ProtectedRegion protectedRegion : applicableRegions.getRegions()) {
                if (protectedRegion.getId().toLowerCase().equals(region.toLowerCase())) {
                    return true;
                }
            }
        }
        return false;
    }
}
