package com.emet.test

object testSimple {
  val init = "init"
  var ttt = init
  def main(args: Array[String]) {

    val sss = "ccc"
    println(sss)
    sss match {
      case "eee" | 
      	   "ccc" => {
        println("case:"+sss)
      }
      case _ => {
        println("default")
      }
    }

    if (!ttt.equals(init))
      println("true");
    else
      println("false");
    ttt = "12345"
    if (!ttt.equals("init")) {
      val extractedLocalValue = "2.true"
      println(extractedLocalValue);
    }; else
      println("2.false");

  }

}