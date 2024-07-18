package file_searcher;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import file_handler.FileManager;

public class SearchSerialDataFile extends Searcher{

	public SearchSerialDataFile() {
		
	}
	public void searchSerialDataFile(FileManager fm, int [] searchKeys, int [] keys) throws IOException {
		int [] diskAcc = new int [keys.length];
		byte[] ReadDataPage;
		int totDiskAcc = 0;
		if(fm.OpenFile() == -1) {
			throw new IOException();
		}
		
		System.out.println("============ keys data file =======================================");
		for(int j = 0; j < searchKeys.length; j++) {				//for every key go to page 1 of file
			fm.getRAF().seek(128);
			int key = keys[searchKeys[j]];
			diskAcc[j] = 0;
			while(fm.getRAF().getFilePointer()<fm.getRAF().length()) {		//read whole file page by page
				ReadDataPage = fm.ReadNextBlock();
				diskAcc[j] += 1;
				totDiskAcc += 1;
				ByteArrayInputStream bis= new ByteArrayInputStream(ReadDataPage);
				DataInputStream ois= new DataInputStream(bis);
				for(int i =0; i<32;i++) {					//each page has 32 numbers
					if(i % 8 == 0) {
						if (key == ois.readInt()){			//if key is found
							System.out.print("Found"+" ");			
							System.out.print(key+" "+"after ");			
							System.out.print(diskAcc[j]+" disk accesses");
							System.out.println();
							break;
						}
						continue;
					}
					ois.readInt();
				}
			}
		}
		System.out.println();
		System.out.println("Average disk accesses for unordered data file: "+(totDiskAcc/searchKeys.length));
		System.out.println();
		if(fm.CloseFile() == 0)
			throw new IOException();
	}
}
