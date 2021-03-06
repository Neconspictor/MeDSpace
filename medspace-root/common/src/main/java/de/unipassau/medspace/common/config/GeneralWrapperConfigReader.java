package de.unipassau.medspace.common.config;

import de.unipassau.medspace.common.config.general_wrapper.Config;
import de.unipassau.medspace.common.config.general_wrapper.NamespacesAdapter;
import de.unipassau.medspace.common.rdf.Namespace;
import de.unipassau.medspace.common.rdf.RDFProvider;
import de.unipassau.medspace.common.util.XmlUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import de.unipassau.medspace.common.config.GeneralWrapperConfig.GeneralWrapperConfigData;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Used to read a general wrapper config file.
 */
public class GeneralWrapperConfigReader {

  /**
   * Logger instance of this class.
   */
  private static Logger log = LoggerFactory.getLogger(GeneralWrapperConfigReader.class);

  /**
   * The RDF provider.
   */
  private final RDFProvider provider;

  private final PathResolveParser resolveParser;


  /**
   * Constructs a new {@link GeneralWrapperConfigReader}.
   * @param provider The RDF provider to use.
   * @param resolveParser The path resolve parser to use.
   */
  public GeneralWrapperConfigReader(RDFProvider provider, PathResolveParser resolveParser) {
    this.provider = provider;
    this.resolveParser = resolveParser;
  }

  /**
   * Creates a new ConfigurationReader and initializes it with default values.
   * @return A pre-filled builder to a GeneralWrapperConfig.
   */
  public GeneralWrapperConfigData createDefaultConfig() {
    GeneralWrapperConfigData config = new GeneralWrapperConfigData();

    String format = Constants.STANDARD_OUTPUT_FORMAT;

    if (!provider.isValid(format)) {
      throw new IllegalStateException("Default output language isn't valid!");
    }

    config.setOutputFormat(format);
    return config;
  }

  /**
   * Reads an general wrapper config file from the filesystem.
   * @param filename Filename of the general wrapper config file
   * @return The read configuration file
   * @throws IOException if an error occurs
   */
  public GeneralWrapperConfig readConfig(String filename) throws IOException {

    Config config;
    GeneralWrapperConfigData result = new GeneralWrapperConfigData();

    try {
      // Read document into DOM
      Schema schema = XmlUtil.createSchema(new String[]{Constants.WRAPPER_VALIDATION_SCHEMA});
      JAXBContext context = JAXBContext.newInstance(Config.class);
      Unmarshaller unmarshaller = context.createUnmarshaller();
      unmarshaller.setSchema(schema);

      config = (Config) unmarshaller.unmarshal(new File(filename));

      replaceMacros(config);

      result.setDescription(config.getDescription());
      result.setForceReindex(config.isForceReindex());
      result.setConnectToRegister(config.isConnectToRegister());
      result.setIndexDirectory(new File(config.getIndexDirectoy()).toPath());
      result.setServices(config.getServices().getService());

      NamespacesAdapter namespacesAdapter = new NamespacesAdapter();
      Map<String, Namespace> namespaces =  namespacesAdapter.unmarshal(config.getNamespaces());

      result.setNamespaces(namespaces);
      result.setRegisterURL(config.getRegisterUrl());
      result.setUseIndex(config.getIndexDirectoy() != null);

      // ensure that the output format is a valid rdf format
      String format = config.getOutputFormat();
      if (!provider.isValid(format)) {
        throw createOutputFormatNotSupportedException(format, provider);
      }

      result.setOutputFormat(format);
    }
    catch (SAXParseException spe) {
      throw new IOException("Error while parsing XML file: " + "line " +
          spe.getLineNumber() +
          ", uri: " + spe.getSystemId() + ", reason: " +
          spe.getMessage(), spe);

    } catch (IOException | JAXBException | SAXException e) {
      throw new IOException("Error while parsing XML file: ", e);
    }

    return result.build();
  }

  private static IOException createOutputFormatNotSupportedException(String format, RDFProvider provider) {
    StringBuilder builder = new StringBuilder();
    builder.append("RDF uutput format isn't supported: " + format + "\n");
    builder.append("Supported formats are:\n");
    String prettyPrint = provider.getSupportedFormatsPrettyPrint();
    builder.append(prettyPrint);
    return new IOException(builder.toString());
  }

  private void replaceMacros(Config config) {
    String indexDirectory = config.getIndexDirectoy();
    indexDirectory = resolveParser.replaceMacros(indexDirectory);
    config.setIndexDirectoy(indexDirectory);
  }
}