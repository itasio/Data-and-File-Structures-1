package nodes;

public class IndexNode extends SimpleNode{
	private int page;

	public IndexNode(int key, int page) {
		super(key);
		this.setPage(page);
	}

	public int getPage() {
		return this.page;
	}

	public void setPage(int page) {
		this.page = page;
	}
	public String toString() {
		return "key:" + this.getKey()+ " "+this.page;
	}
}
