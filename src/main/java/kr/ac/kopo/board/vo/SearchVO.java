package kr.ac.kopo.board.vo;

public class SearchVO {

	private int currentPage = 1;
	private int perPage = 30;
	private int totalCount = 0;

	private int categoryNo = 0;       // 0 = 전체
	private String searchType = "";   // title | content | writer | titleContent
	private String keyword = "";

	// Oracle ROWNUM 페이징 계산값
	public int getStartRow() {
		return (currentPage - 1) * perPage + 1;
	}

	public int getEndRow() {
		return currentPage * perPage;
	}

	// 페이지 네비게이션 (5페이지 단위 블록)
	public int getTotalPage() {
		return (int) Math.ceil((double) totalCount / perPage);
	}

	public int getStartPage() {
		return ((currentPage - 1) / 5) * 5 + 1;
	}

	public int getEndPage() {
		int end = getStartPage() + 4;
		return Math.min(end, getTotalPage());
	}

	// EL(${search.hasPrevBlock})에서 접근하므로 JavaBean 규약상 is 접두사 필요
	public boolean isHasPrevBlock() {
		return getStartPage() > 1;
	}

	public boolean isHasNextBlock() {
		return getEndPage() < getTotalPage();
	}

	public int getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(int currentPage) {
		this.currentPage = Math.max(1, currentPage);
	}

	public int getPerPage() {
		return perPage;
	}

	public void setPerPage(int perPage) {
		this.perPage = perPage;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

	public int getCategoryNo() {
		return categoryNo;
	}

	public void setCategoryNo(int categoryNo) {
		this.categoryNo = categoryNo;
	}

	public String getSearchType() {
		return searchType;
	}

	public void setSearchType(String searchType) {
		this.searchType = searchType == null ? "" : searchType;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword == null ? "" : keyword;
	}
}
