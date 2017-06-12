package de.fuberlin.wiwiss.d2r;

import java.util.*;
import java.io.*;

import de.unipassau.medspace.util.sql.SelectStatement;
import org.apache.jena.rdf.model.Model;
import org.apache.log4j.Logger;

import java.util.Map.Entry;

import de.fuberlin.wiwiss.d2r.factory.ModelFactory;
import de.fuberlin.wiwiss.d2r.exception.D2RException;
import de.fuberlin.wiwiss.d2r.exception.FactoryException;

/**
 * D2R processor exports data from a RDBMS into an RDF model using a D2R MAP.
 * D2R MAP is a declarative, XML-based language to describe mappings between the relational
 * database model and the graph-based RDF data model. The resulting model can be serialized as RDF, N3, N-TRIPLES or exported
 * directly as Jena model. The processors is compliant with all relational databases offering JDBC or ODBC access.
 * The processor can be used in a servlet environment to dynamically publish XHTML pages
 * containing RDF, as a database connector in applications working with Jena models or as a command line tool.
 * The D2R Map language specification and usage examples are found at
 * http://www.wiwiss.fu-berlin.de/suhl/bizer/d2rmap/D2Rmap.htm.
 *
 * <BR><BR>History:
 * <BR>18-05-2017   : Updated for Java 8; removed unsafe operations; all-embracing refactoring
 * <BR>07-21-2004   : Process map methods added.
 * <BR>07-21-2004   : Connection and driver accessors added. 
 * <BR>07-21-2004   : Error handling changed to Log4J.
 * <BR>09-25-2003   : Changed for Jena2.
 * <BR>01-15-2003   : Initial version of this class.
 *
 * @author Chris Bizer chris@bizer.de / David Goeth goeth@fim.uni-passau.de
 * @version V0.3.1
 */
public class D2rProcessor {
  private Vector<D2RMap> maps;
  private HashMap<String, String> namespaces;
  private Model model;

  /** log4j logger used for this class */
  private static Logger log = Logger.getLogger(D2rProcessor.class);

  private DataSourceManager dataSourceManager;


  public D2rProcessor(Configuration config, DataSourceManager dataSourceManager) throws D2RException {
    assert config != null;
    assert dataSourceManager != null;

    maps = config.getMaps();
    namespaces = config.getNamespaces();

    // we don't want others to change the state of the processor
    config.setMaps(null);
    config.setNamespaces(null);

    this.dataSourceManager = dataSourceManager;

    for (D2RMap map : maps) {
      map.init(dataSourceManager.getDataSource());
    }
  }


  public Model doKeywordSearch(String keyword) throws D2RException {
    try {
      clear();
    }
    catch (FactoryException e) {
      throw new D2RException("Could not get default Model from the ModelFactory.", e);
    }

    final String condition = "LIKE '%" + keyword + "%'";

    // Generate instances for all maps
    for (D2RMap map : maps) {

      SelectStatement query = map.getQuery();
      query.addTemporaryColumnCondition(condition);
      map.generateResources(this, dataSourceManager.getDataSource(), new Vector<>());
    }
    for (D2RMap map : maps)
      map.generateResourceProperties(this);

    // add namespaces
    for (Entry<String, String> ent : namespaces.entrySet()) {
      this.model.setNsPrefix(ent.getKey(), ent.getValue());
    }

    //Return model
    return this.model;
  }

  /**
   * Processes the D2R map and returns a Jena model containing all generated instances.
   * @return Jena model containing all generated instances.
   * @throws D2RException Thrown if an error occurs while generating the RDF instances or if no D2RMap was read before
   */
  public Model generateTestAsModel() throws D2RException {
    try {
      clear();
    }
    catch (FactoryException e) {
      throw new D2RException("Could not get default Model from the ModelFactory.", e);
    }

    // Generate instances for all maps
    this.generateTestMaps();

    // add namespaces
    for (Entry<String, String> ent : namespaces.entrySet()) {
      this.model.setNsPrefix(ent.getKey(), ent.getValue());
    }

    //Return model
    return this.model;
  }

  private void clear() throws FactoryException {
    // Clear model
    this.model = null;
    this.model = ModelFactory.getInstance().createDefaultModel();

    // clear maps
    for (D2RMap map : maps)
      map.clear();
  }

  private void generateTestMaps() throws D2RException {
    D2RMap map = maps.elementAt(3);
      map.generateResources(this, dataSourceManager.getDataSource(), new Vector<>());
      map.generateResourceProperties(this);
  }

  /**
   * Processes the D2R map and returns a Jena model containing all generated instances.
   * @return Jena model containing all generated instances.
   * @throws D2RException Thrown if an error occurs while generating the RDF instances or if no D2RMap was read before
   */
  public Model generateAllInstancesAsModel() throws D2RException {
    try {
      clear();
    }
    catch (FactoryException e) {
      throw new D2RException("Could not get default Model from the ModelFactory.", e);
    }

    // Generate instances for all maps
    this.generateInstancesForAllMaps();

    // add namespaces
    for (Entry<String, String> ent : namespaces.entrySet()) {
      this.model.setNsPrefix(ent.getKey(), ent.getValue());
    }

    //Return model
    return this.model;
  }

  /**
   * Processes the D2R map outputting the results to the "model" parameter.
   * NOTE: A map has to be loaded previously
   * @param model Model to save instances to. NOTE: The parameter mustn't to be null.
   * @throws D2RException Thrown if an error occurs
   * @throws NullPointerException Thrown if <code> model</code> is null
   */
  private void outputToModel(Model model) throws
      D2RException {

    // Backup the Model object (maintains model's state)
    Model originalModel = this.model;

    log.debug("Processing map to model.");

    if (model == null) {
      throw new NullPointerException("model mustn't be null");
    }

    // use model parameter
    this.model = model;

    // Generate instances for all maps
    this.generateInstancesForAllMaps();

    // add namespaces
    for (Entry<String, String> ent : namespaces.entrySet()) {
      this.model.setNsPrefix(ent.getKey(), ent.getValue());
    }

    //reset model member
    this.model = originalModel;
  }

  /** Generated instances for all D2R maps. */
  private void generateInstancesForAllMaps() throws D2RException {
    for (D2RMap map : maps)
      map.generateResources(this, dataSourceManager.getDataSource(), new Vector<>());
    for (D2RMap map : maps)
      map.generateResourceProperties(this);
  }

  /**
   * Returns the D2R map identified by the id parameter.
   * @return D2R Map.
   */
  D2RMap getMapById(String id) {
    for (D2RMap map : maps) {
      if (map.getId().equals(id)) {
        return map;
      }
    }
    return null;
  }

  /**
   * Returns a reference to the Jena model.
   * @return Jena Model.
   */
  protected Model getModel() {
    return this.model;
  }



  /**
   * Translates a qName to an URI using the namespace mapping of the D2R map.
   * @param qName Qualified name to be translated. See <a href="https://www.w3.org/TR/REC-xml-names/#dt-qualname">
   *              https://www.w3.org/TR/REC-xml-names/#dt-qualname</a> for a detailed description
   * @return the URI of the qualified name.
   */
  @SuppressWarnings("SpellCheckingInspection")
  String getNormalizedURI(String qName) {
    String prefix = D2rUtil.getNamespacePrefix(qName);
    String uriPrefix = namespaces.get(prefix);
    if (uriPrefix != null) {
      String localName = D2rUtil.getLocalName(qName);
      return uriPrefix + localName;
    }
    else {
      return qName;
    }
  }
}