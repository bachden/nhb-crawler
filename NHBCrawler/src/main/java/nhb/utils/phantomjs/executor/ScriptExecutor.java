package nhb.utils.phantomjs.executor;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nhb.utils.phantomjs.exception.ExecuteCommandError;

public interface ScriptExecutor extends ConcreteCommandExecutor {

	default String execute(File scriptFile, String... params) throws ExecuteCommandError {
		List<String> commands = new ArrayList<>();
		commands.add(scriptFile.getAbsolutePath());
		commands.addAll(Arrays.asList(params));
		String[] commandArr = commands.toArray(new String[0]);
		return this.execute(commandArr);
	}
}
