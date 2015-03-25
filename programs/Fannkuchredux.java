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

import java.util.ArrayList;
import java.util.List;

/**
 * fannkuch-redux benchmark : Indexed-access to tiny integer-sequence
 * http://benchmarksgame.alioth.debian.org/u32/performance.php?test=fannkuchredux#about
 * @author Ganesha Upadhyaya
 */

class Result {
	int maxFlips;
	int chkSums;
	public Result(int m, int c) {
		this.maxFlips = m;
		this.chkSums = c;
	}
	int get_max_flip() {
		return this.maxFlips;
	}
	int get_chk_sum() {
		return chkSums;
	}
}

capsule fannkuchred(int task, int n, Collector c) {
	int NCHUNKS = 150;
	int CHUNKSZ = 0;
	int NTASKS = 0;
	int[] maxFlips = null;
	int[] chkSums = null;
	//AtomicInteger taskId = new AtomicInteger(0);
	int[] p = null, pp = null, count = null;
    int[] Fact = null;
	
	void print() 
	{
        for ( int i = 0; i < p.length; i++ ) {
            System.out.print( p[i] + 1 );
        }
        System.out.println();
    }
	
	private void firstPermutation( int idx ) 
	{
        for ( int i=0; i<p.length; ++i ) {
           p[i] = i;
        }

        for ( int i=count.length-1; i>0; --i ) {
            int d = idx / Fact[i];
            count[i] = d;
            idx = idx % Fact[i];

            System.arraycopy( p, 0, pp, 0, i+1 );
            for ( int j=0; j<=i; ++j ) {
                p[j] = j+d <= i ? pp[j+d] : pp[j+d-i-1];
            }
        }
    }
	
	private boolean nextPermutation()
    {
        int first = p[1];
        p[1] = p[0];
        p[0] = first;
        
        int i=1; 
        while ( ++count[i] > i ) {
            count[i++] = 0;
            int next = p[0] = p[1];
            for ( int j=1; j<i; ++j ) {
                p[j] = p[j+1];
            }
            p[i] = first;
            first = next;
        }
        return true;
    }

	private int countFlips()
    {
        int flips = 1;
        int first = p[0];
        if ( p[first] != 0 ) {
            System.arraycopy( p, 0, pp, 0, pp.length );
            do {
                 ++flips;
                 for ( int lo = 1, hi = first - 1; lo < hi; ++lo, --hi ) {
                    int t = pp[lo];
                    pp[lo] = pp[hi];
                    pp[hi] = t;
                 }
                 int t = pp[first];
                 pp[first] = first;
                 first = t;
            } while ( pp[first] != 0 );
        }
        return flips;
    }
	
	private Result runTask()
    {
        int idxMin = task*CHUNKSZ;
        int idxMax = Math.min( Fact[n], idxMin+CHUNKSZ );

        firstPermutation( idxMin );

        int maxflips = 1;
        int chksum = 0;
        for ( int i=idxMin;; ) {

            if ( p[0] != 0 ) {
                int flips = countFlips();
                maxflips = Math.max( maxflips, flips );
                chksum += i%2 ==0 ? flips : -flips;
            }

		    if ( ++i == idxMax ) {
		        break;
		    }
            nextPermutation();
        }
		//maxFlips[task] = maxflips;
		//chkSums[task]  = chksum;
		return new Result(maxflips, chksum);
    }
	
	void work()
    {
        p     = new int[n];
        pp    = new int[n];
        count = new int[n];    
        
        Fact = new int[n+1];
        Fact[0] = 1;
        for ( int i=1; i<Fact.length; ++i ) {
            Fact[i] = Fact[i-1] * i;
        }

        CHUNKSZ = (Fact[n] + NCHUNKS - 1) / NCHUNKS;
    	NTASKS = (Fact[n] + CHUNKSZ - 1) / CHUNKSZ;
        maxFlips = new int[NTASKS];
        chkSums  = new int[NTASKS];
        
        //int task;
        //while ( ( task = taskId.getAndIncrement() ) < NTASKS ) {
        //return	runTask();
        Result res = runTask();
        //for(int i=0; i<1024; i++) {
        	//System.out.println("Ping Collector!");
        //	c.ping();
        //}
        c.collect(res);
        //}
    }
}

capsule Collector {
	int flips = 0, chk = 0;
	int n = 8;
	void collect(Result res) {
		//for(Result res : results) {
			int u = res.get_max_flip();
			int v = res.get_chk_sum();
			flips = Math.max( flips, u );
			chk += v;
		//}
		n--;
		if (n == 0)
			printResult(flips, chk );
	}
	private void printResult( int res, int chk ) 
	{
        System.out.println( chk+"\nPfannkuchen("+n+") = "+res );
    }
	void ping() {
		//System.out.println("Alive!");
	}
	
}

capsule Fannkuchredux(String[] args) {
	int n = 12;
	design {
		int n = 12;//args.length > 0 ? Integer.parseInt( args[0] ) : 12;
		/*if ( n < 0 || n > 12 ) {         // 13! won't fit into int
            printResult( n, -1, -1 );
            return;
        }
        if ( n <= 1 ) {
            printResult( n, 0, 0 );
            return;
        }*/
        
	fannkuchred frdux[8];
    	Collector c;
        frdux[0](0,n,c);
        frdux[1](1,n,c);
        frdux[2](2,n,c);
        frdux[3](3,n,c);
        frdux[4](4,n,c);
        frdux[5](5,n,c);
        frdux[6](6,n,c);
        frdux[7](7,n,c);
	}
	
	private void printResult( int res, int chk ) 
	{
        System.out.println( chk+"\nPfannkuchen("+n+") = "+res );
    }
	
	void run() 
	{
		//List<Result> results = new ArrayList<Result>();
		//for(int iter=0; iter<25; iter++) {
			for(fannkuchred fr: frdux) {
				fr.work();
			}
		//}
		/*int flips = 0, chk = 0;
		for(Result res : results) {
			int u = res.get_max_flip();
			int v = res.get_chk_sum();
			flips = Math.max( flips, u );
			chk += v;
		}
		printResult(flips, chk );*/
	}

}
