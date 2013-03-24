package br.com.trancatela;

import java.io.IOException;
import java.util.ArrayList;

import javax.bluetooth.DataElement;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.obex.ClientSession;


import com.intel.bluetooth.RemoteDeviceHelper;

public class Principal implements DiscoveryListener {

	private static Object lock = new Object();
	public ArrayList<RemoteDevice> devices;

	static RemoteDevice Android_Device, Nokia;

	public Principal() {
		devices = new ArrayList<RemoteDevice>();
	}

	public static void main(String[] args) {
		Principal listener = new Principal();

		try {
			LocalDevice localDevice = LocalDevice.getLocalDevice();

			DiscoveryAgent agent = localDevice.getDiscoveryAgent();
			agent.startInquiry(DiscoveryAgent.GIAC, listener);

			try {
				synchronized (lock) {
					lock.wait();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
				return;
			}

			System.out.println("Device Inquiry Completed. ");

			UUID[] uuidSet = new UUID[1];
			uuidSet[0] = new UUID(0x1105); // OBEX Object Push service

			int[] attrIDs = new int[] { 0x0100 // Service name
			};

			for (RemoteDevice device : listener.devices) {
				agent.searchServices(attrIDs, uuidSet, device, listener);

				try {
					synchronized (lock) {
						lock.wait();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
					return;
				}

				System.out.println("Service search finished.");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void deviceDiscovered(RemoteDevice btDevice, DeviceClass arg1) {
		String name = "";
		try {
			name = btDevice.getFriendlyName(false);
		} catch (Exception e) {
			name = btDevice.getBluetoothAddress();
		}
		if(name.equals("MotoA953")){
		devices.add(btDevice);
		Nokia = devices.get(0);
		System.out.println("device found: " + name);
		}

	}

	public void inquiryCompleted(int arg0) {
		synchronized (lock) {
			lock.notify();
		}
	}

	public void serviceSearchCompleted(int arg0, int arg1) {
		synchronized (lock) {
			lock.notify();
		}
	}

	
	public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
		for (int i = 0; i < servRecord.length; i++) {
			String url = servRecord[i].getConnectionURL(
					ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
			if (url == null) {
				continue;
			}
			DataElement serviceName = servRecord[i].getAttributeValue(0x0100);
			if (serviceName != null) {
				System.out.println("service " + serviceName.getValue()
						+ " found " + url);

				if (serviceName.getValue().equals("OBEX Object Push")) {
					sendMessageToDevice(url);
				}
			} else {
				System.out.println("service found " + url);
			}

		}
	}

	private static void sendMessageToDevice(String serverURL) {
		try {
			System.out.println("Connecting to " + serverURL);

			ClientSession clientSession = (ClientSession) Connector.open(serverURL);

			PollRSSI();

			clientSession.disconnect(null);

			clientSession.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void PollRSSI() {
		int rssi = 0;

		try {

			while (true) {
				try {
					if (Nokia != null)
						rssi = RemoteDeviceHelper.readRSSI(Nokia);
					System.out.println("Nokia RSSI = " + rssi);
					if (rssi < -5) {
						trancaTela();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				Thread.sleep(500);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void trancaTela() throws IOException {
		String os = System.getProperty("os.name").toLowerCase();
		// MacOS X
		if (os.equals("mac os x")) {
			Runtime.getRuntime().exec("/System/Library/Frameworks/ScreenSaver.framework/Resources/ScreenSaverEngine.app/Contents/MacOS/ScreenSaverEngine");
		} else if (os.equals("win")) {
			Runtime.getRuntime().exec("ScreenSaver /s");
		} else if (os.equals("nux")) {
			Runtime.getRuntime().exec("xscreensaver-command -activate");
		}
	}
}