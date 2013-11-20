package com.emet.management;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;

public class GsonMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		IFAdminDTOJ obj = new IFAdminDTOJ();
		Gson gson = new Gson();

//		String json = gson.toJson(obj);
//		try {
//			// write converted json data to a file named "file.json"
//			FileWriter writer = new FileWriter("c:\\file.json");
//			writer.write(json);
//			writer.close();
//
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		System.out.println(json);

		// Gson gson = new Gson();

		try {
			BufferedReader br = new BufferedReader(new FileReader(
					"c:\\file.json"));

			// convert the json string back to object
			IFAdminDTOJ obj1 = gson.fromJson(br, IFAdminDTOJ.class);

			System.out.println(obj1);
			System.out.println( " obj1.action: "+obj1.action );
			System.out.println( "obj1.artifactsDir: "+obj1.artifactsDir);
//			System.out.println( obj1.list);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
