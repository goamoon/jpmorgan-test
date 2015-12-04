package jpmorgan.test.bookkeeper;

import static jpmorgan.test.trade.Operation.CANCEL;
import static jpmorgan.test.trade.TradeDirection.BUY;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jpmorgan.test.trade.Operation;
import jpmorgan.test.trade.TradeDirection;
import jpmorgan.test.trade.TradeEvent;

public class DefaultPositionBookKeeperImpl extends PositionBookKeeper {

	// maps the trade id to its latest processed trade version
	private Map<Integer, TradeEvent> lastTradeVersions = new HashMap<>();
	
	@Override
	public void processTradeEvent(TradeEvent tradeEvent) {
		if (tradeEvent == null) {
			return;
		}
		
		try {
			processTradeEventInternal(tradeEvent);
		} catch(Exception ex) {
			System.out.println("An exception occured during processing trade event with id " + tradeEvent.getTradeID() + ": " + ex.getMessage());
			ex.printStackTrace();
		}
	}
	
	private void processTradeEventInternal(TradeEvent tradeEvent) {
		System.out.println("Processing Trade Event " + tradeEvent + "...");
		
		// enables processing events arriving in arbitrary order
		if (skipIfHigherVersionAlreadyProcessed(tradeEvent)) {
			return;
		}
				
		revertPreviousTradeVersionIfExists(tradeEvent);
		applyTradeEvent(tradeEvent);
	}
	
	private boolean skipIfHigherVersionAlreadyProcessed(TradeEvent tradeEvent) {
		TradeEvent previousTradeVersion = getPreviousVersion(tradeEvent);
		
		if (previousTradeVersion == null) {
			return false;
		}
		
		int previousVersion = previousTradeVersion.getVersion();
		int currentVersion = tradeEvent.getVersion();
		if (currentVersion < previousVersion) {
			System.out.println("Skipping Trade Event " + tradeEvent + " as a higher version of it had already been processed.");
			return true;
		}
		
		return false;
	}

	private TradeEvent getPreviousVersion(TradeEvent tradeEvent) {
		Integer tradeId = tradeEvent.getTradeID();
		TradeEvent previousTradeVersion = lastTradeVersions.get(tradeId);
		return previousTradeVersion;
	}
	
	private void revertPreviousTradeVersionIfExists(TradeEvent currentTradeEvent) {
		TradeEvent previousTradeVersion = getPreviousVersion(currentTradeEvent);
		if (previousTradeVersion != null) {
			revertPreviousTradeVersion(previousTradeVersion, currentTradeEvent);
		}
	}
	
	private void revertPreviousTradeVersion(TradeEvent previousTradeVersion, TradeEvent currentTradeEvent) {
		System.out.println("Reverting previous trade version: " + previousTradeVersion);
		
		revertPositionQuantity(previousTradeVersion);
		revertPositionTradesIfNecessary(previousTradeVersion, currentTradeEvent);
		lastTradeVersions.remove(previousTradeVersion.getTradeID());
	}

	private void revertPositionTradesIfNecessary(TradeEvent previousTradeEvent, TradeEvent currentTradeEvent) {
		if (previousTradeEvent.isPositionKeyChangedWithinSameTradeId(currentTradeEvent)) {
			Integer tradeId = currentTradeEvent.getTradeID();
			PositionKey positionKey = previousTradeEvent.getPositionKey();
			unregisterTradeEventForPosition(positionKey, tradeId);
		}
	}

	private void revertPositionQuantity(TradeEvent previousTradeVersion) {
		int adjustedTradeQuantiy = adjustQuantityBasedOnOperationAndDirection(previousTradeVersion);
		
		Integer inverseAdjustedTradeQuantity = adjustedTradeQuantiy * -1;
		
		PositionKey positionKey = previousTradeVersion.getPositionKey();
		addPositionQuantity(positionKey, inverseAdjustedTradeQuantity);
	}

	private void applyTradeEvent(TradeEvent tradeEvent) {
		System.out.println("Applying trade event " + tradeEvent);
		
		int adjustedTradeQuantiy = adjustQuantityBasedOnOperationAndDirection(tradeEvent);
		
		PositionKey positionKey = tradeEvent.getPositionKey();
		addPositionQuantity(positionKey, adjustedTradeQuantiy);
		
		int tradeId = tradeEvent.getTradeID();
		registerTradeEventForPosition(positionKey, tradeId);
		lastTradeVersions.put(tradeId, tradeEvent);
	}
	
	private Integer adjustQuantityBasedOnOperationAndDirection(TradeEvent tradeEvent) {
		Operation operation = tradeEvent.getOperation();
		TradeDirection tradeDirection = tradeEvent.getTradeDirection();
		int tradeQuantity = tradeEvent.getTradeQuantity();
		
		Integer operationAdjustedQuantity = adjustQuantityBasedOnOperation(operation, tradeQuantity);
		return adjustQuantityBasedOnTradeDirection(tradeDirection, operationAdjustedQuantity);
	}
	
	private Integer adjustQuantityBasedOnOperation(Operation tradeOperation, Integer quantity) {
		return (tradeOperation.equals(CANCEL) ? 0 : quantity);
	}

	private Integer adjustQuantityBasedOnTradeDirection(TradeDirection tradeDirection, Integer quantity) {
		if (BUY.equals(tradeDirection)) {
			return quantity;
		} else {
			return quantity * -1;
		}
	}
	
	private void addPositionQuantity(PositionKey positionKey, int quantity) {
		Integer currentPositionQuantity = getCurrentPositionQuantity(positionKey);
		Integer updatedPositionQuantity = currentPositionQuantity + quantity;
		positionQuantity.put(positionKey, updatedPositionQuantity);
		System.out.println("New quantity for position " + positionKey + ": " + updatedPositionQuantity);
	}

	private Integer getCurrentPositionQuantity(PositionKey positionKey) {
		Integer currentPositionQuantity = positionQuantity.get(positionKey);
		return (currentPositionQuantity == null ? 0 : currentPositionQuantity);
	}
	
	private void registerTradeEventForPosition(PositionKey positionKey, Integer tradeId) {
		List<Integer> tradesOfPosition = positionTrades.get(positionKey);
		if (tradesOfPosition == null) {
			tradesOfPosition = new ArrayList<Integer>();
			positionTrades.put(positionKey, tradesOfPosition);
		}
		
		if (!tradesOfPosition.contains(tradeId)) {
			tradesOfPosition.add(tradeId);
		}
	}
	
	private void unregisterTradeEventForPosition(PositionKey positionKey, Integer tradeId) {
		List<Integer> tradesOfPosition = positionTrades.get(positionKey);
		if (tradesOfPosition != null) {
			tradesOfPosition.remove(tradeId);
		}
	}
}
