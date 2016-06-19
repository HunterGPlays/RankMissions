package net.terrocidepvp.rankmissions.configuration;

import org.bukkit.entity.EntityType;

import java.util.Map;

public class Entity {
    private final EntityType mob;
    private final String name;
    private final double health;
    private final Map<String, Integer> effects;

    public Entity(EntityType mob,
                  String name,
                  double health,
                  Map<String, Integer> effects) {
        this.mob = mob;
        this.name = name;
        this.health = health;
        this.effects = effects;
    }

    public EntityType getMob() {
        return mob;
    }

    public String getName() {
        return name;
    }

    public double getHealth() {
        return health;
    }

    public Map<String, Integer> getEffects() {
        return effects;
    }
}
