package it.tangodev.ble;

import it.tangodev.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bluez.GattCharacteristic1;
import org.dbus.PropertiesChangedSignal.PropertiesChanged;
import org.freedesktop.DBus.Properties;
import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.Path;
import org.freedesktop.dbus.Variant;
import org.freedesktop.dbus.exceptions.DBusException;

//@SuppressWarnings("rawtypes")
public class BleCharacteristic implements GattCharacteristic1, Properties {

	private static final String GATT_CHARACTERISTIC_INTERFACE = "org.bluez.GattCharacteristic1";
	private static final String CHARACTERISTIC_SERVICE_PROPERTY_KEY = "Service";
	private static final String CHARACTERISTIC_UUID_PROPERTY_KEY = "UUID";
	private static final String CHARACTERISTIC_FLAGS_PROPERTY_KEY = "Flags";
	private static final String CHARACTERISTIC_DESCRIPTORS_PROPERTY_KEY = "Descriptors";
	public static final String CHARACTERISTIC_VALUE_PROPERTY_KEY = "Value";
	
	private BleService service = null;
	private String uuid = "0000180d-0000-1000-8000-00805f9b34fb";
	private List<String> flags;
	private String path = "/it/tangodev/openlaptimer/service/characteristic";
	private boolean isNotifying = false;
	private byte[] value;
	
	public BleCharacteristic(BleService service) {
		flags = new ArrayList<String>();
		flags.add("read");
		flags.add("write");
		flags.add("notify");
		
		this.service = service;
	}
	
	public void export(DBusConnection dbusConnection) throws DBusException {
		dbusConnection.exportObject(this.getPath().toString(), this);
	}
	
	public Path getPath() {
		return new Path(path);
	}
	
	public Map<String, Map<String, Variant>> getProperties() {
		System.out.println("Characteristic -> getCharacteristicProperties");
		
		Map<String, Variant> characteristicMap = new HashMap<String, Variant>();
		
		Variant<Path> servicePathProperty = new Variant<Path>(service.getPath());
		characteristicMap.put(CHARACTERISTIC_SERVICE_PROPERTY_KEY, servicePathProperty);
		
		Variant<String> uuidProperty = new Variant<String>(this.uuid);
		characteristicMap.put(CHARACTERISTIC_UUID_PROPERTY_KEY, uuidProperty);
		
		Variant<String[]> flagsProperty = new Variant<String[]>(Utils.getStringArrayFromList(this.flags));
		characteristicMap.put(CHARACTERISTIC_FLAGS_PROPERTY_KEY, flagsProperty);
		
		// TODO manage Descriptors
		Variant<Path[]> descriptorsPatProperty = new Variant<Path[]>(new Path[0]);
		characteristicMap.put(CHARACTERISTIC_DESCRIPTORS_PROPERTY_KEY, descriptorsPatProperty);
		
		Map<String, Map<String, Variant>> externalMap = new HashMap<String, Map<String, Variant>>();
		externalMap.put(GATT_CHARACTERISTIC_INTERFACE, characteristicMap);
		
		return externalMap;
	}
	
	public void sendNotification(byte[] newValue) {
		this.value = newValue;
		
		try {
			DBusConnection dbusConnection = DBusConnection.getConnection(DBusConnection.SYSTEM);
			
			Variant<byte[]> signalValueVariant = new Variant<byte[]>(this.value);
			Map<String, Variant> signalValue = new HashMap<String, Variant>();
			signalValue.put(BleCharacteristic.CHARACTERISTIC_VALUE_PROPERTY_KEY, signalValueVariant);
			
			PropertiesChanged signal = new PropertiesChanged(this.getPath().toString(), GATT_CHARACTERISTIC_INTERFACE, signalValue, new ArrayList<String>());
			dbusConnection.sendSignal(signal);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean isRemote() {
		return false;
	}

	@Override
	public byte[] ReadValue(Map<String, Variant> option) {
		System.out.println("Characteristic Read option[" + option + "]");
		
//		byte[] b = value.getBytes(Charset.forName("ASCII"));
		return value;
	}

	@Override
	public void WriteValue(byte[] value, Map<String, Variant> option) {
		System.out.println("Characteristic Write option[" + option + "]");
//		this.value = new String(value, Charset.forName("ASCII"));
		this.value = value;
	}

	@Override
	public void StartNotify() {
		if(isNotifying) {
			System.out.println("Characteristic already notifying");
			return;
		}
		this.isNotifying = true;
	}

	@Override
	public void StopNotify() {
		if(!isNotifying) {
			System.out.println("Characteristic already not notifying");
			return;
		}
		this.isNotifying = false;
	}
	
	@Override
	public <A> A Get(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <A> void Set(String arg0, String arg1, A arg2) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public Map<String, Variant> GetAll(String interfaceName) {
		if(GATT_CHARACTERISTIC_INTERFACE.equals(interfaceName)) {
			return this.getProperties().get(GATT_CHARACTERISTIC_INTERFACE);
		}
		throw new RuntimeException("Interfaccia sbagliata [interface_name=" + interfaceName + "]");
	}
	
}
