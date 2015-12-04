package jpmorgan.test.runner;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import jpmorgan.test.trade.TradeEvent;

public class TradeEventQueue {
	private BlockingQueue<TradeEvent> tradeEventQueue = new ArrayBlockingQueue<>(100);

	public TradeEventQueue(BlockingQueue<TradeEvent> tradeEventQueue) {
		this.tradeEventQueue = tradeEventQueue;
	}
	
	public TradeEvent take() throws InterruptedException {
		return tradeEventQueue.take();
	}
	
	public void put(TradeEvent tradeEvent) {
		try {
			if (tradeEvent != null) {
				tradeEventQueue.put(tradeEvent);
			}
		} catch (InterruptedException e) {
			// As we consistently avoid interruptions, we can safely catch it here
			e.printStackTrace();
		}
		
		return;
	}
}