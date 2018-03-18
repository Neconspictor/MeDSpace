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

    //check that sourceFolder is a directory
    if (!new File(sourceFolder).isDirectory()) {
      throw new IOException("The source folder has to be a valid folder!");
    }

    //check that outputFile is a file
    if (new File(outputFile).isDirectory()) {
      throw new IOException("The source folder has to be a valid folder!");
    }

    this.files = generateFileList(new File(sourceFolder), new File(outputFile));
  }


  public void zip() throws IOException {
    byte[] buffer = new byte[1024];
    FileOutputStream fos = null;
    ZipOutputStream zos = null;
    try {
      fos = new FileOutputStream(outputFile);
      zos = new ZipOutputStream(fos);

      for (String file: files) {
        writeZipEntry(file, zos, buffer);
      }

      zos.closeEntry();

    } finally {
      closeSilently(zos);
    }
  }

  private void writeZipEntry(String file,
                             ZipOutputStream zos,
                             byte[] buffer) throws IOException {

    ZipEntry ze = new ZipEntry(file);
    zos.putNextEntry(ze);

    try (FileInputStream in = new FileInputStream(sourceFolder + File.separator + file)){
      int len;
      while ((len = in .read(buffer)) > 0) {
        zos.write(buffer, 0, len);
      }
    }
  }


  private static List<String> generateFileList(File root, File ignoreOutputFile) throws IOException {

    List<String> files = new ArrayList<>();
    // add file only
    if (root.isFile()) {
      throw new IOException("root is expected to be a directory!");
    }

    if (root.isDirectory()) {
      String[] subNote = root.list();
      for (String filename: subNote) {
        generateFileListSub(root, new File(root, filename), ignoreOutputFile, files);
      }
    }

    return files;
  }

  private static void generateFileListSub(File root,
                                          File file,
                                          File ignoreOutputFile,
                                          List<String> files) throws IOException {

    // ignore the output file to prevent unexpected behaviour!
    if (file.equals(ignoreOutputFile)) return;

    if (file.isFile()) {
      files.add(generateZipEntry(root.getCanonicalPath(), file.getCanonicalPath()));
    }

    if (file.isDirectory()) {
      String[] subNote = root.list();
      for (String filename: subNote) {
        generateFileListSub(root, new File(root, filename), ignoreOutputFile, files);
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