package ar.com.hjg.pngj.samples;

import com.bumptech.glide.pngdecoder.ChunkReader;
import com.bumptech.glide.pngdecoder.ChunkReader.ChunkReaderMode;
import com.bumptech.glide.pngdecoder.ChunkSeqReader;
import com.bumptech.glide.pngdecoder.DeflatedChunksSet;
import ar.com.hjg.pngj.chunks.ChunkRaw;

/**
 * This is a general purpose, low level ChunkseqReader for callback mode.
 * 
 * It's "opaque" in the sense that it does not parse the IHDR, and doesn't know the PNG properties. It uncompress the
 * IDAT, but returns it in arbitrary block sizes (not row per row) . All this can be overriden
 */
public class ChunkSeqReaderOpaque extends ChunkSeqReader {
  protected static int IDAT_BLOCK_SIZE_DEFAULT = 40000;
  protected int blockSize = IDAT_BLOCK_SIZE_DEFAULT;

  protected boolean bufferChunks = false;

  public ChunkSeqReaderOpaque() {}

  @Override
  protected ChunkReader createChunkReaderForNewChunk(String id, int len, long offset, boolean skip) {
    return new ChunkReader(len, id, offset, skip ? ChunkReaderMode.SKIP
        : (bufferChunks ? ChunkReaderMode.BUFFER : ChunkReaderMode.PROCESS)) {
      @Override
      protected void chunkDone() {
        postProcessChunk(this);
      }

      @Override
      protected void processData(int offsetInChunk, byte[] buf, int off, int len) {
        processChunkContent(getChunkRaw(), offsetInChunk, buf, off, len); // only if bufferChunks=false
      }
    };
  }

  @Override
  protected DeflatedChunksSet createIdatSet(String id) {
    return new DeflatedChunksSet(id, blockSize, blockSize) {
      @Override
      protected int processRowCallback() {
        // PngHelperInternal.debug("processing callback inflated: " + getRowFilled());
        processIdatInflatedData(getInflatedRow(), 0, getRowFilled());
        return blockSize;// arbitrary, we ask for more data
      }

      @Override
      protected void processDoneCallback() {
        processIdatDone();
      }

    };

  }


  /**
   * this will be called at the start of all chunks (even skipped and idat)
   * 
   * @param chunkRaw
   * @param buf
   * @param off
   * @param len
   */
  @Override
  protected void startNewChunk(int len, String id, long offset) {
    super.startNewChunk(len, id, offset);
  }

  /**
   * this will be called upon reading portions of the chunk, if in PROCESS mode
   * 
   * @param chunkRaw
   * @param buf
   * @param off
   * @param len
   */
  protected void processChunkContent(ChunkRaw chunkRaw, int offsetinchunk, byte[] buf, int off,
      int len) {

  }

  /**
   * this will be called at the end of all chunks (even skipped and idat)
   */
  @Override
  protected void postProcessChunk(ChunkReader chunkR) {
    super.postProcessChunk(chunkR);
  }

  protected void processIdatInflatedData(byte[] inflatedRow, int offset, int len) {}

  protected void processIdatDone() {}


  @Override
  protected void checkSignature(byte[] buf) {
    super.checkSignature(buf);
    // perhaps you want to copy the signature someplace?
  }

  @Override
  protected boolean isIdatKind(String id) {
    return id.equals("IDAT");
  }

  @Override
  protected boolean shouldSkipContent(int len, String id) {
    return super.shouldSkipContent(len, id);
  }

  @Override
  protected boolean shouldCheckCrc(int len, String id) {
    return super.shouldCheckCrc(len, id);
  }



  public void setBufferChunks(boolean bufferChunks) {
    this.bufferChunks = bufferChunks;
  }

  public void setBlockSize(int blockSize) {
    this.blockSize = blockSize;
  }

}
