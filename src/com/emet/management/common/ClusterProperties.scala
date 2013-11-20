package com.emet.management.common;

import scala.collection.JavaConversions._
import scala.collection.mutable.HashMap
import com.emet.management.ssg.CwpUtil
import com.l7tech.gateway.api._
import com.l7tech.gateway.api.Accessor.AccessorNotFoundException
import java.io.BufferedReader
import com.emet.management.IFAdminDTOJ
import com.google.gson.Gson
import java.io.FileReader
import org.slf4j.LoggerFactory
import org.apache.commons.lang.reflect.LocalUtils

object ClusterProperties {
  val log = LoggerFactory.getLogger(getClass());
  def createCWPs(dto:IFAdminDTOJ): Unit = {
    val startTime = System.currentTimeMillis()
    val srcUrl = "https://"+dto.ssgHost+":"+dto.ssgPort+"/wsman"
    log.debug("Url:_{}", srcUrl)
    val srcAccessor = new CwpUtil (srcUrl).getAccessor

//    val updatesMap = JavaScalaConverter.example()
    val updatesMap = LocalUtils.populate(dto)

    for (updateKey <- updatesMap.keys) {
      try {
        val cwpMO = srcAccessor.get("name", updateKey)
        
        cwpMO.setValue(updatesMap(updateKey))
        log.debug("Updating.... cwp:{}",cwpMO.getName() +"="+cwpMO.getValue())
        srcAccessor.put(cwpMO)
//        accessor.delete("name", k)
        log.debug("--- updated:{} ", updateKey)  
        
      } catch {
        case e: AccessorNotFoundException =>
          val cwp = ManagedObjectFactory.createClusterProperty
          cwp.setName(updateKey)
        	log.debug("Creating ....{}", updateKey)
        	cwp.setValue(updatesMap(updateKey))
        	srcAccessor.create(cwp)
        	log.debug( "------- created: {}", updateKey)
         case e99: Exception =>	
           log.error(e99.getLocalizedMessage())
           e99.printStackTrace()
      }          
    }
    println( "end................time::"+( System.currentTimeMillis()-startTime)/1000.0 + "sec")
    
  }
  
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
  

    def loadIFAdminDTO(ifaFile: String): IFAdminDTOJ = {
    try {
      val gson = new Gson();
      val br = new BufferedReader(new FileReader(ifaFile))
      val ret = gson.fromJson(br, classOf[IFAdminDTOJ])
      ret
    } catch {
      case e @ _ => {
        null
      }
    }
  }

}

