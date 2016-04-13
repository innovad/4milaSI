package com.fmila.sportident;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fmila.sportident.serial.FMilaSerialPort;
import com.fmila.sportident.serial.SerialLibrary;
import com.fmila.sportident.serial.SerialUtility;
import com.fmila.sportident.util.FMilaUtility;
import com.fmila.sportident.util.FMilaUtility.Architecture;
import com.fmila.sportident.util.TestUtility;

public class SerialUtilityTest {

  private static SerialLibrary defaultLibrary;

  @BeforeClass
  public static void beforeClass() {
    defaultLibrary = SerialUtility.getLibrary();
  }

  @Test
  public void testPure() throws Exception {
    SerialUtility.setLibrary(SerialLibrary.PURE);
    assertNotNull(SerialUtility.getPorts());
  }

  @Test
  public void testRxTx() throws Exception {
    SerialUtility.setLibrary(SerialLibrary.RXTX);
    assertNotNull(SerialUtility.getPorts());
  }

  @Test
  public void testJssc() throws Exception {
    SerialUtility.setLibrary(SerialLibrary.JSSC);
    assertNotNull(SerialUtility.getPorts());
  }

  @Test(expected = IOException.class)
  public void testPureOpenNA() throws Exception {
    SerialUtility.setLibrary(SerialLibrary.PURE);
    SerialUtility.getPort(38400, "NOT AVAILABLE");
  }

  @Test(expected = IOException.class)
  public void testRxTxOpenNA() throws Exception {
    SerialUtility.setLibrary(SerialLibrary.RXTX);
    SerialUtility.getPort(38400, "NOT AVAILABLE");
  }

  @Test(expected = IOException.class)
  public void testJsscOpenNA() throws Exception {
    Assume.assumeTrue(FMilaUtility.getArchitecture().equals(Architecture.WIN32));
    SerialUtility.setLibrary(SerialLibrary.JSSC);
    SerialUtility.getPort(38400, "NOT AVAILABLE");
  }

  @Test
  public void testPureOpen() throws Exception {
    Assume.assumeTrue(FMilaUtility.getArchitecture().equals(Architecture.MACOSX));
    SerialUtility.setLibrary(SerialLibrary.PURE);
    FMilaSerialPort port = SerialUtility.getPort(38400, TestUtility.getSerialTestingPort());
    port.close();
  }

  @Test
  public void testRxTxOpen() throws Exception {
    Assume.assumeTrue(FMilaUtility.getArchitecture().equals(Architecture.WIN32));
    SerialUtility.setLibrary(SerialLibrary.RXTX);
    FMilaSerialPort port = SerialUtility.getPort(38400, TestUtility.getSerialTestingPort());
    port.close();
  }

  @Test
  public void testJsscOpen() throws Exception {
    Assume.assumeTrue(FMilaUtility.getArchitecture().equals(Architecture.WIN32));
    SerialUtility.setLibrary(SerialLibrary.JSSC);
    FMilaSerialPort port = SerialUtility.getPort(38400, TestUtility.getSerialTestingPort());
    port.close();
  }

  @AfterClass
  public static void afterClass() {
    SerialUtility.setLibrary(defaultLibrary);
  }

}
