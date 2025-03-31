package com.cloud.match.task;

import com.cloud.match.constants.SnapshotConstants;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

// @Component
public class SnapshotBackupTask {

    // @Scheduled(fixedRate = 60000)  // 60 秒
    public void backupSnapshotsToNFS() {
        File localDir = new File(SnapshotConstants.LOCAL_SNAPSHOT_PATH);
        if (!localDir.exists()) {
            System.err.println("Local snapshot directory does not exist!");
            return;
        }

        File[] symbolDirs = localDir.listFiles(File::isDirectory);
        if (symbolDirs != null) {
            for (File symbolDir : symbolDirs) {
                String symbol = symbolDir.getName();
                File nfsDir = new File(SnapshotConstants.NFS_SNAPSHOT_PATH + symbol);
                if (!nfsDir.exists()) {
                    nfsDir.mkdirs();
                }
                backupSymbolSnapshots(symbolDir, nfsDir);
            }
        }
    }

    private void backupSymbolSnapshots(File localDir, File nfsDir) {
        File[] snapshots = localDir.listFiles();
        if (snapshots != null) {
            for (File snapshot : snapshots) {
                try {
                    Path targetPath = Paths.get(nfsDir.getAbsolutePath(), snapshot.getName());
                    Files.copy(snapshot.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("Backup completed: " + snapshot.getName());
                } catch (IOException e) {
                    System.err.println("Failed to backup snapshot: " + snapshot.getName());
                }
            }
        }
    }
}
