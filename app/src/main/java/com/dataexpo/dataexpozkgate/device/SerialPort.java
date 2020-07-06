package com.dataexpo.dataexpozkgate.device;

import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SerialPort {
 	/* Do not remove or rename the field fd: it is used by native method close(); */

	/*emulator.exe -avd AVD5 -netspeed full -netdelay none -qemu -serial COM1*/
	/*am start -n com.agivision.device.concentrator/com.agivision.concentrator.application.Register*/
	/*am force -stop com.agivision.device.concentrator*/
	private FileDescriptor fd;

	public SerialPort(File device, int baudrate) throws SecurityException, IOException {

		/* Check access permission */

		/* if path throw SecurityException must be to ->
		adb shell
  		cd /dev
		chmod 777 ttyS1*/
		if (!device.canRead() || !device.canWrite()) {
			try {
				Process exec = Runtime.getRuntime().exec("su /system/bin");
				String path = device.getAbsolutePath();
				String cmd = "chmod 777 " + path + "\n" + "exit\n";

				exec.getOutputStream().write(cmd.getBytes());

				Log.i(device.toString(), "su /system/bin");
			} catch (Exception e) {
				e.printStackTrace();
				throw new IOException();
			}
		}

		if (!device.canRead() || !device.canWrite()) {
			throw new SecurityException();
		}

		Log.i(device.toString(), "1 try to open");

		fd = open(device.getAbsolutePath(), baudrate);
		if (fd == null) {
			Log.e(device.toString(), "native open returns null");
			throw new IOException();
		}

		Log.i(device.toString(), "2 try to open");
	}

	public InputStream getInputStream() {
		return new FileInputStream(fd);
	}
	public OutputStream getOutputStream() {
		return new FileOutputStream(fd);
	}

	private native static FileDescriptor open(String path, int baudrate);
	public native void close();
	static {
		System.loadLibrary("serial_port");
	}
}
