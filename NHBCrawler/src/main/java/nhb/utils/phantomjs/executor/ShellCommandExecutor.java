package nhb.utils.phantomjs.executor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.CountDownLatch;

import com.nhb.common.Loggable;

import nhb.utils.phantomjs.exception.ExecuteCommandError;

public interface ShellCommandExecutor extends Loggable {

	default void shutdown() {
		// do nothing...
	}

	default Process executeSystemCommand(String... args) throws IOException {
		if (args != null) {
			return Runtime.getRuntime().exec(args);
		}
		return null;
	}

	default String executeAndWatchSystemCommand(String... args) throws ExecuteCommandError {
		try {
			final Process process = this.executeSystemCommand(args);
			ExecuteCommandError[] error = new ExecuteCommandError[1];
			StringBuilder errorSB = new StringBuilder();
			StringBuilder stdOutSB = new StringBuilder();
			if (process != null) {
				CountDownLatch doneSignal = new CountDownLatch(1);
				Thread watcher = new Thread() {
					public void run() {
						final BufferedReader stdOutReader = new BufferedReader(
								new InputStreamReader(process.getInputStream()));
						final BufferedReader errorReader = new BufferedReader(
								new InputStreamReader(process.getErrorStream()));
						try {
							while (true) {
								boolean tobeContinous = false;
								String line = stdOutReader.readLine();
								if (line != null) {
									getLogger().debug("***** Console log: {}", line);
									stdOutSB.append(line).append("\n");
									tobeContinous = true;
								}
								line = errorReader.readLine();
								if (line != null) {
									getLogger().debug("****** Console error: {}", line);
									errorSB.append(line).append("\n");
									tobeContinous = true;
								}
								if (!tobeContinous) {
									break;
								}
							}
						} catch (IOException e) {
							error[0] = new ExecuteCommandError("Error while reading stdOutStream nor errorStream", e);
						}
						doneSignal.countDown();
					};
				};
				watcher.start();
				process.waitFor();
				doneSignal.await();
				if (error[0] != null) {
					throw error[0];
				} else if (errorSB.length() > 0) {
					throw new ExecuteCommandError(errorSB.toString());
				}
				return stdOutSB.toString().trim();
			}
		} catch (IOException | InterruptedException e) {
			throw new ExecuteCommandError("Error while execute command", e);
		}

		return null;
	}
}
