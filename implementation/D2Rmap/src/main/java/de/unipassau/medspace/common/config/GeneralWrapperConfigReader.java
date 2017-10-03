package de.unipassau.medspace.common.config;

import de.unipassau.medspace.common.exception.ParseException;
import de.unipassau.medspace.common.rdf.Namespace;
import de.unipassau.medspace.common.util.XmlUtil;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import de.unipassau.medspace.common.config.GeneralWrapperConfig.GeneralWrapperConfigData;

import javax.xml.validation.Schema;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Used to read a general wrapper config file.
 */
public class GeneralWrapperConfigReader {

  /**
   * Logger instance of this class.
   */
  private static Logger log = LoggerFactory.getLogger(GeneralWrapperConfigReader.class);

  /**
   * Contains all supported org.apache.jena.riot.Lang objects that are supported by the jena framework
   * to be used for streaming. Not all rdf serialization formats supports to stream the triple result set,
   * so not all jena rdf languages are supported.
   */
  private Set<Lang> supportedStreamLanguages;

  /**
   * Constructs a new {@ink GeneralWrapperConfigReader}.
   */
  public GeneralWrapperConfigReader() {
    supportedStreamLanguages = new HashSet<>();
    Collection<RDFFormat> formats = StreamRDFWriter.registered();
    for (RDFFormat format : formats) {
      supportedStreamLanguages.add(format.getLang());
    }

    // delete rdf/null, as it only outputs an empty rdf graph
    // -> not very useful for exporting data.
    supportedStreamLanguages.remove(Lang.RDFNULL);
  }

  /**
   * Creates a new ConfigurationReader and initializes it with default values.
   * @return A pre-filled builder to a GeneralWrapperConfig.
   */
  public static GeneralWrapperConfigData createDefaultConfig() {
    GeneralWrapperConfigData config = new GeneralWrapperConfigData();

    Lang lang;

    try {
      lang = getLangFromString(Constants.OutputFormat.STANDARD_OUTPUT_FORMAT);
    } catch (IllegalArgumentException e) {
      throw new IllegalStateException("Default output language couldn't be mapped to a Lang object!");
    }

    config.setOutputFormat(lang);
    return config;
  }

  /**
   * Reads a namespace element and adds it to the provided Configuration.
   * @param config the Configuration the namespace element should be added to
   * @param elem The namesapce element
   */
  public  static void readComplexTypeNamespace(GeneralWrapperConfigData config, Element elem) {
    String prefix = elem.getAttribute(Constants.Namespace.PREFIX_ATTRIBUTE);
    String namespace = elem.getAttribute(Constants.Namespace.NAMESPACE_ATTRIBUTE);

    if (prefix.equals(""))
      throw new IllegalStateException("prefix not set or empty.");
    if (namespace.equals(""))
      throw new IllegalStateException("namespace not set or empty.");

    config.getNamespaces().put(prefix, new Namespace(prefix, namespace));
  }

  /**
   * Reads an general wrapper config file from the filesystem.
   * @param filename Filename of the general wrapper config file
   * @return The read configuration file
   * @throws IOException if an error occurs
   */
  public GeneralWrapperConfig readConfig(String filename) throws IOException {
    GeneralWrapperConfigData config = createDefaultConfig();
    try {
      // Read document into DOM
      Schema schema = XmlUtil.createSchema(new String[]{Constants.WRAPPER_VALIDATION_SCHEMA});
      Document document = XmlUtil.parseFromFile(filename, schema);

      //read the Document
      readConfig(document, config);
    }
    catch (SAXParseException spe) {
      throw new IOException("Error while parsing XML file: " + "line " +
          spe.getLineNumber() +
          ", uri: " + spe.getSystemId() + ", reason: " +
          spe.getMessage(), spe);

    } catch (IOException | ParseException | SAXException e) {
      throw new IOException("Error while parsing XML file: ", e);
    }

    return config.build();
  }

  /**
   * Parses a string that represents an jena rdf language to the representing java object.
   * @param format The string that represent a jena rdf language.
   * @return The rdf language.
   * @throws IllegalArgumentException If the string couldn't be parsed.
   */
  private static Lang getLangFromString(String format) {
    assert format != null;

    Lang lang = RDFLanguages.shortnameToLang(format);

    if (lang == null)
      throw new IllegalArgumentException("Unknown language format: " + format);

    return lang;
  }


  /**
   * Parses a general wrapper config from a given XML document and stores its content in the specified Configuration.
   * @param document The xml document that represents a valid general wrapper config file.
   * @param config The configuration builder to fill.
   * @throws IOException If the document does not contain a medspace-wrapper-config-specification wrapper root element
   * or another IO-Error occurs.
   * @throws ParseException if another parse error occurs.
   */
  private void readConfig(Document document, GeneralWrapperConfigData config) throws IOException, ParseException {
    // Read namespaces
    NodeList list = document.getElementsByTagNameNS(Constants.WRAPPER_NS, Constants.Namespace.NAME);
    int numNodes = list.getLength();
    for (int i = 0; i < numNodes; i++) {
      Element elem = (Element) list.item(i);
      readComplexTypeNamespace(config, elem);
    }

    // Read the root element
    list = document.getElementsByTagNameNS(Constants.WRAPPER_NS, Constants.Root.NAME);
    Element root = (Element) list.item(0);

    if (root == null)
      throw new IOException("No root element was specified in the mapping");

    // check if a index is wished and if it is the case, then read ut the index store directory
    list = root.getElementsByTagNameNS(Constants.WRAPPER_NS, Constants.Index.NAME);
    if (list.getLength() > 0)
      readIndexElement(config, (Element) list.item(0));

    //OutputFormat is a required element that exists exact one time
    list = root.getElementsByTagNameNS(Constants.WRAPPER_NS, Constants.OutputFormat.NAME);
    readOutputFormatElement(config, (Element) list.item(0));

    //The register element has to occur exactly one time.
    list = root.getElementsByTagNameNS(Constants.WRAPPER_NS, Constants.Register.NAME);
    readRegisterElement(config, (Element) list.item(0));

  }

  /**
   * Reads the index directory from a given element and adds it to the given config builder.
   * @param config The config builder the read index directory should be added to.
   * @param elem The element that represents an index element.
   * @throws IOException If the index directory couldn't be parsed.
   */
  private static void readIndexElement(GeneralWrapperConfigData config, Element elem) throws IOException {
    String directory = elem.getAttribute(Constants.Index.DIRECTORY_ATTRIBUTE);

    if (directory == null) {
      throw new IllegalArgumentException("index directory attribute mustn't be null!");
    }

    Path path = null;

    try {
      //path = FileUtil.createDirectory(directory);
      path = Paths.get(directory);
    } catch (InvalidPathException e) {
      throw new IOException("Error while trying to createDoc index directory path", e);
    }


    config.setUseIndex(true);
    config.setIndexDirectory(path);
  }


  /**
   * Parses the rdf output format language
   * @param config The config builder the read output format should be added to.
   * @param elem The element that contains the output format.
   * @throws ParseException If the rdf language couldn't be parsed from the content of the specified element.
   */
  private void readOutputFormatElement(GeneralWrapperConfigData config, Element elem) throws ParseException {
    String format = elem.getTextContent();
    Lang lang = null;

    assert format != null;

    try {
      lang = getLangFromString(format);
    } catch (IllegalArgumentException e) {
      throw new ParseException("Error while retrieving rdf language", e);
    }

    if (!supportedStreamLanguages.contains(lang)) {
      StringBuilder supportedLangs = new StringBuilder();
      for (Lang l : supportedStreamLanguages) {
        supportedLangs.append(l.getLabel());
        supportedLangs.append("\n");
      }

      supportedLangs.delete(supportedLangs.length() -1, supportedLangs.length());

      throw new ParseException("RDF language isn't supported for streaming rdf triples: " + lang.getLabel() +
          "\nSupported languages are:\n" + supportedLangs.toString());
    }

    config.setOutputFormat(lang);
  }

  /**
   * Reads a Register element and adds it to a config builder.
   * @param config The config builder the read register url should be added to.
   * @param elem Represents a valid Register element.
   * @throws IllegalArgumentException If 'elem' isn't a valid Register element.
   * @throws ParseException If no url could be created.
   */
  private static void readRegisterElement(GeneralWrapperConfigData config, Element elem) throws ParseException {
    String urlAttribute = elem.getAttribute(Constants.Register.URL_ATTRIBUTE);

    if (urlAttribute == null)
      throw new IllegalArgumentException("Register url attribute mustn't be null!");

    URL url;
    try {
      url = new URL(urlAttribute);
    } catch (MalformedURLException e) {
      throw new ParseException("Couldn't get URL to the register. Read source data: " + urlAttribute);
    }

    config.setRegisterURL(url);
  }
}