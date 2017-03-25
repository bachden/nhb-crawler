package nhb.utils.phantomjs.executor;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nhb.utils.SystemChecker;
import nhb.utils.phantomjs.exception.ExecuteCommandError;

public interface ConcreteCommandExecutor extends ShellCommandExecutor {

	String getExecutorName();

	default File getExecutor() {
		File file = null;
		if (this instanceof FileCachable) {
			file = ((FileCachable) this).getCachedFile();
			if (file != null) {
				getLogger().info("Found cached file '{}', ignore checking from system path", file.getAbsolutePath());
				return file;
			}
		}

		file = SystemChecker.checkInstalledCommand(getExecutorName());
		if ((file != null) && (this instanceof FileCachable)) {
			getLogger().info("Cache system installed command executable file: {}", file);
			((FileCachable) this).cacheFile(file);
		}
		return file;
	}

	default String execute(String... params) throws ExecuteCommandError {
		List<String> commands = new ArrayList<>();
		commands.add(this.getExecutor().getAbsolutePath());
		commands.addAll(Arrays.asList(params));
		getLogger().debug("Executing command array: {}", commands);
		return this.executeAndWatchSystemCommand(commands.toArray(new String[0]));
	}
}
