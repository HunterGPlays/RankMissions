package net.terrocidepvp.rankmissions.configuration;

public class Mission {
    private final String missionName;
    private final Item item;
    private final Settings settings;
    private final Entity entity;
    private final Actions actions;

    public Mission(String missionName,
                   Item item,
                   Settings settings,
                   Entity entity,
                   Actions actions) {
        this.missionName = missionName;
        this.item = item;
        this.settings = settings;
        this.entity = entity;
        this.actions = actions;
    }

    public String getMissionName() {
        return missionName;
    }

    public Item getItem() {
        return item;
    }

    public Settings getSettings() {
        return settings;
    }

    public Entity getEntity() {
        return entity;
    }

    public Actions getActions() {
        return actions;
    }
}
