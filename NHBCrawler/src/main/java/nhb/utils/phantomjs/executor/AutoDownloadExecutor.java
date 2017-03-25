package nhb.utils.phantomjs.executor;

import java.io.File;

import nhb.utils.phantomjs.downloader.ExecutableFileDownloader;

public interface AutoDownloadExecutor extends ExecutableFileDownloader, FileCachable, ConcreteCommandExecutor {

	@Override
	default File getExecutor() {
		File executor = ConcreteCommandExecutor.super.getExecutor();
		if (executor == null) {
			executor = this.download();
		}
		return executor;
	}
}
