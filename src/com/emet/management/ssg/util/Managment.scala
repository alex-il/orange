package com.emet.management.ssg.util

import com.l7tech.gateway.api.Accessor
import com.l7tech.gateway.api.PolicyMO
import com.emet.management.ssg.IInternal._

object Managment {

	val services = scala.collection.mutable.HashMap.empty[String, PolicyMO]

	def getFolderId( name: String ): String = {
		val k = name + "_Invoke"
		if ( services contains ( k ) ) {
			val r = services.get( k ).get.getPolicyDetail.getFolderId
			System.err.println( r );
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