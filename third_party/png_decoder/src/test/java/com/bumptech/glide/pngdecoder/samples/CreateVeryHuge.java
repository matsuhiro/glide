package ar.com.hjg.pngj.samples;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Random;

import com.bumptech.glide.pngdecoder.FilterType;
import com.bumptech.glide.pngdecoder.ImageInfo;
import com.bumptech.glide.pngdecoder.ImageLineHelper;
import com.bumptech.glide.pngdecoder.ImageLineInt;
import com.bumptech.glide.pngdecoder.PngWriter;
import ar.com.hjg.pngj.test.TestSupport;

/**
 * Creates a VERY huge image (more than 2GB!!!)
 * <p>
 */
public class CreateVeryHuge {

  public static void createVeryHuge(String filename, final int cols, final int rows) {
    OutputStream os = null;
    try {
      os =
          filename == null ? TestSupport.createNullOutputStream() : new FileOutputStream(new File(
              filename));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    PngWriter png = new PngWriter(os, new ImageInfo(cols, rows, 8, false));
    png.setFilterType(FilterType.FILTER_NONE); // no compression at PNG prediction level
    png.setIdatMaxSize(0x10000 * 256);
    png.setCompLevel(0); // // no compression at ZLIB level
    ImageLineInt iline1 = new ImageLineInt(png.imgInfo);
    Random r = new Random();
    for (int j = 0; j < cols; j++) { // pure noise line
      ImageLineHelper.setPixelRGB8(iline1, j, r.nextInt(256), r.nextInt(256), r.nextInt(256));
    }
    long t0 = System.currentTimeMillis();
    for (int row = 0; row < rows; row++) {
      png.writeRow(iline1, row);
    }
    png.end();
    int dt = (int) (System.currentTimeMillis() - t0);
    System.out.println("Created: " + png.imgInfo.toString());
    System.out.printf("%d msecs, %.1f msecs/MPixel \n", dt, dt * 1000000.0 / (cols * rows));
  }

  public void testOnNullDevice() {
    createVeryHuge(null, 30000, 30000);
  }

  static void doitOnDisk() throws Exception {
    String filename = "C:/temp/huge.png";
    // createVeryHuge(filename,30000,30000); // WARNING: this creates an image of about 2.5 Gb !!!
    SampleShowChunks.showChunks(new File(filename), true);
  }

  public static void main(String[] args) throws Exception {
    doitOnDisk();
  }

}
