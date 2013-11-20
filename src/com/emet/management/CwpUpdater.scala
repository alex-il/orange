package com.emet.management

import org.slf4j.LoggerFactory
import utils.ifaDto
import utils.{ ifaDto_= => ifaDto_= }
import utils.loadIFAdminDTO
import com.emet.management.common.IInternal._
import org.apache.commons.lang.StringUtils

/**
 *
 * @author Oleg Branopolsky
 *
 */
object CwpUpdater {
  val logger = LoggerFactory.getLogger(getClass());
  def main(args: Array[String]): Unit = {
    if (args.length > 0) {
      logger.debug("file.param:{}", args.apply(0))
      var ifaDto = loadIFAdminDTO(args.apply(0))
      update(ifaDto)
    } else {
      ifaDto = loadIFAdminDTO("d:\\2\\1.p")
      logger.debug("d:/2/1.p")
      update(ifaDto)
    }

  }

  /**
   *
   *
   */
  def update(ifaDto: IFAdminDTOJ) = {

    val serviceName = ifaDto.serviceName
    val serviceType = ifaDto.serviceType
    val urlProviderEndpoint =
      if (!ifaDto.serviceType.equals("HTTP")) { ifaDto.urlProviderEndpointHost }
      else {
        ifaDto.urlProviderEndpointHost + ifaDto.semicolumn + ifaDto.urlProviderEndpointPort + ifaDto.urlProviderEndpointURI
      }
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
    val cancelTO = ifaDto.cancelTO
    val qdrain_be_cl = ifaDto.qdrain_be_cl
    val qdrain_sn_cl = ifaDto.qdrain_sn_cl
    val serviceConcurrencyLimit = ifaDto.serviceConcurrencyLimit
    val serviceSyncConcurrency = ifaDto.serviceSyncConcurrency
    val service_ASyncConcurrency = ifaDto.service_ASyncConcurrency
    val connectionName = if(!ifaDto.serviceType.equals("HTTP")){ urlProviderEndpoint } else {ifaDto.connName} 
    val backEndConcurrencyLimit = ifaDto.backEndConcurrencyLimit
    val ipWhiteList = ifaDto.ipWhiteList

    logger.info("Ok. setSsgServiceConfig")
  }
}
  
	
	

