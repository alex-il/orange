/**
 *
 */
package com.emet.management

import java.io.BufferedReader
import java.io.FileReader

import com.google.gson.Gson

/**
 * @author oleg
 *
 */
object GsonReader {

  def main(args: Array[String]): Unit = {
    val gson = new Gson();
    
    val br = new BufferedReader(new FileReader(
					"c:\\file.json"));

    val br2 = scala.io.Source.fromFile("c:\\file.json")
    
    val data = gson.fromJson(br, classOf[DataObjectJ])
    System.out.println(data.data1);
    System.err.println(data.data2);
//    System.err.println(data.data3);
    System.err.println(data.list);
    
  }

}