package jpmorgan.test.trade;

import jpmorgan.test.bookkeeper.PositionKey;

public class TradeEvent {
	private int tradeID;
	private int version;
	private String securityIdentifier;
	private int tradeQuantity;
	private TradeDirection tradeDirection;
	private String account;
	private Operation operation;
	
	public TradeEvent(int tradeID, int version, String securityIdentifier,
			int tradeQuantity, TradeDirection tradeDirection, String account,
			Operation operation) {
		this.tradeID = tradeID;
		this.version = version;
		this.securityIdentifier = securityIdentifier;
		this.tradeQuantity = tradeQuantity;
		this.tradeDirection = tradeDirection;
		this.account = account;
		this.operation = operation;
	}
	
	public PositionKey getPositionKey() {
		return new PositionKey(account, securityIdentifier);
	}
	
	public boolean isPositionKeyChangedWithinSameTradeId(TradeEvent otherTradeEvent) {
		Integer otherTradeId = otherTradeEvent.getTradeID();
		if (!otherTradeId.equals(tradeID)) {
			return false;
		}
		
		String otherSecurity = otherTradeEvent.getSecurityIdentifier();
		boolean securityChanged = !otherSecurity.equals(securityIdentifier);
		
		String otherAccount = otherTradeEvent.getAccount();
		boolean accountChanged = !otherAccount.equals(account);
		
		return securityChanged || accountChanged;
	}
	
	public int getTradeID() {
		return tradeID;
	}

	public int getVersion() {
		return version;
	}

	public String getSecurityIdentifier() {
		return securityIdentifier;
	}
	
	public int getTradeQuantity() {
		return tradeQuantity;
	}

	public TradeDirection getTradeDirection() {
		return tradeDirection;
	}

	public String getAccount() {
		return account;
	}

	public Operation getOperation() {
		return operation;
	}
	
	@Override
	public String toString() {
		return "TradeEvent [tradeID=" + tradeID + ", version=" + version + ", securityIdentifier=" + securityIdentifier
				+ ", tradeQuantity=" + tradeQuantity + ", tradeDirection=" + tradeDirection + ", account=" + account
				+ ", operation=" + operation + "]";
	}

}
