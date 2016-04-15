package io.dropwizard.metrics.httpclient;

import java.io.IOException;

import org.apache.http.HttpClientConnection;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;

public interface HttpExecutor {

	public abstract HttpResponse execute(HttpRequest request,
			HttpClientConnection conn, HttpContext context)
			throws HttpException, IOException;

}