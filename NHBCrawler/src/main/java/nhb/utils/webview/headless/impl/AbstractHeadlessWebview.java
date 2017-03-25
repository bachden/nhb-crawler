package nhb.utils.webview.headless.impl;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.util.EntityUtils;

import lombok.Getter;
import nhb.utils.webview.headless.HeadlessWebview;
import nhb.utils.webview.headless.HeadlessWebviewCallback;

public abstract class AbstractHeadlessWebview implements HeadlessWebview {

	@Getter
	private final CookieStore cookieStore;

	@Getter
	private String uri;

	@Getter
	private String targetHost;

	@Getter
	private String pageSource;

	protected AbstractHeadlessWebview(CookieStore cookieStore) {
		this.cookieStore = cookieStore;
	}

	protected String extractUri(HttpClientContext context) {
		return HeadlessWebview.extractUri(context);
	}

	protected void processResponse(HttpClientContext context, HttpResponse response, HeadlessWebviewCallback callback) {
		this.uri = extractUri(context);
		this.targetHost = context.getTargetHost().toURI();
		try {
			this.pageSource = EntityUtils.toString(response.getEntity());
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
