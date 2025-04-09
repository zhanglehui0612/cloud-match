//package com.cloud.match.store.snapshot;
//
//import com.alibaba.fastjson.JSON;
//import com.cloud.match.avro.TransactionLogAvro;
//import com.cloud.match.model.*;
//import com.cloud.match.service.SnapshotService;
//import com.cloud.match.store.TransactionLogStore;
//
//import java.util.Comparator;
//import java.util.List;
//
//public class SnapshotRestorer {
//
//    private final SnapshotService snapshotService;
//    private final TransactionLogStore transactionLogStore;
//
//    public SnapshotRestorer(SnapshotService snapshotService, TransactionLogStore transactionLogStore) {
//        this.snapshotService = snapshotService;
//        this.transactionLogStore = transactionLogStore;
//    }
//
//    public void restore(String symbol) {
//
//        OrderBookSnapshot orderBookSnapshot = snapshotService.loadOrderBookSnapshot(symbol);
//        UserPositionSnapshot userPositionSnapshot = snapshotService.loadUserPositionSnapshot(symbol);
//        OffsetSnapshot offsetSnapshot = snapshotService.loadOffsetSnapshot();
//        UserPosition userPosition = snapshotService.restoreUserPositionFromSnapshot(userPositionSnapshot);
//        OrderBook orderBook = snapshotService.restoreOrderBookFromSnapshot(orderBookSnapshot);
//        Offset offset = snapshotService.restoreOffsetFromSnapshot(offsetSnapshot);
//
//        long minFileOffset = orderBookSnapshot.
//        List<TransactionLogAvro> logs = transactionLogStore.queryAfterOffset(symbol, )
//        logs.sort(Comparator.comparingLong(TransactionLogAvro::getId));
//
//        for (TransactionLogAvro log : logs) {
//            if (log.getType().equals("DEAL")) {
//                MatchEvent event = JSON.parseObject(log.getData().toString(), MatchEvent.class);
//                switch (event.getType()) {
//                    case "DEAL":
//                        orderBook.addOrder();
//                        userPosition.update();
//                    case "CANCEL":
//                        cancelOrderMatchHandler.match(message, event);
//                }
//                orderBook.addOrder();
//                userPosition.applyDeal(deal);
//            } else if (log.getType().equals("CANCEL")) {
//                CancelEvent cancel = JSON.parseObject(log.getData().toString(), CancelEvent.class);
//                orderBook.applyCancel(cancel);
//            }
//
//            MatchEvent<?> event = parseMatchEvent(msgBody);
//            switch (event.getType()) {
//                case "DEAL":
//                    dealOrderMatchHandler.match(message, event);
//                case "CANCEL":
//                    cancelOrderMatchHandler.match(message, event);
//            }
//        }
//    }
//}
//
