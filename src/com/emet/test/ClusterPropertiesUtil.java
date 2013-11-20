package com.emet.test;

import java.util.HashMap;
import java.util.Set;

import com.l7tech.gateway.api.Accessor;
import com.l7tech.gateway.api.Accessor.AccessorException;
import com.l7tech.gateway.api.Client;
import com.l7tech.gateway.api.ClientFactory;
import com.l7tech.gateway.api.ClientFactory.InvalidOptionException;
import com.l7tech.gateway.api.ClusterPropertyMO;
import com.l7tech.gateway.api.ManagedObjectFactory;

public class ClusterPropertiesUtil {
	private static ClientFactory clientFactory;
	/**
	 * 
	 * Tester
	 * 
	 * @throws InvalidOptionException
	 * 
	 */
	public static void main(String[] args) throws InvalidOptionException {
		System.out.println("Working .....................");
		long startTime = System.currentTimeMillis();
		String gwurlDev = "https://10.4.11.227:8443/wsman";
		String gwurlTst1 = "https://10.4.11.229:8443/wsman";
		System.out.println(gwurlDev);
		String user = "autoImport";
		String password = "7layer7layer";
		
		createClientFactory(user, password);

		Accessor<ClusterPropertyMO> accessorSrc = getAccessor(gwurlDev, ClusterPropertyMO.class);
		Accessor<ClusterPropertyMO> accessorDest = getAccessor(gwurlTst1, ClusterPropertyMO.class);
		// List of CPs to update
		HashMap<String, String> updatesMap = new HashMap<String, String>();
		updatesMap.put("1_cwp____", "val1_");

		ClusterPropertyMO cwp = ManagedObjectFactory.createClusterProperty();
		Set<String> keySet = updatesMap.keySet();
		System.out.println("Updating.... cwp 888: ");
		for (String updateKey : keySet) {
			try {
				ClusterPropertyMO cwpMO = accessorSrc.get("name", updateKey);
				System.out.println("--------- key/val; ----------------------");
				cwpMO.setValue(updatesMap.get(updateKey));
				System.out.println(cwpMO.getName() + " = " + cwpMO.getValue());
				System.out.println("cwpMO.getProperties():" + cwpMO.getProperties());
				System.out.println("--------- key/val; end----------------------");
				accessorSrc.put(cwpMO);
			} catch (AccessorException e) {
				System.out.println(e.getLocalizedMessage());
				cwp.setName(updateKey);
				System.out.println("Creating ...." + updateKey);
				cwp.setValue(updatesMap.get(updateKey));
				try {
					accessorSrc.create(cwp);
				} catch (AccessorException e1) {
					e1.printStackTrace();
				}
				System.out.println(updateKey + " created");
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			System.out.println(updateKey + " updated");
		}
		System.out.println("end................time::"
				+ (System.currentTimeMillis() - startTime) / 1000. + "sec");
	}

	private static Accessor<ClusterPropertyMO> getAccessor(String gwurl,
			Class<ClusterPropertyMO> obj) {
		Client client = clientFactory.createClient(gwurl);
		Accessor<ClusterPropertyMO> accessor = client.getAccessor(obj);
		return accessor;
	}

	private static ClientFactory createClientFactory(String user, String password)
			throws InvalidOptionException {
		clientFactory = ClientFactory.newInstance();

		clientFactory.setAttribute(ClientFactory.ATTRIBUTE_USERNAME, user);
		clientFactory.setAttribute(ClientFactory.ATTRIBUTE_PASSWORD, password);
		clientFactory.setFeature(ClientFactory.FEATURE_CERTIFICATE_VALIDATION,
				false);
		clientFactory.setFeature(ClientFactory.FEATURE_HOSTNAME_VALIDATION, false);
		return clientFactory;
	}

}
