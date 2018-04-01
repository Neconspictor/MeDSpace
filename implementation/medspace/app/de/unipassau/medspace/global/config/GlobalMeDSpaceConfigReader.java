package de.unipassau.medspace.global.config;

import de.unipassau.medspace.common.config.PathResolveParser;
import de.unipassau.medspace.common.play.ProjectResourceManager;
import de.unipassau.medspace.common.util.XmlUtil;
import de.unipassau.medspace.global.config.mapping.ConfigMapping;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * A reader for the global MeDSpace configuration.
 */
public class GlobalMeDSpaceConfigReader {

  private static final String THIS_BASE_URL_TOKEN = "[server-address]";



  private final String globalServerConfigSpecificationFile;

  private final String thisBaseURL;

  private final PathResolveParser pathResolveParser;

  /**
   * Creates a new PdfConfigReader object.
   * @param globalServerConfigSpecificationFile The XSD validation schema for the global MeDSpace configuration.
   * @param resourceManager The resource manager.
   * @param thisBaseURL The base URL of the server of this application.
   */
  public GlobalMeDSpaceConfigReader(String globalServerConfigSpecificationFile,
                                    ProjectResourceManager resourceManager, String thisBaseURL) {
    this.globalServerConfigSpecificationFile = globalServerConfigSpecificationFile;
    this.pathResolveParser = resourceManager.get();
    this.thisBaseURL = thisBaseURL;
  }


  /**
   * Parses the PDF wrapper RDF mappings from a given file.
   *
   * @param fileName The file to parse.
   * @throws JAXBException If an error related to JAXB occurs.
   * @throws IOException If an IO error occurs.
   * @throws SAXException If an error related to XML parsing occurs.
   */
  public ConfigMapping parse(String fileName) throws JAXBException, IOException, SAXException {
    JAXBContext context = JAXBContext.newInstance(ConfigMapping.class);
    Unmarshaller unmarshaller = context.createUnmarshaller();
    Schema schema = XmlUtil.createSchema(new String[]{globalServerConfigSpecificationFile});
    unmarshaller.setSchema(schema);
    ConfigMapping config = (ConfigMapping) unmarshaller.unmarshal(new File(fileName));

    resolvePaths(config);

    resolveBaseAddresses(config);

    return config;
  }

  private void resolveBaseAddresses(ConfigMapping config) {
    String dataCollectorBaseURL = config.getDataCollector().getBaseURL();
    dataCollectorBaseURL = replaceThisURL(dataCollectorBaseURL);
    config.getDataCollector().setBaseURL(dataCollectorBaseURL);

    String registerBaseURL = config.getRegister().getBaseURL();
    registerBaseURL = replaceThisURL(registerBaseURL);
    config.getRegister().setBaseURL(registerBaseURL);
  }

  private String replaceThisURL(String baseURL) {
    if (baseURL.startsWith(THIS_BASE_URL_TOKEN)) {
      baseURL = baseURL.substring(THIS_BASE_URL_TOKEN.length(), baseURL.length());
      baseURL = thisBaseURL + baseURL;
    }

    // base URLs have to end with a forward slash
    if (!baseURL.endsWith("/")) {
      baseURL += "/";
    }

    return baseURL;
  }

  private void resolvePaths(ConfigMapping config) {
    String nativeStoreDirectory = config.getDataCollector().getNativeStoreDirectory();
    nativeStoreDirectory = pathResolveParser.replaceMacros(nativeStoreDirectory);
    config.getDataCollector().setNativeStoreDirectory(nativeStoreDirectory);
  }
}