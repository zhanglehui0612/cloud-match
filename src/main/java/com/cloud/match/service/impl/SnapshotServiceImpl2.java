package com.cloud.match.service.impl;

//@Service
//public class SnapshotServiceImpl implements SnapshotService {
//
//    @Value("${match.snapshot.dir}")
//    private String snapshotDir; // 共享存储路径（如K8s PV挂载点）
//    private final ReentrantLock lock = new ReentrantLock();
//
//
//    // 加载最新订单快照
//    public OrderBookSnapshot loadOrderBookSnapshot(String symbol) {
//        Path latestFile = findLatestSnapshotFile(symbol, "order");
//        if (latestFile == null) return null;
//        return (OrderBookSnapshot) deserializeSnapshot(latestFile);
//    }
//
//    // 加载最新用户仓位快照
//    public UserPositionSnapshot loadPositionSnapshot(String symbol) {
//        Path latestFile = findLatestSnapshotFile(symbol, "position");
//        return (UserPositionSnapshot) deserializeSnapshot(latestFile);
//    }
//
//    // 保存订单快照（异步）
//    public void saveOrderBookSnapshot(OrderBookSnapshot snapshot) {
//        saveSnapshotAsync(snapshot, "order");
//    }
//
//    // 保存用户仓位快照（异步）
//    public void savePositionSnapshot(UserPositionSnapshot snapshot) {
//        saveSnapshotAsync(snapshot, "position");
//    }
//
//
//    // 异步保存快照（避免阻塞撮合线程）
//    private void saveSnapshotAsync(Snapshot snapshot, String type) {
//        new Thread(() -> {
//            lock.lock();
//            try {
//                String filename = String.format("%s_%d_%s.snap",
//                        snapshot.getSymbol(), snapshot.getTimestamp(), type);
//                Path path = Paths.get(snapshotDir, filename);
//                Files.write(path, snapshot.serialize());
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            } finally {
//                lock.unlock();
//            }
//        }).start();
//    }
//
//    // 查找最新快照文件
//    private Path findLatestSnapshotFile(String symbol, String type) {
//        try {
//            return Files.list(Paths.get(snapshotDir))
//                    .filter(path -> path.toString().contains(symbol) && path.toString().contains(type))
//                    .max((p1, p2) -> Long.compare(p1.toFile().lastModified(), p2.toFile().lastModified()))
//                    .orElse(null);
//        } catch (IOException e) {
//            throw new RuntimeException("Failed to find snapshot", e);
//        }
//    }
//
//    // 反序列化快照文件
//    private Snapshot deserializeSnapshot(Path path) {
//        try {
//            byte[] data = Files.readAllBytes(path);
//            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
//            return (Snapshot) ois.readObject();
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to deserialize snapshot", e);
//        }
//    }
//
//    // 创建目录（如不存在）
//    private void createDirIfNotExists(String dir) {
//        try {
//            Files.createDirectories(Paths.get(dir));
//        } catch (IOException e) {
//            throw new RuntimeException("Failed to create directory", e);
//        }
//    }
//}
