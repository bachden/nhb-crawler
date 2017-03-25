package nhb.utils.phantomjs.executor.phantomjs;

import nhb.utils.phantomjs.executor.ScriptExecutor;

public interface PhantomJsExecutor extends ScriptExecutor {

	static final String PHANTOMJS = "phantomjs";

	void setUseCasperJS(boolean useCasperJS);

	@Override
	default String getExecutorName() {
		return PHANTOMJS;
	}

	static PhantomJsExecutor newDefault() {
		return new DefaultPhantomJsExecutor();
	}
}
