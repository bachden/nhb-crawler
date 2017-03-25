package nhb.utils.phantomjs.statics;

import nhb.utils.SystemChecker;

public interface C {

	String PHANTOMJS = "phantomjs";
	String WHERE_CMD = "where";
	String WHICH_CMD = "which";

	static enum OS {
		WINDOWS, MACOS, LINUX;
	}

	static class DefaultPlatformExecutableExtension {
		static final String WINDOWS = ".exe";
		static final String LINUX = "";
		static final String MACOS = "";

		public static String getExtensionForCurrentOS() {
			return getExtensionForOS(SystemChecker.getCurrentOS());
		}

		public static String getExtensionForOS(OS os) {
			switch (os) {
			case WINDOWS:
				return WINDOWS;
			case LINUX:
				return LINUX;
			case MACOS:
				return MACOS;
			}
			return null;
		}
	}
}
