package com.example.telematicsscanner.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "telemetry_logs")
public class TelemetryLog {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public long timestamp;
    public int engineRpm;
    public String coolantTemp;
    public boolean isSyncedWithCloud; // This acts as our flag for the background worker

    public TelemetryLog(long timestamp, int engineRpm, String coolantTemp) {
        this.timestamp = timestamp;
        this.engineRpm = engineRpm;
        this.coolantTemp = coolantTemp;
        this.isSyncedWithCloud = false; // Default to false until the PHP server confirms receipt
    }
}