package com.fmila.sportident;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.fmila.sportident.serial.FMilaSerialPort;

public class SIStationSerialPortHandlerTest {

  @Test
  public void testStationInit() throws Exception {
    FMilaSerialPort port = new FMilaSerialTestPort();
    Object lock = new Object();
    SIStationSerialPortHandler handler = new SIStationSerialPortHandler(Mockito.mock(DownloadCallback.class), lock, port);
    handler.handleData(new byte[]{0, -16, 0, 0, 0, 77, 0, 0, 0});
    Assert.assertFalse(handler.isInitialized());
  }

  @Test(expected = DownloadException.class)
  public void testStationConfigStandardProtocol() throws Exception {
    FMilaSerialPort port = new FMilaSerialTestPort();
    Object lock = new Object();
    SIStationSerialPortHandler handler = new SIStationSerialPortHandler(Mockito.mock(DownloadCallback.class), lock, port);
    handler.handleData(new byte[]{0, -125, 0, 0, 0, 0, 0, 0, 0, 0});
  }

  @Test
  public void testStationConfigExtendedProtocol() throws Exception {
    FMilaSerialPort port = new FMilaSerialTestPort();
    Object lock = new Object();
    SIStationSerialPortHandler handler = new SIStationSerialPortHandler(Mockito.mock(DownloadCallback.class), lock, port);
    handler.handleData(new byte[]{2, -125, 4, 0, 26, 116, 5, -80, -72, 3});
    Assert.assertTrue(handler.isInitialized());
  }

}
