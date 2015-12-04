package jpmorgan.test.bookkeeper;

import static jpmorgan.test.trade.Operation.AMEND;
import static jpmorgan.test.trade.Operation.CANCEL;
import static jpmorgan.test.trade.Operation.NEW;
import static jpmorgan.test.trade.TradeDirection.BUY;
import static jpmorgan.test.trade.TradeDirection.SELL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import jpmorgan.test.trade.TradeEvent;
import junit.framework.TestSuite;

public class DefaultPositionBookKeeperImplTest extends TestSuite {
	private PositionBookKeeper positionBookKeeper;
	private DefaultPositionBookKeeperImplTestHelper helper;
	
	@Before
	public void init() throws Exception {
		positionBookKeeper = new DefaultPositionBookKeeperImpl();
		helper = new DefaultPositionBookKeeperImplTestHelper(positionBookKeeper);
	}
	
	@Test
	public void testNullTradeEvent() {
		try {
			positionBookKeeper.processTradeEvent(null);
		} catch (Exception e) {
			Assert.fail("Shouldn't have thrown.");
		}
	}
	
	@Test
	public void testNewBuy() {
		String account = "ACC-1111";
		String security = "ABC";
		TradeEvent te1 = new TradeEvent(1, 1, security, 10, BUY, account, NEW);
		
		positionBookKeeper.processTradeEvent(te1);
		
		helper.assertPositionQuantityAndTradeList(new PositionKey(account, security), 10, Arrays.asList(1));
	}
	
	@Test
	public void testNewSell() {
		String account = "ACC-1111";
		String security = "ABC";
		TradeEvent te1 = new TradeEvent(1, 1, security, 10, SELL, account, NEW);
		
		positionBookKeeper.processTradeEvent(te1);
		
		helper.assertPositionQuantityAndTradeList(new PositionKey(account, security), -10, Arrays.asList(1));
	}
	
	@Test
	public void testCancel() {
		String account = "ACC-1111";
		String security = "ABC";
		TradeEvent te1 = new TradeEvent(1, 1, security, 10, SELL, account, CANCEL);
		
		positionBookKeeper.processTradeEvent(te1);
				
		helper.assertPositionQuantityAndTradeList(new PositionKey(account, security), 0, Arrays.asList(1));
	}
	
	@Test
	public void testAmendAsFirstEvent() {
		String account = "ACC-1111";
		String security = "ABC";
		TradeEvent te1 = new TradeEvent(1, 1, security, 10, BUY, account, AMEND);
		
		positionBookKeeper.processTradeEvent(te1);
						
		// if AMEND is the first event, it is treated as NEW
		helper.assertPositionQuantityAndTradeList(new PositionKey(account, security), 10, Arrays.asList(1));
	}
	
	@Test
	public void testAmendBuy() {
		String account = "ACC-1111";
		String security = "ABC";
		TradeEvent te1 = new TradeEvent(1, 1, security, 10, BUY, account, NEW);
		TradeEvent te2 = new TradeEvent(1, 1, security, 100, BUY, account, AMEND);
		
		positionBookKeeper.processTradeEvent(te1);
		positionBookKeeper.processTradeEvent(te2);
						
		helper.assertPositionQuantityAndTradeList(new PositionKey(account, security), 100, Arrays.asList(1));
	}
	
	@Test
	public void testAmendSell() {
		String account = "ACC-1111";
		String security = "ABC";
		TradeEvent te1 = new TradeEvent(1, 1, security, 10, SELL, account, NEW);
		TradeEvent te2 = new TradeEvent(1, 1, security, 100, SELL, account, AMEND);
		
		positionBookKeeper.processTradeEvent(te1);
		positionBookKeeper.processTradeEvent(te2);
						
		helper.assertPositionQuantityAndTradeList(new PositionKey(account, security), -100, Arrays.asList(1));
	}
	
	@Test
	public void testAmendBuyToSell() {
		String account = "ACC-1111";
		String security = "ABC";
		TradeEvent te1 = new TradeEvent(1, 1, security, 10, BUY, account, NEW);
		TradeEvent te2 = new TradeEvent(1, 1, security, 100, SELL, account, AMEND);
		
		positionBookKeeper.processTradeEvent(te1);
		positionBookKeeper.processTradeEvent(te2);
						
		helper.assertPositionQuantityAndTradeList(new PositionKey(account, security), -100, Arrays.asList(1));
	}
	
	@Test
	public void testAmendSellToBuy() {
		String account = "ACC-1111";
		String security = "ABC";
		TradeEvent te1 = new TradeEvent(1, 1, security, 10, SELL, account, NEW);
		TradeEvent te2 = new TradeEvent(1, 1, security, 100, BUY, account, AMEND);
		
		positionBookKeeper.processTradeEvent(te1);
		positionBookKeeper.processTradeEvent(te2);
						
		helper.assertPositionQuantityAndTradeList(new PositionKey(account, security), 100, Arrays.asList(1));
	}
	
	@Test
	public void testCancelBuy() {
		String account = "ACC-1111";
		String security = "ABC";
		TradeEvent te1 = new TradeEvent(1, 1, security, 10, BUY, account, NEW);
		TradeEvent te2 = new TradeEvent(1, 1, security, 100, BUY, account, CANCEL);
		
		positionBookKeeper.processTradeEvent(te1);
		positionBookKeeper.processTradeEvent(te2);
						
		helper.assertPositionQuantityAndTradeList(new PositionKey(account, security), 0, Arrays.asList(1));
	}
	
	@Test
	public void testCancelSell() {
		String account = "ACC-1111";
		String security = "ABC";
		TradeEvent te1 = new TradeEvent(1, 1, security, 10, SELL, account, NEW);
		TradeEvent te2 = new TradeEvent(1, 1, security, 100, BUY, account, CANCEL);
		
		positionBookKeeper.processTradeEvent(te1);
		positionBookKeeper.processTradeEvent(te2);
						
		helper.assertPositionQuantityAndTradeList(new PositionKey(account, security), 0, Arrays.asList(1));
	}
	
	@Test
	public void testBuyMultipleVersions() {
		String account = "ACC-1111";
		String security = "ABC";
		TradeEvent te1 = new TradeEvent(1, 1, security, 10, BUY, account, NEW);
		TradeEvent te2 = new TradeEvent(1, 2, security, 100, BUY, account, NEW);
		
		positionBookKeeper.processTradeEvent(te1);
		positionBookKeeper.processTradeEvent(te2);
						
		helper.assertPositionQuantityAndTradeList(new PositionKey(account, security), 100, Arrays.asList(1));
	}
	
	@Test
	public void testSellMultipleVersions() {
		String account = "ACC-1111";
		String security = "ABC";
		TradeEvent te1 = new TradeEvent(1, 1, security, 10, SELL, account, NEW);
		TradeEvent te2 = new TradeEvent(1, 2, security, 100, SELL, account, NEW);
		
		positionBookKeeper.processTradeEvent(te1);
		positionBookKeeper.processTradeEvent(te2);
						
		helper.assertPositionQuantityAndTradeList(new PositionKey(account, security), -100, Arrays.asList(1));
	}
	
	@Test
	public void testCancelInSecondVersion() {
		String account = "ACC-1111";
		String security = "ABC";
		TradeEvent te1 = new TradeEvent(1, 1, security, 10, SELL, account, NEW);
		TradeEvent te2 = new TradeEvent(1, 2, security, 100, SELL, account, CANCEL);
		
		positionBookKeeper.processTradeEvent(te1);
		positionBookKeeper.processTradeEvent(te2);
						
		// as only the second version of the event should be considered, 
		// the first version should be reversed, then the CANCEL applied 
		helper.assertPositionQuantityAndTradeList(new PositionKey(account, security), 0, Arrays.asList(1));
	}
	
	@Test
	public void testBuyMultipleTradeIds() {
		String account = "ACC-1111";
		String security = "ABC";
		TradeEvent te1 = new TradeEvent(1, 1, security, 10, BUY, account, NEW);
		TradeEvent te2 = new TradeEvent(2, 1, security, 100, BUY, account, NEW);
		
		positionBookKeeper.processTradeEvent(te1);
		positionBookKeeper.processTradeEvent(te2);		
				
		helper.assertPositionQuantityAndTradeList(new PositionKey(account, security), 110, Arrays.asList(1, 2));
	}
	
	@Test
	public void testSellMultipleTradeIds() {
		String account = "ACC-1111";
		String security = "ABC";
		TradeEvent te1 = new TradeEvent(1, 1, security, 10, SELL, account, NEW);
		TradeEvent te2 = new TradeEvent(2, 1, security, 100, SELL, account, NEW);
		
		positionBookKeeper.processTradeEvent(te1);
		positionBookKeeper.processTradeEvent(te2);		
				
		helper.assertPositionQuantityAndTradeList(new PositionKey(account, security), -110, Arrays.asList(1, 2));
	}
	
	@Test
	public void testBuyAndSellMultipleTradeIds() {
		String account = "ACC-1111";
		String security = "ABC";
		TradeEvent te1 = new TradeEvent(1, 1, security, 10, BUY, account, NEW);
		TradeEvent te2 = new TradeEvent(2, 1, security, 100, SELL, account, NEW);
		
		positionBookKeeper.processTradeEvent(te1);
		positionBookKeeper.processTradeEvent(te2);
					
		helper.assertPositionQuantityAndTradeList(new PositionKey(account, security), -90, Arrays.asList(1, 2));
	}
	
	@Test
	public void testCancelMultipleTradeIds() {
		String account = "ACC-1111";
		String security = "ABC";
		TradeEvent te1 = new TradeEvent(1, 1, security, 10, SELL, account, NEW);
		TradeEvent te2 = new TradeEvent(2, 1, security, 0, SELL, account, CANCEL);
		
		positionBookKeeper.processTradeEvent(te1);
		positionBookKeeper.processTradeEvent(te2);
						
		// the two trades should be applied independently of each other, the CANCEL shouldn't cancel an other trade
		helper.assertPositionQuantityAndTradeList(new PositionKey(account, security), -10, Arrays.asList(1, 2));
	}
	
	@Test
	public void testChangeSecurityWithinSameTradeId() {
		String account = "ACC-1111";
		String security1 = "ABC1";
		String security2 = "ABC2";
		TradeEvent te1 = new TradeEvent(1, 1, security1, 10, BUY, account, NEW);
		TradeEvent te2 = new TradeEvent(1, 2, security2, 100, SELL, account, NEW);
		
		positionBookKeeper.processTradeEvent(te1);
		positionBookKeeper.processTradeEvent(te2);
						
		helper.assertPositionQuantityAndTradeList(new PositionKey(account, security1), 0, Arrays.asList());
		helper.assertPositionQuantityAndTradeList(new PositionKey(account, security2), -100, Arrays.asList(1));
	}
	
	@Test
	public void testChangeAccountWithinSameTradeId() {
		String account1 = "ACC-1111";
		String account2 = "ACC-2222";
		String security = "ABC";
		TradeEvent te1 = new TradeEvent(1, 1, security, 10, BUY, account1, NEW);
		TradeEvent te2 = new TradeEvent(1, 2, security, 100, SELL, account2, NEW);
		
		positionBookKeeper.processTradeEvent(te1);
		positionBookKeeper.processTradeEvent(te2);
						
		helper.assertPositionQuantityAndTradeList(new PositionKey(account1, security), 0, Arrays.asList());
		helper.assertPositionQuantityAndTradeList(new PositionKey(account2, security), -100, Arrays.asList(1));
	}

	@Test
	public void testVersionWrongOrder() {
		String account = "ACC-1111";
		String security = "ABC";
		TradeEvent te1 = new TradeEvent(1, 2, security, 10, BUY, account, NEW);
		TradeEvent te2 = new TradeEvent(1, 1, security, 100, SELL, account, NEW);
		
		positionBookKeeper.processTradeEvent(te1);
		positionBookKeeper.processTradeEvent(te2);
						
		// should be 10 which corresponds to the highest version
		helper.assertPositionQuantityAndTradeList(new PositionKey(account, security), 10, Arrays.asList(1));
	}
	
	@Test
	public void testAmendReceivedEarlier() {
		String account = "ACC-1111";
		String security = "ABC";
		TradeEvent te1 = new TradeEvent(1, 2, security, 10, BUY, account, AMEND);
		TradeEvent te2 = new TradeEvent(1, 1, security, 100, BUY, account, NEW);
		
		positionBookKeeper.processTradeEvent(te1);
		positionBookKeeper.processTradeEvent(te2);
						
		// should be 10 which corresponds to the highest version
		helper.assertPositionQuantityAndTradeList(new PositionKey(account, security), 10, Arrays.asList(1));
	}
	
	@Test
	public void testCancelReceivedEarlierThanAmend() {
		String account = "ACC-1111";
		String security = "ABC";
		TradeEvent te1 = new TradeEvent(1, 2, security, 10, BUY, account, CANCEL);
		TradeEvent te2 = new TradeEvent(1, 1, security, 100, BUY, account, AMEND);
		
		positionBookKeeper.processTradeEvent(te1);
		positionBookKeeper.processTradeEvent(te2);
						
		// should be 10 which corresponds to the highest version
		helper.assertPositionQuantityAndTradeList(new PositionKey(account, security), 0, Arrays.asList(1));
	}
	
	@Test
	public void testWithDataFromProblemDescription() {
		List<TradeEvent> tradeEvents = initTestDataFromProblemDescription();
		
		for (TradeEvent te: tradeEvents) {
			positionBookKeeper.processTradeEvent(te);
		}
		
		assertDataFromProblemDescription(false);
	}
	
	@Test
	public void testWithDataFromProblemDescriptionInRandomOrder() {
		List<TradeEvent> tradeEvents = initTestDataFromProblemDescription();
		
		for (int i=0; i<100; i++) {			
			Collections.shuffle(tradeEvents);
			
			System.out.println("Randomized trade events: ");
			for (TradeEvent te: tradeEvents) {
				System.out.print(te.getTradeID() + "/" + te.getVersion() + "; ");
			}
			
			for (TradeEvent te: tradeEvents) {
				positionBookKeeper.processTradeEvent(te);
			}
			
			assertDataFromProblemDescription(true);
		}
	}
	
	private List<TradeEvent> initTestDataFromProblemDescription() {
		List<TradeEvent> tradeEvents = new ArrayList<TradeEvent>();
		tradeEvents.add(new TradeEvent(1234, 1, "XYZ", 100, BUY, "ACC-1234", NEW));
		tradeEvents.add(new TradeEvent(1234, 2, "XYZ", 150, BUY, "ACC-1234", AMEND));
		tradeEvents.add(new TradeEvent(5678, 1, "QED", 200, BUY, "ACC-2345", NEW));
		tradeEvents.add(new TradeEvent(7897, 2, "QED", 0, BUY, "ACC-2345", CANCEL));
		tradeEvents.add(new TradeEvent(2233, 1, "RET", 100, SELL, "ACC-3456", NEW));
		tradeEvents.add(new TradeEvent(2233, 2, "RET", 400, SELL, "ACC-3456", AMEND));
		tradeEvents.add(new TradeEvent(2233, 3, "RET", 0, SELL, "ACC-3456", CANCEL));
		tradeEvents.add(new TradeEvent(8896, 1, "YUI", 300, BUY, "ACC-4567", NEW));
		tradeEvents.add(new TradeEvent(6638, 1, "YUI", 100, SELL, "ACC-4567", NEW));
		tradeEvents.add(new TradeEvent(6363, 1, "HJK", 200, BUY, "ACC-5678", NEW));
		tradeEvents.add(new TradeEvent(7666, 1, "HJK", 200, BUY, "ACC-5678", NEW));
		tradeEvents.add(new TradeEvent(6363, 2, "HJK", 100, BUY, "ACC-5678", AMEND));
		tradeEvents.add(new TradeEvent(7666, 2, "HJK", 50, SELL, "ACC-5678", AMEND));
		tradeEvents.add(new TradeEvent(8686, 1, "FVB", 100, BUY, "ACC-6789", NEW));
		tradeEvents.add(new TradeEvent(8686, 2, "GBN", 100, BUY, "ACC-6789", AMEND));
		tradeEvents.add(new TradeEvent(9654, 1, "FVB", 200, BUY, "ACC-6789", NEW));
		tradeEvents.add(new TradeEvent(1025, 1, "JKL", 100, BUY, "ACC-7789", NEW));
		tradeEvents.add(new TradeEvent(1036, 1, "JKL", 100, BUY, "ACC-7789", NEW));
		tradeEvents.add(new TradeEvent(1025, 2, "JKL", 100, SELL, "ACC-8877", AMEND));
		tradeEvents.add(new TradeEvent(1122, 1, "KLO", 100, BUY, "ACC-9045", NEW));
		tradeEvents.add(new TradeEvent(1122, 2, "HJK", 100, SELL, "ACC-9045", AMEND));
		tradeEvents.add(new TradeEvent(1122, 3, "KLO", 100, SELL, "ACC-9045", AMEND));
		tradeEvents.add(new TradeEvent(1144, 1, "KLO", 300, BUY, "ACC-9045", NEW));
		tradeEvents.add(new TradeEvent(1144, 2, "KLO", 400, BUY, "ACC-9045", AMEND));
		tradeEvents.add(new TradeEvent(1155, 1, "KLO", 600, SELL, "ACC-9045", NEW));
		tradeEvents.add(new TradeEvent(1155, 2, "KLO", 0, BUY, "ACC-9045", CANCEL));
		
		return tradeEvents;
	}
	
	private void assertDataFromProblemDescription(boolean assertTradeListAsSet) {
		helper.assertPositionQuantityAndTradeList(new PositionKey("ACC-1234", "XYZ"), 150, Arrays.asList(1234), assertTradeListAsSet);
		// there are two trades for this position: 5678 and 7897
		// even though 7898 is a CANCEL, it can't cancel 5678, so the remaining amount should be 200
		// the resulting amount would be 0, if both trades would have the same id, then the one with version 2 would win, which is a cancel
		//helper.assertPositionQuantityAndTradeList(new PositionKey("ACC-2345", "QED"), 0, Arrays.asList(5678, 7897), assertTradeListAsSet);
		helper.assertPositionQuantityAndTradeList(new PositionKey("ACC-2345", "QED"), 200, Arrays.asList(5678, 7897), assertTradeListAsSet);
		helper.assertPositionQuantityAndTradeList(new PositionKey("ACC-3456", "RET"), 0, Arrays.asList(2233), assertTradeListAsSet);
		helper.assertPositionQuantityAndTradeList(new PositionKey("ACC-4567", "YUI"), 200, Arrays.asList(8896, 6638), assertTradeListAsSet);
		helper.assertPositionQuantityAndTradeList(new PositionKey("ACC-5678", "HJK"), 50, Arrays.asList(6363, 7666), assertTradeListAsSet);
		helper.assertPositionQuantityAndTradeList(new PositionKey("ACC-6789", "GBN"), 100, Arrays.asList(8686), assertTradeListAsSet);
		//helper.assertPositionQuantityAndTradeList(new PositionKey("ACC-6789", "FVB"), 200, Arrays.asList(8686, 9654), assertTradeListAsSet); // 8686 is reverted
		helper.assertPositionQuantityAndTradeList(new PositionKey("ACC-6789", "FVB"), 200, Arrays.asList(9654), assertTradeListAsSet);
		//helper.assertPositionQuantityAndTradeList(new PositionKey("ACC-7789", "JKL"), 100, Arrays.asList(1036, 1025), assertTradeListAsSet); // 1025 is reverted
		helper.assertPositionQuantityAndTradeList(new PositionKey("ACC-7789", "JKL"), 100, Arrays.asList(1036), assertTradeListAsSet);
		helper.assertPositionQuantityAndTradeList(new PositionKey("ACC-8877", "JKL"), -100, Arrays.asList(1025), assertTradeListAsSet);
		helper.assertPositionQuantityAndTradeList(new PositionKey("ACC-9045", "KLO"), 300, Arrays.asList(1122, 1144, 1155), assertTradeListAsSet);
		//helper.assertPositionQuantityAndTradeList(new PositionKey("ACC-9045", "HJK"), 0, Arrays.asList(1122), assertTradeListAsSet); // 1122 is reverted
		helper.assertPositionQuantityAndTradeList(new PositionKey("ACC-9045", "HJK"), 0, Arrays.asList(), assertTradeListAsSet);
	}
}
