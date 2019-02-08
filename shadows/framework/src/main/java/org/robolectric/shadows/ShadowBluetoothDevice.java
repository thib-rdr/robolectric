package org.robolectric.shadows;

import static android.bluetooth.BluetoothDevice.BOND_NONE;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.O;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.shadow.api.Shadow.directlyOn;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.IBluetooth;
import android.content.Context;
import android.os.Handler;
import android.os.ParcelUuid;
import java.util.ArrayList;
import java.util.List;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;

@Implements(BluetoothDevice.class)
public class ShadowBluetoothDevice {

  public static BluetoothDevice newInstance(String address) {
    return ReflectionHelpers.callConstructor(
        BluetoothDevice.class, ReflectionHelpers.ClassParameter.from(String.class, address));
  }

  @RealObject private BluetoothDevice realBluetoothDevice;
  private String name;
  private ParcelUuid[] uuids;
  private int bondState = BOND_NONE;
  private boolean createdBond = false;
  private boolean fetchUuidsWithSdpResult = false;
  private int fetchUuidsWithSdpCount = 0;
  private int type = BluetoothDevice.DEVICE_TYPE_UNKNOWN;
  private final List<BluetoothGatt> bluetoothGatts = new ArrayList<>();

  /**
   * Implements getService() in the same way the original method does, but ignores any Exceptions
   * from invoking {@link android.bluetooth.BluetoothAdapter#getBluetoothService}.
   */
  @Implementation
  protected static IBluetooth getService() {
    // Attempt to call the underlying getService method, but ignore any Exceptions. This allows us
    // to easily create BluetoothDevices for testing purposes without having any actual Bluetooth
    // capability.
    try {
      return directlyOn(BluetoothDevice.class, "getService");
    } catch (Exception e) {
      // No-op.
    }
    return null;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Implementation
  protected String getName() {
    return name;
  }

  /** Sets the return value for {@link BluetoothDevice#getType}. */
  public void setType(int type) {
    this.type = type;
  }

  /**
   * Overrides behavior of {@link BluetoothDevice#getType} to return pre-set result.
   *
   * @return Value set by calling {@link ShadowBluetoothDevice#setType}. If setType has not
   *     previously been called, will return BluetoothDevice.DEVICE_TYPE_UNKNOWN.
   */
  @Implementation(minSdk = JELLY_BEAN_MR2)
  protected int getType() {
    return type;
  }

  /** Sets the return value for {@link BluetoothDevice#getUuids}. */
  public void setUuids(ParcelUuid[] uuids) {
    this.uuids = uuids;
  }

  /**
   * Overrides behavior of {@link BluetoothDevice#getUuids} to return pre-set result.
   *
   * @returns Value set by calling {@link ShadowBluetoothDevice#setUuids}. If setUuids has not
   *     previously been called, will return null.
   */
  @Implementation
  protected ParcelUuid[] getUuids() {
    return uuids;
  }

  /** Sets value of bond state for {@link BluetoothDevice#getBondState}. */
  public void setBondState(int bondState) {
    this.bondState = bondState;
  }

  /**
   * Overrides behavior of {@link BluetoothDevice#getBondState} to return pre-set result.
   *
   * @returns Value set by calling {@link ShadowBluetoothDevice#setBondState}. If setBondState has
   *     not previously been called, will return {@link BluetoothDevice#BOND_NONE} to indicate the
   *     device is not bonded.
   */
  @Implementation
  protected int getBondState() {
    return bondState;
  }

  /** Sets whether this device has been bonded with. */
  public void setCreatedBond(boolean createdBond) {
    this.createdBond = createdBond;
  }

  /** Returns whether this device has been bonded with. */
  @Implementation
  protected boolean createBond() {
    return createdBond;
  }

  /** Sets value of the return result for {@link BluetoothDevice#fetchUuidsWithSdp}. */
  public void setFetchUuidsWithSdpResult(boolean fetchUuidsWithSdpResult) {
    this.fetchUuidsWithSdpResult = fetchUuidsWithSdpResult;
  }

  /**
   * Overrides behavior of {@link BluetoothDevice#fetchUuidsWithSdp}. This method updates the
   * counter which counts the number of invocations of this method.
   *
   * @returns Value set by calling {@link ShadowBluetoothDevice#setFetchUuidsWithSdpResult}. If not
   *     previously set, will return false by default.
   */
  @Implementation
  protected boolean fetchUuidsWithSdp() {
    fetchUuidsWithSdpCount++;
    return fetchUuidsWithSdpResult;
  }

  /** Returns the number of times fetchUuidsWithSdp has been called. */
  public int getFetchUuidsWithSdpCount() {
    return fetchUuidsWithSdpCount;
  }

  @Implementation(minSdk = JELLY_BEAN_MR2)
  protected BluetoothGatt connectGatt(
      Context context, boolean autoConnect, BluetoothGattCallback callback) {
    return connectGatt(callback);
  }

  @Implementation(minSdk = M)
  protected BluetoothGatt connectGatt(
      Context context, boolean autoConnect, BluetoothGattCallback callback, int transport) {
    return connectGatt(callback);
  }

  @Implementation(minSdk = O)
  protected BluetoothGatt connectGatt(
      Context context,
      boolean autoConnect,
      BluetoothGattCallback callback,
      int transport,
      int phy,
      Handler handler) {
    return connectGatt(callback);
  }

  private BluetoothGatt connectGatt(BluetoothGattCallback callback) {
    BluetoothGatt bluetoothGatt = ShadowBluetoothGatt.newInstance(realBluetoothDevice);
    bluetoothGatts.add(bluetoothGatt);
    ShadowBluetoothGatt shadowBluetoothGatt = Shadow.extract(bluetoothGatt);
    shadowBluetoothGatt.setGattCallback(callback);
    return bluetoothGatt;
  }

  /**
   * Returns all {@link BluetoothGatt} objects created by calling {@link
   * ShadowBluetoothDevice#connectGatt}.
   */
  public List<BluetoothGatt> getBluetoothGatts() {
    return bluetoothGatts;
  }

  /**
   * Causes {@link BluetoothGattCallback#onConnectionStateChange to be called for every GATT client.
   * @param status Status of the GATT operation
   * @param newState The new state of the GATT profile
   */
  public void simulateGattConnectionChange(int status, int newState) {
    for (BluetoothGatt bluetoothGatt : bluetoothGatts) {
      ShadowBluetoothGatt shadowBluetoothGatt = shadowOf(bluetoothGatt);
      BluetoothGattCallback gattCallback = shadowBluetoothGatt.getGattCallback();
      gattCallback.onConnectionStateChange(bluetoothGatt, status, newState);
    }
  }
}
