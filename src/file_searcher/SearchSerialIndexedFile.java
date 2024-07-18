package file_searcher;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import file_handler.FileManager;

public class SearchSerialIndexedFile extends Searcher{

	public SearchSerialIndexedFile() {
		
	}
	
//	public void searchIndexedFile(RandomAccessFile fileA, FileManager f1, RandomAccessFile fl, FileManager fm, int [] searchKeys, int [] keys) throws IOException {
	public void searchIndexedFile(FileManager f1, FileManager f2, int [] searchKeys, int [] keys) throws IOException {
		int [] diskAcc = new int [keys.length];
		byte[] ReadDataPage = new byte[FileManager.page_size];
		int totDiskAcc = 0;
		if(f1.OpenFile() == -1 || f2.OpenFile() == -1) {
			throw new IOException();
		}
		System.out.println("============ keys index file ======================================");
		for(int j = 0; j<searchKeys.length; j++) {				//for every key go to page 1 of file
			f2.getRAF().seek(0);
			int key = keys[searchKeys[j]];
			diskAcc[j] = 0;
			while(f2.getRAF().getFilePointer()<f2.getRAF().length()) {		//read whole file page by page
				ReadDataPage = f2.ReadNextBlock();
				diskAcc[j] += 1;
				totDiskAcc += 1;
				ByteArrayInputStream bis= new ByteArrayInputStream(ReadDataPage);
				DataInputStream ois= new DataInputStream(bis);
				for(int i =0; i<32;i++) {						//each page has 32 numbers
					if(i % 2 == 0) {
						if (key == ois.readInt()){				//if key is found
							diskAcc[j] += 1;		//disk accesses +1 because i count one more access
							totDiskAcc += 1;		// to get file page from file A
							int pos = ois.readInt();
							byte []ReadDataPage2 = f1.ReadBlock(pos);	//go to pos page of dataFile and retrieve page file 
							ByteArrayInputStream bis2= new ByteArrayInputStream(ReadDataPage2);
							DataInputStream ois2= new DataInputStream(bis2);
							//search to page 
							for(int k =0; k<32;k++) {					//each page has 32 numbers
								if(k % 8 == 0) {
									if (key == ois2.readInt()){			//if key is found
										System.out.print("Found"+" ");			
										System.out.print(key+" in page "+pos+" after ");			
										System.out.print(diskAcc[j]+" disk accesses ");
										System.out.print("Info node: ");
										for(int m = 0; m < 7; m++) {
											System.out.print(ois2.readInt()+" ");
										}
										System.out.println();
										break;
									}
									continue;
								}
								ois2.readInt();
				
							}
							
							break;
						}
						continue;			//if key != ois.readInt()
					}
					ois.readInt();
					;
	
				}	
			}
		}
		System.out.println();
		System.out.println("Average disk accesses for unordered indexed file: "+(totDiskAcc/searchKeys.length));
		System.out.println();
		if(f1.CloseFile() == 0 || f2.CloseFile() == 0) {
			throw new IOException();
		}
	}
}
