package com.ecollege.android.api;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

import android.util.Log;

import com.ecollege.api.exceptions.*;
import com.ecollege.api.AbstractHttpClient;

public class AndroidHttpClient extends AbstractHttpClient {

	private static final String TAG = "AndroidHttpClient";
	private static final String USER_AGENT = "eCollege Android Client";

	@Override
	public String get(String url, Map<String, String> headers, Map<String, String> params) {
		HttpGet get = new HttpGet(buildUri(url, params));
		String response = execute(get);
		return response;
	}

	@Override
	public String post(String url, Map<String, String> headers, Map<String, String> params) {
		HttpPost post = new HttpPost(url);
		setParameters(post, params);
		String response = execute(post);
		return response;
	}

	/**
	 * This guy is called for GET and DELETE
	 * @param method
	 * @param parameterMap
	 * @throws Exception
	 */
	private URI buildUri(String url, Map<String, String> paramMap) throws ServiceException {
		try {
			if (null == paramMap) {
				return new URI(url);
			}
			StringBuilder newUrl = new StringBuilder(url + "?");
			String value;
			for (String key : paramMap.keySet()) {
				value = paramMap.get(key);
				newUrl.append(key + "=" + URLEncoder.encode(value.toString(), "UTF-8") + "&");
			}
			return new URI(newUrl.toString());
		} catch (Exception e) {
			Log.e(getClass().getName(), "problem setting params", e);
			throw new ServiceException("unable to set parameters.", e);
		}
	}

	/**
	 * This guy is called for POST and PUT methods
	 * @param method
	 * @param parameterMap
	 * @throws Exception
	 */
	private void setParameters(HttpEntityEnclosingRequestBase method, Map<String, String> parameterMap) throws ServiceException {
		try {
			Log.d(TAG, "Setting params to: ");
			ArrayList<NameValuePair> nvps = new ArrayList<NameValuePair>();
			if (null == parameterMap) {
				return;
			}
			String value;
			for (String key : parameterMap.keySet()) {
				value = parameterMap.get(key);
				nvps.add(new BasicNameValuePair(key, value));
			}
			method.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
			
		} catch (Exception e) {
			Log.e(getClass().getName(), "problem setting params", e);
			throw new ServiceException("unable to set parameters.", e);
		}
	}
	
	private String execute(HttpRequestBase method) throws ServiceException {
		HttpContext localContext = new BasicHttpContext();
		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		CookieStore cookieStore = new BasicCookieStore();
		localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpProtocolParams.setUserAgent(httpClient.getParams(), USER_AGENT);
		HttpProtocolParams.setContentCharset(httpClient.getParams(), "UTF-8");
		HttpProtocolParams.setHttpElementCharset(httpClient.getParams(), "UTF-8");
		
		try {
			Log.d(TAG, "Calling: " + method.getRequestLine().toString());
			String response = httpClient.execute(method, responseHandler, localContext);
			return response;
		} catch (HttpResponseException httpre) {
			if (httpre.getStatusCode() == 422) {
				Log.e(getClass().getName(), "got 422", httpre);
				ValidationException ve = new ValidationException(httpre);
				throw ve;
			} else if (httpre.getStatusCode() == 401) {
				Log.e(getClass().getName(), "got 401", httpre);
				UnauthorizedException ue = new UnauthorizedException(httpre);
				throw ue;
			} else if (httpre.getStatusCode() == 404) {
				Log.e(getClass().getName(), "got 404", httpre);
				NotFoundException nfe = new NotFoundException(httpre);
				throw nfe;
			}
			Log.e(getClass().getName(), "got http response exception", httpre);

			throw new ServiceException(httpre);
		} catch (ClientProtocolException cpe) {
			Log.e(getClass().getName(), "client protocol exception", cpe);
			throw new ServiceException(cpe);
		} catch (SocketException se) {
			Log.e(getClass().getName(), "socket exception", se);
			// TODO it seems to be the case that timeouts always
			// end up as a socket instead of socket timeout exception.
			// Research this
			throw new TimeoutException(se);
		} catch (SocketTimeoutException stoe) {
			Log.e(getClass().getName(), "socket timeout exception", stoe);
			throw new TimeoutException(stoe);
		} catch (IOException ioe) {
			Log.e(getClass().getName(), "IO exception", ioe);
			throw new ServiceException(ioe);
		}
	}
}
