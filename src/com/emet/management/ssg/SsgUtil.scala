package com.emet.management.ssg

import com.l7tech.gateway.api.Client
import com.l7tech.gateway.api.ClientFactory
import com.emet.management.common.IInternal._
import com.l7tech.gateway.api.Accessor
import com.l7tech.gateway.api.ClusterPropertyMO
import java.util.HashMap

class SsgUtil( url: String) {
	var clients = scala.collection.mutable.HashMap.empty[String, Client]
  val gwurl: String = url
  
  def getClient(): Client = {
	  if( clients.contains(url)){
	    clients(url)
	  }
    val clientFactory = ClientFactory.newInstance()
    clientFactory.setAttribute(ClientFactory.ATTRIBUTE_USERNAME, "admin")
    clientFactory.setAttribute(ClientFactory.ATTRIBUTE_PASSWORD, "password")
    clientFactory.setFeature(ClientFactory.FEATURE_CERTIFICATE_VALIDATION, false)
    clientFactory.setFeature(ClientFactory.FEATURE_HOSTNAME_VALIDATION, false)
    val c = clientFactory.createClient(gwurl)
    clients.put(url, c)
    c
  }

}