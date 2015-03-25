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

/**
 * k-nucleotide benchmark : Hashtable update and k-nucleotide strings
 * http://benchmarksgame.alioth.debian.org/u32/performance.php?test=knucleotide 
 * @author Ganesha Upadhyaya
 */

import java.util.*;
import java.io.*;

final class ByteString implements Comparable<ByteString> {
	public int hash, count = 1;
	public final byte bytes[];

	public ByteString(int size) {
		bytes = new byte[size];
	}

	public void calculateHash(byte k[], int offset) {
		int temp = 0;
		for (int i = 0; i < bytes.length; i++) {
			byte b = k[offset + i];
			bytes[i] = b;
			temp = temp * 31 + b;
		}
		hash = temp;
	}

	public int hashCode() {
		return hash;
	}

	public boolean equals(Object obj) {
		return Arrays.equals(bytes, ((ByteString) obj).bytes);
	}

	public int compareTo(ByteString other) {
		if (other.count != count) {
			return other.count - count;
		} else {
			// Without this case, if there are two or more strings
			// with exactly the same count in a Map, then the
			// TreeSet constructor called in writeFrequencies will
			// only add the first one, and the rest will not
			// appear in the output. Also this is required to
			// satisfy the rules of the k-nucleotide problem.
			return toString().compareTo(other.toString());
		}
	}

	public String toString() {
		return new String(bytes);
	}
}

class Result {
	Map<ByteString, ByteString> m;
	public Result(Map<ByteString, ByteString> map) {
		this.m = map;
	}
	Map<ByteString, ByteString> get() {
		return m;
	}
}

capsule Nucleotide(Collector c) {
	void createFragmentMap(byte[] sequence, int offset, int fragmentLength) {
		HashMap<ByteString, ByteString> map = new HashMap<ByteString, ByteString>();
		int lastIndex = sequence.length - fragmentLength + 1;
		ByteString key = new ByteString(fragmentLength);
		for (int index = offset; index < lastIndex; index += fragmentLength) {
			key.calculateHash(sequence, index);
			ByteString fragment = map.get(key);
			if (fragment != null) {
				fragment.count++;
			} else {
				map.put(key, key);
				key = new ByteString(fragmentLength);
			}
		}
		c.collect(new Result(map));
	}
}

capsule SequenceGen {
	byte[] generate() {
		byte[] sequence = null;
		try {
			String line;
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			while ((line = in.readLine()) != null) {
				if (line.startsWith(">THREE"))
					break;
			}
	
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte bytes[] = new byte[100];
			while ((line = in.readLine()) != null) {
				if (line.length() > bytes.length)
					bytes = new byte[line.length()];
	
				int i;
				for (i = 0; i < line.length(); i++)
					bytes[i] = (byte) line.charAt(i);
				baos.write(bytes, 0, i);
			}
			sequence = baos.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sequence;
	}
}

capsule Collector {
	byte[] sequence;
	List<Result> resultObjs = new ArrayList<Result>();
	int n = 46;
	
	void init(byte[] seq) {
		sequence = seq;
	}
	
	void collect(Result res) {
		n--;
		resultObjs.add(res);
		if (n == 0) {
			process();
		}
	}
	
	private void process() {
		List<Map<ByteString, ByteString>> results = new ArrayList<Map<ByteString, ByteString>>();
		for(Result resObj : resultObjs) {
			results.add(resObj.get());
		}
		StringBuilder sb = new StringBuilder();
		sb.append(writeFrequencies(sequence.length, results.get(0)));
		sb.append(writeFrequencies(sequence.length - 1, sumTwoMaps(results.get(1), results.get(2))));
		
		String[] nucleotideFragments = { "ggt", "ggta", "ggtatt", "ggtattttaatt", "ggtattttaatttatagt" };
		for (String nucleotideFragment : nucleotideFragments) {
		    sb.append(writeCount(results, nucleotideFragment));
		}
		System.out.print(sb.toString());
	}
	
	private String writeFrequencies(float totalCount,
			Map<ByteString, ByteString> frequencies) {
		SortedSet<ByteString> list = new TreeSet<ByteString>(
				frequencies.values());
		StringBuilder sb = new StringBuilder();
		for (ByteString k : list)
			sb.append(String.format("%s %.3f\n", k.toString().toUpperCase(),
					(float) (k.count) * 100.0f / totalCount));

		return sb.append('\n').toString();
	}
	
	private Map<ByteString, ByteString> sumTwoMaps(Map<ByteString, ByteString> map1,
			Map<ByteString, ByteString> map2) {
		for (Map.Entry<ByteString, ByteString> entry : map2.entrySet()) {
			ByteString sum = map1.get(entry.getKey());
			if (sum != null)
				sum.count += entry.getValue().count;
			else
				map1.put(entry.getKey(), entry.getValue());
		}
		return map1;
	}
	
	private String writeCount(List<Map<ByteString, ByteString>> results,
			String nucleotideFragment) {
		ByteString key = new ByteString(nucleotideFragment.length());
		key.calculateHash(nucleotideFragment.getBytes(), 0);

		int count = 0;
		for (Map<ByteString, ByteString> result : results) {
			ByteString temp = result.get(key);
			if (temp != null)
				count += temp.count;
		}

		return count + "\t" + nucleotideFragment.toUpperCase() + '\n';
	}
	
}

capsule Knucleotide (String args[]) {
	design {
		 SequenceGen sGen;
		 Nucleotide nucleotides[46];
		 Collector c;
		 wireall(nucleotides, c);
	}
	
	void run() {
		byte[] sequence = sGen.generate();
		c.init(sequence);
		int count = 0;
		int[] fragmentLengths = { 1, 2, 3, 4, 6, 12, 18 };
		for (int fragmentLength : fragmentLengths) {
		    for (int index=0; index<fragmentLength; index++) {
				final int offset = index;
				final int finalFragmentLength = fragmentLength;
				nucleotides[count].createFragmentMap(sequence, offset, finalFragmentLength);
		    	count++;
		    }
		}
	}
}