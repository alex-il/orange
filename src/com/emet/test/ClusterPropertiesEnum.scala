import com.l7tech.gateway.api._
import scala.collection.mutable.HashMap
import collection.JavaConversions._

object ClusterPropertiesEnum {

  def main(args: Array[String]): Unit = {
    val listener = "192.168.57.101"
    val gwlistener = listener + ":8443"
    val gwurl = "https://" + gwlistener + "/wsman"
    val user = "autoImport"
    val password = "7layer7layer"

    val factory = ClientFactory.newInstance()

    factory.setAttribute(ClientFactory.ATTRIBUTE_USERNAME, user)
    factory.setAttribute(ClientFactory.ATTRIBUTE_PASSWORD, password)
    factory.setFeature(ClientFactory.FEATURE_CERTIFICATE_VALIDATION, false)
    factory.setFeature(ClientFactory.FEATURE_HOSTNAME_VALIDATION, false)

    // Create accessor objects
    val client = factory.createClient(gwurl)
    val CPAccessor = client.getAccessor(classOf[ClusterPropertyMO])

    // Get a list of all 
    val CPs = HashMap.empty[String, ClusterPropertyMO]
    for (cp <- CPAccessor.enumerate) {
      CPs.put(cp.getName, cp)
    }

    // List of CPs to update
    val toUpdate = HashMap.empty[String, String]
    toUpdate.put("yoel1", "val1")
    toUpdate.put("yoel2", "val2")
    toUpdate.put("yoel3", "val3")
    toUpdate.put("yoel4", "val4")
    toUpdate.put("yoel5", "val5")

    val CP = ManagedObjectFactory.createClusterProperty

    for (k <- toUpdate.keys) {
      if (CPs.keys.contains(k)) {
        val acc = CPs(k)
        acc.setValue(toUpdate(k))
        CPAccessor.put(acc)
        println(k + " updated")
      } else {

        CP.setName(k)
        CP.setValue(toUpdate(k))
        CPAccessor.create(CP)
        println(k + " created")
      }
    }
  }

}