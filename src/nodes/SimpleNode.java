package nodes;

public abstract class SimpleNode{
	private int key;

	public SimpleNode(int key) {
		this.setKey(key);
	}
	public int getKey() {
		return this.key;
	}

	public void setKey(int key) {
		this.key = key;
	}
	public abstract String toString();
	
}
