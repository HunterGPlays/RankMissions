package net.terrocidepvp.rankmissions.commands;

import net.terrocidepvp.rankmissions.RankMissions;
import net.terrocidepvp.rankmissions.configuration.Item;
import net.terrocidepvp.rankmissions.configuration.Mission;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class CommandManager implements CommandExecutor {
    private RankMissions plugin;
    public CommandManager(RankMissions plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            plugin.getConfigManager().getPluginMessages().getHelpMenu().forEach(sender::sendMessage);
            return true;
        }
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("help")) {
                plugin.getConfigManager().getPluginMessages().getHelpMenu().forEach(sender::sendMessage);
                return true;
            }
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("giveall")
                    && sender.hasPermission("rankmissions.admin")) {
                for (Mission mission : plugin.getConfigManager().getMissions()) {
                    if (mission.getMissionName().equalsIgnoreCase(args[1])) {
                        Item item = mission.getItem();
                        ItemStack itemStack = new ItemStack(item.getType(), 1, item.getData());
                        ItemMeta itemMeta = itemStack.getItemMeta();
                        itemMeta.setDisplayName(item.getName());
                        itemMeta.setLore(item.getLore());
                        itemStack.setItemMeta(itemMeta);
                        for (Player player : plugin.getServer().getOnlinePlayers()) {
                            Inventory inventory = player.getInventory();
                            plugin.getConfigManager().getPluginMessages().getGivenItem().forEach(str -> player.sendMessage(str
                                    .replace("%mission%", mission.getMissionName())));
                            if (inventory.firstEmpty() != -1) {
                                inventory.addItem(itemStack);
                            } else {
                                player.getWorld().dropItem(player.getLocation(), itemStack);
                                plugin.getConfigManager().getPluginMessages().getNoSpace().forEach(player::sendMessage);
                            }
                        }
                        sender.sendMessage(ChatColor.GREEN + "Given all online players a " + args[1] + " mission!");
                        return true;
                    }
                }
                sender.sendMessage("That is not a valid mission!");
            } else if (args[0].equalsIgnoreCase("stats")) {
                //noinspection deprecation
                OfflinePlayer player = plugin.getServer().getOfflinePlayer(args[1]);
                List<String> completedMissions = plugin.getPluginDatabase().downloadCompletedMissions(player.getUniqueId().toString());
                if (completedMissions.size() != 0) {
                    sender.sendMessage(ChatColor.AQUA + player.getName() + ChatColor.GRAY + " has completed " + completedMissions.size() + " missions:");
                    completedMissions.forEach(str -> sender.sendMessage(ChatColor.DARK_GRAY + " - " + ChatColor.WHITE + str));
                } else {
                    sender.sendMessage(ChatColor.AQUA + player.getName() + ChatColor.GRAY + " hasn't completed any missions.");
                }
                return true;
            }
        }
        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("give")
                    && sender.hasPermission("rankmissions.admin")) {
                //noinspection deprecation
                Player player = plugin.getServer().getPlayer(args[1]);
                if (player == null
                        || !player.isOnline()) {
                    sender.sendMessage(ChatColor.RED + "The player '" + args[1] + "' is not online!");
                    return true;
                }
                for (Mission mission : plugin.getConfigManager().getMissions()) {
                    if (mission.getMissionName().equalsIgnoreCase(args[2])) {
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
                        return true;
                    }
                }
                sender.sendMessage("That is not a valid mission!");
            }
        }
        plugin.getConfigManager().getPluginMessages().getInvalidArguments().forEach(sender::sendMessage);
        return true;
    }
}
