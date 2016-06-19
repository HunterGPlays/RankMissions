package net.terrocidepvp.rankmissions.configuration;

import org.bukkit.Material;

import java.util.List;

public class Item {
    private final Material type;
    private final short data;
    private final String name;
    private final List<String> lore;
    private final List<String> activateInRegion;

    public Item(Material type,
                short data,
                String name,
                List<String> lore,
                List<String> activateInRegion) {
        this.type = type;
        this.data = data;
        this.name = name;
        this.lore = lore;
        this.activateInRegion = activateInRegion;
    }

    public Material getType() {
        return type;
    }

    public short getData() {
        return data;
    }

    public String getName() {
        return name;
    }

    public List<String> getLore() {
        return lore;
    }

    public List<String> getActivateInRegion() {
        return activateInRegion;
    }
}
