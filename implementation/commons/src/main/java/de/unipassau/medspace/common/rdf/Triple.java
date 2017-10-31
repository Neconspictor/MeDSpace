package de.unipassau.medspace.common.rdf;

/**
 * Created by David Goeth on 28.10.2017.
 */
public interface Triple {

  String getSubject();

  String getPredicate();

  RDFValue getObject();
}