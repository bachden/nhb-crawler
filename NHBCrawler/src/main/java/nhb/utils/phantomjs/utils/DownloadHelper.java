package nhb.utils.phantomjs.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import nhb.utils.phantomjs.exception.DownloadException;
import nhb.utils.phantomjs.exception.InvalidFileException;

public class DownloadHelper {

	private static final Logger logger = LoggerFactory.getLogger(DownloadHelper.class);

	private static class UrlFileVO {
		private File file;
		private String url;

		public UrlFileVO(String url, File file) {
			this.url = url;
			this.file = file;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj != null && obj instanceof UrlFileVO) {
				UrlFileVO other = (UrlFileVO) obj;
				if (this.file == null) {
					if (other.file != null) {
						return false;
					}
				} else {
					if ((other.file == null) || !this.file.getAbsolutePath().equals(other.file.getAbsolutePath())) {
						return false;
					}
				}
				if (this.url == null) {
					if (other.url != null) {
						return false;
					}
				} else {
					if ((other.url == null) || !this.url.equals(other.url)) {
						return false;
					}
				}
				return true;
			}
			return false;
		}
	}

	private static final Map<UrlFileVO, Lock> downloadUrlToFileLockMap = new ConcurrentHashMap<>();

	private static final Lock getUrlToFileLock(String url, File file) {
		if (file == null) {
			throw new NullPointerException("File cannot be null");
		}
		UrlFileVO keyVO = new UrlFileVO(url, file);
		if (!downloadUrlToFileLockMap.containsKey(keyVO)) {
			synchronized (downloadUrlToFileLockMap) {
				if (!downloadUrlToFileLockMap.containsKey(keyVO)) {
					downloadUrlToFileLockMap.put(keyVO, new ReentrantLock());
				}
			}
		}
		return downloadUrlToFileLockMap.get(keyVO);
	}

	public static File download(String downloadUrl, File folder, String fileName) {

		if (folder == null) {
			throw new NullPointerException("Download location cannot be null");
		}

		if (folder.exists()) {
			if (folder.isFile()) {
				throw new InvalidFileException(
						"Location to save downloaded file already existing as a file, cannot be overrided");
			} else {
				// do nothing, it's existing folder and ok to download
			}
		} else {
			try {
				Files.createDirectories(folder.toPath());
			} catch (IOException e) {
				throw new InvalidFileException("Cannot create parent directory to downloaded file");
			}
		}

		File savedFile = new File(folder.getAbsoluteFile() + File.separator + fileName);

		URL url;
		try {
			url = new URL(downloadUrl);
		} catch (MalformedURLException e) {
			throw new DownloadException("Invalid download url: " + downloadUrl, e);
		}

		logger.debug("Download '{}' from url '{}' and save to '{}'", fileName, url, savedFile.getAbsolutePath());

		Lock lock = getUrlToFileLock(null, savedFile);

		boolean locked = lock.tryLock();

		if (!locked) {
			try {
				locked = lock.tryLock(15, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				throw new DownloadException("Waiting for lock has failed");
			}
		}

		if (locked) {
			if (savedFile.exists()) {
				logger.info("File to be downloaded has been completed on other progress, ignore downloading");
				return savedFile;
			}
			try (ReadableByteChannel rbc = Channels.newChannel(url.openStream());
					FileOutputStream fos = new FileOutputStream(savedFile)) {
				fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
				logger.info("Download complete!!!");
			} catch (Exception e) {
				throw new DownloadException("Download file error", e);
			} finally {
				lock.unlock();
			}
		} else {
			throw new RuntimeException("Cannot wait for old progress to be completed", new TimeoutException());
		}

		return savedFile;
	}

	public static void downloadAndUnzip(String downloadUrl, File folder, String fileName) {
		File downloadedFile = download(downloadUrl, folder, fileName);
		try {
			ZipFile zipFile = new ZipFile(downloadedFile);
			zipFile.extractAll(folder.getAbsolutePath());
		} catch (ZipException e) {
			throw new RuntimeException("Error while unzip downloaded file", e);
		}
	}
}
