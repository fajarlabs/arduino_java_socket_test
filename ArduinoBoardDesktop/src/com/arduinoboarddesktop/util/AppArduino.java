package com.arduinoboarddesktop.util;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Enumeration;

import javax.swing.JTextArea;

/**
 * 
 * @author masfajar
 *
 */
public class AppArduino implements SerialPortEventListener {
	// Definisikasn serial port
	SerialPort serialPort = null;

	// Definisi array serial port
	private static final String PORT_NAMES[] = {
			// "/dev/tty.usbmodem", // Mac OS X
			// "/dev/usbdev", // Linux
			// "/dev/tty", // Linux
			// "/dev/serial", // Linux
			"COM3", // Windows
	};

	private String appName;
	private BufferedReader input;
	private OutputStream output;

	// Port open timeout
	private static final int TIME_OUT = 1000; 
	// Arduino serial port
	private static final int DATA_RATE = 9600; 
	
	private JTextArea textarea;

	/**
	 * 
	 * @param textarea
	 */
	public AppArduino(JTextArea textarea) {
		appName = getClass().getName();
		this.textarea = textarea;
	}
	
	public AppArduino() {
		appName = getClass().getName();
	}
	
	// Initialisasi aplikasi
	public boolean initialize() {
		try {
			// Port id
			CommPortIdentifier portId = null;
			// Get port identifier
			Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();
			
			// Masukkan informasi kedalam textarea
			this.textarea.append("Mencoba menghubungkan :\n");
			this.textarea.setCaretPosition(this.textarea.getDocument().getLength());
			
			// Enumerate system ports and try connecting to Arduino over each
			while (portId == null && portEnum.hasMoreElements()) {
				
				// Iterate through your host computer's serial port IDs
				CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
				
				// Update informasi
				this.textarea.append("port" + currPortId.getName()+"\n");
				this.textarea.setCaretPosition(this.textarea.getDocument().getLength());
				
				// Cari sambungan port
				for (String portName : PORT_NAMES) {
					if (currPortId.getName().equals(portName) || currPortId.getName().startsWith(portName)) {

						// Try to connect to the Arduino on this port
						// Open serial port
						serialPort = (SerialPort) currPortId.open(appName, TIME_OUT);
						portId = currPortId;
						
						this.textarea.append("Terhubung dengan port" + currPortId.getName()+"\n");
						this.textarea.setCaretPosition(this.textarea.getDocument().getLength());
						
						// Jika sudah ditemukan portnya lalu hentikan iterasi
						break;
					}
				}
			}

			// Jika port id atau serial portnya null
			if (portId == null || serialPort == null) {
				
				// Update informasi
				this.textarea.append("Oops... Tidak dapat terhubung dengan arduino!!!\n");
				this.textarea.setCaretPosition(this.textarea.getDocument().getLength());
				
				// Hentikan program
				return false;
			}

			// set port parameters
			serialPort.setSerialPortParams(DATA_RATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);

			// add event listeners
			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);

			// Give the Arduino some time
			try {
				Thread.sleep(2000);
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}

			// Success 
			return true;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Stop
		return false;
	}

	public void sendData(String data) {
		try {
			this.textarea.append("Mengirim data: '" + data + "' \n");
			this.textarea.setCaretPosition(this.textarea.getDocument().getLength());

			// open the streams and send the "y" character
			output = serialPort.getOutputStream();
			output.write(data.getBytes());
		} catch (Exception e) {
			System.err.println(e.toString());
			System.exit(0);
		}
	}

	//
	// This should be called when you stop using the port
	//
	public synchronized void close() {
		if (serialPort != null) {
			serialPort.removeEventListener();
			serialPort.close();
		}
	}

	//
	// Handle serial port event
	//
	@Override
	public synchronized void serialEvent(SerialPortEvent oEvent) {
		// System.out.println("Event received: " + oEvent.toString());
		try {
			switch (oEvent.getEventType()) {
			case SerialPortEvent.DATA_AVAILABLE:
				if (input == null) {
					input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
				}
				String inputLine = input.readLine();
				this.textarea.append(inputLine+"\n");
				this.textarea.setCaretPosition(this.textarea.getDocument().getLength());
				//System.out.println(inputLine);
				break;

			default:
				break;
			}
		} catch (Exception e) {
			System.err.println(e.toString());
		}
	}

//  Contoh jika menggunakan script langsung

//	public static void main(String[] args) throws Exception {
//		ArduinoTest1 test = new ArduinoTest1();
//		if (test.initialize()) {
//			test.sendData("y");
//			try {
//				Thread.sleep(2000);
//			} catch (InterruptedException ie) {
//			}
//			test.sendData("n");
//			try {
//				Thread.sleep(2000);
//			} catch (InterruptedException ie) {
//			}
//			test.close();
//		}
//
//		// Wait 5 seconds then shutdown
//		try {
//			Thread.sleep(2000);
//		} catch (InterruptedException ie) {
//		}
//	}
}
