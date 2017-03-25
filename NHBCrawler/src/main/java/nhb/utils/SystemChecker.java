package nhb.utils;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nhb.utils.phantomjs.exception.ExecuteCommandError;
import nhb.utils.phantomjs.executor.ShellCommandExecutor;
import nhb.utils.phantomjs.statics.C;
import nhb.utils.phantomjs.statics.C.OS;

public final class SystemChecker {

	private static final Logger logger = LoggerFactory.getLogger(SystemChecker.class);

	private static ShellCommandExecutor shellCommandExecutor = new ShellCommandExecutor() {
	};

	public static File checkInstalledCommand(String commandName) {
		if (commandName == null) {
			throw new NullPointerException("Application name to check for installed cannot be null");
		}
		try {
			final OS os = getCurrentOS();
			final String command = os == OS.WINDOWS ? C.WHERE_CMD : C.WHICH_CMD;
			String path = shellCommandExecutor.executeAndWatchSystemCommand(command, commandName);
			if (path != null && !path.isEmpty()) {
				return new File(path);
			} else {
				logger.debug("Installed application for name '{}' cannot be found in system path", commandName);
				return null;
			}
		} catch (ExecuteCommandError e) {
			logger.error("Error while checking installed phantomjs in system path", e);
			throw new RuntimeException(e);
		}
	}

	public static C.OS getCurrentOS() {
		final String os = System.getProperty("os.name").toLowerCase();
		if (os.contains("win")) {
			return OS.WINDOWS;
		} else if (os.contains("mac")) {
			return OS.MACOS;
		} else {
			return OS.LINUX;
		}
	}
}
