package com.elance;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class GetMacActivity extends Activity implements OnClickListener,
		OnLongClickListener {
	private static final String TAG = "com.elance.GetMacActivity";

	private TextView tv_mac;
	private EditText et_mac0, et_mac1, et_mac2, et_mac3, et_mac4, et_mac5;
	private Button btn_change, btn_backup, btn_recover;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		init();
		initListeners();
	}

	private void init() {
		setContentView(R.layout.main);
		tv_mac = (TextView) findViewById(R.main.tv_mac);
		et_mac0 = (EditText) findViewById(R.main.et_mac0);
		et_mac1 = (EditText) findViewById(R.main.et_mac1);
		et_mac2 = (EditText) findViewById(R.main.et_mac2);
		et_mac3 = (EditText) findViewById(R.main.et_mac3);
		et_mac4 = (EditText) findViewById(R.main.et_mac4);
		et_mac5 = (EditText) findViewById(R.main.et_mac5);
		btn_change = (Button) findViewById(R.main.btn_change);
		btn_backup = (Button) findViewById(R.main.btn_backup);
		btn_recover = (Button) findViewById(R.main.btn_recover);
		File busybox = new File(Util.BUSYBOX_PATH);
		if (!busybox.exists()) {
			Util.getassetsfile(this, "busybox", busybox);
			Util.runCommand("chmod 777 " + Util.BUSYBOX_PATH);
		}
		String mac = Util.getMac(this);
		if (mac != null) {
			tv_mac.setText(mac);
		}
		if (Util.changeable) {
			btn_change.setEnabled(true);
			btn_backup.setEnabled(true);
		}
	}

	private void initListeners() {
		btn_change.setOnClickListener(this);
		btn_backup.setOnClickListener(this);
		btn_recover.setOnClickListener(this);
		tv_mac.setOnLongClickListener(this);
	}

	public boolean onLongClick(View v) {
		if (v.getId() == R.main.tv_mac) {
			AlertDialog.Builder builder = new Builder(this);
			builder.setTitle("Copy");
			builder.setPositiveButton("Copy",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
							clipboard.setText(tv_mac.getText().toString());
						}
					});
			builder.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
			builder.create().show();
		}
		return false;
	}

	public void onClick(View v) {
		AlertDialog.Builder builder = new Builder(this);
		builder.setTitle("WARNING!!!");
		if (v.getId() == R.main.btn_change) {
			final String mac0 = et_mac0.getText().toString();
			final String mac1 = et_mac1.getText().toString();
			final String mac2 = et_mac2.getText().toString();
			final String mac3 = et_mac3.getText().toString();
			final String mac4 = et_mac4.getText().toString();
			final String mac5 = et_mac5.getText().toString();
			if ((mac0 + mac1 + mac2 + mac3 + mac4 + mac5).length() != 12) {
				Toast.makeText(this, "MAC Address Invalid!", Toast.LENGTH_LONG)
						.show();
				return;
			}
			builder.setMessage(R.string.change);
			builder.setPositiveButton("OK,Go on",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							Util.setMac(mac0, mac1, mac2, mac3, mac4, mac5);
							dialog.dismiss();
							tv_mac.setText(Util.getMac(GetMacActivity.this));
							Toast.makeText(GetMacActivity.this,
									"MAC Address Changed!", Toast.LENGTH_LONG)
									.show();
						}
					});
		} else if (v.getId() == R.main.btn_backup) {
			builder.setMessage(R.string.backup);
			builder.setPositiveButton("OK,Go on",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							if (Util.backup())
								Toast.makeText(GetMacActivity.this,
										"Backup successfully",
										Toast.LENGTH_LONG).show();
							else
								Toast.makeText(GetMacActivity.this,
										"Cant find nvs_map.bin",
										Toast.LENGTH_LONG).show();
						}
					});
		} else if (v.getId() == R.main.btn_recover) {
			builder.setMessage(R.string.recover);
			builder.setPositiveButton("OK,Go on",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							if (Util.recover()) {
								tv_mac.setText(Util.getMac(GetMacActivity.this));
								Toast.makeText(GetMacActivity.this,
										"Recover successfully",
										Toast.LENGTH_LONG).show();
							} else
								Toast.makeText(GetMacActivity.this,
										"Cant find nvs_map.bin.backup",
										Toast.LENGTH_LONG).show();
						}
					});
		}
		builder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		builder.create().show();
	}
}