package pbts.simulation;

import java.util.ArrayList;

public class Utility {

	public static String arr2String(ArrayList<Integer> a){
		String s = "";
		for(int i = 0; i < a.size(); i++) s = s + a.get(i) + ",";
		return s;
	}
	public static String arr2String(int[] a){
		String s = "";
		for(int i = 0; i < a.length; i++) s = s + a[i] + ",";
		return s;
	}
	public static String arr2String(int[] a, int sz){
		String s = "";
		for(int i = 0; i < sz; i++) s = s + a[i] + ",";
		return s;
	}
	public static String parserSpace(String line) {
		char[] ch= line.toCharArray();
		int n=0;
		for (int i=1;i<ch.length;i++){
			if (ch[i]==' ' && ch[n]=='\t') continue;
			n++;
			ch[n]=ch[i];
			if (ch[n]==' ') ch[n]='\t';
		}
		n++;
		return new String(ch, 0, n);
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
