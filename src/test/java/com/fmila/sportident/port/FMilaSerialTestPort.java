package com.fmila.sportident.port;

import java.io.IOException;

import com.fmila.sportident.serial.FMilaSerialEventListener;
import com.fmila.sportident.serial.FMilaSerialPort;

public class FMilaSerialTestPort implements FMilaSerialPort {

  public FMilaSerialTestPort() {
  }

  @Override
  public void write(byte... data) throws IOException {
  }

  @Override
  public void close() {
  }

  @Override
  public void addEventListener(FMilaSerialEventListener listener) {
  }

}
