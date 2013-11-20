package com.emet.management

import java.sql.DriverManager
import org.slf4j.LoggerFactory
import utils.ifaDto
import utils.{ ifaDto_= => ifaDto_= }
import utils.loadIFAdminDTO
import org.apache.commons.lang.StringUtils

/**
 *
 * @author Oleg Branopolsky
 *
 */
object SsgConfigurationUpdater {
  val logger = LoggerFactory.getLogger(getClass());
  def main(args: Array[String]): Unit = {
    if (args.length > 0) {
      var ifaDto = loadIFAdminDTO(args.apply(0))
      setSsgServiceConfig(ifaDto)
    } else {
      //      TODO Oleg for debug
      //      createPolicy("d:\\2\\INSERT_SIM_CS_RESULT_150726.param")
      ifaDto = loadIFAdminDTO("./1.param")
      setSsgServiceConfig(ifaDto)
      //      -9 //params file does not exist
    }

  }

  def setSsgServiceConfig(ifaDto: IFAdminDTOJ): String = {
    val driver = "oracle.jdbc.OracleDriver"
    val username = ifaDto.ToraUser
    val password = ifaDto.ToraPwd
    val apostroph = '''
    val urlTml = "jdbc:oracle:thin:@//%s:%s/%s"
    //    val url = "jdbc:oracle:thin:@//it3tst3:1521/iftst3"
    val url = urlTml.format(ifaDto.ToraHost, ifaDto.ToraPort, ifaDto.ToraSID)
    logger.debug("Connecting to DB: {}", url)
    Class.forName(driver)
    val connection = DriverManager.getConnection(url, username, password)
    val statement = connection.createStatement

    val serviceName = ifaDto.serviceName
    val serviceType = ifaDto.serviceType
    //    val httpProviderUrl = "http://vmssint3:7001/selfadmin/GC_SELFSERVICE_CHECK_TOKEN_CRM"
    val urlProviderEndpoint = if( !ifaDto.serviceType.equals("HTTP")) 
    					{ifaDto.urlProviderEndpointHost} 
    					else{
    					  ifaDto.urlProviderEndpointHost+ifaDto.semicolumn+ifaDto.urlProviderEndpointPort+ifaDto.urlProviderEndpointURI
    					} //CONNECTION_ROUTE_NAME
    val argsOra = StringUtils.remove(ifaDto.args, apostroph)
    val sourceEnc = StringUtils.remove(ifaDto.sourceEncoding, apostroph)
    val targetEnc = StringUtils.remove(ifaDto.targetEncoding, apostroph)
    val method = StringUtils.remove(ifaDto.method, apostroph)
    val parsed = StringUtils.remove(ifaDto.parsed, apostroph)
    val queryString = ifaDto.queryString
    val urlEncoding = StringUtils.remove(ifaDto.urlEncoding, apostroph)
    //    ------------------------
    val isAsync = StringUtils.remove(ifaDto.isAsync, apostroph)
    val eta = StringUtils.remove(ifaDto.serviceEta, apostroph)
    val xmlRender = StringUtils.remove(ifaDto.xmlRender, apostroph)

    val isServiceEnable = if (ifaDto.isServiceEnable) 'Y' else 'N'
    val invocationPerMinute = StringUtils.remove(ifaDto.invocationPerMinute, apostroph)
    val allowedRerqOrigin = StringUtils.remove(ifaDto.allowedRerqOrigin, apostroph)
    val blockedRerqOrigin = StringUtils.remove(ifaDto.blockedRerqOrigin, apostroph)
    val customerTO = StringUtils.remove(ifaDto.customerTO, apostroph)
    val providerTO = ifaDto.providerTO
    val cancelTO   = ifaDto.cancelTO
    val qdrain_be_cl = ifaDto.qdrain_be_cl
    val qdrain_sn_cl = ifaDto.qdrain_sn_cl
    val serviceConcurrencyLimit = ifaDto.serviceConcurrencyLimit
    val serviceSyncConcurrency = ifaDto.serviceSyncConcurrency
    val service_ASyncConcurrency = ifaDto.service_ASyncConcurrency
    val connectionName = if(!ifaDto.serviceType.equals("HTTP")) urlProviderEndpoint else ifaDto.connName
    val  backEndConcurrencyLimit =  ifaDto.backEndConcurrencyLimit
    //------------------
    val ipWhiteList = ifaDto.ipWhiteList

    //    val queryValuesFormat = "'%s', '%s', '%s', %s, %s, '%s', '%s', '%s', '%s', '%s','%s','%s','%s'"
    //    val queryValues = queryValuesFormat.format(serviceName, urlProviderEndpoint, "N", 30, 10, parsed, paramList, sourceEnc, targetEnc, method, queryString, urlEncoding, xmlRender)
    
//    val queryValues = "'%s', '%s', '%s', %s, '%s', '%s', '%s', '%s', '%s','%s','%s','%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s','%s','%s'".
//      format(serviceName, connectionName, isAsync, eta, parsed, argsOra, sourceEnc, targetEnc, method, queryString, urlEncoding, xmlRender, isServiceEnable, invocationPerMinute, allowedRerqOrigin, blockedRerqOrigin, qdrain_sn_cl, qdrain_be_cl,  servicConcurrencyLimit, serviceSyncConcurrency, service_ASyncConcurrency)
    val queryValues = "'%s', '%s', '%s', %s, '%s', '%s', '%s', '%s', '%s','%s','%s','%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s','%s', '%s', '%s','%s','%s',%s".
      format(serviceName, connectionName, isAsync, eta, parsed, argsOra, sourceEnc, targetEnc, method, queryString, urlEncoding, xmlRender
          , isServiceEnable, invocationPerMinute, allowedRerqOrigin, blockedRerqOrigin, customerTO, providerTO,cancelTO, qdrain_sn_cl, qdrain_be_cl,
          serviceConcurrencyLimit, serviceSyncConcurrency, service_ASyncConcurrency, backEndConcurrencyLimit)

    val insertQuery = "INSERT INTO L7_SERVICE_CONFIG" +
    	" (SERVICE_NAME, CONNECTION_ROUTE_NAME, IS_ASYNC, ETA, IS_PARSED, PARAMETER_LIST, " +
    	"SOURCE_ENCODING, TARGET_ENCODING, METHOD, QUERY_STRING, URL_ENCODING" +
      "	, SHOULD_RENDER, ENABLED, INVOCATIONS_PER_MINUTE, ALLOWED_REQ_ORIGIN, BLOCK_REQ_ORIGIN" +
      ", TIMEOUT_CUSTOMER, TIMEOUT_PROVIDER, CANCEL_TIMEOUT" +
      " , QDRAIN_SERVICE_NAME_C_L, QDRAIN_BACK_END_C_L, SERVICE_NAME_CONCURRENCY_LIMIT, SERVICE_NAME_SYNC_CONC, SERVICE_NAME_ASYNC_CONC, BACK_END_CONCURRENCY_LIMIT" +
      "	) VALUES (" + queryValues + ")"

    logger.debug("Query " + insertQuery)

    try { ///ret = 0 - indicates failure ret>= 1 OK
      val ret = statement.executeUpdate(insertQuery)
      logger.debug("Insert result:" + ret)
    } catch {
      case e @ _ => {
      	logger.debug("Deleting Service Name: " + serviceName)
        val deleteQuery = "DELETE FROM L7_SERVICE_CONFIG WHERE SERVICE_NAME='%s'".format(serviceName)
            logger.debug(" .. deleteQuery: " + deleteQuery)
            logger.debug("After Deleting Service Name: " + serviceName)
        try {
          val ret = statement.executeUpdate(deleteQuery)
          logger.debug("Delete result:" + ret)
          //insert          
          try {
            val retInsert = statement.executeUpdate(insertQuery)
            logger.debug("after 2nd insert:" + retInsert)
          } catch {
            case e @ _ => {
              logger.error("cant insert Service Name: " + serviceName +" "+e.getMessage())
              "@error: cant insert Service Name: " + serviceName
            }
          }

        } catch {
          case e @ _ => {
              logger.error("cant insert Service Name: " + serviceName +" "+e.getMessage())
            "@@error: cant delete Service Name: " + serviceName
          }
        }

      }
    }
    logger.info("Ok. setSsgServiceConfig")
    "done"
  }
}
  
	
	

