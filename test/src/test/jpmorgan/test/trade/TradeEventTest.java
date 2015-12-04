package jpmorgan.test.trade;

import static jpmorgan.test.trade.Operation.NEW;
import static jpmorgan.test.trade.TradeDirection.BUY;

import org.junit.Assert;
import org.junit.Test;

import jpmorgan.test.trade.TradeEvent;
import junit.framework.TestSuite;

public class TradeEventTest extends TestSuite {

	@Test
	public void testPositionKeyChange() {
		TradeEvent te = new TradeEvent(1, 1, "ABC", 10, BUY, "ACC-1111", NEW );
		TradeEvent teDifferentSecurity = new TradeEvent(1, 1, "DEF", 10, BUY, "ACC-1111", NEW );
		TradeEvent teDifferentAccount = new TradeEvent(1, 1, "ABC", 10, BUY, "ACC-2222", NEW );
		
		Assert.assertTrue(te.isPositionKeyChangedWithinSameTradeId(teDifferentSecurity));
		Assert.assertTrue(te.isPositionKeyChangedWithinSameTradeId(teDifferentAccount));
		Assert.assertFalse(te.isPositionKeyChangedWithinSameTradeId(te));
	}
}
