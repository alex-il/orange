package com.emet.test
import com.l7tech.gateway.api._
import scala.collection.mutable.HashMap
import collection.JavaConversions._
import com.l7tech.gateway.api.Accessor.AccessorNotFoundException
import com.emet.management.ssg.CwpUtil

object CwpPropertiesCopier {
/**
 *
 * Tester 
 * 
 */  
  def main(args: Array[String]): Unit = {
//    testCreateCWPs
    createCWPs
  }
  
  def testCreateCWPs(): Unit = {
    println("Working .....................")
    val startTime = System.currentTimeMillis()
    val gwlistener = ":8443"
    val srcUrl = "https://10.4.11.229:8443/wsman"
    println("srcUrl" +srcUrl)
    val srcAccessor = new CwpUtil (srcUrl).getAccessor

    // List of CPs to update
    val updatesMap = HashMap.empty[String, String]

    updatesMap.put("1__cwp__", "val1_")
//    mapToUpdate.put("2_cwp", "val22")
//    mapToUpdate.put("3_cwp", "val33")
//    mapToUpdate.put("4_cwp", "val44")
//    mapToUpdate.put("5_cwp", "val55")
//    mapToUpdate.put("6_cwp", "val66")

    
    
    for (updateKey <- updatesMap.keys) {
      try {
      	println("Updating.... cwp: ")
        val cwpMO = srcAccessor.get("name", updateKey)
        
        cwpMO.setValue(updatesMap(updateKey))
        println(cwpMO.getName()+" = "+cwpMO.getValue())
        println(cwpMO.getProperties())
        
        srcAccessor.put(cwpMO)
//        accessor.delete("name", k)
        println(updateKey + " updated")  
        
      } catch {
        case e: AccessorNotFoundException =>
          System.err.println( e.getLocalizedMessage() )
          val cwp = ManagedObjectFactory.createClusterProperty
          cwp.setName(updateKey)
        	println("Creating ...."+ updateKey)
        	cwp.setValue(updatesMap(updateKey))
        	srcAccessor.create(cwp)
        	println(updateKey + " created")
         case e99: Exception =>	
           e99.printStackTrace()
      }          
    }
    println( "end................time::"+( System.currentTimeMillis()-startTime)/1000.0 + "sec")
    
  }
  
//  def populateCwpMap(dto:String):HashMap = {
//    val cwpMap = HashMap.empty[String, String]
//    cwpMap
//  }
  
//---------------------------------------------------------
  def createCWPs(): Unit = {
    println("Working ..........enumerationg...........")
    val startTime = System.currentTimeMillis()
    val gwlistener = ":8443"
    val srcUrl = "https://10.4.11.227:8443/wsman"
//    val gwurl = "https://10.5.199.41:8443/wsman"
    val srcAccessor = new CwpUtil(srcUrl).getAccessor
    println("srcUrl" + srcUrl)

    val updatesMap = HashMap.empty[String, String]
    val currentMap = HashMap.empty[String, ClusterPropertyMO]
        
    var name = ""
    var count = 0  
    for (cp <- srcAccessor.enumerate) {
      if(cp.getName.contains("CS_MANAGE_ORDER_ONE") ){
      	name = cp.getName
        println(name  +";  value: " +cp.getValue())
      	updatesMap.put(name, cp.getValue())
      	count = count+1
//        name = cp.getName.replace("CS_MANAGE_ORDER_ONE", "Update_Asset_Status")
      }
    }
    println("Found "+count+ " params")
    println( "Read time::"+( System.currentTimeMillis()-startTime)/1000.0 + "sec")
    val destUrl = "https://10.4.11.229:8443/wsman" 
    val destAccessor = new CwpUtil(destUrl ).getAccessor
    println("destUrl" + destUrl)

    updatesMap.put("11_cwp____", "1_val1_")
    for (updateKey <- updatesMap.keys) {
      print(count + ".\t")
      try {
      	println("Updating.... cwp.")
        val cwpMO = destAccessor.get("name", updateKey)
        cwpMO.setValue(updatesMap(updateKey))
        print(cwpMO.getName()+" = "+cwpMO.getValue())
//        srcAccessor.put(cwpMO)
        destAccessor.put(cwpMO)
//        accessor.delete("name", k)
        println("\t updated")  
      } catch {
        case e: AccessorNotFoundException =>
          System.err.println( e.getLocalizedMessage() )
          val cwp = ManagedObjectFactory.createClusterProperty
          cwp.setName(updateKey)
        	println("Creating ...."+ updateKey)
        	cwp.setValue(updatesMap(updateKey))
        	destAccessor.create(cwp)
        	println(updateKey + " created")
        case e99: Exception =>	
          e99.printStackTrace()
      }          
    count = count -1
    }
    
    println( "end................time::"+( System.currentTimeMillis()-startTime)/1000.0 + "sec")
  }
  
  
}

