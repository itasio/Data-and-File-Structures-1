package file_searcher;

import java.io.IOException;
import java.util.Vector;

import file_handler.FileManager;
import nodes.SimpleNode;

public abstract class Binary_Search extends Searcher{
	public abstract int searchOrderedFile(FileManager fm, int keySearch) throws IOException;
	
	public  boolean doSearch(int leftIndex, int rightIndex, int keySearch, Vector<? extends SimpleNode> vec) {
		if (rightIndex >= leftIndex) { 
            int mid = leftIndex + (rightIndex - leftIndex) / 2; 
  
            // If the element is present at the 
            // middle itself 
            if (vec.elementAt(mid).getKey() == keySearch) 
            {
            	System.out.println("Found key: "+keySearch);	
                return true; 
            }
            // If element is smaller than mid, then 
            // it can only be present in left sub-array
            if (vec.elementAt(mid).getKey() > keySearch) 
                return doSearch(leftIndex, mid - 1, keySearch, vec); 
  
            // Else the element can only be present 
            // in right sub-array
            return doSearch(mid + 1, rightIndex, keySearch, vec); 
        } 
  
        // We reach here when element is not present in array. 
        // We return false in this case, so the data array can not contain this value!
		return false; 	
	}

	
}
