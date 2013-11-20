package com.emet.management;



import java.util.ArrayList;
import java.util.List;
 
public class DataObjectJ {
	int data1 = 199;
	String data2 = "hello";
	Boolean data3 = true;
	List<String> list = new ArrayList<String>() {
	  {
		add("String 1");
		add("String 2");
		add("String 3");
	  }
	};
	
 
//	@Override
//	public String toString() {
//	   return "DataObject [data1=" + data1 + ", data2=" + data2 + ", list="
//		+ list + "]";
//	}
 
}