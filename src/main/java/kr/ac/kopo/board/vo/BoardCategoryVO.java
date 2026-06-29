package kr.ac.kopo.board.vo;

public class BoardCategoryVO {

	private int no;
	private String name;
	private String isAdminOnly;
	private int sortOrder;

	public int getNo() {
		return no;
	}

	public void setNo(int no) {
		this.no = no;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIsAdminOnly() {
		return isAdminOnly;
	}

	public void setIsAdminOnly(String isAdminOnly) {
		this.isAdminOnly = isAdminOnly;
	}

	public int getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
	}
}
