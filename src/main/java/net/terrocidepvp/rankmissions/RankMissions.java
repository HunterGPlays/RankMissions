package net.terrocidepvp.rankmissions;

import net.terrocidepvp.rankmissions.commands.CommandManager;
import net.terrocidepvp.rankmissions.configuration.Item;
import net.terrocidepvp.rankmissions.databases.MySQL;
import net.terrocidepvp.rankmissions.databases.SQL;
import net.terrocidepvp.rankmissions.databases.SQLite;
import net.terrocidepvp.rankmissions.handlers.ActiveMission;
import net.terrocidepvp.rankmissions.handlers.ConfigManager;
import net.terrocidepvp.rankmissions.handlers.MissionManager;
import net.terrocidepvp.rankmissions.hooks.WorldGuardHook;
import net.terrocidepvp.rankmissions.listeners.EntityDamageListener;
import net.terrocidepvp.rankmissions.listeners.EntityDeathListener;
import net.terrocidepvp.rankmissions.listeners.InteractListener;
import net.terrocidepvp.rankmissions.listeners.JoinListener;
import net.terrocidepvp.rankmissions.utils.VersionUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RankMissions extends JavaPlugin {
    private SQL database;
    private Set<SQL> databases = new HashSet<>();
    private int[] serverVersion;
    private ConfigManager configManager;
    private MissionManager missionManager;
    private WorldGuardHook worldGuardHook;

    private static RankMissions instance;
    public static RankMissions getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        reloadConfig();

        if (!getConfig().isSet("config-version")) {
            getLogger().severe("The config.yml file is broken!");
            getLogger().severe("The plugin failed to detect a 'config-version'.");
            getLogger().severe("The plugin will not load until you generate a new, working config OR if you fix the config.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        int configVersion = 2;
        /*
         Updated for: Mob spawning region check toggle
         Release: v1.0.5
        */
        if (getConfig().getInt("config-version") != configVersion) {
            getLogger().severe("Your config is outdated!");
            getLogger().severe("The plugin will not load unless you change the config version to " + configVersion + ".");
            getLogger().severe("This means that you will need to reset your config, as there may have been major changes to the plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        configManager = new ConfigManager(this);
        missionManager = new MissionManager(this);

        databases.add(new MySQL(this));
        databases.add(new SQLite(this));
        setupDatabase();
        if (!database.checkConnection()) {
            getLogger().severe("Error with database! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
        }

        serverVersion = VersionUtil.getMCVersion(getServer().getVersion());
        getLogger().info("Running server version " + Integer.toString(serverVersion[0]) + "." + Integer.toString(serverVersion[1]));

        if (getServer().getPluginManager().isPluginEnabled("WorldEdit")
                && getServer().getPluginManager().isPluginEnabled("WorldGuard")) {
            worldGuardHook = new WorldGuardHook(this);
        } else {
            getLogger().info("We didn't hook into WorldGuard since it is not enabled. Region checks will not work!");
        }

        getLogger().info("Registering listeners...");
        getCommand("rankmissions").setExecutor(new CommandManager(this));
        new InteractListener(this);
        new EntityDamageListener(this);
        new EntityDeathListener(this);
        new JoinListener(this);
    }

    @Override
    public void onDisable() {
        getPluginDatabase().disconnect();
        for (ActiveMission activeMission : getMissionManager().getActiveMissions()) {
            // Kill living entity.
            activeMission.getEntity().setHealth(0);
            // If player isn't online, queue them.
            Player player = activeMission.getDeployedBy();
            if (!player.isOnline()) {
                List<String> queuedMissions = getPluginDatabase().downloadQueuedMissions(player.getUniqueId().toString());
                queuedMissions.add(activeMission.getMission().getMissionName());
                getPluginDatabase().updateQueuedData(player.getUniqueId().toString(), queuedMissions);
                continue;
            }
            // Generate item.
            Item item = activeMission.getMission().getItem();
            ItemStack itemStack = new ItemStack(item.getType(), 1, item.getData());
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(item.getName());
            itemMeta.setLore(item.getLore());
            itemStack.setItemMeta(itemMeta);
            // Give player the item.
            Inventory inventory = player.getInventory();
            getConfigManager().getPluginMessages().getGivenItem().forEach(str -> player.sendMessage(str
                    .replace("%mission%", activeMission.getMission().getMissionName())));
            if (inventory.firstEmpty() != -1) {
                inventory.addItem(itemStack);
            } else {
                player.getWorld().dropItem(player.getLocation(), itemStack);
                getConfigManager().getPluginMessages().getNoSpace().forEach(player::sendMessage);
            }
            // Remove active mission.
            getMissionManager().getActiveMissions().remove(activeMission);
        }
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    private boolean setupDatabase() {
        String type = getConfig().getString("database.type");
        database = null;
        for (SQL db : databases) {
            if (type.equalsIgnoreCase(db.getConfigName())) {
                database = db;
                getLogger().info("Database set to " + db.getConfigName() + ".");
                break;
            }
        }
        if (database == null) {
            getLogger().severe("Database type does not exist! Please use 'mysql' or 'sqlite'.");
            return false;
        }
        return true;
    }

    public SQL getPluginDatabase() {
        return database;
    }

    public MissionManager getMissionManager() {
        return missionManager;
    }

    public int[] getServerVersion() {
        return serverVersion;
    }

    public WorldGuardHook getWorldGuardHook() {
        return worldGuardHook;
    }
}
