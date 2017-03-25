package nhb.utils.webview.headless.impl;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.util.EntityUtils;

import com.nhb.common.Loggable;

import lombok.Getter;
import lombok.Setter;
import nhb.utils.webview.headless.HeadlessWebview;
import nhb.utils.webview.headless.HeadlessWebviewCallback;

public abstract class AbstractHeadlessWebview implements HeadlessWebview, Loggable {

	@Setter
	@Getter
	private String userAgent;

	@Getter
	private final CookieStore cookieStore;

	@Getter
	private String uri;

	@Getter
	private String targetHost;

	private String pageSource;

	private AtomicBoolean parsePageSource = new AtomicBoolean(true);

	protected AbstractHeadlessWebview(CookieStore cookieStore) {
		this.cookieStore = cookieStore;
	}

	protected String extractUri(HttpClientContext context) {
		return HeadlessWebview.extractUri(context);
	}

	@Override
	public void setParsePageSource(boolean parsePageSource) {
		this.parsePageSource.set(parsePageSource);
	}

	@Override
	public boolean getParsePageSource() {
		return this.parsePageSource.get();
	}

	@Override
	public String getPageSource() {
		if (this.parsePageSource.get()) {
			return this.pageSource;
		}
		throw new IllegalStateException("Cannot get page source when 'parsePageSource' is setting to false");
	}

	protected void processResponse(HttpClientContext context, HttpResponse response, HeadlessWebviewCallback callback) {
		this.uri = extractUri(context);
		this.targetHost = context.getTargetHost().toURI();
		try {
			if (this.getParsePageSource()) {
				this.pageSource = EntityUtils.toString(response.getEntity());
			}
			if (callback != null) {
				callback.onComplete(context, response);
			}
		} catch (ParseException | IOException e) {
			if (callback != null) {
				callback.onFailure(context, e);
			}
		}
	}
}
