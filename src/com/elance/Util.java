package com.elance;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;

public class Util {
	private static final String TAG = "com.elance.Util";
	public static final String BUSYBOX_PATH = "/data/data/com.elance/busybox";
	public static boolean changeable = false;

	public static int getassetsfile(Context context, String fileName,
			File tagFile) {
		int retVal = 0;
		try {
			InputStream in = context.getAssets().open(fileName);
			if (in.available() == 0) {
				return retVal;
			}
			FileOutputStream out = new FileOutputStream(tagFile);
			int read;
			byte[] buffer = new byte[4096];
			while ((read = in.read(buffer)) > 0) {
				out.write(buffer, 0, read);
			}
			out.close();
			in.close();
			retVal = 1;
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
		return retVal;
	}

	public static String getMac(Context context) {
		String mac = null;
		String[] paths = runCommand(BUSYBOX_PATH + " find -name nvs_map.bin");
		if (paths != null && paths.length > 0)
			try {
				FileInputStream fis = new FileInputStream(paths[0]);
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
					if (hexs.length == 12) {
						changeable = true;
						mac = hexs[11] + ":" + hexs[10] + ":" + hexs[6] + ":"
								+ hexs[5] + ":" + hexs[4] + ":" + hexs[3];
					}
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		if (mac == null) {
			WifiManager wfm = (WifiManager) context
					.getSystemService(Context.WIFI_SERVICE);
			mac = wfm.getConnectionInfo().getMacAddress();
		}
		return mac;
	}

	public static void setMac(String mac0, String mac1, String mac2,
			String mac3, String mac4, String mac5) {
		String[] paths = runCommand(BUSYBOX_PATH + " find -name nvs_map.bin");
		if (paths != null) {
			for (String p : paths) {
				runCommand(BUSYBOX_PATH + " cp " + p + " " + p + ".backup");
				try {
					File f = new File(p);
					FileInputStream fis = new FileInputStream(f);
					byte[] buffer = new byte[(int) f.length()];
					fis.read(buffer);
					buffer[11] = hexStringToBytes(mac0)[0];
					buffer[10] = hexStringToBytes(mac1)[0];
					buffer[6] = hexStringToBytes(mac2)[0];
					buffer[5] = hexStringToBytes(mac3)[0];
					buffer[4] = hexStringToBytes(mac4)[0];
					buffer[3] = hexStringToBytes(mac5)[0];
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
	}

	public static boolean backup() {
		String[] paths = runCommand(BUSYBOX_PATH + " find -name nvs_map.bin");
		if (paths != null && paths.length > 0) {
			for (String p : paths) {
				runCommand(BUSYBOX_PATH + " cp " + p + " " + p + ".backup");
			}
			return true;
		} else
			return false;
	}

	public static boolean recover() {
		String[] paths = runCommand(BUSYBOX_PATH
				+ " find -name nvs_map.bin.backup");
		if (paths != null && paths.length > 0) {
			for (String p : paths) {
				runCommand(BUSYBOX_PATH + " cp " + p + " "
						+ p.substring(0, p.lastIndexOf(".")));
			}
			return true;
		} else
			return false;
	}

	private static byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}

	public static byte[] hexStringToBytes(String hexString) {
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

	public static String[] runCommand(String command) {
		Log.e("DEBUG", command);
		Process process = null;
		DataOutputStream os = null;
		String path = null;
		try {
			process = Runtime.getRuntime().exec("su");
			os = new DataOutputStream(process.getOutputStream());
			os.writeBytes(command + "\n");
			os.writeBytes("exit\n");
			os.flush();
			InputStream is = process.getInputStream();
			byte[] bs = new byte[4096];
			is.read(bs);
			path = new String(bs);
			process.waitFor();
		} catch (Exception e) {
			Log.e("DEBUG",
					"Unexpected error - Here is what I know: " + e.getMessage());
		} finally {
			try {
				if (os != null) {
					os.close();
				}
				process.destroy();
			} catch (Exception e) {
				// nothing
			}
		}
		String[] paths = null;
		if (path != null && !path.trim().equals("")) {
			paths = path.substring(0, path.lastIndexOf("\n")).split("\n");
		}
		return paths;
	}
}
