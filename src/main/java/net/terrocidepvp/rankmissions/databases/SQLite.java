package net.terrocidepvp.rankmissions.databases;

import net.terrocidepvp.rankmissions.RankMissions;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;

public class SQLite extends SQL {
    public SQLite(RankMissions plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "SQLite";
    }

    @Override
    protected Connection getNewConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
            return DriverManager.getConnection("jdbc:sqlite:" + new File(plugin.getDataFolder(), "playerinfo.db").getAbsolutePath());
        } catch (Exception e) {
            return null;
        }
    }
}
