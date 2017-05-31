package ar.com.hjg.pngj.samples;

import java.io.File;

import com.bumptech.glide.pngdecoder.IImageLine;
import com.bumptech.glide.pngdecoder.ImageLineHelper;
import com.bumptech.glide.pngdecoder.ImageLineInt;
import com.bumptech.glide.pngdecoder.PngReader;
import com.bumptech.glide.pngdecoder.PngReaderInt;
import com.bumptech.glide.pngdecoder.PngWriter;
import ar.com.hjg.pngj.chunks.ChunkCopyBehaviour;
import ar.com.hjg.pngj.chunks.PngChunkTextVar;

/**
 * Example: decreases the red channel by half, increase the green by 20.
 * 
 * Only for RGB
 */
public class SampleDecreaseRed {

  public static void convert(String origFilename, String destFilename) {
    PngReader pngr = new PngReader(new File(origFilename));
    System.out.println(pngr.toString());
    int channels = pngr.imgInfo.channels;
    if (channels < 3 || pngr.imgInfo.bitDepth != 8)
      throw new RuntimeException("This method is for RGB8/RGBA8 images");
    PngWriter pngw = new PngWriter(new File(destFilename), pngr.imgInfo, true);
    pngw.copyChunksFrom(pngr.getChunksList(), ChunkCopyBehaviour.COPY_ALL_SAFE);
    pngw.getMetadata()
        .setText(PngChunkTextVar.KEY_Description, "Decreased red and increased green");
    for (int row = 0; row < pngr.imgInfo.rows; row++) { // also: while(pngr.hasMoreRows())
      IImageLine l1 = pngr.readRow();
      int[] scanline = ((ImageLineInt) l1).getScanline(); // to save typing
      for (int j = 0; j < pngr.imgInfo.cols; j++) {
        scanline[j * channels] /= 2;
        scanline[j * channels + 1] = ImageLineHelper.clampTo_0_255(scanline[j * channels + 1] + 20);
      }
      pngw.writeRow(l1);
    }
    pngr.end(); // it's recommended to end the reader first, in case there are trailing chunks to
                // read
    pngw.end();
  }

  public static void convert2(String origFilename, String destFilename) {
    PngReaderInt pngr = new PngReaderInt(new File(origFilename));
    System.out.println(pngr.toString());
    int channels = pngr.imgInfo.channels;
    if (channels < 3)
      throw new RuntimeException("This method is for RGB/RGBA images");
    PngWriter pngw = new PngWriter(new File(destFilename), pngr.imgInfo, true);
    pngw.copyChunksFrom(pngr.getChunksList(), ChunkCopyBehaviour.COPY_ALL_SAFE);
    pngw.getMetadata()
        .setText(PngChunkTextVar.KEY_Description, "Decreased red and increased green");
    while (pngr.hasMoreRows()) {
      ImageLineInt l1 = pngr.readRowInt();
      int[] scanline = l1.getScanline(); // to save typing
      for (int j = 0; j < pngr.imgInfo.cols; j++) {
        scanline[j * channels] /= 2;
        scanline[j * channels + 1] = ImageLineHelper.clampTo_0_255(scanline[j * channels + 1] + 20);
      }
      pngw.writeRow(l1);
    }
    pngr.end(); // it's recommended to end the reader first, in case there are trailing chunks to
                // read
    pngw.end();
  }

  public static void main(String[] args) throws Exception {
    if (args.length != 2 || args[0].equals(args[1])) {
      System.err.println("Arguments: [pngsrc] [pngdest]");
      System.exit(1);
    }
    convert2(args[0], args[1]);
    System.out.println("Done: " + args[1]);
  }
}
