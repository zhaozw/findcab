package com.findcab.handler;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.findcab.object.Drivers;

public class DriversHandler extends AbsHandler {
	List<Drivers> list;

	@Override
	public Object parseResponse(String responseStr) throws Exception {
		// TODO Auto-generated method stub

		System.out.println("responseStr---->" + responseStr);
		// JSONObject jsonObject = new JSONObject(responseStr);
		//
		// JSONArray array = new JSONArray(responseStr);
		// list = new ArrayList<Drivers>();
		// for (int i = 0; i < array.length(); i++) {
		//
		// list.add(new Drivers((JSONObject) array.get(i)));
		//
		// }

		return list;

	}
}
