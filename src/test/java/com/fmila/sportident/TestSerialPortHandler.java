package com.fmila.sportident;

import java.io.IOException;


public class TestSerialPortHandler extends AbstractSISerialPortHandler {

  private byte[] data;

  public TestSerialPortHandler(DownloadSession session) {
    super(session, new FMilaSerialTestPort());
  }

  @Override
  protected void handleData(byte[] data) throws IOException, DownloadException {
    this.data = data;
  }

  public byte[] getData() {
    return data;
  }

}
