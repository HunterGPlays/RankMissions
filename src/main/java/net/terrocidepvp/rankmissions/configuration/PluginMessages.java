package net.terrocidepvp.rankmissions.configuration;

import java.util.List;

public class PluginMessages {
    private final List<String> noPermission;
    private final List<String> invalidArguments;
    private final List<String> helpMenu;
    private final List<String> givenItem;
    private final List<String> noSpace;
    private final List<String> requiredMissionsNotCompleted;
    private final List<String> completedMissionThatBlacklists;
    private final List<String> noRepeat;
    private final List<String> alreadyCompletingMission;
    private final List<String> failedToSpawnEntity;
    private final List<String> diedByNaturalCauses;
    private final List<String> notInRegion;

    public PluginMessages(List<String> noPermission,
                          List<String> invalidArguments,
                          List<String> helpMenu,
                          List<String> givenItem,
                          List<String> noSpace,
                          List<String> requiredMissionsNotCompleted,
                          List<String> completedMissionThatBlacklists,
                          List<String> noRepeat,
                          List<String> alreadyCompletingMission,
                          List<String> failedToSpawnEntity,
                          List<String> diedByNaturalCauses,
                          List<String> notInRegion) {
        this.noPermission = noPermission;
        this.invalidArguments = invalidArguments;
        this.helpMenu = helpMenu;
        this.givenItem = givenItem;
        this.noSpace = noSpace;
        this.requiredMissionsNotCompleted = requiredMissionsNotCompleted;
        this.completedMissionThatBlacklists = completedMissionThatBlacklists;
        this.noRepeat = noRepeat;
        this.alreadyCompletingMission = alreadyCompletingMission;
        this.failedToSpawnEntity = failedToSpawnEntity;
        this.diedByNaturalCauses = diedByNaturalCauses;
        this.notInRegion = notInRegion;
    }

    public List<String> getNoPermission() {
        return noPermission;
    }

    public List<String> getInvalidArguments() {
        return invalidArguments;
    }

    public List<String> getHelpMenu() {
        return helpMenu;
    }

    public List<String> getGivenItem() {
        return givenItem;
    }

    public List<String> getNoSpace() {
        return noSpace;
    }

    public List<String> getRequiredMissionsNotCompleted() {
        return requiredMissionsNotCompleted;
    }

    public List<String> getCompletedMissionThatBlacklists() {
        return completedMissionThatBlacklists;
    }

    public List<String> getNoRepeat() {
        return noRepeat;
    }

    public List<String> getAlreadyCompletingMission() {
        return alreadyCompletingMission;
    }

    public List<String> getFailedToSpawnEntity() {
        return failedToSpawnEntity;
    }

    public List<String> getDiedByNaturalCauses() {
        return diedByNaturalCauses;
    }

    public List<String> getNotInRegion() {
        return notInRegion;
    }
}
