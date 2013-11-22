package com.emet.management.ssg.util

import com.l7tech.gateway.api.Accessor
import com.l7tech.gateway.api.PolicyMO
import com.emet.management.ssg.IInternal._

object Managment {

	val polices = scala.collection.mutable.HashMap.empty[String, PolicyMO]

	def getFolderId( name: String ): String = {
		val k = name + "_Invoke"
		if ( polices contains ( k ) ) {
			val r = polices.get( k ).get.getPolicyDetail.getFolderId
			r
		}
		else {
			"999999"
		}
	}

	/**
	  *
	  */
	def isObjectExist( policyAccessor: Accessor[PolicyMO], fName: String ): PolicyMO = {
		try {
			policyAccessor.get( imq, fName )
		}
		catch {
			case _ => null
		}

	}

}