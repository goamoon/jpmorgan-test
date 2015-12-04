package jpmorgan.test.runner;

import static jpmorgan.test.trade.Operation.AMEND;
import static jpmorgan.test.trade.Operation.NEW;
import static jpmorgan.test.trade.TradeDirection.BUY;

import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import jpmorgan.test.bookkeeper.DefaultPositionBookKeeperImpl;
import jpmorgan.test.bookkeeper.DefaultPositionBookKeeperImplTestHelper;
import jpmorgan.test.bookkeeper.PositionBookKeeper;
import jpmorgan.test.bookkeeper.PositionKey;
import jpmorgan.test.trade.TradeEvent;
import junit.framework.TestSuite;

public class TradeEventProcessorRunnerTest extends TestSuite {
	private TradeEventProcessorRunner tradeEventProcessorRunner;
	private DefaultPositionBookKeeperImplTestHelper helper;
	
	@Before
	public void startTradeEventProcessorThread() throws Exception {
		BlockingQueue<TradeEvent> blockingQueue = new ArrayBlockingQueue<>(100);
		TradeEventQueue tradeEventQueue = new TradeEventQueue(blockingQueue);
		PositionBookKeeper positionBookKeeper = new DefaultPositionBookKeeperImpl();
		tradeEventProcessorRunner = new TradeEventProcessorRunner(tradeEventQueue, positionBookKeeper);
		Thread tradeEventProcessorThread = new Thread(tradeEventProcessorRunner);
		tradeEventProcessorThread.start();
		helper = new DefaultPositionBookKeeperImplTestHelper(positionBookKeeper);
	}
	
	@Test
	public void testNullTradeEvent() {
		try {
			tradeEventProcessorRunner.receiveTradeEvent(null);
		} catch (Exception e) {
			Assert.fail("Shouldn't have thrown.");
		}
	}	
	
	@Test
	public void testReceiveTradeEvents() {
		String account = "ACC-1111";
		String security = "ABC";
		TradeEvent te1 = new TradeEvent(1, 1, security, 10, BUY, account, NEW);
		TradeEvent te2 = new TradeEvent(2, 1, security, 100, BUY, account, AMEND);
		
		tradeEventProcessorRunner.receiveTradeEvent(te1);
		tradeEventProcessorRunner.receiveTradeEvent(te2);
		
		waitForEventsToBeProcessed(2);
				
		helper.assertPositionQuantityAndTradeList(new PositionKey(account, security), 110, Arrays.asList(1, 2));
	}

	private void waitForEventsToBeProcessed(int sentEvents) {
		int processedTradeEvents = tradeEventProcessorRunner.getNumberOfProcessedTradeEvents();
		while (processedTradeEvents < sentEvents) {
			processedTradeEvents = tradeEventProcessorRunner.getNumberOfProcessedTradeEvents();
		}
	}
}

