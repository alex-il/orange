package com.emet.management;

/**
 * 
 * @author Oleg B.
 * 
 */
public class IFAdminDTOJ {
	String action;
	String serviceName;
	String envName;
	public String ssgHost;
	public String ssgPort;
	String ssgUser;
	String ssgPwd;
	String artifactsDir;
	String serviceFolderName = "EsbII";
	String standardFolderName = "EsbII_StandardFragments";
	String jmsProviderUrl = "tcp://vmems1test3:7222"; // jms server
	String emsQueueConnectionFactory;
	String jmsTracerProviderUrl;
	String jmsTracerQueueConnectionFactory;
	String serviceType;
	String connName;
	String urlProviderEndpointHost;
	String semicolumn = ":";
	String urlProviderEndpointPort = "80";
	String urlProviderEndpointSID;
	String urlProviderEndpointURI;
	String urlProviderEndpointUser;
	String urlProviderEndpointPwd;
	String parsed;
	String targetEncoding;
	String sourceEncoding;
	String funcName; // Func_name
	String args; // param_list
	String queryString;
	String host;
	String port;
	String method;
	String urlEncoding;
	String xmlRender = "N";
	Boolean isServiceEnable = Boolean.TRUE;
	Boolean isJdbcConnEnable = Boolean.TRUE;
	Boolean isDebugEnabled = Boolean.FALSE;
	String wsdlFileName = "SERVICE_NAME.wsdl";
	String jmsInboundPrefix = "Inbound: ";
	String baseDir = ".";
	String oraHost;
	String oraPort;
	String oraUser;
	String oraPwd;
	String oraSID;
	String jmsScriptLocation = "d:\\ws-372\\Migrate\\tibcoRun.bat";
	String isAsync = "N"; // "Y" OR "N"
	String serviceSyncConcurrency;
	String service_ASyncConcurrency;
	String serviceConfigurerUri = ":8080/configuration/UpdateConfig?serviceName=";
	String serviceEta = "30";
	String serviceEtaDevider = "1";
	Boolean isFwII = Boolean.FALSE;
	String allowedRerqOrigin = "allowedRerqOrigin";
	String invocationPerMinute = "100";
	String blockedRerqOrigin = "blockedRerqOrigin";
	String customerTO = "777";
	String providerTO = "888";
	String cancelTO = "999";
	String qdrain_be_cl;
	String qdrain_sn_cl;
	String serviceConcurrencyLimit = "111";
	String ipWhiteList = "0.0.0.0";
	String ToraHost;
	String ToraPort;
	String ToraUser;
	String ToraPwd;
	String ToraSID;
	Integer minOraDbConnectionPool = 50;
	Integer maxOraDbConnectionPool = 150;
	Integer backEndConcurrencyLimit = 33;
	
	@Override
	public String toString() {
		return "IFAdminDTOJ [action=" + action + ", serviceName=" + serviceName
				+ ", envName=" + envName + ", ssgHost=" + ssgHost + ", ssgPort="
				+ ssgPort + ", ssgUser=" + ssgUser + ", ssgPwd=" + ssgPwd
				+ ", artifactsDir=" + artifactsDir + ", serviceFolderName="
				+ serviceFolderName + ", standardFolderName=" + standardFolderName
				+ ", jmsProviderUrl=" + jmsProviderUrl + ", emsQueueConnectionFactory="
				+ emsQueueConnectionFactory + ", jmsTracerProviderUrl="
				+ jmsTracerProviderUrl + ", jmsTracerQueueConnectionFactory="
				+ jmsTracerQueueConnectionFactory + ", serviceType=" + serviceType
				+ ", connName=" + getConnName() + ", urlProviderEndpointHost="
				+ urlProviderEndpointHost + ", urlProviderEndpointPort="
				+ urlProviderEndpointPort + ", urlProviderEndpointSID="
				+ urlProviderEndpointSID + ", urlProviderEndpointURI="
				+ urlProviderEndpointURI + ", urlProviderEndpointUser="
				+ urlProviderEndpointUser + ", urlProviderEndpointPwd="
				+ urlProviderEndpointPwd + ", parsed=" + parsed + ", targetEncoding="
				+ targetEncoding + ", sourceEncoding=" + sourceEncoding + ", funcName="
				+ funcName + ", args=" + args + ", queryString=" + queryString
				+ ", host=" + host + ", port=" + port + ", method=" + method
				+ ", urlEncoding=" + urlEncoding + ", xmlRender=" + xmlRender
				+ ", isServiceEnable=" + isServiceEnable + ", isJdbcConnEnable="
				+ isJdbcConnEnable + ", isDebugEnabled=" + isDebugEnabled
				+ ", wsdlFileName=" + wsdlFileName + ", jmsInboundPrefix="
				+ jmsInboundPrefix + ", baseDir=" + baseDir + ", oraHost=" + oraHost
				+ ", oraPort=" + oraPort + ", oraUser=" + oraUser + ", oraPwd="
				+ oraPwd + ", oraSID=" + oraSID + ", jmsScriptLocation="
				+ jmsScriptLocation + ", isAsync=" + isAsync
				+ ", serviceSyncConcurrency=" + serviceSyncConcurrency
				+ ", service_ASyncConcurrency=" + service_ASyncConcurrency
				+ ", serviceConfigurerUri=" + serviceConfigurerUri + ", serviceEta="
				+ serviceEta + ", serviceEtaDevider=" + serviceEtaDevider
				+ ", allowedRerqOrigin=" + allowedRerqOrigin + ", invocationPerMinute="
				+ invocationPerMinute + ", blockedRerqOrigin=" + blockedRerqOrigin
				+ ", customerTO=" + customerTO + ", providerTO=" + providerTO
				+ ", cancelTO=" + cancelTO + ", qdrain_be_cl=" + qdrain_be_cl
				+ ", qdrain_sn_cl=" + qdrain_sn_cl + ", serviceConcurrencyLimit="
				+ serviceConcurrencyLimit + ", ipWhiteList=" + ipWhiteList
				+ ", ToraHost=" + ToraHost + ", ToraPort=" + ToraPort + ", ToraUser="
				+ ToraUser + ", ToraPwd=" + ToraPwd + ", ToraSID=" + ToraSID
				+ ", minOraDbConnectionPool=" + minOraDbConnectionPool
				+ ", maxOraDbConnectionPool=" + maxOraDbConnectionPool
				+ ", backEndConcurrencyLimit=" + backEndConcurrencyLimit + "]";
	}
	
	public String getConnName(){
		return (serviceType.equals("HTTP"))?urlProviderEndpointHost + semicolumn + urlProviderEndpointPort + urlProviderEndpointURI:connName;
	}
	
}