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
import utils.Service
import utils.delFolderRec
import utils.findOrCreate
import utils.fragmentImport
import utils.loadIFAdminDTO
import utils.printFolders
import utils.restImport
import utils.soapImport
import utils.updateFragment

// 
/**
  * @author Oleg Branopolsky
  * @author Yoel Jacobsen - E&M Computing LTD
  */

object PolicyCreatorEsbII {

	val logger = LoggerFactory.getLogger( getClass() );
	/**
	  * main
	  */
	def main( args: Array[String] ): Unit = {
		System.err.println( "--------------" );
		org.apache.commons.lang.bol.AppVersion.get();
		if ( args.length > 0 )
			createPolicy( args.apply( 0 ) )
		else {
			System.err.println( "----No file found----------" );
			-9
		}
	}

	/**
	  *
	  * createPolicy
	  *
	  */
	def createPolicy( ifaFile: String ): String = {
		val startTime = System.currentTimeMillis()
		val ifaDto = loadIFAdminDTO( ifaFile )
		if ( ifaDto == null ) {
			logger.error( "Cant load params file" )
			errorTag + "Cant load params file"
			System.exit( -944 )
		}
		utils.ifaDto = ifaDto

		ifaDto.action match {
			case "checkout" => {
				logger.trace( "action : checkout, exit(100)" )
				System.err.println( done );
				System.exit( 100 )
			}
			case "create" => {
				logger.trace( "action : create" )
			}

			case "updateJdbcConn" => {
				logger.trace( "action : updateJdbcConn" )
				createSingle( ifaDto )
				System.err.println( done );
				System.exit( 110 )
			}

			case "delete" => {
				logger.trace( "action : delete" )
				System.err.println( done );
				System.exit( 115 )
			}

			case "update" => {
				//        SsgConfigurationUpdater.setSsgServiceConfig(ifaDto)
				//        updateSsgParam()
				ClusterProperties.createCWPs( ifaDto )
				logger.debug( "action:update. Service:{}", ifaDto.isServiceEnable )
				System.err.println( done )
				System.exit( 125 )
			}

			case _ => {
				logger.error( "action is unsupported" )
				System.err.println( errorTag )
				System.exit( -999 )
			}

		}

		// REAL or STUB
		val stub = false // REAL SERVICES
		val ssgEnvName = ifaDto.envName
		val ssgSocket = ifaDto.ssgHost + ":" + ifaDto.ssgPort
		val jmsServer = ifaDto.jmsProviderUrl

		// SERVICE templateing workarounds
		// FUNCDATE => Don't use a multivalued field for JDBC FUNC arguments
		val workarounds = List( "FUNCDATE" )
		val ssgUrl = "https://" + ssgSocket + "/wsman"
		val ssgUser = ifaDto.ssgUser
		val ssgPwd = ifaDto.ssgPwd
		val artifactsDir = ifaDto.artifactsDir
		val serviceFolderName = ifaDto.serviceFolderName
		val standardFolderName = ifaDto.standardFolderName

		val basicWsdl = scala.io.Source.fromFile( new File( ifaDto.wsdlFileName ).getPath ).mkString
		val supportDefaultValues = true
		val jmsProviderUrl = ifaDto.jmsProviderUrl
		val sfs = List()
		val emsQueueConnectionFactory = "ESBL7QCF"
		val recreateStandardFragments = false
		val jmsInboundPrefix = ifaDto.jmsInboundPrefix

		//    val qdEndPointEsbIITemplate = "/Legacy-Imported/%s/asyncQueueListener"
		val qdEndPointEsbIITemplate = "/EsbII/%s/asyncQueueListener"

		val sfmnames = List(
			"GovernanceInvoker_EsbII"
				, 
			"ConcurrencyLimitsChecker_EsbII",
			"SOAPResponseTemplate_EsbII", "AsyncDecision_EsbII", 
			"Responser_EsbII", "AsyncEarlyResponse_EsbII", "SNMPMessageSender_EsbII", 
			"AsyncInvocator_EsbII", "Escaper_EsbII", "UnEscaper_EsbII", "DBE_Accept_ESBII", 
			"DBE_Execute_ESBII", "DBE_Receive_ESBII", "DBE_Response_ESBII", "ErrorHandler_EsbII", 
			"ErrorResponseImmediately_EsbII", "GovernanceResponser_EsbII", 
			"InvocationResponseHandler_EsbII", 
			"PayloadExtractor_EsbII", "ServiceConfigurationReader_EsbII", 
			"TimeoutCalculator_EsbII", "RequestIDGenerator_EsbII", "PostInvoker_EsbII", 
			"PreInvocation_EsbII"
		)

		//-------------------------------------------------------      
		val sfmnames2 = List(
			"Legacy - Splunk Data Creator",
			"Legacy - Timeout Calculator",
			"Legacy - Generate Request ID",
			"Legacy - SOAP Response Template",
			"Legacy - Check Concurrency Limits",
			"Legacy - Async Decision",
			"Legacy - Send SNMP Message",
			"Legacy - Async Invocation",
			"Legacy - Audit Accept Request To DBE",
			"Legacy - Audit Executing to DBE",
			"Legacy - Audit Request To DBE",
			"Legacy - Audit Response to DBE",
			"Legacy - Audit To Tracer",
			"Legacy - Create JDBC arguments",
			"Legacy - Extract payload element",
			"Legacy - Get Cached Response",
			"Legacy - Get Service Configuration",
			"Legacy - Handle Invocation Response",
			"Legacy - GetResponse governance policy",
			"Legacy - Escaper",
			"Legacy - Invoke governance policy",
			"Legacy - Pre Invocation Fragment",
			"Legacy - Post Invocation Fragment",
			"Legacy - Parse Payload",
			"Legacy - Parse POST arguments",
			"Legacy - Parse GET arguments",
			"Legacy - Synchronous Invocation",
			"Legacy - Tracer Push DBE onto queue",
			"Legacy - Tracer Send DBE to Database",
			"Legacy - Tracer Audit Request To DBE",
			"Legacy - Add Xml Header",
			"Legacy - Async Early Response",
			"Legacy - Escaper Restorer",
			"Legacy - Get Response Status",
			"Legacy - HTTP Renderer",
			"Legacy - XML Request Reply Validator By Xsd",
			"Legacy - IF_REPLY Validator By Xsd",
			"Legacy - IF_REQUEST Validator By Xsd",
			"Legacy - Last details",
			"Legacy - Setup concurrency limits"
		)

		//    val serviceInvoke = "SERVICE_NAME - Invoke"
		val serviceInvoke = "SERVICE_NAME_Invoke"
		val pfmnames = List(
			//      "SERVICE_NAME - Pre Invocation 1",
			//      "SERVICE_NAME - Pre Invocation 2",
			//      "SERVICE_NAME - Post Invocation 1",
			//      "SERVICE_NAME - Post Invocation 2"
			"SERVICE_NAME__Pre_Invocation1", "SERVICE_NAME__Pre_Invocation2", "SERVICE_NAME_PostInvocation1", "SERVICE_NAME_PostInvocation2"
		)

		//    val qdEndPointTemplate = "/Legacy-Imported/%s/asyncQueueListener"
		val qdEndPointTemplate = "/EsbII/%s/asyncQueueListener"
		val qdname = "SERVICE_NAME - QDRAIN"
		//    val endpoint = "/LegacyImported"
		val endpoint = "/EsbII"
		val psname = "SERVICE_NAME_main"

		val serviceTypes = List( "ORACLE", "HTTP", "ESBII" )
		val backendStats = HashMap.empty[String, Int]

		// Template for JDBC connection reference
		val jdbcRefStr = """<JdbcConnectionReference RefType="com.l7tech.console.policy.exporter.JdbcConnectionReference">
            <ConnectionName>%s</ConnectionName>
            <DriverClass>%s</DriverClass>
            <JdbcUrl>%s</JdbcUrl>
            <UserName>%s</UserName>
        </JdbcConnectionReference>"""

		val fragments = scala.collection.mutable.HashMap.empty[String, String]
		val client = new SsgUtil( ssgUrl ).getClient
		val folderAccessor = client.getAccessor( classOf[FolderMO] )
		val policyAccessor = client.getAccessor( classOf[PolicyMO] )
		val jdbcAccessor = client.getAccessor( classOf[JDBCConnectionMO] )
		val jmsAccessor = client.getAccessor( classOf[JMSDestinationMO] )
		val standardFolderId = findOrCreate( client, "-5002", standardFolderName )
		val serviceFolderId = findOrCreate( client, "-5002", serviceFolderName )
		printFolders( folderAccessor )

		// Build JDBC connection reference map (name => XML)
		logger.trace( "Build JDBC connection references" )
		val jdbcConnections = HashMap.empty[String, String]

		for ( c <- jdbcAccessor.enumerate ) {
			val connName = c.getName
			val connDriver = c.getDriverClass
			val connUrl = c.getJdbcUrl
			val connUser = c.getConnectionProperties()( "user" ).asInstanceOf[String]
			jdbcConnections.put( c.getName, jdbcRefStr.format( connName, connDriver, connUrl, connUser ) )
			if ( connName.equals( ifaDto.connName ) ) {
				JdbcCreator.createSingle( ifaDto )
			}
		}

		logger.trace( "=====jdbcConnections=====", jdbcConnections )
		logger.debug( jdbcConnections.keySet.size.toString + " JDBC connections loaded" )

		if ( ifaDto.isFwII ) {
			for ( fName <- sfmnames ) {
				val mo = isObjectExist( policyAccessor, fName )
				if ( mo == null ) {
					logger.debug( "Importing... Creating:" + fName )
					val fragmentId = fragmentImport( client, standardFolderId, fName, fName, artifactsDir, fragments, null, jdbcConnections )
					val createdFragment = policyAccessor.get( fragmentId )
					fragments.put( fName, createdFragment.getGuid )
					logger.debug( "Import. Created ===> " + fragmentId )
				}else{
					logger.debug( "Importing... updating:" + fName )
					val fragmentId = updateFragment( mo, client, standardFolderId, fName, fName, artifactsDir, fragments, null, jdbcConnections )
					val createdFragment = policyAccessor.get( fragmentId )
					fragments.put( fName, createdFragment.getGuid )
					logger.debug( "Import. Updated ===> " + fragmentId )
				}
			}
		}
//		else {
			for ( p <- policyAccessor.enumerate ) {
				val d = p.getPolicyDetail
				services.put(d.getName, p)
				if ( d.getFolderId == standardFolderId ) {
					fragments.put( d.getName, p.getGuid )
				}
//			}
		}

		// HashMap to record imported services and their load time
		val imported = HashMap.empty[String, Long]

		// HashMap to record imported services service object
		val serviceObjs = HashMap.empty[String, Service]

		// Mapping of serviceNmae to id for later JMS hardwiring
		val serviceIds = HashMap.empty[String, String]

		val importStart = System.currentTimeMillis
		val serviceName = ifaDto.serviceName
		val serviceType = ifaDto.serviceType
		logger.trace( "Found " + serviceName + "; type: " + serviceType )

		// Create a base folder for the policies and service
		val baseFolder = ManagedObjectFactory.createFolder
		baseFolder.setName( serviceName )
		baseFolder.setFolderId( serviceFolderId )
		logger.trace( "Creating service {} ", serviceName )

		var baseFolderId = "";
		try {
			baseFolderId = folderAccessor.create( baseFolder )
			if ( ifaDto.action.equals( "update" ) ) {
				logger.error( "The {} service does not exist! for update action", ifaDto.serviceName )
				sys.error( errorTag + ifaDto.serviceName + " service does not exist" )
				System.exit( -199 )
			}
		}
		catch {
			case _ => {
				logger.trace( "Folder for service: {} exist, updating...", serviceName )
				logger.trace( "Deleting...service: {}", serviceName )
				delFolderRec( client, ifaDto.serviceName )
				baseFolderId = folderAccessor.create( baseFolder )
			}
		}

			serviceType match {
			case "ORACLE" => {
				println( "An oracle service" )
				val parsed = ifaDto.parsed
				val serviceConnection = ifaDto.connName
				val targetEnc = ifaDto.targetEncoding
				val sourceEnc = ifaDto.sourceEncoding
				val template = ifaDto.funcName //ora function name 
				val args = ifaDto.args
				val serviceObj = new Service( serviceName, sourceEnc, targetEnc, serviceConnection, template, parsed, args, serviceType, "null", "null", "null", workarounds, "null", false )
				serviceObjs.put( serviceName, serviceObj )
				println( "Oracle Connection: " + serviceConnection )
				println( "Orecle Template: " + template )
				println( "Parsed: " + parsed )
				println( "serviceObj: " + serviceObj )
			}

			case "SQLSERVER" => { logger.trace( "sqlservice type not supported" ) }

			case "HTTP" => {
				logger.trace( "An HTTP service" )
				val method = ifaDto.method
				val parsed = ifaDto.parsed
				val urlEncoding = ifaDto.urlEncoding
				val serviceConnection = "http://%s:%s/%s".format( ifaDto.urlProviderEndpointHost, ifaDto.urlProviderEndpointPort, ifaDto.urlProviderEndpointURI )
				val targetEnc = ifaDto.targetEncoding
				val sourceEnc = ifaDto.sourceEncoding
				val xmlRender = ifaDto.xmlRender
				val args = ifaDto.args
				val queryString = ifaDto.queryString
				val serviceObj = new Service( serviceName, sourceEnc, targetEnc, serviceConnection, "null", parsed, args, serviceType, method, queryString, urlEncoding, workarounds, xmlRender, false )
				serviceObjs.put( serviceName, serviceObj )
				logger.debug( "Endpoint: " + endpoint )
				logger.debug( "serviceObj: " + serviceObj )
			}

			case "ESBII" => {
				logger.trace( "An HTTP service" )
				val method = ifaDto.method
				val parsed = ifaDto.parsed
				val urlEncoding = ifaDto.urlEncoding
				val serviceConnection = "http://%s:%s/%s".format( ifaDto.urlProviderEndpointHost, ifaDto.urlProviderEndpointPort, ifaDto.urlProviderEndpointURI )
				val targetEnc = ifaDto.targetEncoding
				val sourceEnc = ifaDto.sourceEncoding
				val xmlRender = ifaDto.xmlRender
				val args = ifaDto.args
				val queryString = ifaDto.queryString
				val serviceObj = new Service( serviceName, sourceEnc, targetEnc, serviceConnection, "null", parsed, args, serviceType, method, queryString, urlEncoding, workarounds, xmlRender, false )
				serviceObjs.put( serviceName, serviceObj )
				logger.debug( "Endpoint: " + endpoint )
				logger.debug( "serviceObj: " + serviceObj )
			}

			case _ => { logger.debug( "The serviceType:{} does not supoorted now.", serviceType ) }
		}

		// Standard treatment for all supported backends
		val serviceObj = serviceObjs( serviceName )

		// Import the service specific Invoke fragment
		val fm = if ( stub ) "STUB " + serviceInvoke else serviceType + " " + serviceInvoke
		val fname = serviceInvoke.replace( "SERVICE_NAME", serviceName )
//		generate( client, baseFolderId, fm, fname, artifactsDir, fragments, serviceObj, jdbcConnections )
		val fragmentId = fragmentImport( client, baseFolderId, fm, fname, artifactsDir, fragments, serviceObj, jdbcConnections )
		val createdFragment = policyAccessor.get( fragmentId )
		val guid = createdFragment.getGuid
		fragments.put( fname, guid )

		// Import common service specific fragments
		for ( fm <- pfmnames ) {
			val fname = fm.replace( "SERVICE_NAME", serviceName )
			logger.debug( "Importing " + fname )
			
//			generate( client, baseFolderId, fm, fname, artifactsDir, fragments, serviceObj, jdbcConnections )
			val fragmentId = fragmentImport( client, baseFolderId, fm, fname, artifactsDir, fragments, serviceObj, jdbcConnections )
			val createdFragment = policyAccessor.get( fragmentId )
			val guid = createdFragment.getGuid
			fragments.put( fname, guid )
			logger.debug( "Imported " + fname + " ===> " + guid )
			
		}

		// Import the Qdrain rest service
		val qdServiceName = qdname.replace( "SERVICE_NAME", serviceName )
		val qdEndPoint = qdEndPointTemplate.format( serviceName )
		val qdId = restImport( client, baseFolderId, qdname, qdServiceName, artifactsDir, fragments, qdEndPoint, null, serviceObj, jdbcConnections )
		serviceIds.put( serviceName, qdId )

		// Import the base SOAP service
		val pid = soapImport( client, baseFolderId, psname, serviceName, basicWsdl, artifactsDir, fragments, endpoint, serviceObj, jdbcConnections )
		java.lang.Thread.sleep( 500 )

		imported.put( serviceName, System.currentTimeMillis - importStart )

		// Make sure the JMS listeners are there and configured
		val jmsDest = ManagedObjectFactory.createJMSDestination
		val jmsConn = ManagedObjectFactory.createJMSConnection
		val jmsDetail = ManagedObjectFactory.createJMSDestinationDetails

		val jmsDestinations = HashMap.empty[String, JMSDestinationMO]
		for ( d <- jmsAccessor.enumerate ) {
			jmsDestinations.put( d.getJmsDestinationDetail.getName, d )
		}

		for ( sn <- imported.keySet ) {
			val destName = jmsInboundPrefix + sn
			val aDest = if ( jmsDestinations.contains( destName ) ) jmsDestinations( destName ) else jmsDest
			val connProps = HashMap.empty[String, Any]
			val destProps = HashMap.empty[String, Any]
			val cntxProps = HashMap.empty[String, Any]

			// Connection properties
			connProps.put( "jndi.initialContextFactoryClassname", "com.tibco.tibjms.naming.TibjmsInitialContextFactory" )
			connProps.put( "jndi.providerUrl", jmsProviderUrl )
			connProps.put( "queue.connectionFactoryName", emsQueueConnectionFactory )
			connProps.put( "l7.jms.jmsSessionCacheSize", 25 )

			// Context Properties ===> This is where the connection is "wired" to a service
			cntxProps.put( "com.l7tech.server.jms.prop.hardwired.service.id", serviceIds( sn ) )
			cntxProps.put( "com.l7tech.server.jms.prop.hardwired.service.bool", "true" )
			cntxProps.put( "com.l7tech.server.jms.prop.contentType.source", "" )
			cntxProps.put( "com.l7tech.server.jms.prop.contentType.value", "" )
			cntxProps.put( "com.tibco.tibjms.naming.ssl_auth_only", "com.l7tech.server.jms.prop.boolean.false" )
			cntxProps.put( "com.tibco.tibjms.ssl.enable_verify_hostname", "com.l7tech.server.jms.prop.boolean.false" )
			cntxProps.put( "com.tibco.tibjms.ssl.enable_verify_host", "com.l7tech.server.jms.prop.boolean.false" )
			cntxProps.put( "com.tibco.tibjms.ssl.auth_only", "com.l7tech.server.jms.prop.boolean.false" )
			cntxProps.put( "com.tibco.tibjms.naming.ssl_enable_verify_host", "com.l7tech.server.jms.prop.boolean.false" )

			// Connection setup
			jmsConn.setProperties( connProps.asInstanceOf[HashMap[String, java.lang.Object]] )
			jmsConn.setContextPropertiesTemplate( cntxProps.asInstanceOf[HashMap[String, java.lang.Object]] )
			jmsConn.setProviderType( JMSConnection.JMSProviderType.TIBCO_EMS )
			jmsConn.setTemplate( false )

			// Destination properties
			destProps.put( "inbound.acknowledgementType", "ON_COMPLETION" )
			destProps.put( "type", "Queue" )

			// Destination detail Setup
			jmsDetail.setName( destName )
			jmsDetail.setDestinationName( "q.service." + ssgEnvName + "." + sn )
			jmsDetail.setEnabled( true )
			jmsDetail.setInbound( true )
			jmsDetail.setTemplate( false )
			jmsDetail.setProperties( destProps.asInstanceOf[HashMap[String, java.lang.Object]] )

			// The very destination
			aDest.setJmsConnection( jmsConn )
			aDest.setJmsDestinationDetail( jmsDetail )

			if ( aDest == jmsDest ) {
				// Create a new one
				logger.debug( "Creating JMS Destination: " + destName )
				val id = jmsAccessor.create( aDest )
			}
			else {
				// Destination exist. Only update with service
				logger.debug( "Updating JMS Destination: " + destName )
				val id = jmsAccessor.put( aDest )
			}
		}

		val qcScriptFile = new File( "./createq.txt" )
		val queuePrintWriter = new PrintWriter( qcScriptFile )
		for ( sn <- serviceObjs.values ) {
			queuePrintWriter.println( "connect " + ifaDto.jmsProviderUrl )
			queuePrintWriter.println( "delete queue q.service." + ssgEnvName + "." + sn.name )
			queuePrintWriter.println( "commit" )
			queuePrintWriter.println( "Create queue q.service." + ssgEnvName + "." + sn.name )
			queuePrintWriter.println( "commit" )
			logger.debug( "jmsProviider:" + ifaDto.jmsProviderUrl + "delete queue q.service." + ssgEnvName + "." + sn.name )
			logger.debug( "Create queue q.service. {}", ssgEnvName + "." + sn.name )
		}
		queuePrintWriter.close

		//    SsgConfigurationUpdater.setSsgServiceConfig(ifaDto)
		//    updateSsgParam()
		ClusterProperties.createCWPs( ifaDto )
		logger.debug( "Fineshed............. runtime::{} sec", ( System.currentTimeMillis() - startTime ) / 1000.0 )
		Executor.execute( ifaDto.jmsScriptLocation )
		logger.trace( done )
		System.err.println( done )
		done
	}

}