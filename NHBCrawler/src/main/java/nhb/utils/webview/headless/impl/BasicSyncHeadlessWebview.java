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

	private final Object monitorObject = new Object();
	private CloseableHttpClient httpClient;

	public BasicSyncHeadlessWebview(CookieStore cookieStore) {
		super(cookieStore);
	}

	public BasicSyncHeadlessWebview() {
		this(new BasicCookieStore());
	}

	public BasicSyncHeadlessWebview(String userAgent) {
		this(new BasicCookieStore());
		this.setUserAgent(userAgent);
	}

	public BasicSyncHeadlessWebview(CookieStore cookieStore, String userAgent) {
		this(cookieStore);
		this.setUserAgent(userAgent);
	}

	@Override
	public void setUserAgent(String userAgent) {
		synchronized (monitorObject) {
			super.setUserAgent(userAgent);
			if (this.httpClient != null) {
				try {
					this.httpClient.close();
				} catch (IOException e) {
					getLogger().error("Error while closing old http client", e);
				}
				this.httpClient = null;
			}
		}
	}

	protected CloseableHttpClient getHttpClient() {
		if (this.httpClient == null) {
			synchronized (monitorObject) {
				if (this.httpClient == null) {
					HttpClientBuilder builder = HttpClientBuilder.create()
							.setRedirectStrategy(new LaxRedirectStrategy());
					if (this.getUserAgent() != null) {
						builder.setUserAgent(this.getUserAgent());
					}
					this.httpClient = builder.build();
				}
			}
		}
		return httpClient;
	}

	@Override
	public void load(final String uri, HeadlessWebviewCallback callback) {
		RequestBuilder rb = RequestBuilder.get().setUri(uri);
		this.submit(rb.build(), callback);
	}

	@Override
	public void close() throws IOException {
		if (this.httpClient != null) {
			this.httpClient.close();
		}
	}

	@Override
	public void ajax(HttpUriRequest request, HeadlessWebviewCallback callback) {
		HttpClientContext context = new HttpClientContext();
		context.setCookieStore(this.getCookieStore());

		try {
			HttpResponse response = this.getHttpClient().execute(request, context);
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
			HttpResponse response = this.getHttpClient().execute(request, context);
			this.processResponse(context, response, callback);
		} catch (IOException e) {
			if (callback != null) {
				callback.onFailure(context, e);
			}
		}
	}

}
