package com.cloud.match.model;

import com.google.common.collect.Lists;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class UserPosition implements Serializable {
    @Serial
    private static final long serialVersionUID = 1144245684176172195L;

    private String symbol;

    private final Map<String, List<Position>> userPositions = new ConcurrentHashMap<>();

    public UserPosition(String symbol) {
        this.symbol = symbol;
    }

    public void update(String userId, String symbol, MatchResult matchResult) {
        boolean isBuyParty = matchResult.getBuyerUserId().equals(userId);
        Order order = isBuyParty ? matchResult.getBuyOrder() :  matchResult.getSellOrder();
        List<Position> positions = userPositions.get(userId);
        Optional<Position> optional = positions.stream()
                .filter(pos -> pos.getSymbol().equals(symbol))
                .filter(pos -> pos.getMarginType() == order.getMarginType())                                   // 全仓模式
                .filter(pos -> pos.getPositionMode() == order.getPositionMode())
                .filter(pos -> pos.getPositionType() == order.getPositionType())
                .findFirst();
        if (optional.isEmpty()) {
            Position position = Position.builder()
                    .positionId(UUID.randomUUID().toString())
                    .marginType(order.getMarginType())
                    .positionAmount(matchResult.getQuantity())
                    .positionMode(order.getPositionMode())
                    .orderSide(order.getSide())
                    .userId(userId)
                    .symbol(symbol)
                    .positionType(order.getPositionType())
                    .build();
            if (CollectionUtils.isEmpty(positions)) {
                positions = Lists.newArrayList();
                positions.add(position);
                userPositions.put(userId, positions);
                return;
            }
            positions.add(position);
            return;
        }

        Position position = optional.get();
        if (order.getPositionType() == 1) {
            position.setPositionAmount(position.getPositionAmount().add(matchResult.getQuantity()));
        } else {
            BigDecimal positionAmount = position.getPositionAmount().subtract(matchResult.getQuantity());
            position.setPositionAmount(positionAmount.compareTo(BigDecimal.ZERO) >= 0 ? positionAmount : BigDecimal.ZERO);
        }

        if (position.getPositionAmount().compareTo(BigDecimal.ZERO) == 0) {
            positions.remove(position);
        }
    }

    public Position getUserPosition(Order order) {
        String userId = order.getUserId();
        List<Position> positions = this.userPositions.get(userId);
        if (CollectionUtils.isEmpty(positions)) {
            return null;
        }

        Optional<Position> optional = positions.stream()
                .filter(pos -> pos.getSymbol().equals(order.getSymbol()))
                .filter(pos -> pos.getMarginType() == order.getMarginType())                                   // 全仓模式
                .filter(pos -> pos.getPositionMode() == order.getPositionMode())
                .filter(pos -> pos.getPositionType() == order.getPositionType())
                .findFirst();
        return optional.get();
    }
}
