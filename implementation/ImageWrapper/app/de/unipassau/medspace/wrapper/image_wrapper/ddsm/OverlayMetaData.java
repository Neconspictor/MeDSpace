package de.unipassau.medspace.wrapper.image_wrapper.ddsm;

import de.unipassau.medspace.common.util.FileUtil;
import de.unipassau.medspace.common.util.ParserUtil;
import de.unipassau.medspace.common.util.StringUtil;
import de.unipassau.medspace.wrapper.image_wrapper.ddsm.lesion.Calcification;
import de.unipassau.medspace.wrapper.image_wrapper.ddsm.lesion.LesionType;
import de.unipassau.medspace.wrapper.image_wrapper.ddsm.lesion.Mass;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static de.unipassau.medspace.wrapper.image_wrapper.ddsm.Abnormality.*;
import static de.unipassau.medspace.wrapper.image_wrapper.ddsm.lesion.Calcification.*;
import static de.unipassau.medspace.wrapper.image_wrapper.ddsm.lesion.Mass.*;


/**
 * Represents meta data of an overlay file.
 */
public class OverlayMetaData extends DDSM_CaseIdentifiable {

  /**
   * The TOTAL_ABNORMALITIES token in an overlay file.
   */
  public static final String TOTAL_ABNORMALITIES = "TOTAL_ABNORMALITIES";



  private List<Abnormality> abnormalities;

  private int totalAbnormalities;


  /**
   * Creates a new OverlayMetaData object.
   * @param id The ID for this overlay file.
   * @param caseName The name of the case this objects belongs to.
   */
  private OverlayMetaData(String id, String caseName) {
    super(id, caseName);
    abnormalities = new ArrayList<>();
    totalAbnormalities = 0;
  }

  /**
   * Parses overlay meta data from an overlay file.
   * @param file The overlay source file
   * @param id The ID for the overlay file.
   * @param caseName The name of the case the overlay file belongs to.
   * @return
   * @throws IOException
   */
  public static OverlayMetaData parse(File file, String id, String caseName) throws IOException {
    OverlayMetaData result =  new Parser(id).parse(file, caseName);
    return result;
  }

  /**
   * Provides the list of abnormalities.
   * @return the list of abnormalities.
   */
  public List<Abnormality> getAbnormalities() {
    return abnormalities;
  }

  /**
   * Provides the total number of abnormalities.
   * @return the total number of abnormalities.
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
   * A Parser for an overlay file.
   */
  private static class Parser{

    private final String overlayID;

    private int lesionTypeCounter = 0;

    /**
     * Creates a new Parser object.
     * @param overlayID The overlay id to use.
     */
    private Parser(String overlayID) {
      this.overlayID = overlayID;
    }

    /**
     * Parses overlay meta data from an overlay file.
     * @param file The overlay file.
     * @param caseName The name of the case the overlay file belongs to.
     *
     * @return overlay meta data from an overlay file.
     * @throws IOException If an IO error occurs.
     */
    public OverlayMetaData parse(File file, String caseName) throws IOException  {
      List<String> content = FileUtil.getLineContent(file);

      if (content.size() == 0) throw new IOException("Couldn't read content of overlay meta data file: " + file);

      //the first line contains the total abnormality field
      int totalAbnormality = parseTotalAbnormalityCount(content);

      List<Abnormality> abnormalities = new ArrayList<>();

      for (int i = 0; i < totalAbnormality; ++i) {
        Abnormality abnormality = parseAbnormality(content,caseName);
        abnormalities.add(abnormality);
      }

      if (totalAbnormality != abnormalities.size()) {
        throw new IOException("totalAbnormality(" + totalAbnormality + ") doesn't match read abnormailities(" +
            abnormalities.size() + ")");
      }

      OverlayMetaData result = new OverlayMetaData(overlayID, caseName);
      result.abnormalities = abnormalities;
      result.totalAbnormalities = totalAbnormality;
      return result;
    }


    private Abnormality parseAbnormality(List<String> content, String caseName) throws IOException {

      // Get abnormality number
      List<String> tokens = StringUtil.tokenize(content.remove(0), " \t");
      ParserUtil.pullExpectedToken(tokens, ABNORMALITY);
      int abnormalityNumber = ParserUtil.pullInt(tokens);

      // Lesion type
      List<LesionType> lesionTypes = parseLesionTypes(content,caseName);

      // assessment
      tokens = StringUtil.tokenize(content.remove(0), " \t");
      ParserUtil.pullExpectedToken(tokens, ASSESSMENT);
      int assessment = ParserUtil.pullInt(tokens);

      // SUBTLETY
      tokens = StringUtil.tokenize(content.remove(0), " \t");
      ParserUtil.pullExpectedToken(tokens, SUBTLETY);
      int subtlety = ParserUtil.pullInt(tokens);

      // PATHOLOGY
      tokens = StringUtil.tokenize(content.remove(0), " \t");
      ParserUtil.pullExpectedToken(tokens, PATHOLOGY);
      String pathology = tokens.remove(0);

      // TOTAL_OUTLINES
      tokens = StringUtil.tokenize(content.remove(0), " \t");
      ParserUtil.pullExpectedToken(tokens, TOTAL_OUTLINES);
      int totalOutlines = ParserUtil.pullInt(tokens);

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
          overlayID + "#" + abnormalityNumber,
          caseName);
    }


    private List<LesionType> parseLesionTypes(List<String> content, String caseName) throws IOException {

      List<LesionType> result = new ArrayList<>();

      while(StringUtil.beginsWithToken(content.get(0), LESION_TYPE)) {
        LesionType type = parseLesionType(content, caseName);
        result.add(type);
      }

      return result;
    }


    private LesionType parseLesionType(List<String> content, String caseName) throws IOException {

      List<String> tokens = StringUtil.tokenize(content.remove(0), " \t");
      ParserUtil.pullExpectedToken(tokens, LESION_TYPE);
      String lesionTypeStr = tokens.remove(0);
      LesionType lesionType;

      if (lesionTypeStr.equals(Mass.MASS)) {
        lesionType = parseMass(tokens, caseName);
      } else if (lesionTypeStr.equals(Calcification.CALCIFICATION)) {
        lesionType = parseCalcification(tokens, caseName);
      } else {
        throw new IOException("Unknown LESION_TYPE:  " + lesionTypeStr);
      }

      return lesionType;
    }


    private Calcification parseCalcification(List<String> tokens, String caseName) throws IOException {
      ParserUtil.pullExpectedToken(tokens, TYPE);
      String type = tokens.remove(0);

      ParserUtil.pullExpectedToken(tokens, DISTRIBUTION);
      String distribution = tokens.remove(0);

      return new Calcification(type, distribution, createLesionTypeId(), caseName);
    }


    private Mass parseMass(List<String> tokens, String caseName) throws IOException {
      ParserUtil.pullExpectedToken(tokens, SHAPE);
      String shape = tokens.remove(0);

      ParserUtil.pullExpectedToken(tokens, MARGINS);
      String margins = tokens.remove(0);

      return new Mass(shape, margins, createLesionTypeId(), caseName);
    }


    private static int parseTotalAbnormalityCount(List<String> content) throws IOException {

      //the first line contains the total abnormality field
      List<String> tokens = StringUtil.tokenize(content.remove(0), " \t");
      while(tokens.size() != 2) {
        tokens = StringUtil.tokenize(content.remove(0), " \t");
      }

      ParserUtil.pullExpectedToken(tokens, TOTAL_ABNORMALITIES);

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