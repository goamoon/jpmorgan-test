package jpmorgan.test.bookkeeper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jpmorgan.test.trade.TradeEvent;

/*
 * By processing Trade Events it maintains the actual quantity and list of contributing trades of positions
 */
public abstract class PositionBookKeeper {
	// maps positions to their actual quantity
	protected final Map<PositionKey, Integer> positionQuantity = new HashMap<>();
	
	// maps positions to the list of contributing tradeIds
	protected final Map<PositionKey, List<Integer>> positionTrades = new HashMap<>();
	
	public Integer getPositionQuantity(PositionKey positionKey) {
		Integer quantity = positionQuantity.get(positionKey);
		return (quantity == null ? 0 : quantity);
	}
	
	public List<Integer> getPositionTrades(PositionKey positionKey) {
		List<Integer> tradesOfThePosition = positionTrades.get(positionKey);
		return (tradesOfThePosition == null ? new ArrayList<Integer>() : tradesOfThePosition);
	}
	
	public abstract void processTradeEvent(TradeEvent tradeEvent);
}
