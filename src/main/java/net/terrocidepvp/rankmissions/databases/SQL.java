package net.terrocidepvp.rankmissions.databases;

import net.terrocidepvp.rankmissions.RankMissions;
import org.apache.commons.lang.StringUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public abstract class SQL {
    private Connection connection;
    private ConcurrentHashMap<String, String> cache;
    private ConcurrentHashMap<String, String> queuedCache;
    public RankMissions plugin;

    private class SearchedPlayer {
        private String uuid;
        private String completedMissions;

        SearchedPlayer(String uuid, String completedMissions) {
            this.uuid = uuid;
            this.completedMissions = completedMissions;
        }

        public String getUuid() {
            return uuid;
        }

        public String getCompletedMissions() {
            return completedMissions;
        }
    }

    SQL(RankMissions plugin) {
        cache = new ConcurrentHashMap<>(8, 0.9f, 1);
        queuedCache = new ConcurrentHashMap<>(8, 0.9f, 1);
        this.plugin = plugin;
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.createStatement().execute("/* ping */ SELECT 1");
                    SQL.this.updateTables();
                }
            } catch (SQLException e) {
                connection = SQL.this.getNewConnection();
            }
        }, 20L, 1200L);
    }

    public List<SearchedPlayer> searchPlayer(String search) {
        List<SearchedPlayer> results = new ArrayList<>();
        String sqlSearch = "%";
        for (char c : search.toCharArray()) {
            sqlSearch = sqlSearch + c + "%";
        }
        ArrayList<ConcurrentHashMap<String, String>> data = query("SELECT uuid, completedMissions FROM rankmissions WHERE uuid LIKE '" + sqlSearch + "';", true);
        if (data == null)
            return null;
        for (ConcurrentHashMap<String, String> row : data) {
            results.addAll(row.entrySet().stream().map(entry -> new SearchedPlayer(entry.getKey(), entry.getValue())).collect(Collectors.toList()));
        }
        return results;
    }

    private ArrayList<ConcurrentHashMap<String, String>> query(String sql, boolean hasReturn) {
        if (!checkConnection()) {
            plugin.getLogger().severe("Error with database!");
            return null;
        }
        try {
            PreparedStatement statement = connection.prepareStatement(sql);

            if (!hasReturn) {
                statement.execute();
                return null;
            }
            ResultSet set = statement.executeQuery();
            ResultSetMetaData md = set.getMetaData();
            int columns = md.getColumnCount();
            ArrayList<ConcurrentHashMap<String, String>> list = new ArrayList<>(50);
            while (set.next()) {
                ConcurrentHashMap<String, String> row = new ConcurrentHashMap<>(columns);
                for (int i = 1; i <= columns; ++i) {
                    row.put(md.getColumnName(i), set.getString(i));
                }
                list.add(row);
            }
            if (list.isEmpty())
                return null;
            return list;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean checkConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = getNewConnection();
                if (connection == null || connection.isClosed())
                    return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void disconnect() {
        cache.clear();
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateTables() {
        query("CREATE TABLE IF NOT EXISTS rankmissions (uuid char(36) NOT NULL, completedMissions varchar(8000) NOT NULL, PRIMARY KEY (uuid));", false);
        query("CREATE TABLE IF NOT EXISTS rankmissions_queued (uuid char(36) NOT NULL, queuedMissions varchar(8000) NOT NULL, PRIMARY KEY (uuid));", false);
        query("CREATE TABLE IF NOT EXISTS rankmissions_version (version int(1) NOT NULL);", false);
        ArrayList<ConcurrentHashMap<String, String>> results = query("SELECT version FROM rankmissions_version;", true);
        // int version;
        if (results == null) {
            query("INSERT INTO rankmissions_version (version) VALUES (1);", false);
            // version = 1;
        }
        // else {
        // version = Integer.parseInt(results.get(0).get("version"));
        // }
    }

    public String getConfigName() {
        return getName().toLowerCase().replace(" ", "");
    }

    protected abstract String getName();

    protected abstract Connection getNewConnection();

    public List<String> downloadQueuedMissions(String uuid) {
        if (queuedCache.containsKey(uuid))
            return new ArrayList<>(Arrays.asList(StringUtils.split(queuedCache.get(uuid), ',')));
        ArrayList<ConcurrentHashMap<String, String>> data = query(
                "SELECT queuedMissions FROM rankmissions_queued WHERE uuid = '" + uuid + "';", true);
        if (data == null) {
            return new ArrayList<>();
        }
        String queuedMissions = data.get(0).get("queuedMissions");
        queuedCache.put(uuid, queuedMissions);
        return new ArrayList<>(Arrays.asList(StringUtils.split(queuedCache.get(uuid), ',')));
    }

    public void updateQueuedData(String uuid, List<String> queuedMissions) {
        if (queuedMissions.isEmpty()) {
            deleteQueuedData(uuid);
            return;
        }
        String joinedMissions = StringUtils.join(queuedMissions, ',');
        ArrayList<ConcurrentHashMap<String, String>> data = query(
                "SELECT queuedMissions FROM rankmissions_queued WHERE uuid = '" + uuid + "';", true);
        if (data == null) {
            insertQueuedData(uuid, joinedMissions);
        } else {
            query("UPDATE rankmissions_queued SET queuedMissions = '" + joinedMissions + "' WHERE uuid = '" + uuid + "';", false);
        }
        queuedCache.put(uuid, joinedMissions);
    }

    private void insertQueuedData(String uuid, String queuedMissions) {
        queuedCache.put(uuid, queuedMissions);
        query("INSERT INTO rankmissions_queued (uuid, queuedMissions) VALUES ('" + uuid + "','" + queuedMissions + "');", false);
    }

    public List<String> downloadCompletedMissions(String uuid) {
        if (cache.containsKey(uuid))
            return new ArrayList<>(Arrays.asList(StringUtils.split(cache.get(uuid), ',')));
        ArrayList<ConcurrentHashMap<String, String>> data = query(
                "SELECT completedMissions FROM rankmissions WHERE uuid = '" + uuid + "';", true);
        if (data == null) {
            return new ArrayList<>();
        }
        String completedMissions = data.get(0).get("completedMissions");
        cache.put(uuid, completedMissions);
        return new ArrayList<>(Arrays.asList(StringUtils.split(cache.get(uuid), ',')));
    }

    public void updatePlayerData(String uuid, List<String> completedMissions) {
        if (completedMissions.isEmpty()) {
            deletePlayerData(uuid);
            return;
        }
        String joinedMissions = StringUtils.join(completedMissions, ',');
        ArrayList<ConcurrentHashMap<String, String>> data = query(
                "SELECT completedMissions FROM rankmissions WHERE uuid = '" + uuid + "';", true);
        if (data == null) {
            insertPlayerData(uuid, joinedMissions);
        } else {
            query("UPDATE rankmissions SET completedMissions = '" + joinedMissions + "' WHERE uuid = '" + uuid + "';", false);
        }
        cache.put(uuid, joinedMissions);
    }

    private void insertPlayerData(String uuid, String completedMissions) {
        cache.put(uuid, completedMissions);
        query("INSERT INTO rankmissions (uuid, completedMissions) VALUES ('" + uuid + "','" + completedMissions + "');", false);
    }

    public void deletePlayerData(String uuid) {
        removeFromCache(uuid);
        query("DELETE FROM rankmissions WHERE uuid = '" + uuid + "';", false);
    }

    public void deleteQueuedData(String uuid) {
        removeFromQueuedCache(uuid);
        query("DELETE FROM rankmissions_queued WHERE uuid = '" + uuid + "';", false);
    }

    private void removeFromCache(String uuid) {
        if (cache.containsKey(uuid)) {
            cache.remove(uuid);
        }
    }

    private void removeFromQueuedCache(String uuid) {
        if (queuedCache.containsKey(uuid)) {
            queuedCache.remove(uuid);
        }
    }
}
