package nodes;

import java.util.Arrays;



public class Node extends SimpleNode{
	private int [] data;			//7 elements
	

	public Node(int key, int [] data) {
		super(key);
		this.setData(data);
	}

	public int [] getData() {
		return this.data;
	}
	public int getElementData(int number) {
		if(number >= this.data.length) {
			System.out.println("Max number of elements: "+this.data.length);
			return Integer.MAX_VALUE;
		}
		return this.data[number];
	}

	public void setData(int [] data) {
		this.data = data;
	}
	public String toString() {
		return "key:" + getKey()+ Arrays.toString(data);
	}
	
	
}
