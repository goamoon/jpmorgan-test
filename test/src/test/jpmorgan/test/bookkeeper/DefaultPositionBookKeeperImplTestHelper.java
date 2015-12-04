package jpmorgan.test.bookkeeper;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.junit.Assert;

public class DefaultPositionBookKeeperImplTestHelper {
	private PositionBookKeeper positionBookKeeper;
	
	public DefaultPositionBookKeeperImplTestHelper(PositionBookKeeper positionBookKeeper) {
		this.positionBookKeeper = positionBookKeeper;
	}

	public void assertPositionQuantityAndTradeList(PositionKey positionKey, int expectedQuantity, List<Integer> expectedTradeIds) {
		assertPositionQuantity(positionKey, expectedQuantity);
		assertPositionTradeList(positionKey, expectedTradeIds, false);
	}
	
	public void assertPositionQuantityAndTradeList(PositionKey positionKey, int expectedQuantity, 
			List<Integer> expectedTradeIds, boolean assertTradeListAsSet) {
		assertPositionQuantity(positionKey, expectedQuantity);
		assertPositionTradeList(positionKey, expectedTradeIds, assertTradeListAsSet);
	}

	public void assertPositionTradeList(PositionKey positionKey, Collection<Integer> expectedTradeIds, boolean assertTradeListAsSet) {
		List<Integer> positionTrades = positionBookKeeper.getPositionTrades(positionKey);
		Assert.assertNotNull(positionTrades);
		if (assertTradeListAsSet) {
			Assert.assertEquals(new HashSet<Integer>(expectedTradeIds), new HashSet<Integer>(positionTrades));
		} else {
			Assert.assertArrayEquals(expectedTradeIds.toArray(), positionTrades.toArray());
		}
	}

	public void assertPositionQuantity(PositionKey positionKey, int expectedQuantity) {
		Integer positionQuantity = positionBookKeeper.getPositionQuantity(positionKey);
		Assert.assertNotNull(positionQuantity);
		Assert.assertEquals(expectedQuantity, positionQuantity.intValue());
	}
}
