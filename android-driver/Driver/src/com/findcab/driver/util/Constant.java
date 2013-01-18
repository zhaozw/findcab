package com.findcab.driver.util;

/**
 * @author xy
 * @date 2012-3-3
 */
public class Constant {
	/**
	 * 这个用来存储从信息对象
	 */
	public static final int SUCCESS = 111;
	public static final int FAILURE = 222;
	public static final int ERROR = 333;

	public static final String BASEURL = "http://vissul.com:8989/api/";

	public static final String DRIVERS_SIGNUP = BASEURL + "drivers/signup/";// 司机注册(司机端)
	public static final String DRIVERS_SIGNIN = BASEURL + "drivers/signin/";// 司机登陆
	public static final String DRIVERS_PASSENGERS = BASEURL + "passengers/";// 我附近得乘客
	public static final String CONVERSATIONS = BASEURL + "conversations/";// 我附近得乘客

}
