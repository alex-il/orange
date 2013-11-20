package com.emet.test

import java.io.File
import scala.collection.JavaConversions.asScalaIterator
import scala.collection.mutable.HashMap
import scala.collection.mutable.Set
import com.l7tech.gateway.api.ClientFactory
import com.l7tech.gateway.api.ClusterPropertyMO
import com.l7tech.gateway.api.FolderMO
import com.l7tech.gateway.api.JDBCConnectionMO
import com.l7tech.gateway.api.JMSDestinationMO
import com.l7tech.gateway.api.PolicyMO
import com.l7tech.gateway.api.ServiceMO
import com.emet.management.ssg.SsgUtil


object QCreator {
  def main(args: Array[String]) = {
    val gwurl = "https://" + "192.168.57.101:8443"  + "/wsman"
    val user = "oleg"
    val password = "qwe"

    val factory = ClientFactory.newInstance()
    factory.setAttribute(ClientFactory.ATTRIBUTE_USERNAME, user)
    factory.setAttribute(ClientFactory.ATTRIBUTE_PASSWORD, password)
    factory.setFeature(ClientFactory.FEATURE_CERTIFICATE_VALIDATION, false)
    factory.setFeature(ClientFactory.FEATURE_HOSTNAME_VALIDATION, false)

    // Create accessor objects 
    val client1 = factory.createClient(gwurl)
    val client = new SsgUtil(gwurl).getClient
    val folderAccessor = client.getAccessor(classOf[FolderMO])
    val policyAccessor = client.getAccessor(classOf[PolicyMO])
    val jdbcAccessor = client.getAccessor(classOf[JDBCConnectionMO])
    val jmsAccessor = client.getAccessor(classOf[JMSDestinationMO])
    val serviceAccessor = client.getAccessor(classOf[ServiceMO])
    val srcAccessor = client.getAccessor(classOf[ClusterPropertyMO])

    // Build QDRAIN service reference map (name => id) 
    println("Starting .......")
    for (cp <- srcAccessor.enumerate) {
      println(cp.getName() +": \t\t" + cp.getValue())
      val cwpMO = srcAccessor.get("name", "oleg___1")
    }
    println("---- end .......")

  }
}