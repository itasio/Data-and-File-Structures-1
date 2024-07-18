package nodes;

import java.util.Comparator;

public class IndexNodeComparator implements Comparator<IndexNode>{


	@Override
	public int compare(IndexNode o1, IndexNode o2) {		
		return	o1.getKey() - o2.getKey();
	}

}