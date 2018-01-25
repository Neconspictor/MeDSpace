package de.unipassau.medspace.common.config.general_wrapper;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO
 */
public class NamespacesAdapter extends XmlAdapter<Namespaces,
    Map<String, de.unipassau.medspace.common.rdf.Namespace>> {
  @Override
  public Map<String, de.unipassau.medspace.common.rdf.Namespace> unmarshal(Namespaces v) throws Exception {
    Map<String, de.unipassau.medspace.common.rdf.Namespace> result = new HashMap<>();
    for (Namespace namespace : v.getNamespace()) {
      result.put(namespace.getPrefix(), convert(namespace));
    }
    return result;
  }

  @Override
  public Namespaces marshal(Map<String, de.unipassau.medspace.common.rdf.Namespace> v) throws Exception {
    Namespaces result  = new Namespaces();
    List<Namespace> list = result.getNamespace();

    for (de.unipassau.medspace.common.rdf.Namespace namespace : v.values()) {
      list.add(convert(namespace));
    }

    return result;
  }


  /**
   * TODO
   * @param v
   * @return
   * @throws Exception
   */
  private de.unipassau.medspace.common.rdf.Namespace convert(Namespace v) throws Exception {
    return new de.unipassau.medspace.common.rdf.Namespace(v.getPrefix(), v.getNamespace());
  }

  /**
   * TODO
   * @param v
   * @return
   * @throws Exception
   */
  private Namespace convert(de.unipassau.medspace.common.rdf.Namespace v) throws Exception {

    Namespace result = new Namespace();
    result.setPrefix(v.getPrefix());
    result.setNamespace(v.getFullURI());
    return result;
  }
}
