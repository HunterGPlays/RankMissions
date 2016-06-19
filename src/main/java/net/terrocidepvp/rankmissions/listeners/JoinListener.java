package net.terrocidepvp.rankmissions.listeners;

import net.terrocidepvp.rankmissions.RankMissions;
import net.terrocidepvp.rankmissions.configuration.Item;
import net.terrocidepvp.rankmissions.configuration.Mission;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class JoinListener implements Listener {
    private RankMissions plugin;

    public JoinListener(RankMissions plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        List<String> queuedMissions = plugin.getPluginDatabase().downloadQueuedMissions(player.getUniqueId().toString());
        boolean changes = false;
        if (!queuedMissions.isEmpty()) {
            for (Mission mission : plugin.getConfigManager().getMissions()) {
                if (queuedMissions.contains(mission.getMissionName())) {
                    // Generate item.
                    Item item = mission.getItem();
                    ItemStack itemStack = new ItemStack(item.getType(), 1, item.getData());
                    ItemMeta itemMeta = itemStack.getItemMeta();
                    itemMeta.setDisplayName(item.getName());
                    itemMeta.setLore(item.getLore());
                    itemStack.setItemMeta(itemMeta);
                    Inventory inventory = player.getInventory();
                    plugin.getConfigManager().getPluginMessages().getGivenItem().forEach(str -> player.sendMessage(str
                            .replace("%mission%", mission.getMissionName())));
                    if (inventory.firstEmpty() != -1) {
                        inventory.addItem(itemStack);
                    } else {
                        player.getWorld().dropItem(player.getLocation(), itemStack);
                        plugin.getConfigManager().getPluginMessages().getNoSpace().forEach(player::sendMessage);
                    }
                    queuedMissions.remove(mission.getMissionName());
                    changes = true;
                }
            }
        }
        if (changes) {
            plugin.getPluginDatabase().updateQueuedData(player.getUniqueId().toString(), queuedMissions);
        }

    }
}
