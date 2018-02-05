package de.unipassau.medspace.wrapper.image_wrapper.ddsm;



import de.unipassau.medspace.wrapper.image_wrapper.ddsm.lesion.Calcification;
import de.unipassau.medspace.wrapper.image_wrapper.ddsm.lesion.LesionType;
import de.unipassau.medspace.wrapper.image_wrapper.ddsm.lesion.Mass;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static de.unipassau.medspace.wrapper.image_wrapper.ddsm.Util.*;


/**
 * TODO
 */
public class OverlayMetaData {

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
  private String id;

  /**
   * TODO
   */
  private OverlayMetaData() {
    abnormalities = new ArrayList<>();
    totalAbnormalities = 0;
    id = null;
  }

  /**
   * TODO
   * @param file
   * @return
   * @throws IOException
   */
  public static OverlayMetaData parse(File file, String id) throws IOException {
    OverlayMetaData result =  new Parser(id).parse(file);
    result.id = id;
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
  public String getId() {
    return id;
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

    private final String overlayID;

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
      List<String> content = getLineContent(file);

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

      OverlayMetaData result = new OverlayMetaData();
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
      List<String> tokens = tokenize(content.remove(0));
      parseExpectedToken(tokens, ABNORMALITY);
      int abnormalityNumber = parseInt(tokens);

      // Lesion type
      List<LesionType> lesionTypes = parseLesionTypes(content);

      // assessment
      tokens = tokenize(content.remove(0));
      parseExpectedToken(tokens, ASSESSMENT);
      int assessment = parseInt(tokens);

      // SUBTLETY
      tokens = tokenize(content.remove(0));
      parseExpectedToken(tokens, SUBTLETY);
      int subtlety = parseInt(tokens);

      // PATHOLOGY
      tokens = tokenize(content.remove(0));
      parseExpectedToken(tokens, PATHOLOGY);
      String pathology = tokens.remove(0);

      // TOTAL_OUTLINES
      tokens = tokenize(content.remove(0));
      parseExpectedToken(tokens, TOTAL_OUTLINES);
      int totalOutlines = parseInt(tokens);

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
    private static List<LesionType> parseLesionTypes(List<String> content) throws IOException {

      List<LesionType> result = new ArrayList<>();

      while(beginsWithToken(content.get(0), LESION_TYPE)) {
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
    private static LesionType parseLesionType(List<String> content) throws IOException {
      List<String> tokens = tokenize(content.remove(0));
      parseExpectedToken(tokens, LESION_TYPE);
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
    private static Calcification parseCalcification(List<String> tokens) throws IOException {
      parseExpectedToken(tokens, TYPE);
      String type = tokens.remove(0);

      parseExpectedToken(tokens, DISTRIBUTION);
      String distribution = tokens.remove(0);

      return new Calcification(type, distribution);
    }

    /**
     * TODO
     * @param tokens
     * @return
     * @throws IOException
     */
    private static Mass parseMass(List<String> tokens) throws IOException {
      parseExpectedToken(tokens, SHAPE);
      String shape = tokens.remove(0);

      parseExpectedToken(tokens, MARGINS);
      String margins = tokens.remove(0);

      return new Mass(shape, margins);
    }

    /**
     * TODO
     * @param content
     * @return
     * @throws IOException
     */
    private static int parseTotalAbnormalityCount(List<String> content) throws IOException {

      //the first line contains the total abnormality field
      List<String> tokens = tokenize(content.remove(0));
      while(tokens.size() != 2) {
        tokens = tokenize(content.remove(0));
      }

      parseExpectedToken(tokens, TOTAL_ABNORMALITIES);

      try {
        return Integer.parseInt(tokens.remove(0));
      } catch (NumberFormatException e) {
        throw new IOException("Couldn't retrieve " + TOTAL_ABNORMALITIES + " value; cause: ", e);
      }

    }
  }
}