package kr.ac.kopo.message.vo;

public class ConversationVO {

	private String partnerId;
	private String partnerNickname;
	private int unreadCount;
	private String lastDate;

	public String getPartnerId() {
		return partnerId;
	}

	public void setPartnerId(String partnerId) {
		this.partnerId = partnerId;
	}

	public String getPartnerNickname() {
		return partnerNickname;
	}

	public void setPartnerNickname(String partnerNickname) {
		this.partnerNickname = partnerNickname;
	}

	public int getUnreadCount() {
		return unreadCount;
	}

	public void setUnreadCount(int unreadCount) {
		this.unreadCount = unreadCount;
	}

	public String getLastDate() {
		return lastDate;
	}

	public void setLastDate(String lastDate) {
		this.lastDate = lastDate;
	}
}
