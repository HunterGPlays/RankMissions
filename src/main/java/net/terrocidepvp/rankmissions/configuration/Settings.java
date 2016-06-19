package net.terrocidepvp.rankmissions.configuration;

import java.util.List;

public class Settings {
    private final int startDelay;
    private final boolean usePermission;
    private final boolean repeatable;
    private final List<String> requiredMissions;
    private final List<String> blacklistIfMissionComplete;

    public Settings(int startDelay,
                    boolean usePermission,
                    boolean repeatable,
                    List<String> requiredMissions,
                    List<String> blacklistIfMissionComplete) {
        this.startDelay = startDelay;
        this.usePermission = usePermission;
        this.repeatable = repeatable;
        this.requiredMissions = requiredMissions;
        this.blacklistIfMissionComplete = blacklistIfMissionComplete;
    }

    public int getStartDelay() {
        return startDelay;
    }

    public boolean isUsePermission() {
        return usePermission;
    }

    public boolean isRepeatable() {
        return repeatable;
    }

    public List<String> getRequiredMissions() {
        return requiredMissions;
    }

    public List<String> getBlacklistIfMissionComplete() {
        return blacklistIfMissionComplete;
    }
}
