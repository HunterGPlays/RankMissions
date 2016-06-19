package net.terrocidepvp.rankmissions.handlers;

import net.terrocidepvp.rankmissions.configuration.Mission;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class ActiveMission {
    private final Mission mission;
    private Player deployedBy;
    private LivingEntity entity;
    private boolean started;
    private final long clickedAt;

    public ActiveMission(Mission mission,
                         Player deployedBy,
                         LivingEntity entity,
                         boolean started,
                         long clickedAt) {
        this.mission = mission;
        this.deployedBy = deployedBy;
        this.entity = entity;
        this.started = started;
        this.clickedAt = clickedAt;
    }

    public Mission getMission() {
        return mission;
    }

    public Player getDeployedBy() {
        return deployedBy;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public long getClickedAt() {
        return clickedAt;
    }
}
