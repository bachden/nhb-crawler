package nhb.utils.webview.headless.impl;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;

import com.nhb.common.Loggable;

import nhb.utils.webview.headless.HeadlessWebviewCallback;
import nhb.utils.webview.headless.exception.HttpAsyncRequestCancelledException;

public class BasicAsyncHeadlessWebview extends AbstractHeadlessWebview {

	private static final class AsyncHttpCallback implements FutureCallback<HttpResponse>, Loggable {

		private final BasicAsyncHeadlessWebview webView;
		private final HttpClientContext context;
		private final HeadlessWebviewCallback callback;

		private AsyncHttpCallback(BasicAsyncHeadlessWebview webView, HttpClientContext context,
				HeadlessWebviewCallback callback) {
			this.webView = webView;
			this.context = context;
			this.callback = callback;
		}

		@Override
		public void failed(Exception ex) {
			if (this.callback != null) {
				this.callback.onFailure(context, ex);
			}
		}

		@Override
		public void completed(HttpResponse response) {
			if (this.webView != null) {
				this.webView.processResponse(context, response, callback);
			}
		}

		@Override
		public void cancelled() {
			if (this.callback != null) {
				this.callback.onFailure(context, new HttpAsyncRequestCancelledException());
			}
		}
	}

	private final Object monitorObject = new Object();
	private CloseableHttpAsyncClient httpAsyncClient;

	public BasicAsyncHeadlessWebview() {
		this(new BasicCookieStore());
	}

	public BasicAsyncHeadlessWebview(CookieStore cookieStore) {
		super(cookieStore);

	}

	public BasicAsyncHeadlessWebview(CookieStore cookieStore, String userAgent) {
		super(cookieStore);
		this.setUserAgent(userAgent);
	}

	public BasicAsyncHeadlessWebview(String userAgent) {
		super(new BasicCookieStore());
		this.setUserAgent(userAgent);
	}

	protected CloseableHttpAsyncClient getHttpAsyncClient() {
		if (httpAsyncClient == null) {
			synchronized (monitorObject) {
				if (this.httpAsyncClient == null) {
					HttpAsyncClientBuilder builder = HttpAsyncClientBuilder.create()
							.setRedirectStrategy(new LaxRedirectStrategy());
					if (this.getUserAgent() != null) {
						builder.setUserAgent(this.getUserAgent());
					}
					this.httpAsyncClient = builder.build();
				}
			}
		}
		return httpAsyncClient;
	}

	@Override
	public void setUserAgent(String userAgent) {
		synchronized (monitorObject) {
			super.setUserAgent(userAgent);
			if (this.httpAsyncClient != null) {
				try {
					this.httpAsyncClient.close();
				} catch (IOException e) {
					getLogger().error("Error while closing old http async client", e);
				}
				this.httpAsyncClient = null;
			}
		}
	}

	@Override
	public void load(final String uri, HeadlessWebviewCallback callback) {
		RequestBuilder rb = RequestBuilder.get().setUri(uri);
		this.submit(rb.build(), callback);
	}

	@Override
	public void close() throws IOException {
		if (this.getHttpAsyncClient().isRunning()) {
			synchronized (this.getHttpAsyncClient()) {
				if (this.getHttpAsyncClient().isRunning()) {
					this.getHttpAsyncClient().close();
				}
			}
		}
	}

	@Override
	public void ajax(HttpUriRequest request, HeadlessWebviewCallback callback) {
		HttpClientContext context = new HttpClientContext();
		context.setCookieStore(this.getCookieStore());

		if (!this.getHttpAsyncClient().isRunning()) {
			synchronized (this.getHttpAsyncClient()) {
				if (!this.getHttpAsyncClient().isRunning()) {
					this.getHttpAsyncClient().start();
				}
			}
		}

		this.getHttpAsyncClient().execute(request, context, new FutureCallback<HttpResponse>() {

			@Override
			public void failed(Exception ex) {
				if (callback != null) {
					callback.onFailure(context, ex);
				}
			}

			@Override
			public void completed(HttpResponse result) {
				if (callback != null) {
					callback.onComplete(context, result);
				}
			}

			@Override
			public void cancelled() {
				if (callback != null) {
					callback.onFailure(context, new HttpAsyncRequestCancelledException());
				}
			}
		});
	}

	@Override
	public void submit(HttpUriRequest request, HeadlessWebviewCallback callback) {

		HttpClientContext context = new HttpClientContext();
		context.setCookieStore(this.getCookieStore());

		if (!this.getHttpAsyncClient().isRunning()) {
			synchronized (this.getHttpAsyncClient()) {
				if (!this.getHttpAsyncClient().isRunning()) {
					this.getHttpAsyncClient().start();
				}
			}
		}

		this.getHttpAsyncClient().execute(request, context, new AsyncHttpCallback(this, context, callback));
	}

}
