package de.unipassau.medspace.wrapper.image_wrapper.ddsm;

import de.unipassau.medspace.wrapper.image_wrapper.ddsm.lesion.Calcification;
import de.unipassau.medspace.wrapper.image_wrapper.ddsm.lesion.LesionType;
import de.unipassau.medspace.wrapper.image_wrapper.ddsm.lesion.Mass;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * TODO
 */
public class OverlayMetaData extends Identifiable {

  /**
   * TODO
   */
  private List<Abnormality> abnormalities;

  /**
   * TODO
   */
  private int totalAbnormalities;

  /**
   * TODO
   */
  private OverlayMetaData(String id) {
    super(id);
    abnormalities = new ArrayList<>();
    totalAbnormalities = 0;
  }

  /**
   * TODO
   * @param file
   * @return
   * @throws IOException
   */
  public static OverlayMetaData parse(File file, String id) throws IOException {
    OverlayMetaData result =  new Parser(id).parse(file);
    return result;
  }

  /**
   * TODO
   */
  public List<Abnormality> getAbnormalities() {
    return abnormalities;
  }

  /**
   * TODO
   */
  public int getTotalAbnormalities() {
    return totalAbnormalities;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();

    builder.append("OverlayMetaData: {\n");

    builder.append("totalAbnormalities: " + totalAbnormalities + "\n");

    for (Abnormality abnormality : abnormalities) {
      builder.append(abnormality + "\n");
    }

    builder.append("}");

    return builder.toString();
  }

  /**
   * TODO
   */
  private static class Parser{

    /**
     * TODO
     */
    private static final String TOTAL_ABNORMALITIES = "TOTAL_ABNORMALITIES";

    /**
     * TODO
     */
    private static final String ABNORMALITY = "ABNORMALITY";

    /**
     * TODO
     */
    private static final String LESION_TYPE = "LESION_TYPE";

    /**
     * TODO
     */
    private static final String ASSESSMENT = "ASSESSMENT";


    /**
     * TODO
     */
    private static final String SUBTLETY = "SUBTLETY";

    /**
     * TODO
     */
    private static final String PATHOLOGY = "PATHOLOGY";


    /**
     * TODO
     */
    private static final String TOTAL_OUTLINES = "TOTAL_OUTLINES";


    /**
     * TODO
     */
    private static final String BOUNDARY = "BOUNDARY";


    /**
     * TODO
     */
    private static final String TYPE = "TYPE";

    /**
     * TODO
     */
    private static final String DISTRIBUTION = "DISTRIBUTION";


    /**
     * TODO
     */
    private static final String SHAPE = "SHAPE";


    /**
     * TODO
     */
    private static final String MARGINS = "MARGINS";

    /**
     * TODO
     */
    private final String overlayID;

    /**
     * TODO
     */
    private int lesionTypeCounter = 0;

    /**
     * TODO
     * @param overlayID
     */
    private Parser(String overlayID) {
      this.overlayID = overlayID;
    }

    /**
     * TODO
     * @param file
     * @return
     * @throws IOException
     */
    public OverlayMetaData parse(File file) throws IOException  {
      List<String> content = Util.getLineContent(file);

      if (content.size() == 0) throw new IOException("Couldn't read content of overlay meta data file: " + file);

      //the first line contains the total abnormality field
      int totalAbnormality = parseTotalAbnormalityCount(content);

      List<Abnormality> abnormalities = new ArrayList<>();

      for (int i = 0; i < totalAbnormality; ++i) {
        Abnormality abnormality = parseAbnormality(content);
        abnormalities.add(abnormality);
      }

      if (totalAbnormality != abnormalities.size()) {
        throw new IOException("totalAbnormality(" + totalAbnormality + ") doesn't match read abnormailities(" +
            abnormalities.size() + ")");
      }

      OverlayMetaData result = new OverlayMetaData(overlayID);
      result.abnormalities = abnormalities;
      result.totalAbnormalities = totalAbnormality;
      return result;
    }

    /**
     * TODO
     * @param content
     * @return
     * @throws IOException
     */
    private Abnormality parseAbnormality(List<String> content) throws IOException {

      // Get abnormality number
      List<String> tokens = Util.tokenize(content.remove(0));
      Util.parseExpectedToken(tokens, ABNORMALITY);
      int abnormalityNumber = Util.parseInt(tokens);

      // Lesion type
      List<LesionType> lesionTypes = parseLesionTypes(content);

      // assessment
      tokens = Util.tokenize(content.remove(0));
      Util.parseExpectedToken(tokens, ASSESSMENT);
      int assessment = Util.parseInt(tokens);

      // SUBTLETY
      tokens = Util.tokenize(content.remove(0));
      Util.parseExpectedToken(tokens, SUBTLETY);
      int subtlety = Util.parseInt(tokens);

      // PATHOLOGY
      tokens = Util.tokenize(content.remove(0));
      Util.parseExpectedToken(tokens, PATHOLOGY);
      String pathology = tokens.remove(0);

      // TOTAL_OUTLINES
      tokens = Util.tokenize(content.remove(0));
      Util.parseExpectedToken(tokens, TOTAL_OUTLINES);
      int totalOutlines = Util.parseInt(tokens);

      /**
       * TOTAL_OUTLINES specifies the number of outlines that were made by a radiologist
       * The first outline looks as follows:
       * BOUNDARY
       * ... outline stuff in one line...
       *
       * If there are more than one outline the following outlines are marked with a "CORE" token
       * CORE
       * ... outline stuff in one line ...
       *
       * We don't need the outlines so we skeep them
       */
      for (int i = 0; i < totalOutlines; ++i) {
        content.remove(0);
        content.remove(0);
      }

      return new Abnormality(abnormalityNumber,
          lesionTypes,
          assessment,
          subtlety,
          pathology,
          totalOutlines,
          overlayID + "#" + abnormalityNumber);
    }

    /**
     * TODO
     * @param content
     * @return
     * @throws IOException
     */
    private List<LesionType> parseLesionTypes(List<String> content) throws IOException {

      List<LesionType> result = new ArrayList<>();

      while(Util.beginsWithToken(content.get(0), LESION_TYPE)) {
        LesionType type = parseLesionType(content);
        result.add(type);
      }

      return result;
    }

    /**
     * TODO
     * @param content
     * @return
     * @throws IOException
     */
    private LesionType parseLesionType(List<String> content) throws IOException {

      List<String> tokens = Util.tokenize(content.remove(0));
      Util.parseExpectedToken(tokens, LESION_TYPE);
      String lesionTypeStr = tokens.remove(0);
      LesionType lesionType;

      if (lesionTypeStr.equals(Mass.MASS)) {
        lesionType = parseMass(tokens);
      } else if (lesionTypeStr.equals(Calcification.CALCIFICATION)) {
        lesionType = parseCalcification(tokens);
      } else {
        throw new IOException("Unknown LESION_TYPE:  " + lesionTypeStr);
      }

      return lesionType;
    }

    /**
     * TODO
     * @param tokens
     * @return
     * @throws IOException
     */
    private Calcification parseCalcification(List<String> tokens) throws IOException {
      Util.parseExpectedToken(tokens, TYPE);
      String type = tokens.remove(0);

      Util.parseExpectedToken(tokens, DISTRIBUTION);
      String distribution = tokens.remove(0);

      return new Calcification(type, distribution, createLesionTypeId());
    }

    /**
     * TODO
     * @param tokens
     * @return
     * @throws IOException
     */
    private Mass parseMass(List<String> tokens) throws IOException {
      Util.parseExpectedToken(tokens, SHAPE);
      String shape = tokens.remove(0);

      Util.parseExpectedToken(tokens, MARGINS);
      String margins = tokens.remove(0);

      return new Mass(shape, margins, createLesionTypeId());
    }

    /**
     * TODO
     * @param content
     * @return
     * @throws IOException
     */
    private static int parseTotalAbnormalityCount(List<String> content) throws IOException {

      //the first line contains the total abnormality field
      List<String> tokens = Util.tokenize(content.remove(0));
      while(tokens.size() != 2) {
        tokens = Util.tokenize(content.remove(0));
      }

      Util.parseExpectedToken(tokens, TOTAL_ABNORMALITIES);

      try {
        return Integer.parseInt(tokens.remove(0));
      } catch (NumberFormatException e) {
        throw new IOException("Couldn't retrieve " + TOTAL_ABNORMALITIES + " value; cause: ", e);
      }

    }

    private String createLesionTypeId() {
      String id = overlayID + "#" + lesionTypeCounter;
      ++lesionTypeCounter;
      return id;
    }
  }
}