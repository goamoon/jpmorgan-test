package jpmorgan.test.runner;

import java.util.List;

import jpmorgan.test.bookkeeper.PositionBookKeeper;
import jpmorgan.test.bookkeeper.PositionKey;
import jpmorgan.test.trade.TradeEvent;

public class TradeEventProcessorRunner implements Runnable {
	
	private TradeEventQueue tradeEventQueue;
	private PositionBookKeeper positionBookKeeper;
	
	private int processedTradeEvents;
	
	public TradeEventProcessorRunner(TradeEventQueue tradeEventQueue, PositionBookKeeper positionBookKeeper) throws Exception {
		if (tradeEventQueue == null) {
			throw new Exception("The Trade Event Queue can not be null.");
		}
		
		if (positionBookKeeper == null) {
			throw new Exception("The Position Accountant can not be null.");
		}
		
		this.tradeEventQueue = tradeEventQueue;
		this.positionBookKeeper = positionBookKeeper;
		
		this.processedTradeEvents = 0;
	}
	
	@Override
	public void run() {
		startProcessingTradeEvents();
	}

	private void startProcessingTradeEvents() {
		System.out.println("Starting trade event processor...");
		
		do {
			try {
				TradeEvent tradeEvent = tradeEventQueue.take();
				positionBookKeeper.processTradeEvent(tradeEvent);
				processedTradeEvents++;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} while (true);
	}
	
	public void receiveTradeEvent(TradeEvent tradeEvent) {
		tradeEventQueue.put(tradeEvent);
	}
	
	public Integer getPositionQuantity(PositionKey positionKey) {
		return positionBookKeeper.getPositionQuantity(positionKey);
	}
	
	public List<Integer> getPositionTrades(PositionKey positionKey) {
		return positionBookKeeper.getPositionTrades(positionKey);
	}
	
	public int getNumberOfProcessedTradeEvents() {
		return processedTradeEvents;
	}
}
