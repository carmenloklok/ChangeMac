package com.elance;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class GetMacActivity extends Activity implements OnClickListener,
		OnLongClickListener {
	public static final String TAG = "com.elance.GetMacActivity";
	public static final String PATH0 = "/config/wifi/nvs_map.bin";
	public static final String PATH1 = "/pds/wifi/nvs_map.bin";
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
		tv_mac.setText(getMac());
	}

	private void initListeners() {
		btn_change.setOnClickListener(this);
		btn_backup.setOnClickListener(this);
		btn_recover.setOnClickListener(this);
		tv_mac.setOnLongClickListener(this);
	}

	private String getMac() {
		File f = new File(PATH0);
		if (!f.exists())
			f = new File(PATH1);
		if (f.exists())
			try {
				FileInputStream fis = new FileInputStream(f);
				byte[] buffer = new byte[12];
				fis.read(buffer);
				if (buffer != null && buffer.length != 0) {
					StringBuilder buf = new StringBuilder();
					for (int j = 0; j < 12; ++j) {
						int i = buffer[j] & 0xff;
						if (i <= 0xf) {
							buf.append("0");
						}
						buf.append(Integer.toHexString(i)).append(" ");
					}
					String[] hexs = buf.toString().trim().split(" ");
					if (hexs.length != 12)
						return null;
					return hexs[11] + ":" + hexs[10] + ":" + hexs[6] + ":"
							+ hexs[5] + ":" + hexs[4] + ":" + hexs[3];
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		else
			return null;
		return null;
	}

	private byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}

	public byte[] hexStringToBytes(String hexString) {
		if (hexString == null || hexString.equals("")) {
			return null;
		}
		hexString = hexString.toUpperCase();
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++) {
			int pos = i * 2;
			d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
		}
		return d;
	}

	private void setMac() {
		File f = new File(PATH0);
		if (!f.exists())
			f = new File(PATH1);
		if (f.exists()) {
			try {
				FileInputStream fis = new FileInputStream(f);
				byte[] buffer = new byte[(int) f.length()];
				fis.read(buffer);
				buffer[11] = hexStringToBytes(et_mac0.getText().toString())[0];
				buffer[10] = hexStringToBytes(et_mac1.getText().toString())[0];
				buffer[6] = hexStringToBytes(et_mac2.getText().toString())[0];
				buffer[5] = hexStringToBytes(et_mac3.getText().toString())[0];
				buffer[4] = hexStringToBytes(et_mac4.getText().toString())[0];
				buffer[3] = hexStringToBytes(et_mac5.getText().toString())[0];
				FileOutputStream fos = new FileOutputStream(f);
				fos.write(buffer);
				fos.flush();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
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
		if (v.getId() == R.main.btn_change) {
			AlertDialog.Builder builder = new Builder(this);
			builder.setTitle("WARNING!!!");
			builder.setMessage(R.string.warning);
			builder.setPositiveButton("OK,Go on",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							setMac();
							dialog.dismiss();
							tv_mac.setText(getMac());
							Toast.makeText(GetMacActivity.this,
									"MAC Address Changed!", Toast.LENGTH_LONG)
									.show();
						}
					});
			builder.setNegativeButton("No,I ll backup first",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
			builder.create().show();
		}
		if (v.getId() == R.main.btn_backup) {
			int found = 0;
			try {
				File f0 = new File(PATH0);
				if (f0.exists()) {
					found++;
					File f01 = new File(PATH0 + ".backup");
					FileInputStream fis = new FileInputStream(f0);
					byte[] buffer = new byte[(int) f0.length()];
					fis.read(buffer);
					FileOutputStream fos = new FileOutputStream(f01);
					fos.write(buffer);
					fos.flush();
				}

				File f1 = new File(PATH1);
				if (f1.exists()) {
					found++;
					File f11 = new File(PATH1 + ".backup");
					FileInputStream fis = new FileInputStream(f1);
					byte[] buffer = new byte[(int) f1.length()];
					fis.read(buffer);
					FileOutputStream fos = new FileOutputStream(f11);
					fos.write(buffer);
					fos.flush();
				}
				if (found > 0)
					Toast.makeText(GetMacActivity.this, "Backup success!",
							Toast.LENGTH_LONG).show();
				else
					Toast.makeText(GetMacActivity.this, "Backup fail!",
							Toast.LENGTH_LONG).show();
			} catch (Exception e) {
				Log.e(TAG, e.getMessage());
			}
		}
		if (v.getId() == R.main.btn_recover) {
			int found = 0;
			try {
				File f0 = new File(PATH0 + ".backup");
				if (f0.exists()) {
					found++;
					File f01 = new File(PATH0);
					FileInputStream fis = new FileInputStream(f0);
					byte[] buffer = new byte[(int) f0.length()];
					fis.read(buffer);
					FileOutputStream fos = new FileOutputStream(f01);
					fos.write(buffer);
					fos.flush();
				}

				File f1 = new File(PATH1 + ".backup");
				if (f1.exists()) {
					found++;
					File f11 = new File(PATH1);
					FileInputStream fis = new FileInputStream(f1);
					byte[] buffer = new byte[(int) f1.length()];
					fis.read(buffer);
					FileOutputStream fos = new FileOutputStream(f11);
					fos.write(buffer);
					fos.flush();
				}
				if (found > 0)
					Toast.makeText(GetMacActivity.this, "Recover success!",
							Toast.LENGTH_LONG).show();
				else
					Toast.makeText(GetMacActivity.this, "Recover fail!",
							Toast.LENGTH_LONG).show();
			} catch (Exception e) {
				Log.e(TAG, e.getMessage());
			}
		}
	}
}