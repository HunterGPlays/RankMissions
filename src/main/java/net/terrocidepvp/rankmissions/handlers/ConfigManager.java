package net.terrocidepvp.rankmissions.handlers;

import net.terrocidepvp.rankmissions.RankMissions;
import net.terrocidepvp.rankmissions.configuration.*;
import net.terrocidepvp.rankmissions.utils.ColorCodeUtil;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionEffectType;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class ConfigManager {
    private List<Mission> missions = new LinkedList<>();
    private PluginMessages pluginMessages;

    public ConfigManager(RankMissions plugin) {
        FileConfiguration config = plugin.getConfig();
        Logger logger = plugin.getLogger();
        config.getConfigurationSection("missions").getKeys(false).forEach(key -> {
            String itemType = config.getString("missions." + key + ".item.type");
            if (itemType == null) {
                logger.severe("The item type at 'missions." + key + ".item.type' is null (empty)! Skipping mission.");
                return;
            }
            Material itemMaterial = Material.getMaterial(itemType);
            if (itemMaterial == null) {
                logger.severe("The item material at 'missions." + key + ".item.type' is invalid! Skipping mission.");
                return;
            }
            short itemData = (short) config.getInt("missions." + key + ".item.data");
            String itemName = config.getString("missions." + key + ".item.name");
            if (itemName == null) {
                logger.severe("The item name at 'missions." + key + ".item.name' is null (empty)! Skipping mission.");
                return;
            }
            itemName = ColorCodeUtil.translate(itemName);
            List<String> itemLore = config.getStringList("missions." + key + ".item.lore");
            if (itemLore == null) {
                logger.severe("The item lore at 'missions." + key + ".item.lore' is null (empty)! Skipping mission.");
                return;
            }
            itemLore = ColorCodeUtil.translate(itemLore);
            List<String> activateInRegion = config.getStringList("missions." + key + ".item.activate-in-region");
            Item item = new Item(itemMaterial, itemData, itemName, itemLore, activateInRegion);

            int startDelay = config.getInt("missions." + key + ".settings.start-delay");
            boolean usePermission = config.getBoolean("missions." + key + ".settings.use-permission");
            boolean repeatable = config.getBoolean("missions." + key + ".settings.repeatable");
            List<String> requiredMissions = config.getStringList("missions." + key + ".settings.required-missions");
            List<String> blacklistedIfMissionComplete = config.getStringList("missions." + key + ".settings.blacklisted-if-mission-complete");
            Settings settings = new Settings(startDelay, usePermission, repeatable, requiredMissions, blacklistedIfMissionComplete);

            String entityMob = config.getString("missions." + key + ".entity.mob");
            if (entityMob == null) {
                logger.severe("The entity type at 'missions." + key + ".entity.mob' is null (empty)! Skipping mission.");
                return;
            }
            EntityType entityType;
            try {
                entityType = EntityType.valueOf(entityMob);
            } catch (IllegalArgumentException e) {
                logger.severe("The entity type at 'missions." + key + ".entity.mob' is invalid! Skipping mission.");
                return;
            }
            String entityName = ColorCodeUtil.translate(config.getString("missions." + key + ".entity.name"));
            if (entityName == null) {
                logger.severe("The entity name at 'missions." + key + ".entity.name' is null (empty)! Skipping mission.");
                return;
            }
            double entityHealth = config.getDouble("missions." + key + ".entity.health");
            if (entityHealth == 0) {
                logger.severe("The entity health at 'missions." + key + ".entity.health' is either not set or at 0! Skipping mission.");
                return;
            }
            List<String> entityEffects = config.getStringList("missions." + key + ".entity.effects");
            Map<String, Integer> splitEffects = new ConcurrentHashMap<>();
            for (String entityEffect : entityEffects) {
                String[] effect = entityEffect.split(":");
                if (effect.length != 2) {
                    logger.severe("The entity effect '" + entityEffect + "' at 'missions." + key + ".entity.effects' does not contain the effect and strength! Skipping effect.");
                    continue;
                }
                PotionEffectType potionEffectType = PotionEffectType.getByName(effect[0]);
                if (potionEffectType == null) {
                    logger.severe("The entity effect '" + effect[0] + "' at 'missions." + key + ".entity.effects' is invalid! Skipping effect.");
                    continue;
                }
                int amplifier;
                try {
                    amplifier = Integer.valueOf(effect[1]);
                } catch (NumberFormatException e) {
                    logger.severe("The entity effect amplifier at '" + splitEffects + "' at 'missions." + key + ".entity.effects' is not a valid integer! Skipping effect.");
                    continue;
                }
                splitEffects.put(effect[0], amplifier);
            }
            Entity entity = new Entity(entityType, entityName, entityHealth, splitEffects);

            List<String> onClick = config.getStringList("missions." + key + ".actions.on-click");
            List<String> onStart = config.getStringList("missions." + key + ".actions.on-start");
            List<String> onComplete = config.getStringList("missions." + key + ".actions.on-complete");
            Actions actions = new Actions(onClick, onStart, onComplete);

            missions.add(new Mission(key, item, settings, entity, actions));
        });

        List<String> noPermission = ColorCodeUtil.translate(config.getStringList("plugin-messages.no-permission"));
        List<String> invalidArgs = ColorCodeUtil.translate(config.getStringList("plugin-messages.invalid-args"));
        List<String> helpMenu = ColorCodeUtil.translate(config.getStringList("plugin-messages.help-menu"));
        List<String> givenItem = ColorCodeUtil.translate(config.getStringList("plugin-messages.given-item"));
        List<String> noSpace = ColorCodeUtil.translate(config.getStringList("plugin-messages.no-space"));
        List<String> requiredMissionsNotCompleted = ColorCodeUtil.translate(config.getStringList("plugin-messages.required-missions-not-completed"));
        List<String> completedMissionThatBlacklists = ColorCodeUtil.translate(config.getStringList("plugin-messages.completed-mission-that-blacklists"));
        List<String> noRepeat = ColorCodeUtil.translate(config.getStringList("plugin-messages.no-repeat"));
        List<String> alreadyCompletingMission = ColorCodeUtil.translate(config.getStringList("plugin-messages.already-completing-mission"));
        List<String> failedToSpawnEntity = ColorCodeUtil.translate(config.getStringList("plugin-messages.failed-to-spawn-entity"));
        List<String> diedByNaturalCauses = ColorCodeUtil.translate(config.getStringList("plugin-messages.died-by-natural-causes"));
        List<String> notInRegion = ColorCodeUtil.translate(config.getStringList("plugin-messages.not-in-region"));
        pluginMessages = new PluginMessages(
                noPermission,
                invalidArgs,
                helpMenu,
                givenItem,
                noSpace,
                requiredMissionsNotCompleted,
                completedMissionThatBlacklists,
                noRepeat,
                alreadyCompletingMission,
                failedToSpawnEntity,
                diedByNaturalCauses,
                notInRegion);
    }

    public List<Mission> getMissions() {
        return missions;
    }

    public PluginMessages getPluginMessages() {
        return pluginMessages;
    }
}
