package com.findcab.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
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
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.GeoPoint;
import com.baidu.mapapi.ItemizedOverlay;
import com.baidu.mapapi.LocationListener;
import com.baidu.mapapi.MKAddrInfo;
import com.baidu.mapapi.MKBusLineResult;
import com.baidu.mapapi.MKDrivingRouteResult;
import com.baidu.mapapi.MKPoiInfo;
import com.baidu.mapapi.MKPoiResult;
import com.baidu.mapapi.MKSearch;
import com.baidu.mapapi.MKSearchListener;
import com.baidu.mapapi.MKSuggestionResult;
import com.baidu.mapapi.MKTransitRouteResult;
import com.baidu.mapapi.MKWalkingRouteResult;
import com.baidu.mapapi.MapActivity;
import com.baidu.mapapi.MapController;
import com.baidu.mapapi.MapView;
import com.baidu.mapapi.Overlay;
import com.baidu.mapapi.OverlayItem;
import com.baidu.mapapi.Projection;
import com.findcab.R;
import com.findcab.object.DriversInfo;
import com.findcab.object.Passengers;
import com.findcab.util.BaiLocationOverlay;
import com.findcab.util.Constant;
import com.findcab.util.HttpTools;
import com.findcab.util.Tools;
import com.iflytek.mscdemo.IatActivity;

public class LocationOverlay extends MapActivity implements OnClickListener {

	static MapView mMapView = null;
	LocationListener mLocationListener = null;
	BaiLocationOverlay mLocationOverlay = null;
	public Context context;
	String mStrKey = "8BB7F0E5C9C77BD6B9B655DB928B74B6E2D838FD";
	BMapManager mBMapMan = null;
	List<GeoPoint> pList;
	static View mPopView = null; // 点击mark时弹出的气泡View
	static View popView = null; // 点击mark时弹出的气泡View
	int iZoom = 0;
	MapController mapController;
	private Button locate;
	private Button call;
	GeoPoint pt;

	private double lat;
	private double lng;

	private List<DriversInfo> listInfo;
	private Passengers info;

	private TextView title;
	// 定义搜索服务类
	private MKSearch mMKSearch;
	private String address;
	private TextView text_address;
	private String end;
	private String start;

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
		popView = super.getLayoutInflater().inflate(R.layout.popview_title,
				null);
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
		mLocationOverlay = new BaiLocationOverlay(this, mMapView, popView);

		mMapView.getOverlays().add(mLocationOverlay);

		initLocation();
		// mLocationListener = new LocationListener() {
		//
		// @Override
		// public void onLocationChanged(Location location) {
		// if (location != null) {
		// lat = location.getLatitude();
		// lng = location.getLongitude();
		//
		// pt = new GeoPoint((int) (location.getLatitude() * 1e6),
		// (int) (location.getLongitude() * 1e6));
		// mMapView.getController().animateTo(pt);
		// // getDrivers();
		// }
		// }
		// };

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

		// getDrivers();
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

		locate = (Button) findViewById(R.id.locate);
		locate.setOnClickListener(this);
		call = (Button) findViewById(R.id.call);
		call.setOnClickListener(this);

		title = (TextView) findViewById(R.id.title);

		text_address = (TextView) findViewById(R.id.text_address);

		// 初始化MKSearch
		mMKSearch = new MKSearch();
		mMKSearch.init(mBMapMan, new MySearchListener());
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			info = (Passengers) bundle.getSerializable("Passengers");
		}

	}

	@Override
	protected void onPause() {
		// BMapApiDemoApp app = (BMapApiDemoApp) this.getApplication();
		// mBMapMan.getLocationManager().removeUpdates(mLocationListener);
		mLocationOverlay.disableMyLocation();
		mLocationOverlay.disableCompass();
		mBMapMan.stop();
		super.onPause();
	}

	@Override
	protected void onResume() {

		// mBMapMan.getLocationManager().requestLocationUpdates(mLocationListener);
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

		public OverItemT(Drawable marker, Context context) {
			super(boundCenterBottom(marker));

			this.marker = marker;
			this.mContext = context;

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

	/**
	 * 内部类实现MKSearchListener接口,用于实现异步搜索服务
	 * 
	 * @author liufeng
	 */
	public class MySearchListener implements MKSearchListener {
		/**
		 * 根据经纬度搜索地址信息结果
		 * 
		 * @param result
		 *            搜索结果
		 * @param iError
		 *            错误号（0表示正确返回）
		 */
		@Override
		public void onGetAddrResult(MKAddrInfo result, int iError) {
			if (result == null) {
				return;
			}
			StringBuffer sb = new StringBuffer();
			// 经纬度所对应的位置
			sb.append(result.strAddr).append("/n");

			// 判断该地址附近是否有POI（Point of Interest,即兴趣点）
			if (null != result.poiList) {
				// 遍历所有的兴趣点信息
				for (MKPoiInfo poiInfo : result.poiList) {
					sb.append("----------------------------------------")
							.append("/n");
					sb.append("名称：").append(poiInfo.name).append("/n");
					sb.append("地址：").append(poiInfo.address).append("/n");
					sb.append("经度：").append(
							poiInfo.pt.getLongitudeE6() / 1000000.0f).append(
							"/n");
					sb.append("纬度：").append(
							poiInfo.pt.getLatitudeE6() / 1000000.0f).append(
							"/n");
					sb.append("电话：").append(poiInfo.phoneNum).append("/n");
					sb.append("邮编：").append(poiInfo.postCode).append("/n");
					// poi类型，0：普通点，1：公交站，2：公交线路，3：地铁站，4：地铁线路
					sb.append("类型：").append(poiInfo.ePoiType).append("/n");

					address = poiInfo.address;

					if (address == null) {
						address = poiInfo.name;
					}
				}
			}
			// 将地址信息、兴趣点信息显示在TextView上
			if (address != null) {

				text_address.setText(address);
			}

			System.out.println("addressTextView.setText----->" + address);
		}

		/**
		 * 驾车路线搜索结果
		 * 
		 * @param result
		 *            搜索结果
		 * @param iError
		 *            错误号（0表示正确返回）
		 */
		@Override
		public void onGetDrivingRouteResult(MKDrivingRouteResult result,
				int iError) {
		}

		/**
		 * POI搜索结果（范围检索、城市POI检索、周边检索）
		 * 
		 * @param result
		 *            搜索结果
		 * @param type
		 *            返回结果类型（11,12,21:poi列表 7:城市列表）
		 * @param iError
		 *            错误号（0表示正确返回）
		 */
		@Override
		public void onGetPoiResult(MKPoiResult result, int type, int iError) {
		}

		/**
		 * 公交换乘路线搜索结果
		 * 
		 * @param result
		 *            搜索结果
		 * @param iError
		 *            错误号（0表示正确返回）
		 */
		@Override
		public void onGetTransitRouteResult(MKTransitRouteResult result,
				int iError) {
		}

		/**
		 * 步行路线搜索结果
		 * 
		 * @param result
		 *            搜索结果
		 * @param iError
		 *            错误号（0表示正确返回）
		 */
		@Override
		public void onGetWalkingRouteResult(MKWalkingRouteResult result,
				int iError) {
		}

		@Override
		public void onGetBusDetailResult(MKBusLineResult arg0, int arg1) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onGetSuggestionResult(MKSuggestionResult arg0, int arg1) {
			// TODO Auto-generated method stub

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
		case R.id.locate:

			if (pt != null) {

				mMapView.getController().animateTo(pt);
			}
			break;
		case R.id.call:

			Intent intent = new Intent(context, IatActivity.class);
			startActivityForResult(intent, 1);
			// sendTrips();
			break;

		default:
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);

		if (data != null) {

			if (data.hasExtra("start")) {

				start = data.getStringExtra("start");
			} else {
				start = address;
			}
			if (data.hasExtra("end")) {

				end = data.getStringExtra("end");
			}

			if (start == null || start.equals("")) {
				Tools.myToast(context, "无法获取当前位置，请收到输入");
			} else {
				// 发布路线
				// sendTrips();
			}
		}

	}

	/**
	 * 获取我附近得司机
	 */
	private void getDrivers() {
		new Thread(new Runnable() {
			public void run() {
				Map<String, String> map = new HashMap<String, String>();

				// lat = 39.876757965948;
				// lng = 116.65188108138;
				map.put("driver[lat]", String.valueOf(lat));
				map.put("driver[lng]", String.valueOf(lng));

				// String url = "http://vissul.com:8989/api/drivers/";

				String result = HttpTools.GetDate(Constant.DRIVERS, map);

				try {
					JSONObject jsonObject = new JSONObject(result);

					JSONArray array = jsonObject.getJSONArray("drivers");
					listInfo = new ArrayList<DriversInfo>();
					for (int i = 0; i < array.length(); i++) {

						listInfo
								.add(new DriversInfo((JSONObject) array.get(i)));

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
	 * 获取我附近得司机详情
	 */
	private void getDriverInfo() {
		new Thread(new Runnable() {
			public void run() {

				String result = HttpTools.GetDate(Constant.DRIVER_INFO + "1",
						null);

				try {
					JSONObject jsonObject = new JSONObject(result);

					JSONArray array = jsonObject.getJSONArray("drivers");
					listInfo = new ArrayList<DriversInfo>();
					for (int i = 0; i < array.length(); i++) {

						listInfo
								.add(new DriversInfo((JSONObject) array.get(i)));

					}

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// if (listInfo != null && listInfo.size() > 0) {
				// messageHandler.sendEmptyMessage(Constant.SUCCESS);
				// } else {
				//
				// messageHandler.sendEmptyMessage(Constant.FAILURE);
				// }

			}
		}).start();
	}

	/**
	 * 发布路线
	 */
	private void sendTrips() {
		new Thread(new Runnable() {
			public void run() {
				Map<String, String> map = new HashMap<String, String>();

				// lat = 39.876757965948;
				// lng = 116.65188108138;
				map.put("trip[passenger_id]", String.valueOf(info.getId()));
				map.put("trip[start]", "中关村");
				map.put("trip[start_lat]", String.valueOf(lat));
				map.put("trip[start_lng]", String.valueOf(lng));
				map.put("trip[end]", "天通苑");
				map.put("trip[appointment]", "10");
				map.put("trip[end_lat]", String.valueOf(39.876757965948));
				map.put("trip[end_lng]", String.valueOf(116.65188108138));
				String result = HttpTools.PostDate(Constant.TRIPS, map);
				System.out.println("result------>" + result);
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
			getDrivers();
			// 查询该经纬度值所对应的地址位置信息
			// mMKSearch.reverseGeocode(point);

			mMKSearch.reverseGeocode(new GeoPoint((int) (lat * 1e6),
					(int) (lng * 1e6)));
		}
	}

	Handler messageHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {

			case Constant.SUCCESS:
				List<Overlay> list = mMapView.getOverlays();

				Drawable marker = getResources().getDrawable(R.drawable.cear); // �õ���Ҫ���ڵ�ͼ�ϵ���Դ
				marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker
						.getIntrinsicHeight());

				ArrayList<GeoPoint> arrayList = new ArrayList<GeoPoint>();
				DriversInfo info;
				GeoPoint point = null;
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
				title.setText("5公里范围，" + String.valueOf(listInfo.size())
						+ "辆出租车");

				getDriverInfo();
				break;
			case Constant.FAILURE:

				break;

			}
		}
	};
}
