package com.findcab.driver.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.GeoPoint;
import com.baidu.mapapi.ItemizedOverlay;
import com.baidu.mapapi.LocationListener;
import com.baidu.mapapi.MapActivity;
import com.baidu.mapapi.MapController;
import com.baidu.mapapi.MapView;
import com.baidu.mapapi.MyLocationOverlay;
import com.baidu.mapapi.Overlay;
import com.baidu.mapapi.OverlayItem;
import com.baidu.mapapi.Projection;
import com.findcab.driver.object.DriverInfo;
import com.findcab.driver.object.PassengerInfo;
import com.findcab.driver.util.Constant;
import com.findcab.driver.util.HttpTools;
import com.findcab.driver.util.Tools;

public class LocationOverlay extends MapActivity implements OnClickListener {

	static MapView mMapView = null;
	LocationListener mLocationListener = null;
	MyLocationOverlay mLocationOverlay = null;
	public Context context;
	String mStrKey = "8BB7F0E5C9C77BD6B9B655DB928B74B6E2D838FD";
	BMapManager mBMapMan = null;
	List<GeoPoint> pList;
	static View mPopView = null; // 点击mark时弹出的气泡View
	int iZoom = 0;
	MapController mapController;
	private Button answer;
	// private Button call;
	GeoPoint pt;
	Location location;

	private double lat;
	private double lng;

	private List<PassengerInfo> listInfo;

	private RelativeLayout bottom_button, bottom;
	private DriverInfo info;

	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.mapview);
		initManager();

		mMapView = (MapView) findViewById(R.id.bmapView);

		// 设置在缩放动画过程中也显示overlay,默认为不绘制
		mMapView.setDrawOverlayWhenZooming(true);

		// 创建点击mark时的弹出泡泡
		mPopView = super.getLayoutInflater().inflate(R.layout.popview, null);
		mMapView.addView(mPopView, new MapView.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, null,
				MapView.LayoutParams.TOP_LEFT));
		mPopView.setVisibility(View.GONE);
		iZoom = mMapView.getZoomLevel();

		// mMapView.setBuiltInZoomControls(true);

		mMapView.setDrawOverlayWhenZooming(true);
		mMapView.setDoubleClickZooming(false);
		mMapView.setClickable(false);
		mapController = mMapView.getController();
		mapController.setZoom(14);
		mLocationOverlay = new MyLocationOverlay(this, mMapView);
		mMapView.getOverlays().add(mLocationOverlay);
		// initLocation();
		mLocationListener = new LocationListener() {

			@Override
			public void onLocationChanged(Location location) {
				if (location != null) {

					lat = location.getLatitude();
					lng = location.getLongitude();

					pt = new GeoPoint((int) (location.getLatitude() * 1e6),
							(int) (location.getLongitude() * 1e6));
					mMapView.getController().animateTo(pt);
					getPassengers();
					// getConversations();
				}
			}
		};

		// List<Overlay> list = mMapView.getOverlays();
		//
		// Drawable marker = getResources().getDrawable(R.drawable.iconmarka);
		// // �õ���Ҫ���ڵ�ͼ�ϵ���Դ
		// marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker
		// .getIntrinsicHeight()); // Ϊmaker����λ�úͱ߽�
		//
		// double mLat1 = 39.95923; // point1γ��
		// double mLon1 = 116.357428 + 0.05; // point1����
		//
		// double mLat2 = 39.95923;
		// double mLon2 = 116.397428 + 0.05;
		// GeoPoint p1 = new GeoPoint((int) (mLat1 * 1E6), (int) (mLon1 * 1E6));
		// GeoPoint p2 = new GeoPoint((int) (mLat2 * 1E6), (int) (mLon2 * 1E6));
		// pList = new ArrayList<GeoPoint>();
		// pList.add(p1);
		// pList.add(p2);
		// OverItemT overlay = new OverItemT(marker, context, pList);
		// mMapView.getOverlays().add(overlay);
		//
		// list.add(overlay);
		// mMapView.postInvalidate();

	}

	/**
	 * 初始化BMapManager
	 */
	private void initManager() {
		context = this;
		mBMapMan = new BMapManager(getApplication());
		mBMapMan.getLocationManager().setNotifyInternal(1000, 5000);
		mBMapMan.init(mStrKey, new MyGeneralListener());
		mBMapMan.start();
		// 如果使用地图SDK，请初始化地图Activity
		super.initMapActivity(mBMapMan);

		answer = (Button) findViewById(R.id.answer);
		answer.setOnClickListener(this);

		bottom_button = (RelativeLayout) findViewById(R.id.bottom_button);
		// bottom_button.setVisibility(View.INVISIBLE);

		// RelativeLayout layout = (RelativeLayout)
		// findViewById(R.id.my_relative_layout);
		// bottom = (RelativeLayout) findViewById(R.id.bottom);
		// RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
		// LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		// lp.addRule(android.widget.RelativeLayout.ALIGN_PARENT_BOTTOM);
		// layout.addView(bottom, lp);

		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			info = (DriverInfo) bundle.getSerializable("DriverInfo");
		}
	}

	@Override
	protected void onPause() {
		// BMapApiDemoApp app = (BMapApiDemoApp) this.getApplication();
		mBMapMan.getLocationManager().removeUpdates(mLocationListener);
		mLocationOverlay.disableMyLocation();
		mLocationOverlay.disableCompass();
		mBMapMan.stop();
		super.onPause();
	}

	@Override
	protected void onResume() {

		mBMapMan.getLocationManager().requestLocationUpdates(mLocationListener);
		mLocationOverlay.enableMyLocation();
		mLocationOverlay.enableCompass();
		mBMapMan.start();
		super.onResume();
	}

	class OverItemT extends ItemizedOverlay<OverlayItem> {

		public List<OverlayItem> mGeoList = new ArrayList<OverlayItem>();
		private Drawable marker;
		private Context mContext;

		private double mLat1 = 39.90923;
		private double mLon1 = 116.357428;

		private double mLat2 = 39.90923;
		private double mLon2 = 116.397428;

		private double mLat3 = 39.90923;
		private double mLon3 = 116.437428;

		public OverItemT(Drawable marker, Context context, List<GeoPoint> points) {
			super(boundCenterBottom(marker));

			this.marker = marker;
			this.mContext = context;

			for (int i = 0; i < points.size(); i++) {

				mGeoList.add(new OverlayItem(points.get(i), "P3", "point3"));
			}

			populate(); // createItem(int)方法构造item。一旦有了数据，在调用其它方法前，首先调用这个方法
		}

		public void updateOverlay() {
			populate();
		}

		@Override
		public void draw(Canvas canvas, MapView mapView, boolean shadow) {

			// Projection接口用于屏幕像素坐标和经纬度坐标之间的变换
			Projection projection = mapView.getProjection();
			for (int index = size() - 1; index >= 0; index--) { // 遍历mGeoList
				OverlayItem overLayItem = getItem(index); // 得到给定索引的item

				String title = overLayItem.getTitle();
				// 把经纬度变换到相对于MapView左上角的屏幕像素坐标
				Point point = projection.toPixels(overLayItem.getPoint(), null);

				// 可在此处添加您的绘制代码
				Paint paintText = new Paint();
				paintText.setColor(Color.BLUE);
				paintText.setTextSize(15);
				canvas.drawText(title, point.x - 30, point.y, paintText); // 绘制文本
			}

			super.draw(canvas, mapView, shadow);
			// 调整一个drawable边界，使得（0，0）是这个drawable底部最后一行中心的一个像素
			boundCenterBottom(marker);
		}

		@Override
		protected OverlayItem createItem(int i) {
			// TODO Auto-generated method stub
			return mGeoList.get(i);
		}

		@Override
		public int size() {
			// TODO Auto-generated method stub
			return mGeoList.size();
		}

		@Override
		// 处理当点击事件
		protected boolean onTap(int i) {
			setFocus(mGeoList.get(i));
			// 更新气泡位置,并使之显示
			GeoPoint pt = mGeoList.get(i).getPoint();
			LocationOverlay.mMapView.updateViewLayout(LocationOverlay.mPopView,
					new MapView.LayoutParams(LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT, pt,
							MapView.LayoutParams.BOTTOM_CENTER));
			LocationOverlay.mPopView.setVisibility(View.VISIBLE);
			Toast.makeText(this.mContext, mGeoList.get(i).getSnippet(),
					Toast.LENGTH_SHORT).show();
			return true;
		}

		@Override
		public boolean onTap(GeoPoint arg0, MapView arg1) {
			// TODO Auto-generated method stub
			// 消去弹出的气泡
			LocationOverlay.mPopView.setVisibility(View.GONE);
			return super.onTap(arg0, arg1);
		}
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.answer:

		default:
			break;
		}
	}

	private void getPassengers() {
		new Thread(new Runnable() {
			public void run() {
				Map<String, String> map = new HashMap<String, String>();

				// lat = 39.876757965948;
				// lng = 116.65188108138;

				if (lat != 0) {

					map.put("passenger[lat]", String.valueOf(lat));
					map.put("passenger[lng]", String.valueOf(lng));
				}

				String result = HttpTools.GetDate(Constant.DRIVERS_PASSENGERS,
						map);

				try {
					JSONObject jsonObject = new JSONObject(result);

					JSONArray array = jsonObject.getJSONArray("passengers");

					listInfo = new ArrayList<PassengerInfo>();
					for (int i = 0; i < array.length(); i++) {

						listInfo.add(new PassengerInfo((JSONObject) array
								.get(i)));

					}

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if (listInfo != null && listInfo.size() > 0) {
					messageHandler.sendEmptyMessage(Constant.SUCCESS);
				} else {

					messageHandler.sendEmptyMessage(Constant.FAILURE);
				}

			}
		}).start();
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

			pt = new GeoPoint((int) (location.getLatitude() * 1e6),
					(int) (location.getLongitude() * 1e6));
			mMapView.getController().animateTo(pt);
			getPassengers();
		}
	}

	/**
	 * 我的会话（发送给我的请求）
	 */
	private void getConversations() {
		new Thread(new Runnable() {
			public void run() {
				Map<String, String> map = new HashMap<String, String>();

				map.put("to_id", String.valueOf(info.getId()));
				// map.put("to_id", String.valueOf(1));

				String result = HttpTools.GetDate(Constant.CONVERSATIONS, map);
				System.out.println("result------>" + result);
			}
		}).start();
	}

	/**
	 * 更新会话状态
	 */
	private void changeConversationsStatus() {
		new Thread(new Runnable() {
			public void run() {
				Map<String, String> map = new HashMap<String, String>();

				map.put("conversation[status]", "1");
				map.put("conversation[status_desc]", "accept");

				String result = HttpTools.GetDate(Constant.CONVERSATIONS, map);
				System.out.println("result------>" + result);
			}
		}).start();
	}

	Handler messageHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {

			case Constant.SUCCESS:
				List<Overlay> list = mMapView.getOverlays();

				Drawable marker = getResources().getDrawable(
						R.drawable.iconmarka); // �õ���Ҫ���ڵ�ͼ�ϵ���Դ
				marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker
						.getIntrinsicHeight());

				ArrayList<GeoPoint> arrayList = new ArrayList<GeoPoint>();
				PassengerInfo info;
				GeoPoint point;
				for (int i = 0; i < listInfo.size(); i++) {
					info = listInfo.get(i);
					point = new GeoPoint((int) (info.getLat() * 1e6),
							(int) (info.getLng() * 1e6));
					arrayList.add(point);

					System.out.println("name--->" + info.getName());
				}

				OverItemT overlay = new OverItemT(marker, context, arrayList);
				mMapView.getOverlays().add(overlay);

				list.add(overlay);
				mMapView.postInvalidate();

				break;
			case Constant.FAILURE:

				Tools.myToast(context, "没取到数据");

				break;

			}
		}
	};
}
