package jpmorgan.test.bookkeeper;

public class PositionKey {
	private String account;
	private String securityIdentifier;
	
	public PositionKey(String account, String securityIdentifier) {
		this.account = account;
		this.securityIdentifier = securityIdentifier;
	}
	
	@Override
	public String toString() {
		return securityIdentifier + "/" + account;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((account == null) ? 0 : account.hashCode());
		result = prime * result + ((securityIdentifier == null) ? 0 : securityIdentifier.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PositionKey other = (PositionKey) obj;
		if (account == null) {
			if (other.account != null)
				return false;
		} else if (!account.equals(other.account))
			return false;
		if (securityIdentifier == null) {
			if (other.securityIdentifier != null)
				return false;
		} else if (!securityIdentifier.equals(other.securityIdentifier))
			return false;
		return true;
	}
}
