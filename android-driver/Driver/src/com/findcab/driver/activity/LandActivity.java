package com.findcab.driver.activity;

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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.findcab.driver.object.DriverInfo;
import com.findcab.driver.util.Constant;
import com.findcab.driver.util.HttpTools;
import com.findcab.driver.util.MD5;
import com.findcab.driver.util.Tools;

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

	public ProgressDialog pd;
	protected String error;
	protected DriverInfo info;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		context = this;

		setContentView(R.layout.main);

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

			break;
		case R.id.signup:

			Intent intent = new Intent(LandActivity.this, Signup.class);
			startActivity(intent);
			finish();
			break;

		default:
			break;
		}
	}

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
				} else {

					// 不为空

					Map<String, String> map = new HashMap<String, String>();
					MD5 md5 = new MD5();
					map.put("driver[mobile]", name);
					// map.put("driver[password]", password);
					map.put("driver[password]", md5.getMD5ofStr(password));
					String result = HttpTools.PostDate(Constant.DRIVERS_SIGNIN,
							map);

					JSONObject jsonObject;
					try {
						jsonObject = new JSONObject(result);
						if (jsonObject.has("error")) {

							error = jsonObject.getString("error");
							messageHandler.sendEmptyMessage(Constant.FAILURE);
							return;
						}

						JSONObject object = jsonObject.getJSONObject("driver");
						info = new DriverInfo(object);

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
		intent.putExtra("DriverInfo", info);
		startActivity(intent);
		finish();
	}

}