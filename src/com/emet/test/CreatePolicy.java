package com.emet.test;

import com.l7tech.gateway.api.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CreatePolicy {

	/**
	 * The URL of the Gateway Management Service to connect to.
	 */
	private static final String GATEWAY_URL = "https://192.168.57.101:8443/wsman";

	/**
	 * The credentials for an administrative user with permission to publish a
	 * service.
	 */
	private static final String USERNAME = "admin";
	private static final String PASSWORD = "password";

	/**
	 * The policy to use for the service.
	 * <p/>
	 * This example is the default policy for a Gateway Management service.
	 */
	private static final String policyXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<wsp:Policy xmlns:L7p=\"http://www.layer7tech.com/ws/policy\" xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2002/12/policy\">\n"
			+ "    <wsp:All wsp:Usage=\"Required\">\n" + "        <L7p:SslAssertion/>\n" + "        <L7p:HttpBasic/>\n"
			+ "        <L7p:Authentication>\n" + "            <L7p:IdentityProviderOid longValue=\"-2\"/>\n"
			+ "        </L7p:Authentication>\n" + "        <L7p:GatewayManagement/>\n" + "    </wsp:All>\n" + "</wsp:Policy>";

	/**
	 * Application that publishes a service.
	 * 
	 * @args The applications command line arguments
	 */
	public static void main(final String[] args) {
		try {
			final ClientFactory factory = ClientFactory.newInstance();

			factory.setAttribute(ClientFactory.ATTRIBUTE_USERNAME, USERNAME);
			factory.setAttribute(ClientFactory.ATTRIBUTE_PASSWORD, PASSWORD);

			// For the example we disable certificate and hostname validation,
			// you would not do this in production
			factory.setFeature(ClientFactory.FEATURE_CERTIFICATE_VALIDATION, false);
			factory.setFeature(ClientFactory.FEATURE_HOSTNAME_VALIDATION, false);

			final Client client = factory.createClient(GATEWAY_URL);
			final Accessor<ServiceMO> sAccessor = client.getAccessor(ServiceMO.class);
			final Accessor<PolicyMO> policyAccessor = client.getAccessor(PolicyMO.class);
			PolicyMO policyMO2 = policyAccessor.get("name", "ttt");
 policyMO2 = policyAccessor.get("name", "ttt");
System.out.println("------------------------:"+ policyMO2.getPolicyDetail().getName());
 policyMO2 = policyAccessor.get("name", "CS_REFRESH_SUBSCRIPTION_BILL__Pre_Invocation1");
System.out.println("-----------CS_REFRESH_SUBSCRIPTION_BILL__Pre_Invocation1-------------:"+ policyMO2.getPolicyDetail().getName());


ServiceMO sMO = sAccessor.get("name", "CS_REFRESH_SUBSCRIPTION_BILL");
System.out.println("----------CS_REFRESH_SUBSCRIPTION_BILL--------------:"+ sMO.getServiceDetail().getName());
  sMO = sAccessor.get("name", "ttt_ttt");
System.out.println("----------ttt_ttt--------------:"+ sMO.getServiceDetail().getName());

// Create objects for the policy
			final PolicyMO policyMO = ManagedObjectFactory.createPolicy();
			final PolicyDetail policyDetail = ManagedObjectFactory.createPolicyDetail();
			policyMO.setPolicyDetail(policyDetail);
			policyDetail.setName("Create Policy Test");
			policyDetail.setPolicyType(PolicyDetail.PolicyType.INCLUDE);
			final Map<String, Object> detailProps = new HashMap<String, Object>();
			detailProps.put("soap", "false");

			final ResourceSet policyResourceSet = ManagedObjectFactory.createResourceSet();
			policyResourceSet.setTag("policy");
			final Resource policyResource = ManagedObjectFactory.createResource();
			policyResource.setType("policy");
			policyResource.setContent(policyXml);
			policyResourceSet.setResources(Arrays.asList(policyResource));

			policyMO.setResourceSets(Arrays.asList(policyResourceSet));

			// Create the policy and log the identifier
			final String identifier = policyAccessor.create(policyMO);
			System.out.println("Created policy with identifier: " + identifier);

			// Access the newly created service and log as XML
			final PolicyMO newPolicy = policyAccessor.get(identifier);
			System.out.println("Policy XML:");
			ManagedObjectFactory.write(newPolicy, System.out);
		} catch (ClientFactory.InvalidOptionException e) {
			// Handle error due to an incorrect feature or attribute
			System.err.println("1");
			e.printStackTrace();
		} catch (Accessor.AccessorException e) {
			System.err.println("12");
			// Handle "business logic" errors
			e.printStackTrace();
		} catch (ManagementRuntimeException e) {
			System.err.println("123");
			// Handle any runtime errors, not meaninfully handled by lower level code
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("1234");
			// Handle error due to writing service
			e.printStackTrace();
		}
	}

}
