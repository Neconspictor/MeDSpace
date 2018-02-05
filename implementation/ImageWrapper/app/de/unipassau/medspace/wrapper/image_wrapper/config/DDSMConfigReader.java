package de.unipassau.medspace.wrapper.image_wrapper.config;

import de.unipassau.medspace.common.util.XmlUtil;
import de.unipassau.medspace.wrapper.image_wrapper.config.parsing.*;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * TODO
 */
public class DDSMConfigReader {

  /**
   * TODO
   */
  private static final String MEDSPACE_DDSM_SPECIFICATION_SCHEMA = "./medspace/medspace-ddsm-specifcation.xsd";


  /**
   * TODO
   * @param fileName
   * @throws JAXBException
   * @throws IOException
   * @throws SAXException
   */
  public void parse(String fileName) throws JAXBException, IOException, SAXException {
    RootParsing root = Parser.parse(fileName);
    List<NamespaceParsing> namespaceParsings = root.getNamespace();
    IcsFileParsing container = root.getIcsFile();
    OverlayParsing overlay = root.getOverlay();
    AbnormalityParsing abnormality = root.getAbnormality();
    CalcificationParsing calcification = root.getCalcification();
    MassParsing mass = root.getMass();

  }


  /**
   * TODO
   */
  private static class Parser {
    public static RootParsing parse(String fileName) throws JAXBException, IOException, SAXException {
      JAXBContext context = JAXBContext.newInstance(RootParsing.class);
      Unmarshaller unmarshaller = context.createUnmarshaller();
      Schema schema = XmlUtil.createSchema(new String[]{MEDSPACE_DDSM_SPECIFICATION_SCHEMA});
      unmarshaller.setSchema(schema);
      return (RootParsing) unmarshaller.unmarshal(new File(fileName));
    }
  }
}
