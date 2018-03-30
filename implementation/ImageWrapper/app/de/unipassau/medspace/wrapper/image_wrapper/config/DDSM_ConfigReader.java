package de.unipassau.medspace.wrapper.image_wrapper.config;

import de.unipassau.medspace.common.util.XmlUtil;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import java.io.File;
import java.io.IOException;

/**
 * A reader for the DDSM configuration.
 */
public class DDSM_ConfigReader {

  private final String specificationSchema;

  /**
   * Creates a DDSM_ConfigReader object.
   * @param specificationSchema
   */
  public DDSM_ConfigReader(String specificationSchema) {
    this.specificationSchema = specificationSchema;
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
    return (DDSMConfig) unmarshaller.unmarshal(new File(fileName));
  }
}