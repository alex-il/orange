package com.emet.management.ssg.util

import com.l7tech.gateway.api.Accessor
import com.l7tech.gateway.api.PolicyMO
import com.emet.management.ssg.IInternal._

object Managment {

	/**
	  *
	  */
	def isObjectExist( policyAccessor: Accessor[PolicyMO], fName: String ): PolicyMO = {
		try {
			policyAccessor.get( imq, fName )
		}
		catch {
			case _ =>	null
		}

	}

}