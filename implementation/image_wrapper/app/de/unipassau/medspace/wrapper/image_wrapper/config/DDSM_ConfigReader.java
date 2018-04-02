package de.unipassau.medspace.wrapper.image_wrapper.config;

import de.unipassau.medspace.common.play.ProjectResourceManager;
import de.unipassau.medspace.common.util.XmlUtil;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * A reader for the DDSM configuration.
 */
public class DDSM_ConfigReader {

  private final String specificationSchema;

  private final ProjectResourceManager resourceManager;

  /**
   * Creates a DDSM_ConfigReader object.
   * @param specificationSchema The XSD specification schema for the DDSM configuration file.
   * @param resourceManager The project resource manager.
   */
  public DDSM_ConfigReader(String specificationSchema, ProjectResourceManager resourceManager) {
    this.specificationSchema = specificationSchema;
    this.resourceManager = resourceManager;
  }

  /**
   * Parses a DDSM maping configuration file.
   * @param fileName The DDSM maping configuration file.
   * @return The parsed configuration.
   *
   * @throws JAXBException If an error occurs regarding JAXB-
   * @throws IOException If an IO error occurs.
   * @throws SAXException If an XML error occurs.
   */
  public DDSMConfig parse(String fileName) throws JAXBException, IOException, SAXException {
    JAXBContext context = JAXBContext.newInstance(DDSMConfig.class);
    Unmarshaller unmarshaller = context.createUnmarshaller();

    Schema schema = XmlUtil.createSchema(new String[]{specificationSchema});
    unmarshaller.setSchema(schema);
    DDSMConfig config = (DDSMConfig) unmarshaller.unmarshal(new File(fileName));


    String imageDirectory;

    try {
      imageDirectory = resourceManager.getResolvedPath(config.getImageDirectory());
    } catch (FileNotFoundException e) {
      throw new IOException("Couldn't find/resolve DDSM case folder: " + config.getImageDirectory(), e);
    }
    config.setImageDirectory(imageDirectory);

    return config;
  }
}