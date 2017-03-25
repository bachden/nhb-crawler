package nhb.utils.phantomjs.executor;

import java.io.File;

public interface FileCachable {

	void cacheFile(File file);

	File getCachedFile();
}
