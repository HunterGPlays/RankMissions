package net.terrocidepvp.rankmissions.databases;

import net.terrocidepvp.rankmissions.RankMissions;
import org.bukkit.configuration.Configuration;

import java.sql.Connection;
import java.sql.DriverManager;

public class MySQL extends SQL {
    public MySQL(RankMissions plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "MySQL";
    }

    @Override
    protected Connection getNewConnection() {
        Configuration config = plugin.getConfig();
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String url = "jdbc:mysql://" + config.getString("database.mysql.host") + ":" + config.getString("database.mysql.port") + "/" + config.getString("database.mysql.database-name");
            return DriverManager.getConnection(url, config.getString("database.mysql.user"), config.getString("database.mysql.password"));
        } catch (Exception e) {
            return null;
        }
    }
}
