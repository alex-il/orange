package com.emet.test;

import java.util.HashMap;
import scala.Predef;
import scala.Tuple2;
import scala.collection.JavaConverters;
import scala.collection.immutable.Map;

public class JavaScalaConverter {

	public static void main(String[] args) {
		toScalaMap(example());
	}

	public static void m1() {
		toScalaMap(example());
	}
	
	public static <A, B> Map<A, B> toScalaMap(HashMap<A, B> m) {
		return JavaConverters.mapAsScalaMapConverter(m).asScala()
				.toMap(Predef.<Tuple2<A, B>> conforms());
	}

	public static HashMap<String, String> example() {
		HashMap<String, String> m = new HashMap<String, String>();
		m.put("1_a", "A");
		m.put("12_b", "B");
		m.put("123_c", "C");
		return m;
	}
	
	public static HashMap<String, Object> getHashMap() {
		HashMap<String, Object> m = new HashMap<String, Object>();
		return m;
	}
}
