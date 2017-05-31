package com.bumptech.glide.pngdecoder;

import java.io.OutputStream;

public interface IPngWriterFactory {
  public PngWriter createPngWriter(OutputStream outputStream, ImageInfo imgInfo);
}
