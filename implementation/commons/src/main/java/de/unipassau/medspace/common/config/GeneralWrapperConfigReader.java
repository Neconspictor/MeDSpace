package de.unipassau.medspace.common.config;

import de.unipassau.medspace.common.config.general_wrapper.RegisterUrlAdapter;
import de.unipassau.medspace.common.config.general_wrapper.Config;
import de.unipassau.medspace.common.rdf.RDFProvider;
import de.unipassau.medspace.common.util.XmlUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import de.unipassau.medspace.common.config.GeneralWrapperConfig.GeneralWrapperConfigData;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import java.io.File;
import java.io.IOException;

/**
 * Used to read a general wrapper config file.
 */
public class GeneralWrapperConfigReader {

  /**
   * Logger instance of this class.
   */
  private static Logger log = LoggerFactory.getLogger(GeneralWrapperConfigReader.class);

  /**
   * TODO
   */
  private RDFProvider provider;


  /**
   * Constructs a new {@link GeneralWrapperConfigReader}.
   */
  public GeneralWrapperConfigReader(RDFProvider provider) {
    this.provider = provider;
  }

  /**
   * Creates a new ConfigurationReader and initializes it with default values.
   * @return A pre-filled builder to a GeneralWrapperConfig.
   */
  public GeneralWrapperConfigData createDefaultConfig() {
    GeneralWrapperConfigData config = new GeneralWrapperConfigData();

    String format = Constants.STANDARD_OUTPUT_FORMAT;

    if (!provider.isValid(format)) {
      throw new IllegalStateException("Default output language couldn't be mapped to a RDFFormat object!");
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
      //Document document = XmlUtil.parseFromFile(filename, schema);

      JAXBContext context = JAXBContext.newInstance(Config.class);

      Unmarshaller unmarshaller = context.createUnmarshaller();
      unmarshaller.setSchema(schema);
      //RegisterUrlAdapter adapter = new RegisterUrlAdapter();
      //unmarshaller.setAdapter(RegisterUrlAdapter.class, adapter);
      config = (Config) unmarshaller.unmarshal(new File(filename));

      result.setDescription(config.getDescription());
      result.setConnectToRegister(config.isConnectToRegister());
      result.setIndexDirectory(new File(config.getIndexDirectoy()).toPath());
      result.setServices(config.getServices());
      result.setNamespaces(config.getNamespaces());
      result.setOutputFormat(config.getOutputFormat());
      result.setRegisterURL(config.getRegisterUrl());
      result.setUseIndex(config.getIndexDirectoy() != null);

      /*Marshaller marshaller = context.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      File myTestFile = new File("myTestFile.xml");
      myTestFile.createNewFile();
      marshaller.marshal(config, myTestFile);*/



      //read the Document
      //readConfig(document, config);
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

}