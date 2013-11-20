package com.emet.management;


/**
 * 
 * @author Oleg B.
 * 
 */
public class CwpDto {
	public String allowedRerqOrigin			 			;
	public String backEndConcurrencyLimit 		;		
	public String blockedRerqOrigin			 			;
	public String cancelTO					       			;
	public String connName					       			;
	public String customerTO										;
	public String isServiceEnable			    		;	
	public String serviceEta					      		;
	public String serviceEtaDevider		    		;	
	public String invocationPerMinute		  		;	
	public String ipWhiteList					    		;
	public String isAsync						      		;
	public String parsed						        		;
	public String method					          		;
	public String args   						 					;
	public String qdrain_be_cl									;
	public String qdrain_sn_cl									;
	public String queryString        					;					
	public String serviceName									;	
	public String service_ASyncConcurrency 		;	
	public String serviceConcurrencyLimit			;
	public String serviceSyncConcurrency				;
	public String xmlRender										;
	public String sourceEncoding								;
	public String targetEncoding								;
	public String urlEncoding 									;
	public String providerTO									;
	
	public void init(IFAdminDTOJ d){
		allowedRerqOrigin			 			= d.allowedRerqOrigin			 		  ;
		args						 			  		= d.args						 				  ;
		backEndConcurrencyLimit 		= Integer.toString( d.backEndConcurrencyLimit );
		blockedRerqOrigin			 			= d.blockedRerqOrigin			 		  ;
		cancelTO					      		= d.cancelTO					       		  ;
    connName 										= d.getConnName()									;
		customerTO									= d.customerTO									  ;
		isServiceEnable			    		= (d.isServiceEnable)?"Y":"N" 	  ;
		serviceEta					     		= d.serviceEta					      	  ;
		serviceEtaDevider		    		= d.serviceEtaDevider		    	  ;
		invocationPerMinute		  		= d.invocationPerMinute		  	  ;
		ipWhiteList					    		= d.ipWhiteList					    	  ;
		isAsync						      		= d.isAsync						      	  ;
		parsed						        	= d.parsed						        	  ;
		method					          	= d.method					          	  ;
		qdrain_be_cl								= d.qdrain_be_cl								  ;
		qdrain_sn_cl								= d.qdrain_sn_cl								  ;
		queryString        					= d.queryString        				  ;
		serviceName									= d.serviceName								  ;
		service_ASyncConcurrency 		= d.service_ASyncConcurrency 	  ;
		serviceConcurrencyLimit			= d.serviceConcurrencyLimit		  ;
		serviceSyncConcurrency			= d.serviceSyncConcurrency		  ;
		xmlRender										= d.xmlRender									  ;
		sourceEncoding							= d.sourceEncoding						  ;
		targetEncoding							= d.targetEncoding						  ;
		urlEncoding 								= d.urlEncoding 							  ;
		providerTO 				   				= d.providerTO 								  ;
	}

}