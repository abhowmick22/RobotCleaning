package utils;

import java.util.ArrayList;
import java.util.List;

public class Combinations {
	
	// this method returns all possible subsequences of length k
	public static <T> List<List<T> > getAllSubSequences(List<T> sequence, int k){
		
		List<List<T> > result = new ArrayList<List<T>>();
		if(k==1){				/* Base case */
			for(T item : sequence){
				List<T> l = new ArrayList<T>();
				l.add(item);
				result.add(l);
			}
		}
		else{
			List<List<T>> subs = getAllSubSequences(sequence, k-1);
			for(List<T> sub : subs){
				for(T item : sequence){
					List<T> l = new ArrayList<T>();
					l.addAll(sub);
					l.add(item);
					result.add(l);
				}
			}
		}
		return result;
	}
	
	public static void main(String[] args){
		List<Integer> li = new ArrayList<Integer>();
		li.add(1);li.add(2);li.add(3);
		List<List<Integer> > subs = getAllSubSequences(li, 2);
		System.out.println("subs of length 2 are " + subs.toString());
	}

}
