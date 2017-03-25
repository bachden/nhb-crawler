package nhb.utils.phantomjs.exception;

public class ExecuteCommandError extends Exception {

	private static final long serialVersionUID = 1L;

	public ExecuteCommandError() {
		super();
	}

	public ExecuteCommandError(String message) {
		super(message);
	}

	public ExecuteCommandError(String message, Throwable cause) {
		super(message, cause);
	}
}
