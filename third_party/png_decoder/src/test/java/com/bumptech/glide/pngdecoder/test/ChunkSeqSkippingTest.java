package ar.com.hjg.pngj.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import com.bumptech.glide.pngdecoder.BufferedStreamFeeder;
import com.bumptech.glide.pngdecoder.ChunkReader;
import com.bumptech.glide.pngdecoder.ChunkSeqReader;
import com.bumptech.glide.pngdecoder.ChunkSeqSkipping;
import com.bumptech.glide.pngdecoder.PngHelperInternal;
import ar.com.hjg.pngj.chunks.ChunkRaw;
import ar.com.hjg.pngj.chunks.PngChunkTEXT;

/**
 * Same as {@link ChunkSeqBufferingTest} but with hot processing mode
 * 
 */
public class ChunkSeqSkippingTest extends PngjTest {

  private static final String TEXT_TO_ADD_KEY = "Test";
  private static final String TEXT_TO_ADD = "Hi! testing";

  public static class InsertChunk {
    private final ChunkSeqReader cs;
    private BufferedStreamFeeder streamFeeder;
    private OutputStream os;

    private boolean inserted;
    private boolean beforeIdat;

    /** copies all chunks and inserts a textual in between */
    public InsertChunk(InputStream inputStream, OutputStream osx, boolean beforeIdat) {
      streamFeeder = new BufferedStreamFeeder(inputStream);
      this.beforeIdat = beforeIdat;
      this.os = osx;
      cs = new ChunkSeqSkipping(false) {
        @Override
        protected void postProcessChunk(ChunkReader chunkR) {
          super.postProcessChunk(chunkR);
          chunkR.getChunkRaw().writeChunkCrc(os); // send only the crc
        }


        @Override
        protected void processChunkContent(ChunkRaw chunkRaw, int offsetinChhunk, byte[] buf,
            int off, int len) {
          // PngHelperInternal.debug("processing chunk " + chunkRaw);
          if (offsetinChhunk == 0) {
            chunkRaw.writeChunkHeader(os); // send only the header
          }
          PngHelperInternal.writeBytes(os, buf, off, len);
        }


        @Override
        protected void startNewChunk(int len, String id, long offset) {
          super.startNewChunk(len, id, offset);
          insertMyChunk(id); // insert the text chunk if appropiate
        }
      };
      execute();
    }

    private void execute() {
      PngHelperInternal.writeBytes(os, PngHelperInternal.getPngIdSignature());
      streamFeeder.feedAll(cs);
      try {
        os.close();
      } catch (IOException e) {
        e.printStackTrace();
      }

    }

    private void insertMyChunk(String nextChukn) {
      if (inserted)
        return;
      // this logic puts the text chunk just before first IDAT or just after it
      if ((beforeIdat && nextChukn.equals("IDAT")) || (nextChukn.equals("IEND") && !beforeIdat)) { // insert
        PngChunkTEXT t = new PngChunkTEXT(null);
        t.setKeyVal(TEXT_TO_ADD_KEY, TEXT_TO_ADD);
        t.createRawChunk().writeChunk(os);
        inserted = true;
      }
    }
  }

  @Test
  public void addTextualBefore() {
    TestSupport.getResourcesDir();
    String dest = "test/temp/stripeswt.png";
    new InsertChunk(TestSupport.istream(TestSupport.PNG_TEST_STRIPES), TestSupport.ostream(dest),
        true);
    String s = TestSupport.getChunksSummary(dest);
    // PngHelperInternal.LOGGER.info(s);
    TestCase
        .assertEquals(
            "IHDR[13] pHYs[9] tEXt[16] IDAT[2000] IDAT[2000] IDAT[2000] IDAT[610] tIME[7] iTXt[30] IEND[0] ",
            s);
  }

  @Test
  public void addTextualAfter() {
    String dest = "test/temp/stripeswb.png";
    new InsertChunk(TestSupport.istream(TestSupport.PNG_TEST_STRIPES), TestSupport.ostream(dest),
        false);
    String s = TestSupport.getChunksSummary(dest);
    // PngHelperInternal.LOGGER.info(s);
    TestCase
        .assertEquals(
            "IHDR[13] pHYs[9] IDAT[2000] IDAT[2000] IDAT[2000] IDAT[610] tIME[7] iTXt[30] tEXt[16] IEND[0] ",
            s);
  }

  @Before
  public void setUp() {

  }

}
