// app/src/main/java/com/example/telematicsscanner/database/TelemetryDao.java
package com.example.telematicsscanner.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface TelemetryDao {
    @Insert
    void insert(TelemetryLog log);

    @Query("SELECT * FROM telemetry_logs WHERE isSyncedWithCloud = 0")
    List<TelemetryLog> getUnsyncedLogs();

    @Query("UPDATE telemetry_logs SET isSyncedWithCloud = 1 WHERE id = :logId")
    void markAsSynced(int logId);

    @Query("SELECT * FROM telemetry_logs ORDER BY timestamp ASC")
    List<TelemetryLog> getAllLogsChronological();
}