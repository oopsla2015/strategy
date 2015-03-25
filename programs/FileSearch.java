/*
 * This file is part of the Panini project at Iowa State University.
 *
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/.
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 * 
 * For more details and the latest version of this code please see
 * http://paninij.org
 * 
 * Contributor(s): Ganesha Upadhyaya
 */

import java.util.*;
import java.io.*;
import java.lang.*;
import java.util.Random;
import java.lang.Math;

class ScanAndIndex {
	public static long FNV_1 (String word) {
		long offset_basis = 2166136261L;
		long FNV_prime = 16777619L;
		
		byte[] wordByteArray = word.getBytes();
		long hash = offset_basis;
		
		//for each octet_of_data to be hashed
		for (int i=0; i<wordByteArray.length-1; i++) {
			hash = hash * FNV_prime;
			hash = hash ^ wordByteArray[i];
		}
		
		return hash;
	}
}

capsule FileCrawler (FileScanner fileScanner, 
		FileFilter fileFilter, String path) {

	private boolean alreadyIndexed (File f) {
		return false;
	}
	
	void crawl() {
		System.out.println("[FileCrawler] starting ...");
		File root = new File(path);
		if (!root.exists()) {
			System.out.println("Incorrect directory path!");
			return;
		}
		_crawl(root);
		// done with work, let's stop the crawler
		halt();
	}
	
	private void _crawl(File root) {
		File[] entries;
		entries = root.listFiles(fileFilter);
		if (entries != null) {
			for (File entry: entries) {
				if (entry.isDirectory()) {
					fileScanner.index(entry);
					_crawl(entry);
				} else if (!alreadyIndexed(entry)) {
					fileScanner.index(entry);
				}
			}
		}
	}
	
	private void halt() {
		fileScanner.halt();
		System.out.println("[FileCrawler] stopping ...");
	}
}

capsule FileScanner (Indexer indexer[], Searcher searcher) {
	=>{
		System.out.println("[FileScanner] starting ...");
	}
	
	void index(File file) {
		indexFile(file);
	}
	
	void halt() {
		System.out.println("[FileScanner] stopping ...");
		searcher.search();
	}
	
	private void indexFile(File file) {
		// index the file
		String fileName = file.getName();
		String absolutePath = file.getAbsolutePath();
		String filePath = absolutePath.substring(0,absolutePath.lastIndexOf(File.separator));
		List<String> wordArray = scan(file);//splitFileName(fileName);
		//System.out.println("File: "+absolutePath+", WordCount: "+wordArray.size());
		Random rnd = new Random();
		int id = Math.abs(rnd.nextInt() % 10);
		for (String w : wordArray) {
			if (w != "") {
				w.trim();
				indexer[id].word(w, filePath+"/"+fileName);
			}
		}
	}
	
	private List<String> scan(File file) {
		List<String> words = new ArrayList<String>();
		try {
			Scanner scanner = new Scanner(file);
			while (scanner.hasNext()) {
				String word = scanner.next();
				if (word.startsWith("A"))
				words.add(word);
			}
		} catch (Exception e) {
			
		}
		return words;
	}
	
	private String[] splitFileName(String fileName) {
		String[] fileNameList = fileName.split("{-:. _]+");
		return fileNameList;
	}
}

capsule Indexer () {
	Map<Long, Integer> wordsHashMap = new Hashtable<Long, Integer>();
	
	String[][] arrayOfPaths = new String[5000][];
	int index = 0;
	
	private void insertArrayOfPaths (String w, String path_filename) {
		long hashkey = 0L;
		int index_temp = 0;
		int j = 0;
		boolean existeHashkey = false;
		
		hashkey = ScanAndIndex.FNV_1(w);
		
		if (!wordsHashMap.containsKey(hashkey)) {
			index = index + 1;
			arrayOfPaths[index] = new String[10000];
			wordsHashMap.put(hashkey, index);
		} else {
			existeHashkey = true;
			index_temp = index;
			index = wordsHashMap.get(hashkey);
			int c = 0;
			for (String p : arrayOfPaths[index]) {
				if (p != null) {
					c = c + 1;
				}
			}
			j = c;
		}
		
		//try {
			arrayOfPaths[index][j] = path_filename;
		/*} catch (ArrayIndexOutOfBoundsException e) {
			throw new NewIndexerException(w, path_filename);
			monitor.check();
		}*/
		
		if (existeHashkey) {
			j = j + 1;
			index = index_temp;
		}
	}
	
	private void showPaths(String str) {
		long hashkey = ScanAndIndex.FNV_1(str);
		if (wordsHashMap.get(hashkey) == null) 
			return;
		//System.out.println(hashkey+", "+wordsHashMap);
		long index = wordsHashMap.get(hashkey);
		
		if (index != 0) {
			int len = arrayOfPaths[(int)index].length;
			for (int j=0; j<len; j++) {
				if (arrayOfPaths[(int)index][j] != null) {
					System.out.println(arrayOfPaths[(int)index][j]);
				}
			}
		}
	}
	
	void word(String w, String path_filename) {
		insertArrayOfPaths(w, path_filename);
	}
	
	void searchIndexer(String str) {
		showPaths(str);
	}
}

capsule Searcher (Indexer indexers[], String s) {
	=>{
		System.out.println("[Searcher] starting ...");
	}

	void search() {
		for (Indexer indexer : indexers)
			indexer.searchIndexer(s);
	}
}

capsule FileSearch (String args[]) {
	FileFilter filter = new FileFilter() {
		@Override
        public boolean accept(File pathname) {
			return true;
		}
	};

	design {
		String path = args[0]; 
		String search = "Actor";
		 FileCrawler fc;
		 FileScanner filescanner;
		 Searcher searcher;
		 Indexer indexers[10];//[100];
		
		fc(filescanner, filter, path);
		filescanner(indexers, searcher);
		searcher(indexers, search);
		
	}

	void run() {
		fc.crawl();
	}
}
