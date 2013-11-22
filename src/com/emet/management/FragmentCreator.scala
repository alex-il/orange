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

	def deploy(
//		pa: Accessor[PolicyMO],
		client: Client,
		folderId: String,
		fileName: String,
		fName: String,
		artifactsDir: String,
		fragments: HashMap[String, String],
		serviceObj: Service = null,
		jdbcConnections: HashMap[String, String] = null ) = {

		val pa = client.getAccessor( classOf[PolicyMO] )
		var fragmentId = ""
		var info = "{} created"
		logger.debug( "{} importing ...  ", fName )
		if( polices contains(fName)){
		  val  mo = polices.get(fName).get
		  val folderId = mo.getPolicyDetail.getFolderId
			fragmentId = updateFragment( mo, client, folderId, fileName, fName, artifactsDir, fragments, serviceObj, jdbcConnections )
			info = "{} updated"
		}
		else {
			fragmentId = fragmentImport( client, folderId, fileName, fName, artifactsDir, fragments, serviceObj, jdbcConnections )
		}
		fragments.put( fName, pa.get( fragmentId ).getGuid )
		log.debug( info, fragmentId )

	}
}
