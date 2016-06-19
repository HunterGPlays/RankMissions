package net.terrocidepvp.rankmissions.utils;

public class VersionUtil {
    public static int[] getMCVersion(String version) {
        int pos = version.indexOf("(MC: ");
        String newVersion = version.substring(pos + 5).replace(")", "");
        String[] splitVersion = newVersion.split("\\.");
        int[] newArray = new int[2];
        newArray[0] = Integer.valueOf(splitVersion[0]);
        newArray[1] = Integer.valueOf(splitVersion[1]);
        return newArray;
    }
}
