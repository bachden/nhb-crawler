package nhb.utils.webview.headless;

import java.io.Closeable;

import org.apache.http.HttpHost;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;

public interface HeadlessWebview extends Closeable {

	void setParsePageSource(boolean parsePageSource);

	boolean getParsePageSource();

	void setUserAgent(String userAgent);

	String getUserAgent();

	CookieStore getCookieStore();

	/**
	 * open an url
	 * 
	 * @param url
	 * @param callback
	 */
	void load(String url, HeadlessWebviewCallback callback);

	/**
	 * use for submit form and change url
	 * 
	 * @param request
	 * @param callback
	 */
	void submit(HttpUriRequest request, HeadlessWebviewCallback callback);

	/**
	 * use for simulate ajax call, donot change url
	 * 
	 * @param request
	 * @param callback
	 */
	void ajax(HttpUriRequest request, HeadlessWebviewCallback callback);

	String getUri();

	String getTargetHost();

	default void reload(HeadlessWebviewCallback callback) {
		this.load(this.getUri(), callback);
	}

	String getPageSource();

	public static String extractUri(HttpClientContext context) {
		HttpUriRequest currentReq = (HttpUriRequest) context.getRequest();
		HttpHost currentHost = context.getTargetHost();
		String currentUrl = (currentReq.getURI().isAbsolute()) ? currentReq.getURI().toString()
				: (currentHost.toURI() + currentReq.getURI());
		return currentUrl;
	}
}
