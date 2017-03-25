package nhb.utils.phantomjs.executor.phantomjs;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.nhb.common.utils.FileSystemUtils;

import nhb.utils.phantomjs.exception.ExecuteCommandError;
import nhb.utils.phantomjs.executor.AutoDownloadExecutor;
import nhb.utils.phantomjs.executor.InitableExecutor;
import nhb.utils.phantomjs.statics.C;
import nhb.utils.phantomjs.utils.DownloadHelper;
import nhb.utils.phantomjs.utils.StringTemplate;
import nhb.utils.phantomjs.utils.SystemChecker;

public class DefaultPhantomJsExecutor implements PhantomJsExecutor, AutoDownloadExecutor, InitableExecutor {

	private static final String BIN = "bin";
	private static final String CASPERJS_JS = "casperjs.js";
	private static final String CASPERJS_ZIP = "casperjs.zip";

	private String phantomJsVersion = "2.1.1";
	private String phantomJsDownloadLocation = "resources/executable/phantomjs/bin";
	private String phantomJsDownloadUrlTemplate = "http://download.puppetserver.com/executable/phantomjs/{{version}}/{{os}}/phantomjs{{ext}}";

	private String casperJsVersion = "1.1.4-patched";
	private String casperJsDownloadLocation = "resources/executable/casperjs";
	private String casperJsUrlTemplate = "http://download.puppetserver.com/executable/casperjs/{{version}}/casperjs.zip";

	public DefaultPhantomJsExecutor() {
		// do nothing...
	}

	public DefaultPhantomJsExecutor(Properties props) {
		this();
		this.init(props);
	}

	@Override
	public void init(Properties props) {
		if (props != null) {
			this.phantomJsVersion = props.getProperty("version", this.phantomJsVersion);
			this.phantomJsDownloadLocation = props.getProperty("downloadLocation", this.phantomJsDownloadLocation);
			this.phantomJsDownloadUrlTemplate = props.getProperty("downloadUrlTemplate",
					this.phantomJsDownloadUrlTemplate);
		}
	}

	@Override
	public final String getDownloadURL() {
		String os = SystemChecker.getCurrentOS().name().toLowerCase();
		String extension = C.DefaultPlatformExecutableExtension.getExtensionForCurrentOS();

		Map<String, String> params = new HashMap<>();
		params.put("os", os);
		params.put("ext", extension);
		params.put("version", this.phantomJsVersion);

		return StringTemplate.replace(phantomJsDownloadUrlTemplate, params);
	}

	@Override
	public File getDownloadLocation() {
		return new File(this.phantomJsDownloadLocation);
	}

	@Override
	public final String getDownloadedFileName() {
		return PHANTOMJS;
	}

	// cache downloaded file
	private File phantomjsExecutableFile;

	@Override
	public void cacheFile(File file) {
		this.phantomjsExecutableFile = file;
	}

	@Override
	public File getCachedFile() {
		return this.phantomjsExecutableFile;
	}

	@Override
	public String execute(File scriptFile, String... params) throws ExecuteCommandError {
		if (this.useCasperJS) {
			List<String> args = new ArrayList<>();
			File casperJsFile = this.getCasperJsFile();
			if (casperJsFile == null) {
				throw new NullPointerException("CasperJS file cannot be found, download may be failed");
			}
			args.add(casperJsFile.getAbsolutePath());
			args.add(scriptFile.getAbsolutePath());
			if (params != null) {
				args.addAll(Arrays.asList(params));
			}
			return this.execute(args.toArray(new String[0]));
		}
		return PhantomJsExecutor.super.execute(scriptFile, params);
	}

	private boolean useCasperJS;
	private File casperJsFile = null;

	@Override
	public void setUseCasperJS(boolean useCasperJS) {
		this.useCasperJS = useCasperJS;
	}

	private void downloadCasperJsDependencies(File destinationFolder) {
		Map<String, String> templateParams = new HashMap<>();
		templateParams.put("version", this.casperJsVersion);
		String casperjsZipDownloadUrl = StringTemplate.replace(casperJsUrlTemplate, templateParams);
		DownloadHelper.downloadAndUnzip(casperjsZipDownloadUrl, destinationFolder, CASPERJS_ZIP);
	}

	private File getCasperJsFile() {
		if (this.casperJsFile == null) {
			synchronized (this) {
				if (this.casperJsFile == null) {
					File folder = new File(casperJsDownloadLocation);
					String folderPath = folder.getAbsolutePath();
					String casperJsFilePath = FileSystemUtils.createPathFrom(folderPath, BIN, CASPERJS_JS);
					this.casperJsFile = new File(casperJsFilePath);

					if (!this.casperJsFile.exists()) {
						downloadCasperJsDependencies(folder);
					}
				}
			}
		}
		return this.casperJsFile;
	}
}
