package com.emet.management.ssg

import com.l7tech.gateway.api.Client
import com.l7tech.gateway.api.ClientFactory
import com.emet.management.common.IInternal._
import com.l7tech.gateway.api.Accessor
import com.l7tech.gateway.api.ClusterPropertyMO

 class CwpUtil(url: String)  extends SsgUtil(url){
  val client = getClient()
  def getAccessor(): Accessor[ClusterPropertyMO] = {
		client.getAccessor(classOf[ClusterPropertyMO])
  }

  
}