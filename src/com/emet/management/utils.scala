package com.emet.management

import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.net.InetAddress
import scala.collection.JavaConversions.asScalaIterator
import scala.collection.JavaConversions.mutableMapAsJavaMap
import scala.collection.JavaConversions.seqAsJavaList
import scala.collection.mutable.HashMap
import scala.xml.Attribute
import scala.xml.Elem
import scala.xml.Node
import scala.xml.Null
import scala.xml.Text
import scala.xml.XML
import scala.xml.transform.RewriteRule
import scala.xml.transform.RuleTransformer

import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.DefaultHttpClient
import org.slf4j.LoggerFactory
import com.google.gson.Gson
import com.l7tech.gateway.api.Accessor
import com.l7tech.gateway.api.Client
import com.l7tech.gateway.api.FolderMO
import com.l7tech.gateway.api.ManagedObjectFactory
import com.l7tech.gateway.api.PolicyDetail
import com.l7tech.gateway.api.PolicyMO
import com.l7tech.gateway.api.ResourceSet
import com.l7tech.gateway.api.ServiceMO
import org.apache.log4j.LogManager

object utils {
	val logger = LoggerFactory.getLogger( getClass() );
	//TODO fix class name

	case class Service( name: String,
		sourceEnc: String,
		targetEnc: String,
		connection: String,
		sqlTemplate: String,
		parsed: String,
		args: String,
		serviceType: String,
		httpMethod: String,
		queryString: String,
		urlEncoding: String,
		workarounds: List[String],
		xmlRender: String,
		stub: Boolean )

	var ifaDto: IFAdminDTOJ = new IFAdminDTOJ()

	def setIfaDto( s: IFAdminDTOJ ) = {
		ifaDto = s
		logger.trace( "===>>> in:setIfaDto()" );
	}

	val connectionNameWhiteList = List( "Tracer Database" )

	def getServiceNames( fn: String ) = {
		val nodes = XML.loadString( scala.io.Source.fromFile( fn ).mkString )
		val services = nodes \\ "SERVICE_NAME"
		services map ( _.text )
	}

	def printFolders( accessor: Accessor[FolderMO] ) {
		if ( ifaDto.isDebugEnabled ) {
			val iter = accessor.enumerate
			while ( iter.hasNext ) {
				val f = iter.next
				logger.debug( "Id: " + f.getId + ", Name: " + f.getName );
			}
		}
	}

	def getRemoteCacheOID( remoteCacheName: String ): String = {
		val oid = try {
			val targetEnc = "UTF-8"
			//    val ssgUri = "http://vmesbgwdev1.partnergsm.co.il:8080/GetRemoteCacheOID"
			val ssgUri = "http://" + ifaDto.ssgHost + ":8080/GetRemoteCacheOID"
			val client = new DefaultHttpClient
			val httpRequest = new HttpGet( ssgUri )
			val httpReply = client.execute( httpRequest )
			val stream = httpReply.getEntity.getContent
			scala.io.Source.fromInputStream( stream, targetEnc ).mkString
		}
		catch {
			case _ => {
				logger.error( "cant convert Http reply to string" )
				"@@error cant get oid"
			}
		}
		oid
	}

	//  val remoteCacheOID = getOidByGetRemoteCacheOID("remoteCache")

	def delDesc( client: Client, id: String ) {
		val fa = client.getAccessor( classOf[FolderMO] )
		val pa = client.getAccessor( classOf[PolicyMO] )
		val sa = client.getAccessor( classOf[ServiceMO] )

		// Delete services
		val siter = sa.enumerate
		for ( s <- siter ) {
			val sd = s.getServiceDetail
			if ( sd.getFolderId == id ) {
				logger.debug( "Delete service " + sd.getName )
				sa.delete( s.getId )
			}
		}

		// Delete policies
		val piter = pa.enumerate
		for ( p <- piter.toList.sortBy( _.getId ).reverse ) {
			val pd = p.getPolicyDetail
			if ( pd.getFolderId == id ) {
				logger.debug( "Delete policy " + pd.getName )
				pa.delete( p.getId )
			}
		}

		// Recursively delete folders
		val fiter = fa.enumerate
		while ( fiter.hasNext ) {
			val f = fiter.next
			if ( f.getFolderId == id ) {
				logger.debug( "Id to delete: Parent->" + id + " Id->" + f.getId + " name->'" + f.getName + "'" )
				delFolderRec( client, f.getName )
			}
		}
	}

	def delFolderRec( client: Client, name: String ) {
		logger.debug( "delFolderRec(client: Client, name: String):" + name )
		// Isn't there a better way???

		val fa = client.getAccessor( classOf[FolderMO] )
		val iter = fa.enumerate

		for ( f <- iter ) {
			//TODO Oleg Compare ignore case      
			if ( f.getName == name ) {
				logger.debug( "Id to delete: Id->" + f.getId + " name->'" + f.getName + "'" )
				logger.trace( "First attempt" )
				try {
					fa.delete( f.getId )
				}
				catch {
					case _ => {
						logger.error( "Delete failed. Attempting desc" )
						delDesc( client, f.getId )
						logger.error( "Now for the very " + name )
						fa.delete( f.getId )
					}
				}
			}
		}
	}

	//---------------- start  preparePolicyRs  -------------------------
	def preparePolicyRs( client: Client, baseFolder: String, filename: String,
		name: String, dir: String, fragments: HashMap[String, String],
		service: Service, jdbcConnections: HashMap[String, String] ): ResourceSet = {

		val policyResourceSet = ManagedObjectFactory.createResourceSet
		val aFragment = ManagedObjectFactory.createPolicyReferenceInstruction
		val policyResource = ManagedObjectFactory.createResource
		val origXml = scala.io.Source.fromFile( new File( dir, filename + ".xml" ).getPath )( "UTF-8" ).mkString

		val guidMap = HashMap.empty[String, String]

		// Fix included fragment
		object fixRef extends RewriteRule {
			override def transform( n: Node ): Seq[Node] = {
				n.label match {

					case "IncludedPolicyReference" => {
						val fragname = ( n \ "@name" )
						val oldGuid = ( n \ "@guid" )
						val guid = fragments( fragname.toString )
						guidMap.put( oldGuid.toString, guid )
						logger.debug( "In reference for: " + fragname.toString + " new GUID: " + guid )
						n.asInstanceOf[Elem] % Attribute( None, "guid", Text( guid ), Null )
					}

					case _ => n
				}
			}
		}

		object fixPolicyGuid extends RewriteRule {
			override def transform( n: Node ): Seq[Node] = {
				n.label match {

					case "PolicyGuid" => {
						val oldGuid = ( n \ "@stringValue" )
						//logger.debug("In PolicyGuid, about to fetch replacement for " + oldGuid.toString)
						n.asInstanceOf[Elem] % Attribute( None, "stringValue", Text( guidMap( oldGuid.toString ) ), Null )
					}
					case _ => n
				}
			}
		}

		object fixConnectionName extends RewriteRule {
			// Rename connection names
			// NOT needed for CV based connections

			override def transform( n: Node ): Seq[Node] = {
				n.label match {

					case "ConnectionName" => {
						logger.debug( "nodeName", n.text )
						val cn = n.text
						val sv = n \ "@stringValue"
						// First test if the connection name is parametrized by a CV
						if ( cn.contains( "${" ) || sv.contains( "${" ) )
							n
						else if ( cn != "" && !connectionNameWhiteList.contains( cn ) ) {
							logger.debug( "Replacing ConnectionName: " + cn + " => " + service.connection )
							n.asInstanceOf[Elem].copy( child = Text( service.connection ) )
						}
						else if ( sv != "" && !connectionNameWhiteList.contains( sv ) ) {
							logger.debug( "Replacing ConnectionName stringValue: " + sv + " => " + service.connection )
							n.asInstanceOf[Elem] % Attribute( None, "stringValue", Text( service.connection ), Null )
						}
						else n
					}
					case _ => n
				}
			}
		}

		object fixServiceJdbc extends RewriteRule {
			// Change the connection reference to suite the environment
			// Skips CV based connections
			override def transform( n: Node ): Seq[Node] = {
				n.label match {

					case "JdbcConnectionReference" => {
						val cn = n.\\( "ConnectionName" ).text
						if ( cn.contains( "${" ) ) {
							logger.trace( "Skipping JdbcConnectionReference to: " + cn )
							logger.trace( "Hack: fixed connection" )

							// When fixed by layer 7, enable the following line and remove the one after
							// n
							if ( service.serviceType == "ORACLE" || service.stub == true )
								XML.loadString( jdbcConnections( service.connection ) )
							else
								n
						}
						else {
							logger.debug( "Main Service JDBC: attempting to fetch reference for: " + cn )
							XML.loadString( jdbcConnections( cn ) )
						}
					}
					case _ => n
				}
			}
		}

		object fixSchema extends RewriteRule {
			override def transform( n: Node ): Seq[Node] = {
				n.label match {
					case "Schema" => {
						val schema = service.sqlTemplate.split( '.' )( 0 )
						//print ("New schema: " + schema)
						n.asInstanceOf[Elem] % Attribute( None, "stringValue", Text( schema ), Null )
					}
					case _ => n
				}
			}
		}

		object fixServiceJdbcConnection extends RewriteRule {
			override def transform( n: Node ): Seq[Node] = {
				n.label match {
					case "ConnectionName" => {
						logger.trace( " Replacing: " + ( n \ "@stringValue" ) + " - " + service.connection )
						n.asInstanceOf[Elem] % Attribute( None, "stringValue", Text( service.connection ), Null )
					}
					case _ => n
				}
			}
		}

		/**
		  * @author  Oleg
		  * fix RemoteCacheAssertion OID bug
		  *
		  */
		object fixRemoteCacheAssertion extends RewriteRule {
			override def transform( n: Node ): Seq[Node] = {
				val remoteCacheOID = "11567104"
				//        		val remoteCacheOID = getOidByGetRemoteCacheOID("remoteCache")
				logger.debug( "fixRemoteCacheAssertion" );
				n.label match {
					case "RemoteCacheReference" => {

						n.asInstanceOf[Elem].copy( child =
							n.child.map( { nn: Node => if ( nn.label == "OID" ) <OID>{ remoteCacheOID }</OID> else nn } ) )
					}

					case "L7p:RemoteCacheId" => {
						n.asInstanceOf[Elem] % Attribute( None, "longValue", Text( remoteCacheOID ), Null )
					}
					case _ => n
				}
			}
		}

		/**
		  *
		  *
		  */
		object fixServiceJdbcQuery extends RewriteRule {
			// Change the connection reference to suite the environment
			// Skips CV based connections
			val fs = new RuleTransformer( fixSchema )
			val fsjc = new RuleTransformer( fixServiceJdbcConnection )

			override def transform( n: Node ): Seq[Node] = {
				n.label match {

					case "JdbcQuery" => {
						val cn = n \\ "SqlQuery" \ "@stringValue"
						logger.debug( "Current SQL: " + cn )
						if ( cn.toString.startsWith( "FUNC" ) ) {
							logger.debug( "Fix service JDBC" )

							// FUNCDATE workaround    <--- NOT WORKING          
							val n1 = if ( service.workarounds.contains( "FUNCDATE" ) && ( service.parsed == "true" ) ) {
								val argsn = service.args.split( "," ).size
								val argString = for ( i <- 0 until argsn ) {}
								n
							}
							else n

							val n2 = if ( service.stub ) fsjc( n1 ) else n1 // Fix connection if stub
							if ( service.stub ) n2 else fs( n2 ) // Fix schema
						}
						else n
					}
					case _ => n
				}
			}
		}

		val fixRefTrans = new RuleTransformer( fixRef )
		val policyGuidTrans = new RuleTransformer( fixPolicyGuid )
		val serivceJdbcTrans = new RuleTransformer( fixServiceJdbc )
		val connectionNameTrans = new RuleTransformer( fixConnectionName )
		val serviceJdbcTrans = new RuleTransformer( fixServiceJdbcQuery )
		val remoteCacheAssertionOidTrans = new RuleTransformer( fixRemoteCacheAssertion )

		// Fragment specific fixing
		val finalFixedXML = if ( service != null ) {
			val fixedXml1 = if ( filename.contains( "SERVICE_NAME" ) ) origXml.replace( "SERVICE_NAME", service.name ) else origXml
			val fixedXml2 = fixedXml1.replace( "SERVICE_CONNECTION", service.connection )

			val templateFields = service.sqlTemplate.split( '.' )
			val newTemplate = if ( templateFields.size == 3 ) "%s.%s".format( templateFields( 1 ), templateFields( 2 ) ) else ""
			val fixedXml3 = fixedXml2.replace( "SERVICE_TEMPLATE", newTemplate )
			fixedXml3
		}
		else origXml
		val tr1 = fixRefTrans( XML.loadString( finalFixedXML ) )

		// Enable the following transformation if CV based routing is not used 
		// val tr2 = if (filename.startsWith("SERVICE_NAME")) connectionNameTrans(tr1) else tr1
		val tr2 = tr1
		val tr3 = serivceJdbcTrans( tr2 )

		// Fix Schema and hack connection name until a fix
		val tr4 = if ( filename.endsWith( "Invoke" ) ) serviceJdbcTrans( tr3 ) else tr3
		val tr5 = fixRemoteCacheAssertion( tr4 )
		val policyXml = policyGuidTrans( tr5 )
		val ipAddress = InetAddress.getLocalHost().getHostAddress()
		val policyContent = policyXml.toString.replace( "** @Updated by: ", ipAddress )
		policyResourceSet.setTag( "policy" )
		policyResource.setType( "policy" )
		policyResource.setContent( policyXml toString )
		policyResourceSet.setResources( List( policyResource ) )
		policyResourceSet
	}

	def fragmentImport( client: Client,
		baseFolder: String, filename: String,
		name: String, dir: String, fragments: HashMap[String, String],
		service: Service = null,
		jdbcConnections: HashMap[String, String] = null ): String = {
		
		val policyAccessor = client.getAccessor( classOf[PolicyMO] )
		val policy = ManagedObjectFactory.createPolicy
		val policyDetail = ManagedObjectFactory.createPolicyDetail
		val policyResourceSet = preparePolicyRs( client, baseFolder, filename, name, dir, fragments, service, jdbcConnections )
		policyDetail.setName( name )
		policyDetail.setFolderId( baseFolder )
		policyDetail.setPolicyType( PolicyDetail.PolicyType.INCLUDE )
		policy.setPolicyDetail( policyDetail )
		policy.setResourceSets( List( policyResourceSet ) )
		val id = policyAccessor.create( policy )
		logger.debug( "name:" + name + ", id:" + id + ". policy created " )
		id
	}

	/**
	  *
	  *
	  */
	def updateFragment( p: PolicyMO,
		client: Client,
		baseFolder: String,
		filename: String,
		name: String,
		dir: String, fragments: HashMap[String, String],
		service: Service = null,
		jdbcConnections: HashMap[String, String] = null ): String = {
		val id = p.getId()
		val ss = p.getResourceSets()
		var rs = p.getResourceSets().get( 0 ).getResources()
		// TODO Oleg update content
		val policyResourceSet = preparePolicyRs( client, baseFolder, filename, name, dir, fragments, service, jdbcConnections )
		var r = p.getResourceSets().get( 0 ).getResources().get( 0 )
		var c = r.getContent()
		r.setContent( c )
		val policyAccessor = client.getAccessor( classOf[PolicyMO] )
		//    preparePolicyRs
		p.setResourceSets( List( policyResourceSet ) )
		policyAccessor.put( p )
		logger.debug( "name:" + name + ", id:" + id + ". policy updated " )
		logger.debug( "-----" )
		id
	}

	/**
	 * 
	 * 
	 */ 
	def folderByName( client: Client, name: String ) = {
			def nameMatch( f: FolderMO ): Boolean = {
				f.getName == name
			}
		val folderAccessor = client.getAccessor( classOf[FolderMO] )

		folderAccessor.enumerate.find( nameMatch )

	}
	
	
	/**
	 * 
	 * sn - Service name
	 * endpoint - path such as /ImportedLegacy
	 * 
	 */ 
	def soapImport( client: Client, baseFolder: String, filename: String, name: String, rawWsdl: String, dir: String, fragments: HashMap[String, String],
		endpoint: String, serviceObj: Service, jdbcConnections: HashMap[String, String] ): String = {
		val serviceAccessor = client.getAccessor( classOf[ServiceMO] )

		val service = ManagedObjectFactory.createService
		val detail = ManagedObjectFactory.createServiceDetail
		val httpMapping = ManagedObjectFactory.createHttpMapping
		val soapMapping = ManagedObjectFactory.createSoapMapping
		val wsdlResourceSet = ManagedObjectFactory.createResourceSet
		val wsdlResource = ManagedObjectFactory.createResource

		logger.debug( "Importing " + name + " into " + baseFolder )

		// Basic service details
		logger.trace( "Preparing basic details" )

		detail.setEnabled( true )
		detail.setName( name )
		detail.setFolderId( baseFolder )
		httpMapping.setUrlPattern( endpoint + "/" + name )
		httpMapping.setVerbs( List( "POST" ) )
		soapMapping.setLax( false )
		detail.setServiceMappings( List( httpMapping, soapMapping ) )
		val properties = HashMap.empty[String, Any]
		properties.put( "soap", true );
		properties.put( "soapVersion", "1.1" )
		detail.setProperties( properties.asInstanceOf[HashMap[String, java.lang.Object]] )
		
		logger.trace( "Preparing WSDL" )
		val wsdl = rawWsdl.replace( "SERVICE_NAME", name )
		wsdlResourceSet.setTag( "wsdl" )
		wsdlResource.setType( "wsdl" )
		wsdlResource.setContent( wsdl )
		wsdlResourceSet.setResources( List( wsdlResource ) )

		// Policy
		logger.trace( "Preparing policy" )

		val policyResourceSet = preparePolicyRs( client, baseFolder, filename, name, dir, fragments, serviceObj, jdbcConnections )

		// Service
		logger.trace( "Preparing service" )

		service.setServiceDetail( detail )
		service.setResourceSets( List( wsdlResourceSet, policyResourceSet ) )

		logger.trace( "creating service" )

		val serviceId = serviceAccessor.create( service )
		serviceId
	}
	
	/**
	 * Import a restful or similar service
	 * sn - Service name
	 * endpoint - path such as /ImportedLegacy
	 * verbs - HTTP verbs as List("POST" ..) or null
	 */ 
	def restImport( client: Client, baseFolder: String, filename: String, name: String, dir: String, fragments: HashMap[String, String],
		endpoint: String, verbs: List[String], serviceObj: Service, jdbcConnections: HashMap[String, String] ): String = {
		val serviceAccessor = client.getAccessor( classOf[ServiceMO] )

		val service = ManagedObjectFactory.createService
		val detail = ManagedObjectFactory.createServiceDetail
		val httpMapping = ManagedObjectFactory.createHttpMapping

		logger.debug( "Importing REST service " + name + " into " + baseFolder )

		// Basic service details
		logger.trace( "Preparing basic details" )

		detail.setEnabled( true )
		detail.setName( name )
		detail.setFolderId( baseFolder )
		httpMapping.setUrlPattern( endpoint )
		if ( verbs == null )
			httpMapping.setVerbs( null )
		else
			httpMapping.setVerbs( verbs )

		detail.setServiceMappings( List( httpMapping ) )

		// Policy
		logger.trace( "Preparing policy" )

		val policyResourceSet = preparePolicyRs( client, baseFolder, filename, name, dir, fragments, serviceObj, jdbcConnections )

		// Service
		logger.trace( "Preparing service" )

		service.setServiceDetail( detail )
		service.setResourceSets( List( policyResourceSet ) )

		logger.trace( "creating service" )

		val serviceId = serviceAccessor.create( service )
		serviceId
	}

	def findOrCreate( client: Client, parentId: String, name: String ): String = {
		val fsrch = folderByName( client, name )
		val folderAccessor = client.getAccessor( classOf[FolderMO] )

		fsrch match {
			case Some( folder ) => folder.getId
			case _ => {
				// Create a folder for the standard fragments
				val fname = ManagedObjectFactory.createFolder
				fname.setName( name )
				fname.setFolderId( parentId )
				logger.debug( "Creating " + name )
				folderAccessor.create( fname )
			}
		}
	}

 /**
  * 
  * @author Olegbranopolsky
  * 
  */
	def loadIFAdminDTO( ifaFile: String ): IFAdminDTOJ = {
		try {
			val br = new BufferedReader( new FileReader( ifaFile ) )
			val ret = new Gson().fromJson( br, classOf[IFAdminDTOJ] )
			ret
		}
		catch {
			case e @ _ => {
				logger.error( "Cant load params file: " + ifaFile )
				logger.error( e.getLocalizedMessage() )
				null
			}
		}
	}

	def createUrlString( uri: String ): String = {
		val tml = "http://%s:%s/%s"
		//	  "http://vmesbgwdev1:8080/configuration/UpdateConfigYaara?serviceName=GC_GET_INFO_ODS"
		//    val str = 
		tml.format( ifaDto.ssgHost, "8080", uri )

	}

	/**
	 * 
	 * 
	 */ 
	def updateSsgParam(): Int = {
		logger.error( "Updating params of service:{}", ifaDto.serviceName )
		val oid = try {
			val targetEnc = "UTF-8"
			val ssgUrl = "http://" + ifaDto.ssgHost + ifaDto.serviceConfigurerUri + ifaDto.serviceName
			val client = new DefaultHttpClient
			val httpRequest = new HttpGet( ssgUrl )
			val httpReply = client.execute( httpRequest )
			val stream = httpReply.getEntity.getContent
			scala.io.Source.fromInputStream( stream, targetEnc ).mkString
		}
		catch {
			case _ => {
				logger.error( "cant update params === of service:{}. error -55", ifaDto.serviceName )
				-55 //@return error cant update service params 
			}
		}
		100
	}

}
