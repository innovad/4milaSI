package com.fmila.sportident.processor;

import java.io.IOException;
import java.util.GregorianCalendar;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.fmila.sportident.DownloadCallback;
import com.fmila.sportident.DownloadException;
import com.fmila.sportident.FMilaSerialTestPort;
import com.fmila.sportident.serial.FMilaSerialPort;


public class SICardV5ProcessorTest {

  @Test
  public void test() throws DownloadException, IOException {
    FMilaSerialPort testPort = new FMilaSerialTestPort();

    GregorianCalendar greg = new GregorianCalendar(2012, 1, 1, 12, 30, 00);
    SICardV5Processor processor = new SICardV5Processor(testPort, greg.getTime(), Mockito.mock(DownloadCallback.class));

    byte[] insertedData = new byte[]{2, -27, 6, 0, 4, 0, 0, -10, -19, -29, -101, 3};
    processor.handleCardInserted(insertedData);

    byte[] processData = new byte[]{2, -79, -126, 0, 4, -86, 41, 0, 1, -10, -19, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 101, -10, -19, 35, 2, 42, 52, 10, 86, -11, -90, 40, 1, -28, 0, 7, 0, 34, 35, -116, 52, 36, 99, 54, 37, 30, 88, 37, -93, 90, 38, 43, 0, 96, 39, 108, 95, 40, 15, 99, 41, -86, 100, 42, 12, 0, -18, -18, 0, 0, -18, -18, 0, -18, -18, 0, -18, -18, 0, -18, -18, 0, -18, -18, 0, 0, -18, -18, 0, -18, -18, 0, -18, -18, 0, -18, -18, 0, -18, -18, 0, 0, -18, -18, 0, -18, -18, 0, -18, -18, 0, -18, -18, 0, -18, -18, 0, 0, -18, -18, 0, -18, -18, 0, -18, -18, 0, -18, -18, 0, -18, -18, -26, 7, 3};
    byte[] carddataByte = new byte[128];
    System.arraycopy(processData, 5, carddataByte, 0, 128);
    processor.handleProcessData(carddataByte);

    byte[] processRemoved = new byte[]{2, -25, 6, 0, 4, 0, 0, -10, -19, -61, -105, 3};
    processor.handleCardRemoved(processRemoved);

    Assert.assertEquals("63213", processor.getECardNo());

    Assert.assertEquals(17886000, processor.getCheckTime().longValue());
    Assert.assertEquals(17886000, processor.getClearTime().longValue());
    Assert.assertEquals(7162000, processor.getStartTime().longValue());
    Assert.assertEquals(9004000, processor.getFinishTime().longValue());

    Assert.assertEquals(9, processor.getControlData().size());

    Assert.assertEquals("34", processor.getControlData().get(0).getControlNo());
    Assert.assertEquals("52", processor.getControlData().get(1).getControlNo());
    Assert.assertEquals("54", processor.getControlData().get(2).getControlNo());
    Assert.assertEquals("88", processor.getControlData().get(3).getControlNo());
    Assert.assertEquals("90", processor.getControlData().get(4).getControlNo());
    Assert.assertEquals("96", processor.getControlData().get(5).getControlNo());
    Assert.assertEquals("95", processor.getControlData().get(6).getControlNo());
    Assert.assertEquals("99", processor.getControlData().get(7).getControlNo());
    Assert.assertEquals("100", processor.getControlData().get(8).getControlNo());
  }

  @Test
  public void testHighNumberCard() throws DownloadException, IOException {
    FMilaSerialPort testPort = new FMilaSerialTestPort();

    GregorianCalendar greg = new GregorianCalendar(2012, 1, 1, 12, 30, 00);
    SICardV5Processor processor = new SICardV5Processor(testPort, greg.getTime(), Mockito.mock(DownloadCallback.class));

    byte[] insertedData = new byte[]{2, -27, 6, 0, 3, 0, 3, 126, 116, 81, -112, 3};
    processor.handleCardInserted(insertedData);

    byte[] processData = new byte[]{63, 41, 0, 1, 126, 116, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 101, 126, 116, -18, -18, -18, -18, 1, 86, -18, -18, 40, 3, -11, 0, 7, 0, 0, -18, -18, 0, -18, -18, 0, -18, -18, 0, -18, -18, 0, -18, -18, 0, 0, -18, -18, 0, -18, -18, 0, -18, -18, 0, -18, -18, 0, -18, -18, 0, 0, -18, -18, 0, -18, -18, 0, -18, -18, 0, -18, -18, 0, -18, -18, 0, 0, -18, -18, 0, -18, -18, 0, -18, -18, 0, -18, -18, 0, -18, -18, 0, 0, -18, -18, 0, -18, -18, 0, -18, -18, 0, -18, -18, 0, -18, -18, 0, 0, -18, -18, 0, -18, -18, 0, -18, -18, 0, -18, -18, 0, -18, -18};
    processor.handleProcessData(processData);

    byte[] processRemoved = new byte[]{2, -25, 6, 0, 4, 0, 0, -10, -19, -61, -105, 3};
    processor.handleCardRemoved(processRemoved);

    Assert.assertEquals("332372", processor.getECardNo());
  }

}
