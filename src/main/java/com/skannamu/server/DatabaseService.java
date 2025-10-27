package com.skannamu.server;

import com.skannamu.skannamuMod;
import java.sql.*;
import java.nio.file.Path;

public class DatabaseService {

    private final String DATABASE_URL;

    public DatabaseService(Path gameDirPath) {
        this.DATABASE_URL = "jdbc:sqlite:" + gameDirPath.resolve("skannamu_data.db").toString();
    }

    public Connection connect() throws SQLException {
        return DriverManager.getConnection(DATABASE_URL);
    }
    public void createTables() {
        String sql = "CREATE TABLE IF NOT EXISTS filesystem ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "player_uuid TEXT NOT NULL,"
                + "path TEXT NOT NULL,"
                + "type TEXT NOT NULL,"
                + "content TEXT,"
                + "UNIQUE(player_uuid, path)" // UUID와 경로 조합은 중복 불가
                + ");";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            skannamuMod.LOGGER.info("SQLite filesystem table created or already exists.");
        } catch (SQLException e) {
            skannamuMod.LOGGER.error("Failed to create tables: ", e);
        }
    }

}