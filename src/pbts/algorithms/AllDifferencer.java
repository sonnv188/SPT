package pbts.algorithms;

import java.util.HashSet;

import java.util.*;

public class AllDifferencer {

	int N;
	boolean[] used;
	boolean foundSolution;
	int[] s;
	int[] x;
	HashSet[] D;
	public AllDifferencer(int N){
		this.N = N;
		used = new boolean[N];
	}
	private void TRY(int k){
		if(foundSolution) return;
		Iterator it = D[k].iterator();
		while(it.hasNext()){
			int v = (Integer)it.next();
			if(!used[v]){
				x[k] = v;
				used[v] = true;
				if(k == x.length-1){
					foundSolution = true;
					s = new int[x.length];
					for(int j = 0; j < x.length; j++) s[j] = x[j];
				}else{
					TRY(k+1);
				}
				used[v] = false;
			}
		}
	}
	private void init(){
		foundSolution = false;
		for(int i = 0; i < N; i++) used[i] = false;
	}
	public int[] solve(HashSet[] D){
		// D[i] is the domain of item i, D[i] \subseteq {0,...,N-1}
		x = new int[D.length];
		this.D = D;
		init();
		TRY(0);
		if(!foundSolution) return null;
		return s;
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int N = 10;
		
		AllDifferencer A = new AllDifferencer(N);
		HashSet[] D = new HashSet[5];
		for(int i = 0; i < D.length; i++)
			D[i] = new HashSet<Integer>();
		D[0].add(0);
		D[0].add(1);
		D[0].add(5);
		D[1].add(1);
		D[1].add(0);
		D[1].add(5);
		D[2].add(3);
		D[2].add(6);
		D[2].add(9);
		D[3].add(4);
		D[4].add(7);
		int[] s = A.solve(D);
		if(s == null) System.out.println("NO SOLUTION"); 
		else{
			for(int i = 0; i < D.length; i++)
				System.out.println("s[" + i + "] = " + s[i]);
		}
	}

}
