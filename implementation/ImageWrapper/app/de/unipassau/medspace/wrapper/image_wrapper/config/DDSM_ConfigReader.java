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
 * TODO
 */
public class DDSM_ConfigReader {

  private final String specificationSchema;

  /**
   * TODO
   * @param specificationSchema
   */
  public DDSM_ConfigReader(String specificationSchema) {
    this.specificationSchema = specificationSchema;
  }

  /**
   * TODO
   * @param fileName
   * @throws JAXBException
   * @throws IOException
   * @throws SAXException
   */
  public DDSMConfig parse(String fileName) throws JAXBException, IOException, SAXException {
    JAXBContext context = JAXBContext.newInstance(DDSMConfig.class);
    Unmarshaller unmarshaller = context.createUnmarshaller();

    Schema schema = XmlUtil.createSchema(new String[]{specificationSchema});
    unmarshaller.setSchema(schema);
    return (DDSMConfig) unmarshaller.unmarshal(new File(fileName));
  }
}