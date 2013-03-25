package br.com.trancatela.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.bluetooth.BluetoothStateException;
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

public class BluetoothUtil implements DiscoveryListener {

	private static Object lock = new Object();
	public ArrayList<RemoteDevice> devices;
	
	public ArrayList<String> nomes;
	
	public ArrayList<Boolean> ativos;
	
	public int posicaoAtiva;
	
	DiscoveryAgent agent;
	
	int minRSSI;
	
	private RemoteDevice activeDevice;

	public BluetoothUtil() {
		devices = new ArrayList<RemoteDevice>();
		nomes = new ArrayList<String>();
	}

	public static void main(String[] args) {
		new BluetoothUtil().getDevices();
	}

	public String[] getDevices() {

		String[] saida = null;
		try {
			LocalDevice localDevice = LocalDevice.getLocalDevice();

			agent = localDevice.getDiscoveryAgent();
			agent.startInquiry(DiscoveryAgent.GIAC, this);

			try {
				synchronized (lock) {
					lock.wait();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
				return null;
			}
			
			System.out.println("Device Inquiry Completed. ");
			
			saida = (String[]) nomes.toArray(new String[nomes.size()]);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return saida;
	}
	
	public void execute(List<String> listaNomes, int rssi) {
		
		UUID[] uuidSet = new UUID[1];
		uuidSet[0] = new UUID(0x1105); // OBEX Object Push service
		minRSSI = rssi;
		int[] attrIDs = new int[] { 0x0100 // Service name
		};
		while(true){
		for (RemoteDevice device : devices) {
			try {
				ativos = new ArrayList<Boolean>(listaNomes.size());
				for (String nome : listaNomes) {
					if (device.getFriendlyName(false).equals(nome)) {
						
						activeDevice = device;
						agent.searchServices(attrIDs, uuidSet, device, this);
						
						try {
							synchronized (lock) {
								lock.wait();
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
							return;
						}
					}
				}
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			//System.out.println("Service search finished.");
		}
		}
	}

	public void deviceDiscovered(RemoteDevice btDevice, DeviceClass arg1) {
		String name = "";
		try {
			name = btDevice.getFriendlyName(false);
		} catch (Exception e) {
			name = btDevice.getBluetoothAddress();
		}
		
		devices.add(btDevice);
		nomes.add(name);
		System.out.println("device found: " + name);

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
				//System.out.println("service " + serviceName.getValue() + " found " + url);

				if (serviceName.getValue().equals("OBEX Object Push")) {
					sendMessageToDevice(url);
				}
			} else {
				//System.out.println("service found " + url);
			}

		}
	}

	private void sendMessageToDevice(String serverURL) {
		 
		try {
			//System.out.println("Connecting to " + serverURL);

			ClientSession clientSession = (ClientSession) Connector.open(serverURL);

			PollRSSI();

			clientSession.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	public void PollRSSI() throws Exception {
		int rssi = 0;

		rssi = RemoteDeviceHelper.readRSSI(activeDevice);
		System.out.println(activeDevice.getFriendlyName(false) + " RSSI = " + rssi);
		ativos.add(rssi >= minRSSI);
		int totalInativos = 0;
		for(Boolean ativo : ativos){
			if (!ativo){
				totalInativos++;
			}
		}
		if (totalInativos == ativos.size()){
			trancaTela();
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