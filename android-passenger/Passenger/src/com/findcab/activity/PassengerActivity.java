package com.findcab.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.widget.Toast;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.GeoPoint;
import com.baidu.mapapi.ItemizedOverlay;
import com.baidu.mapapi.LocationListener;
import com.baidu.mapapi.MKEvent;
import com.baidu.mapapi.MKGeneralListener;
import com.baidu.mapapi.MapActivity;
import com.baidu.mapapi.MapView;
import com.baidu.mapapi.MyLocationOverlay;
import com.baidu.mapapi.Overlay;
import com.baidu.mapapi.OverlayItem;
import com.baidu.mapapi.Projection;
import com.findcab.R;
import com.findcab.handler.DriversHandler;
import com.findcab.util.HttpTools;

public class PassengerActivity extends MapActivity {

	static View mPopView = null; // 点击mark时弹出的气泡View
	static MapView mMapView = null;
	LocationListener mLocationListener = null;// onResume时注册此listener，onPause时需要Remove

	int iZoom = 0;
	OverItemT overitem = null;
	public Context context;
	String mStrKey = "8BB7F0E5C9C77BD6B9B655DB928B74B6E2D838FD";
	BMapManager mBMapMan = null;
	private double lat;// 维度
	private double lng;// 经度

	List<GeoPoint> pList;
	MyLocationOverlay mLocationOverlay = null; // 定位图层

	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.mapview);
		initManager();

		mMapView = (MapView) findViewById(R.id.bmapView);
		// mMapView.setBuiltInZoomControls(true);
		// // 设置在缩放动画过程中也显示overlay,默认为不绘制
		// mMapView.setDrawOverlayWhenZooming(true);
		//
		// // 创建点击mark时的弹出泡泡
		// mPopView = super.getLayoutInflater().inflate(R.layout.popview, null);
		// mMapView.addView(mPopView, new MapView.LayoutParams(
		// LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, null,
		// MapView.LayoutParams.TOP_LEFT));
		// mPopView.setVisibility(View.GONE);
		// iZoom = mMapView.getZoomLevel();

		mMapView.setBuiltInZoomControls(true);
		// 设置在缩放动画过程中也显示overlay,默认为不绘制
		mMapView.setDrawOverlayWhenZooming(true);

		// 添加定位图层
		mLocationOverlay = new MyLocationOverlay(this, mMapView);
		mMapView.getOverlays().add(mLocationOverlay);

		// if (initGPS()) {
		//
		// initLocation();
		// }
		// getGrade();
		// 注册定位事件
		mLocationListener = new LocationListener() {

			@Override
			public void onLocationChanged(Location location) {
				if (location != null) {
					GeoPoint pt = new GeoPoint(
							(int) (location.getLatitude() * 1e6),
							(int) (location.getLongitude() * 1e6));
					mMapView.getController().animateTo(pt);
				}
			}

		};
	}

	/**
	 * 初始化BMapManager
	 */
	private void initManager() {
		context = this;
		mBMapMan = new BMapManager(getApplication());
		mBMapMan.getLocationManager().setNotifyInternal(10, 5);
		mBMapMan.init(mStrKey, new MyGeneralListener());
		mBMapMan.start();
		// 如果使用地图SDK，请初始化地图Activity
		super.initMapActivity(mBMapMan);
	}

	/**
	 * 初始化所在GPS位置
	 */
	// private void initLocation() {
	//
	// mMapView.getController();
	//
	// LocationManager locationManager;
	// String serviceName = Context.LOCATION_SERVICE;
	// locationManager = (LocationManager) this.getSystemService(serviceName);
	// // 查询条件
	// Criteria criteria = new Criteria();
	// criteria.setAccuracy(Criteria.ACCURACY_FINE);
	// criteria.setAltitudeRequired(false);
	// criteria.setBearingRequired(false);
	// criteria.setCostAllowed(true);
	// criteria.setPowerRequirement(Criteria.POWER_LOW);
	//
	// String provider = locationManager.getBestProvider(criteria, true);
	// Location location = locationManager.getLastKnownLocation(provider);
	// // 如果位置信息为null，则请求更新位置信息
	// // 设置监听器，自动更新的最小时间为间隔5秒，最小位移变化超过5米
	// if (location == null) {
	// locationManager.requestLocationUpdates(provider, 5000, 5,
	// locationListener);
	// }
	// updateWithNewLocation(location);
	//
	// }

	/**
	 * 更新地图位置
	 * 
	 * @param location
	 */
	private void updateWithNewLocation(Location location) {

		if (location != null) {
			lat = location.getLatitude();
			lng = location.getLongitude();

			double mLat1 = 39.90923; // point1γ��
			double mLon1 = 116.357428; // point1����

			double mLat2 = 39.90923;
			double mLon2 = 116.397428;

			GeoPoint p = new GeoPoint((int) (lat * 1E6), (int) (lng * 1E6));
			GeoPoint p1 = new GeoPoint((int) (mLat1 * 1E6), (int) (mLon1 * 1E6));
			GeoPoint p2 = new GeoPoint((int) (mLat2 * 1E6), (int) (mLon2 * 1E6));
			mMapView.getController().setCenter(p);

			List<Overlay> list = mMapView.getOverlays();

			list.clear();
			Drawable marker = getResources().getDrawable(R.drawable.iconmarka); // �õ���Ҫ���ڵ�ͼ�ϵ���Դ
			marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker
					.getIntrinsicHeight()); // Ϊmaker����λ�úͱ߽�

			pList = new ArrayList<GeoPoint>();
			pList.add(p);
			pList.add(p1);
			pList.add(p2);
			OverItemT overlay = new OverItemT(marker, context, pList);
			mMapView.getOverlays().add(overlay);

			list.add(overlay);
			mMapView.postInvalidate();

			mMapView.getController().animateTo(p);

		}
		// locationView.setText(lat + "  " + lng);
	}

	private LocationListener locationListener = new LocationListener() {
		public void onLocationChanged(Location location) {
			updateWithNewLocation(location);

		}

		public void onProviderDisabled(String provider) {
			updateWithNewLocation(null);
		}

		public void onProviderEnabled(String provider) {
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}

	};

	@Override
	protected void onPause() {
		super.onPause();
		mBMapMan.getLocationManager().removeUpdates(mLocationListener);
	}

	@Override
	protected void onResume() {
		mBMapMan.start();
		mBMapMan.getLocationManager().requestLocationUpdates(mLocationListener);
		super.onResume();
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

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
	 * 得到学段
	 */
	private void getGrade() {

		// results = null;
		Map<String, String> map = new HashMap<String, String>();
		// map.put("level", level);

		try {

			HttpTools.getAndParse(PassengerActivity.this,
					"http://vissul.com:8989/", map, new DriversHandler());
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

}

class MyGeneralListener implements MKGeneralListener {
	@Override
	public void onGetNetworkState(int iError) {
		Log.d("MyGeneralListener", "onGetNetworkState error is " + iError);
		// Toast.makeText(context, "您的网络出错啦！", Toast.LENGTH_LONG).show();
	}

	@Override
	public void onGetPermissionState(int iError) {
		Log.d("MyGeneralListener", "onGetPermissionState error is " + iError);
		if (iError == MKEvent.ERROR_PERMISSION_DENIED) {
			// 授权Key错误：
			// Toast.makeText(PassengerActivity.this,
			// "请在BMapApiDemoApp.java文件输入正确的授权Key！", Toast.LENGTH_LONG)
			// .show();

		}
	}
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
		PassengerActivity.mMapView.updateViewLayout(PassengerActivity.mPopView,
				new MapView.LayoutParams(LayoutParams.WRAP_CONTENT,
						LayoutParams.WRAP_CONTENT, pt,
						MapView.LayoutParams.BOTTOM_CENTER));
		PassengerActivity.mPopView.setVisibility(View.VISIBLE);
		Toast.makeText(this.mContext, mGeoList.get(i).getSnippet(),
				Toast.LENGTH_SHORT).show();
		return true;
	}

	@Override
	public boolean onTap(GeoPoint arg0, MapView arg1) {
		// TODO Auto-generated method stub
		// 消去弹出的气泡
		PassengerActivity.mPopView.setVisibility(View.GONE);
		return super.onTap(arg0, arg1);
	}
}
