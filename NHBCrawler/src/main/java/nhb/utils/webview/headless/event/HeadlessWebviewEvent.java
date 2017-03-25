package nhb.utils.webview.headless.event;

import org.apache.http.client.protocol.HttpClientContext;

import com.nhb.eventdriven.impl.AbstractEvent;

import lombok.Getter;

public class HeadlessWebviewEvent extends AbstractEvent {

	public static final String WEBVIEW_LOADING_CANCELED = "webviewLoadingCanceled";
	public static final String WEBVIEW_LOADING_COMPLETE = "webviewLoadingComplete";
	public static final String WEBVIEW_LOADING_FAILED = "webviewLoadingFailed";

	@Getter
	private Throwable failedCause;

	@Getter
	private HttpClientContext httpContext;

	public static final HeadlessWebviewEvent createCompleteEvent(HttpClientContext context) {
		HeadlessWebviewEvent event = new HeadlessWebviewEvent();
		event.setType(WEBVIEW_LOADING_COMPLETE);
		event.httpContext = context;
		return event;
	}

	public static final HeadlessWebviewEvent createCanceledEvent(HttpClientContext context) {
		HeadlessWebviewEvent event = new HeadlessWebviewEvent();
		event.setType(WEBVIEW_LOADING_CANCELED);
		event.httpContext = context;
		return event;
	}

	public static final HeadlessWebviewEvent createFailedEvent(HttpClientContext context, Throwable failedCause) {
		HeadlessWebviewEvent event = new HeadlessWebviewEvent();
		event.setType(WEBVIEW_LOADING_FAILED);
		event.failedCause = failedCause;
		event.httpContext = context;
		return event;
	}
}
