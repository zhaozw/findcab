package com.findcab.driver.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

/**
 * 网络交互
 * 
 * @author zhangkun
 * @date
 */
public class HttpTools {
	public static int responseStatus;

	private static Uri.Builder buildGetMethod(String url,
			Map<String, String> data) {
		final Uri.Builder builder = new Uri.Builder();
		builder.encodedPath(url);

		if (data != null) {
			for (Map.Entry<String, String> m : data.entrySet()) {
				builder.appendQueryParameter(m.getKey(), m.getValue());
			}
		}
		return builder;
	}

	/**
	 * 判断网络是否可用
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity == null) {
			return false;
		} else {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * 先检查网络连接是否连接
	 * 
	 * @return
	 */
	public static boolean checkNetWork(Context context) {
		if (!isNetworkAvailable(context)) {

			// new AlertDialog.Builder(context)
			// .setIcon(android.R.drawable.ic_dialog_alert)
			// .setTitle("网络连接错误").setMessage("请检查网络连接！")
			// .setPositiveButton("确定", null).show();
			// MyUtils.myAlertDialogForNetError(context, "网络链接失败").show();
			return false;
		}
		return true;
	}

	/**
	 * 先检查网络连接是否连接
	 * 
	 * @return
	 */
	public static boolean checkNetWorkAndFinish(final Context context) {
		if (!isNetworkAvailable(context)) {

			// MyUtils.myAlertDialogForNetError(context, "网络链接失败").show();

			return false;
		}
		return true;
	}

	/*
	 * GEt请求
	 */
	public static String GetDate(String url, Map<String, String> data) {

		if (data != null) {
			url = buildGetMethod(url, data).build().toString();
		}

		System.out.println("url----->" + url);
		HttpGet get = new HttpGet(url);
		HttpClient client = new DefaultHttpClient();
		try {
			HttpResponse response = client.execute(get);// 执行Post方法
			String resultString = EntityUtils.toString(response.getEntity());
			System.out.println("resultString--->" + resultString);
			return resultString;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	}

	/*
	 * POST请求
	 */
	public static String PostDate(String url, Map<String, String> data) {
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		if (data != null) {

			NameValuePair pair;
			for (Map.Entry<String, String> m : data.entrySet()) {
				pair = new BasicNameValuePair(m.getKey(), m.getValue());
				list.add(pair);
			}

		}

		try {
			HttpEntity httpEntity = new UrlEncodedFormEntity(list);// 使用编码构建Post实体
			HttpPost post = new HttpPost(url);
			post.setEntity(httpEntity);// 设置Post实体
			HttpClient client = new DefaultHttpClient();
			HttpResponse response = client.execute(post);// 执行Post方法
			String resultString = EntityUtils.toString(response.getEntity());
			System.out.println("resultString------->" + resultString);
			return resultString;
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}
}
