package com.findcab.driver.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.findcab.driver.activity.R;

public class Tools {
	public static final boolean isShow = true;

	public static void landDialog(final Context context, String content,
			String title) {

		AlertDialog.Builder builder = new AlertDialog.Builder(context);

		builder.setIcon(R.drawable.ic_launcher);
		builder.setTitle(title);
		View layout = View.inflate(context, R.layout.land_dialog, null);
		TextView text_content = (TextView) layout
				.findViewById(R.id.text_content);
		if (content != null) {

			text_content.setText(content);
		}
		// aa.setCancelable(true);
		// aa.setTitle(R.string.lockscreen_charged);
		builder.setView(layout);
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {

			}
		});

		builder.create().show();
	}

	public static void myToast(final Context context, final String text) {

		if (isShow) {

			Toast.makeText(context, text, Toast.LENGTH_SHORT).show();

		}

	}

}
