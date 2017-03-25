package nhb.utils.phantomjs.downloader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Arrays;
import java.util.HashSet;

import nhb.utils.SystemChecker;
import nhb.utils.phantomjs.exception.MakeExecutableException;
import nhb.utils.phantomjs.executor.FileCachable;
import nhb.utils.phantomjs.statics.C;

public interface ExecutableFileDownloader extends FileDownloader {

	@Override
	default File download() {
		if (this instanceof FileCachable) {
			File file = ((FileCachable) this).getCachedFile();
			if (file != null) {
				getLogger().info("Found cached file at {}, ignore download", file.getAbsolutePath());
				return file;
			}
		}

		File downloadedFile = FileDownloader.super.download();
		C.OS currentOS = SystemChecker.getCurrentOS();
		if (currentOS == C.OS.LINUX || currentOS == C.OS.MACOS) {
			getLogger().info("*nix system detected, changing file '{}' permissions to common mod 755", downloadedFile);
			try {
				Files.setPosixFilePermissions(downloadedFile.toPath(),
						new HashSet<>(Arrays.asList(//
								PosixFilePermission.OWNER_WRITE, //
								PosixFilePermission.OWNER_READ, //
								PosixFilePermission.OWNER_EXECUTE, //
								PosixFilePermission.GROUP_READ, //
								PosixFilePermission.GROUP_EXECUTE, //
								PosixFilePermission.OTHERS_EXECUTE, //
								PosixFilePermission.OTHERS_READ)));
			} catch (IOException e) {
				throw new MakeExecutableException("Cannot make downloaded file as executable", e);
			}
		}

		if (this instanceof FileCachable) {
			getLogger().debug("Cache downloaded file '{}'", downloadedFile);
			((FileCachable) this).cacheFile(downloadedFile);
		}
		return downloadedFile;
	}
}
