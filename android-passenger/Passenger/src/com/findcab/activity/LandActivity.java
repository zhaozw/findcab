package com.findcab.activity;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.findcab.R;
import com.findcab.object.Passengers;
import com.findcab.util.Constant;
import com.findcab.util.HttpTools;
import com.findcab.util.MD5;
import com.findcab.util.Tools;

/**
 * 登陆
 * 
 * @author yuqunfeng
 * 
 */
public class LandActivity extends Activity implements OnClickListener {

	private EditText nameEditText = null;
	private EditText passEditText = null;
	private Button butt_land = null;
	private Button signup = null;

	private static String name = null;
	private static String password = null;
	public static Context context = null;

	public static final int SUCCESS = 1;
	public static final int PHONENULL = 2;
	public static final int PASSWORDNULL = 3;
	public static final int PHONEERROR = 4;
	public static final int PASSWORDERROR = 5;

	String[] data = new String[2];
	View aalayout;
	String DeviceId;

	String[] str = new String[3];

	private TextView text_forget;
	public ProgressDialog pd;
	Passengers info;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		context = this;

		setContentView(R.layout.land2);

		initView();
		if (data != null && !data[0].equals("")) {
			// land(data[0], data[1]);
		}
	}

	/**
	 * 初始化view
	 */
	private void initView() {
		data = getData();
		nameEditText = (EditText) findViewById(R.id.edit_account);
		passEditText = (EditText) findViewById(R.id.edit_password);

		nameEditText.setText(data[0]);
		passEditText.setText(data[1]);

		nameEditText.addTextChangedListener(nameWatcher);

		text_forget = (TextView) findViewById(R.id.forget_password);
		text_forget.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);// 下划线
		text_forget.setTextColor(Color.BLUE);

		butt_land = (Button) findViewById(R.id.butt_land);
		butt_land.setOnClickListener(this);
		signup = (Button) findViewById(R.id.signup);
		signup.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.butt_land:

			name = nameEditText.getText().toString().trim();
			password = passEditText.getText().toString().trim();
			land(name, password);
			// startMainActivity();
			break;
		case R.id.signup:

			Intent intent = new Intent(LandActivity.this, Signup.class);
			startActivity(intent);

			break;

		default:
			break;
		}
	}

	private String error;

	/**
	 * 登陆
	 */
	private void land(final String name, final String password) {
		pd = ProgressDialog.show(context, "", "正在登陆...", false, false);
		new Thread(new Runnable() {

			public void run() {

				if (name.equals("")) {
					messageHandler.sendEmptyMessage(PHONENULL);
				} else if (password.equals("")) {

					messageHandler.sendEmptyMessage(PASSWORDNULL);
				} else if (name.equals("") && password.equals("")) {

					messageHandler.sendEmptyMessage(PHONENULL);
				} else {// 不为空

					Map<String, String> map = new HashMap<String, String>();
					MD5 md5 = new MD5();
					map.put("passenger[mobile]", name);
					map.put("passenger[password]", md5.getMD5ofStr(password));
					String resultString = HttpTools.PostDate(Constant.SIGNIN,
							map);

					JSONObject jsonObject;
					try {
						jsonObject = new JSONObject(resultString);
						if (jsonObject.has("error")) {

							error = jsonObject.getString("error");
							messageHandler.sendEmptyMessage(Constant.FAILURE);
							return;
						}

						if (jsonObject.has("passenger")) {

							info = new Passengers(jsonObject
									.getJSONObject("passenger"));
						}

					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					messageHandler.sendEmptyMessage(SUCCESS);
				}
			}
		}).start();
	}

	Handler messageHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {

			case SUCCESS:
				if (pd != null && pd.isShowing()) {
					pd.dismiss();
				}
				save(name, password);
				startMainActivity();

				break;

			case PHONENULL:
				if (pd != null && pd.isShowing()) {
					pd.dismiss();
				}
				Tools.landDialog(context, "手机号码不能为空，请输入手机号", "登陆失败");
				butt_land.setEnabled(true);
				break;

			case PASSWORDNULL:
				if (pd != null && pd.isShowing()) {
					pd.dismiss();
				}
				Tools.landDialog(context, "密码不能为空，请输入密码", "登陆失败");
				butt_land.setEnabled(true);
				break;
			case Constant.FAILURE:
				if (pd != null && pd.isShowing()) {
					pd.dismiss();
				}
				Tools.landDialog(context, error, "登陆失败");
				butt_land.setEnabled(true);
				break;
			}
		}

	};

	/**
	 * 保存用户信息
	 */
	private void save(String name, String password) {
		Editor sharedata = getSharedPreferences("data", 0).edit();

		sharedata.putString("password", password);

		sharedata.putString("name", name);

		sharedata.commit();

	}

	/**
	 * 得到用户
	 */
	private String[] getData() {
		SharedPreferences sharedata = getSharedPreferences("data", 0);
		name = sharedata.getString("name", "");
		password = sharedata.getString("password", "");
		String[] data = { name, password };

		return data;
	}

	/**
	 * 登陆成功跳转页面
	 */
	public void startMainActivity() {
		Intent intent = new Intent(LandActivity.this, LocationOverlay.class);
		intent.putExtra("Passengers", info);
		startActivity(intent);
		finish();
	}

	/**
	 * 名字Watcher
	 */
	private TextWatcher nameWatcher = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			// TODO Auto-generated method stub

		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub

		}

		@Override
		public void afterTextChanged(Editable s) {
		}
	};
}