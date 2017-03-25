package nhb.utils.webview.headless;

import org.apache.http.HttpResponse;
import org.apache.http.client.protocol.HttpClientContext;

public interface HeadlessWebviewCallback {

	void onComplete(HttpClientContext httpContext, HttpResponse response);

	void onFailure(HttpClientContext httpContext, Exception cause);

}
