package com.emet.management;

import scala.collection.JavaConversions.asScalaIterator
import scala.collection.JavaConversions.mutableMapAsJavaMap
import scala.collection.mutable.HashMap

import org.slf4j.LoggerFactory

import com.emet.management.common.IUi.errorTag
import com.emet.management.common.IUi.done
import com.l7tech.gateway.api.ClientFactory
import com.l7tech.gateway.api.JDBCConnectionMO
import com.l7tech.gateway.api.ManagedObjectFactory
import com.emet.management.common.IInternal._
import utils.loadIFAdminDTO

object JdbcCreator {
  val logger = LoggerFactory.getLogger(getClass())

  def createSingleOraConn(dto: IFAdminDTOJ): String = {
    logger.trace("JdbcCreator.createSingle")
    println("dto: " + dto)
    createSingle(dto.ssgHost
        , dto.ssgUser
        , dto.ssgPwd
				, dto.connName
				, dto.urlProviderEndpointHost
				, dto.urlProviderEndpointPort
				, dto.urlProviderEndpointSID
				, dto.urlProviderEndpointUser
				, dto.urlProviderEndpointPwd
//				, dto.isServiceEnable
				, dto)
  }

  def createSingle(dto: IFAdminDTOJ): String = {
    logger.trace("JdbcCreator.createSingle")
    println("dto: " + dto)
    createSingle(
          dto.ssgHost
				, dto.ssgUser
				, dto.ssgPwd
				, dto.connName
				, dto.oraHost
				, dto.oraPort
				, dto.oraSID
				, dto.oraUser
				, dto.oraPwd
//				, dto.isServiceEnable
				, dto
				)
  }

  //("10.4.11.229", "autoImport", "7layer7layer", "1_1_test", "billdbtst1", 
  //  "1521", "bscTTTstst1", "if_user", "if_runner", true)
  def createSingle(
  	ssgHostName: String
	, l7User: String
	, l7Pwd: String
	, connName: String
	, oraDbHost: String
	, port: String
	, oraSID: String
	, oraDbUser: String
	, oraDbPwd: String
//	, isEnable: Boolean
	, dto: IFAdminDTOJ    
  ): String = {
    val gwurl = "https://" + ssgHostName + ":8443/wsman"
    val jdbcClass = "com.l7tech.jdbc.oracle.OracleDriver"
    
    val minOraDbConnectionPool = dto.minOraDbConnectionPool
    val maxOraDbConnectionPool = dto.maxOraDbConnectionPool
    if(minOraDbConnectionPool>=maxOraDbConnectionPool){
      logger.error("minOraDbConnectionPool>=maxOraDbConnectionPool is true, for connectionName:"+connName)
    }
    val factory = ClientFactory.newInstance()
    factory.setAttribute(ClientFactory.ATTRIBUTE_USERNAME, l7User)
    factory.setAttribute(ClientFactory.ATTRIBUTE_PASSWORD, l7Pwd)
    factory.setFeature(ClientFactory.FEATURE_CERTIFICATE_VALIDATION, false)
    factory.setFeature(ClientFactory.FEATURE_HOSTNAME_VALIDATION, false)

    val client = factory.createClient(gwurl)
    val jdbcAccessor = client.getAccessor(classOf[JDBCConnectionMO])
    val jdbcConn = ManagedObjectFactory.createJDBCConnection

    val currentConnection = HashMap.empty[String, JDBCConnectionMO]

    // Get current names from the gateway
    for (jc <- jdbcAccessor.enumerate) {
      currentConnection.put(jc.getName.toLowerCase(), jc)
    }
    logger.debug("Current: " + currentConnection.keySet)

    val connProps = HashMap.empty[String, Any]
    val props = HashMap.empty[String, Any]
//		val oraDbUrl = "jdbc:l7tech:oracle://" + oraDbHost + ":" + port + ";SID=" + oraSID
		val oraDbUrl = "jdbc:l7tech:oracle://" + dto.urlProviderEndpointHost + ":" + dto.urlProviderEndpointPort + ";SID=" + dto.urlProviderEndpointSID
    connProps.put("user", dto.urlProviderEndpointUser);
    connProps.put("password", dto.urlProviderEndpointPwd)
    props.put("minimumPoolSize", minOraDbConnectionPool)
    props.put("maximumPoolSize", maxOraDbConnectionPool)

    // updateProps - a local function to encapsulate property update
    def updateProps(connProps: HashMap[String, Any]) {
      // Some other connection properties DataDirect
      connProps.put("enableCancelTimeout", "true")
      // c3p0
      connProps.put("c3p0.idleConnectionTestPeriod", "300")
      connProps.put("c3p0.maxIdleTime", "0")
      connProps.put("c3p0.maxIdleTimeExcessConnections", "300")
      connProps.put("c3p0.preferredTestQuery", "SELECT 1 FROM dual")
    }

    updateProps(connProps)
    val oraDbConnactionName = connName.toLowerCase()
    val isConnectionExist = currentConnection.contains(oraDbConnactionName)
    val aConn = if (isConnectionExist) currentConnection(oraDbConnactionName) else jdbcConn
    aConn.setDriverClass(jdbcClass)
    aConn.setJdbcUrl(oraDbUrl)
    aConn.setName(oraDbConnactionName)
    aConn.setConnectionProperties(connProps.asInstanceOf[HashMap[String, java.lang.Object]])
    aConn.setProperties(props.asInstanceOf[HashMap[String, java.lang.Object]])

    aConn.setEnabled(dto.isJdbcConnEnable)
    val status = if (isConnectionExist) statusUpdated else statusCreated

    // 	if (aConn == jdbcConn) {
    if (isConnectionExist) {
      logger.debug("Updating: " + oraDbConnactionName)
      val id = jdbcAccessor.put(aConn)
    } else {
      logger.debug("Creating: " + oraDbConnactionName)
      val id = jdbcAccessor.create(aConn)
    }

    logger.debug("End. JdbcCreator.createSingle() " + status)
    logger.trace("End. JdbcCreator.createSingle() " + status)
    System.err.println(done + "JdbcCreator.createSingle() " + status )
    done + "JdbcCreator.createSingle() " + status
  }

  //jdbc:l7tech:oracle://znbscstst3:1521;SID=bscstst3
  //jdbc:l7tech:oracle://billdbtst1:1521;SID=bscstst1        
  def main(args: Array[String]) {

    if (args.length > 0) {
      var ifaDto = loadIFAdminDTO(args.apply(0))
      createSingle(ifaDto)
    } else {
      System.err.println(errorTag + "Params file does not exist");
      return -99
    }
//        createSingle("10.4.11.229", "autoImport", "7layer7layer", "1_1_test", "billdbtst1", "1521", "bscTTTstst1", "if_user", "if_runner", true)
//        createSingle("10.4.11.230", "autoImport", "7layer7layer", "11_BSCS TST1", "it3tst3", "1521", "iftst3", "IFRT_TST3A", "IFRT_TST3A", true)
//        createSingle("10.4.11.227", "autoImport", "7layer7layer", "11_BSCS TST1", "it3tst3", "1521", "iftst3", "IFRT_TST3A", "IFRT_TST3A", true)
  }

}