package de.unipassau.medspace.common.config.general_wrapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An Xml Adapter for XML namespaces property.
 */
public class NamespacesAdapter
{

  public Map<String, de.unipassau.medspace.common.rdf.Namespace> unmarshal(Namespaces v)  {
    Map<String, de.unipassau.medspace.common.rdf.Namespace> result = new HashMap<>();
    for (Namespace namespace : v.getNamespace()) {
      result.put(namespace.getPrefix(), convert(namespace));
    }
    return result;
  }


  public Namespaces marshal(Map<String, de.unipassau.medspace.common.rdf.Namespace> v) {
    Namespaces result  = new Namespaces();
    List<Namespace> list = result.getNamespace();

    for (de.unipassau.medspace.common.rdf.Namespace namespace : v.values()) {
      list.add(convert(namespace));
    }

    return result;
  }


  /**
   * Converts an XML Namespace property to an RDF Namespace.
   * @param v The XML Namespace property to convert.
   * @return The converted RDF namespace.
   * @throws Exception If an error occurs.
   */
  private de.unipassau.medspace.common.rdf.Namespace convert(Namespace v)  {
    return new de.unipassau.medspace.common.rdf.Namespace(v.getPrefix(), v.getNamespace());
  }

  /**
   * Converts an RDF namespace to an XML namespace property.
   * @param v The RDF namespace to convert.
   * @return The converted XML namespace property.
   * @throws Exception If an error occurs.
   */
  private Namespace convert(de.unipassau.medspace.common.rdf.Namespace v)  {

    Namespace result = new Namespace();
    result.setPrefix(v.getPrefix());
    result.setNamespace(v.getFullURI());
    return result;
  }
}