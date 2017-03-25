package nhb.utils.phantomjs.downloader;

import java.io.File;

import com.nhb.common.Loggable;

import nhb.utils.phantomjs.utils.DownloadHelper;

public interface FileDownloader extends Loggable {

	String getDownloadURL();

	String getDownloadedFileName();

	File getDownloadLocation();

	default File download() {
		return DownloadHelper.download(getDownloadURL(), getDownloadLocation(), getDownloadedFileName());
	}
}
