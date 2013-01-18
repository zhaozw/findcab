package com.findcab.activity;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.findcab.R;
import com.findcab.object.PassengerInfo;
import com.findcab.util.Constant;
import com.findcab.util.HttpTools;
import com.findcab.util.MD5;
import com.findcab.util.Tools;

/**
 * 注册
 * 
 * @author yuqunfeng
 * 
 */
public class Signup extends Activity implements OnClickListener {

	private Button back, start;
	private EditText edit_name, edit_mobile, edit_password;
	private String name, mobile, password;
	private CheckBox checkBox;
	private Context context;
	private Location location;
	private double lat;
	private double lng;
	private TextView item;
	private PassengerInfo info;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.register);
		initView();
		initLocation();
	}

	private void initView() {
		context = this;
		back = (Button) findViewById(R.id.back);
		back.setOnClickListener(this);
		start = (Button) findViewById(R.id.start);
		start.setOnClickListener(this);

		edit_name = (EditText) findViewById(R.id.name);
		edit_mobile = (EditText) findViewById(R.id.mobile);
		edit_password = (EditText) findViewById(R.id.password);

		checkBox = (CheckBox) findViewById(R.id.checkBox);

		item = (TextView) findViewById(R.id.item);
		item.setOnClickListener(this);

		if (initGPS()) {

			LocationManager locationManager;
			String serviceName = Context.LOCATION_SERVICE;
			locationManager = (LocationManager) this
					.getSystemService(serviceName);
			// 查询条件
			Criteria criteria = new Criteria();
			criteria.setAccuracy(Criteria.ACCURACY_FINE);
			criteria.setAltitudeRequired(false);
			criteria.setBearingRequired(false);
			criteria.setCostAllowed(true);
			criteria.setPowerRequirement(Criteria.POWER_LOW);

			String provider = locationManager.getBestProvider(criteria, true);
			location = locationManager.getLastKnownLocation(provider);
			if (location != null) {
				lat = location.getLatitude();
				lng = location.getLongitude();
			}
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {

		case R.id.back:

			finish();
			break;
		case R.id.start:
			postInfo();

			// Intent intent = new Intent(context, LocationOverlay.class);
			// startActivity(intent);
			// finish();
			break;
		case R.id.item:
			Tools
					.landDialog(
							context,
							"1注册条款的接受一旦会员在注册页面点击“我同意接受以上注册条款”后，这就表示会员已经阅读并且同意与第一眼相亲网网站达成协议，并接受所有的注册条款。",
							"注册条款");
			break;
		default:
			break;
		}
	}

	/**
	 * 提交信息
	 */
	private void postInfo() {
		new Thread(new Runnable() {
			public void run() {
				name = edit_name.getText().toString().trim();
				mobile = edit_mobile.getText().toString().trim();
				password = edit_password.getText().toString().trim();

				if (!checkBox.isChecked()) {

					Tools.myToast(context, "请确认是否已经阅读同意条款！");

				}
				MD5 md5 = new MD5();
				Map<String, String> map = new HashMap<String, String>();
				map.put("passenger[name]", name);
				map.put("passenger[mobile]", mobile);
				map.put("passenger[password]", md5.getMD5ofStr(password));
				// map.put("passenger[password]", password);
				// lat = 39.876757965948;
				// lng = 116.65188108138;
				
				if (lat!=0) {
					
					map.put("passenger[lat]", String.valueOf(lat));
					map.put("passenger[lng]", String.valueOf(lng));
				}else {
					
					return;
				}

				String result = HttpTools.PostDate(Constant.SIGNUP, map);

				try {
					JSONObject object = new JSONObject(result);
					JSONObject jsonObject = object.getJSONObject("passenger");
					info = new PassengerInfo(jsonObject);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if (info != null) {
					messageHandler.sendEmptyMessage(Constant.SUCCESS);

					return;
				}
				messageHandler.sendEmptyMessage(Constant.FAILURE);
				return;
			}
		}).start();
	}

	Handler messageHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {

			case Constant.SUCCESS:
				Tools.landDialog(context, null, "注册成功");
				break;
			case Constant.FAILURE:
				Tools.landDialog(context, null, "注册失败");

				break;

			}
		}
	};

	private boolean initGPS() {
		LocationManager locationManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);

		// 判断GPS模块是否开启，如果没有则开启
		if (!locationManager
				.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
			Toast.makeText(this, "GPS is not open,Please open it!",
					Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			startActivityForResult(intent, 0);

			return false;
		} else {
			Toast.makeText(this, "GPS is ready", Toast.LENGTH_SHORT);
		}
		return true;
	}

	/**
	 * 初始化所在GPS位置
	 */
	private void initLocation() {
	
		LocationManager locationManager;
		String serviceName = Context.LOCATION_SERVICE;
		locationManager = (LocationManager) this.getSystemService(serviceName);
		// 查询条件
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(true);
		criteria.setPowerRequirement(Criteria.POWER_LOW);
	
		String provider = locationManager.getBestProvider(criteria, true);
		Location location = locationManager.getLastKnownLocation(provider);
		if (location != null) {
	
			lat = location.getLatitude();
			lng = location.getLongitude();
	
			
		}
	}
}
