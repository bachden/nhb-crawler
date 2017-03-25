package nhb.utils.webview.headless.impl;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;

import nhb.utils.webview.headless.HeadlessWebviewCallback;

public class BasicSyncHeadlessWebview extends AbstractHeadlessWebview {

	private final CloseableHttpClient httpClient;

	public BasicSyncHeadlessWebview() {
		this(new BasicCookieStore());
	}

	public BasicSyncHeadlessWebview(CookieStore cookieStore) {
		super(cookieStore);
		this.httpClient = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build();
	}

	@Override
	public void load(final String uri, HeadlessWebviewCallback callback) {
		RequestBuilder rb = RequestBuilder.get().setUri(uri);
		this.submit(rb.build(), callback);
	}

	@Override
	public void close() throws IOException {
		this.httpClient.close();
	}

	@Override
	public void ajax(HttpUriRequest request, HeadlessWebviewCallback callback) {
		HttpClientContext context = new HttpClientContext();
		context.setCookieStore(this.getCookieStore());

		try {
			HttpResponse response = this.httpClient.execute(request, context);
			if (callback != null) {
				callback.onComplete(context, response);
			}
		} catch (IOException e) {
			if (callback != null) {
				callback.onFailure(context, e);
			}
		}
	}

	@Override
	public void submit(HttpUriRequest request, HeadlessWebviewCallback callback) {
		HttpClientContext context = new HttpClientContext();
		context.setCookieStore(this.getCookieStore());

		try {
			HttpResponse response = this.httpClient.execute(request, context);
			this.processResponse(context, response, callback);
		} catch (IOException e) {
			if (callback != null) {
				callback.onFailure(context, e);
			}
		}
	}

}
