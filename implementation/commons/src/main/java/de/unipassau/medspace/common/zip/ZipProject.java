package de.unipassau.medspace.common.zip;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * TODO
 */
public class ZipProject {

  private List<String> files;
  private String outputFile;
  private String sourceFolder;

  public ZipProject(String sourceFolder, String outputFile) throws IOException {
    this.outputFile = outputFile;
    this.sourceFolder = sourceFolder;
    this.files = generateFileList(new File(sourceFolder));
  }


  public void zip() throws IOException {
    byte[] buffer = new byte[1024];
    String source = new File(sourceFolder).getName();
    FileOutputStream fos = null;
    ZipOutputStream zos = null;
    try {
      fos = new FileOutputStream(outputFile);
      zos = new ZipOutputStream(fos);

      for (String file: files) {
        writeZipEntry(file, source, zos, buffer);
      }

      zos.closeEntry();

    } finally {
      closeSilently(zos);
    }
  }

  private void writeZipEntry(String file,
                             String source,
                             ZipOutputStream zos,
                             byte[] buffer) throws IOException {

    ZipEntry ze = new ZipEntry(source + File.separator + file);
    zos.putNextEntry(ze);

    try (FileInputStream in = new FileInputStream(sourceFolder + File.separator + file)){
      int len;
      while ((len = in .read(buffer)) > 0) {
        zos.write(buffer, 0, len);
      }
    }
  }


  public static List<String> generateFileList(File root) throws IOException {

    List<String> files = new ArrayList<>();
    // add file only
    if (root.isFile()) {
      throw new IOException("root is expected to be a directory!");
    }

    if (root.isDirectory()) {
      String[] subNote = root.list();
      for (String filename: subNote) {
        generateFileListSub(root, new File(root, filename), files);
      }
    }

    return files;
  }

  private static void generateFileListSub(File root, File file, List<String> files) throws IOException {
    if (file.isFile()) {
      files.add(generateZipEntry(root.getCanonicalPath(), file.getCanonicalPath()));
    }

    if (root.isDirectory()) {
      String[] subNote = root.list();
      for (String filename: subNote) {
        generateFileListSub(root, new File(root, filename), files);
      }
    }
  }

  private static String generateZipEntry(String sourceFolder, String file) {
    return file.substring(sourceFolder.length() + 1, file.length());
  }

  private static void closeSilently(Closeable closeable) {
    if (closeable == null) return;
    try {
      closeable.close();
    } catch (IOException e) {}
  }
}