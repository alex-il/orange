package com.emet.management

import java.io.File
import java.io.PrintWriter
import scala.collection.JavaConversions.asScalaIterator
import scala.collection.JavaConversions.mapAsScalaMap
import scala.collection.JavaConversions.mutableMapAsJavaMap
import scala.collection.mutable.HashMap
import org.slf4j.LoggerFactory
import com.emet.management.common.ClusterProperties
import com.emet.management.common.IUi.done
import com.emet.management.common.IUi.errorTag
import com.emet.management.ssg.SsgUtil
import com.emet.management.ssg.util.Managment._
import com.l7tech.gateway.api.FolderMO
import com.l7tech.gateway.api.JDBCConnectionMO
import com.l7tech.gateway.api.JMSConnection
import com.l7tech.gateway.api.JMSDestinationMO
import com.l7tech.gateway.api.ManagedObjectFactory
import com.l7tech.gateway.api.PolicyMO
import JdbcCreator.createSingle
import com.emet.management.ssg.IInternal._
import utils._
import com.l7tech.gateway.api.Client
import com.l7tech.gateway.api.Accessor

/**
  *
  * @author Oleg Branopolsky
  *
  */

object FragmentCreator {

	val log = LoggerFactory.getLogger( getClass() );
	/**
	  * main
	  */
	def main( args: Array[String] ): Unit = {

	}

	def generate(
//		pa: Accessor[PolicyMO],
		client: Client,
		standardFolderId: String,
		filename: String,
		fName: String,
		artifactsDir: String,
		fragments: HashMap[String, String],
		service: Service = null,
		jdbcConnections: HashMap[String, String] = null ) = {

		val pa = client.getAccessor( classOf[PolicyMO] )
		var fragmentId = ""
		var info = "{} created"
		var pMO: PolicyMO = null
		logger.debug( "{} importing ...  ", fName )
		val mo = pa.get( imq, fName )
		if ( mo == null ) {
			fragmentId = fragmentImport( client, standardFolderId, fName, fName, artifactsDir, fragments, null, jdbcConnections )
		}
		else {
			fragmentId = updateFragment( mo, client, standardFolderId, fName, fName, artifactsDir, fragments, null, jdbcConnections )
			info = "{} updated"
		}
		fragments.put( fName, pa.get( fragmentId ).getGuid )
		log.debug( info, fragmentId )

	}
}
