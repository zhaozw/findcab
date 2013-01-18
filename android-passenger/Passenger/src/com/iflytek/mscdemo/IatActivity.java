package com.iflytek.mscdemo;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.findcab.R;
import com.findcab.activity.LocationOverlay;
import com.findcab.util.Tools;
import com.iflytek.speech.RecognizerResult;
import com.iflytek.speech.SpeechError;
import com.iflytek.speech.SpeechConfig.RATE;
import com.iflytek.ui.RecognizerDialog;
import com.iflytek.ui.RecognizerDialogListener;

/**
 * 转写页面,通过调用SDK中提供的RecognizerDialog来实现转写功能.
 * 
 * @author iFlytek
 * @since 20120821
 */
public class IatActivity extends Activity implements OnClickListener,
		RecognizerDialogListener {
	// 日志TAG.
	private static final String TAG = "IatDemoActivity";

	// // title的文本内容.
	// private TextView mCategoryText;
	// 识别结果显示
	// private EditText mResultText;
	// 缓存，保存当前的引擎参数到下一次启动应用程序使用.
	private SharedPreferences mSharedPreferences;
	// 识别Dialog
	private RecognizerDialog iatDialog;
	// 地图搜索关键字定义
	public static final String ENGINE_POI = "poi";

	private String start = null;
	private String end = null;
	public Context context = null;

	private Button start_cancel, end_cancel;
	private Button okButton, cancelButton;
	private EditText edit_start, edit_end;
	Intent intent;

	/**
	 * 页面初始化入口函数.
	 * 
	 * @param savedInstanceState
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "[onCreate]" + savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.call);

		initView();
		// 初始化转写Dialog, appid需要在http://open.voicecloud.cn获取.
		iatDialog = new RecognizerDialog(this, "appid="
				+ getString(R.string.app_id));
		iatDialog.setListener(this);

		// 初始化缓存对象.
		mSharedPreferences = getSharedPreferences(getPackageName(),
				MODE_PRIVATE);
	}

	/**
	 * 初始化view
	 */
	private void initView() {
		context = this;
		start_cancel = (Button) findViewById(R.id.start_cancel);
		start_cancel.setOnClickListener(this);
		end_cancel = (Button) findViewById(R.id.end_mic);
		end_cancel.setOnClickListener(this);
	
		edit_start = (EditText) findViewById(R.id.edit_start);
		edit_end = (EditText) findViewById(R.id.edit_end);
	
		okButton = (Button) findViewById(R.id.ok);
		okButton.setOnClickListener(this);
	
		cancelButton = (Button) findViewById(R.id.cancel);
		cancelButton.setOnClickListener(this);
	}

	/**
	 * 初始化engine相关参数.
	 * 
	 * @param
	 */
	@Override
	protected void onStart() {
		super.onStart();
		// 获取之前保存的引擎参数，若没有使用默认的参数sms.

	}

	/**
	 * 按钮点击事件.
	 * 
	 * @param v
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		// 转写按钮
		case R.id.end_mic:
			showIatDialog();
			break;

		case R.id.cancel:

			finish();
			break;
		case R.id.start_cancel:

			edit_start.setText("");
			break;

		case R.id.ok:
			start = edit_start.getText().toString().trim();
			end = edit_end.getText().toString().trim();
			intent = new Intent(IatActivity.this, LocationOverlay.class);

			if (!start.equals("我的位置")) {

				intent.putExtra("start", start);
			}
			if (!end.equals("")) {

				intent.putExtra("end", end);
				setResult(1, intent);
				finish();
			} else {

				Tools.myToast(context, "请输入目的地");
			}

			break;
		default:
			break;
		}
	}

	/**
	 * 显示转写对话框.
	 * 
	 * @param
	 */
	public void showIatDialog() {
		// 获取引擎参数
		String engine = mSharedPreferences.getString(
				getString(R.string.preference_key_iat_engine),
				getString(R.string.preference_default_iat_engine));

		// 获取area参数，POI搜索时需要传入.
		String area = null;
		if (ENGINE_POI.equals(engine)) {
			final String defaultProvince = getString(R.string.preference_default_poi_province);
			String province = mSharedPreferences.getString(
					getString(R.string.preference_key_poi_province),
					defaultProvince);
			final String defaultCity = getString(R.string.preference_default_poi_city);
			String city = mSharedPreferences.getString(
					getString(R.string.preference_key_poi_city), defaultCity);

			if (!defaultProvince.equals(province)) {
				area = "search_area=" + province;
				if (!defaultCity.equals(city)) {
					area += city;
				}
			}
		}

		if (TextUtils.isEmpty(area))
			area = "";
		else
			area += ",";
		// 设置转写Dialog的引擎和poi参数.
		iatDialog.setEngine(engine, area, null);

		// 设置采样率参数，由于绝大部分手机只支持8K和16K，所以设置11K和22K采样率将无法启动录音.
		String rate = mSharedPreferences.getString(
				getString(R.string.preference_key_iat_rate),
				getString(R.string.preference_default_iat_rate));
		if (rate.equals("rate8k"))
			iatDialog.setSampleRate(RATE.rate8k);
		else if (rate.equals("rate11k"))
			iatDialog.setSampleRate(RATE.rate11k);
		else if (rate.equals("rate16k"))
			iatDialog.setSampleRate(RATE.rate16k);
		else if (rate.equals("rate22k"))
			iatDialog.setSampleRate(RATE.rate22k);
		// mResultText.setText(null);
		edit_end.setText(null);
		// 弹出转写Dialog.
		iatDialog.show();
	}

	/**
	 * RecognizerDialogListener的"结束识别会话"回调接口. 参数详见<MSC开发指南>.
	 * 
	 * @param error
	 */
	@Override
	public void onEnd(SpeechError error) {
	}

	/**
	 * RecognizerDialogListener的"返回识别结果"回调接口. 通常服务端会多次返回结果调用此接口，结果需要进行累加.
	 * 
	 * @param results
	 * @param isLast
	 */
	@Override
	public void onResults(ArrayList<RecognizerResult> results, boolean isLast) {
		StringBuilder builder = new StringBuilder();
		for (RecognizerResult recognizerResult : results) {
			builder.append(recognizerResult.text);
		}
		// mResultText.append(builder);
		// mResultText.setSelection(mResultText.length());
		edit_end.append(builder);
		edit_end.setSelection(edit_end.length());

		System.out.println("builder----------->" + builder);
	}

}
