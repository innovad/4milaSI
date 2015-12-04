package com.fmila.sportident.util;

public class FMilaUtility {

	public static enum OperatingSystem {
	    MACOSX, WINDOWS, LINUX, UNKNWOWN
	  }

	  public static enum Architecture {
	    WIN32 {
	      @Override
	      public String toString() {
	        return "Windows 32bit (Windows XP, Vista, 7)";
	      }
	    },
	    WIN64 {
	      @Override
	      public String toString() {
	        return "Windows 64bit (Windows 7 x64)";
	      }
	    },
	    MACOSX {
	      @Override
	      public String toString() {
	        return "Mac OS X 64bit (10.7, 10.8, 10.9, 10.10)";
	      }
	    },
	    UNKNOWN
	  }

	  public static OperatingSystem getPlatform() {
		String property = System.getProperty("os.name") != null ? System.getProperty("os.name") : "";
	    String osname = property.toLowerCase();

	    if (osname.startsWith("windows")) {
	      return OperatingSystem.WINDOWS;
	    }
	    else if (osname.startsWith("mac")) {
	      return OperatingSystem.MACOSX;
	    }
	    else if (osname.startsWith("linux")) {
	      return OperatingSystem.LINUX;
	    }

	    return OperatingSystem.UNKNWOWN;
	  }

	  public static Architecture getArchitecture() {
	    if (OperatingSystem.MACOSX.equals(getPlatform())) {
	      return Architecture.MACOSX;
	    }
	    String property = System.getProperty("os.arch") != null ? System.getProperty("os.arch") : "";
	    String arch = property.toLowerCase();
	    if (arch.contains("64")) {
	      return Architecture.WIN64;
	    }
	    return Architecture.WIN32;
	  }
	
}
