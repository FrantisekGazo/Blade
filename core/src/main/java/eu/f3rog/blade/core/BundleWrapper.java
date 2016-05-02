package eu.f3rog.blade.core;

import android.os.Bundle;
import android.os.Parcelable;
import android.util.SparseArray;

import java.io.Serializable;

/**
 * Class {@link BundleWrapper}
 *
 * @author FrantisekGazo
 * @version 2015-12-01
 */
public class BundleWrapper {

    private Bundle mBundle;

    public BundleWrapper(Bundle bundle) {
        mBundle = bundle;
    }

    public BundleWrapper() {
        this(new Bundle());
    }

    public Bundle getBundle() {
        return mBundle;
    }

    public static BundleWrapper from(Bundle bundle) {
        return new BundleWrapper(bundle);
    }

    //region PUT ----------------------------

    // Serializable
    public <T extends Serializable> void put(String key, T object) {
        mBundle.putSerializable(key, object);
    }

    // Parcelable
    public <T extends Parcelable> void put(String key, T object) {
        mBundle.putParcelable(key, object);
    }

    public <T extends Parcelable> void put(String key, T[] object) {
        mBundle.putParcelableArray(key, object);
    }

    public void put(String key, SparseArray<? extends Parcelable> object) {
        mBundle.putSparseParcelableArray(key, object);
    }

    // Bundle
    public void put(String key, Bundle object) {
        mBundle.putBundle(key, object);
    }

    // boolean
    public void put(String key, boolean object) {
        mBundle.putBoolean(key, object);
    }

    public void put(String key, boolean[] object) {
        mBundle.putBooleanArray(key, object);
    }

    // byte
    public void put(String key, byte object) {
        mBundle.putByte(key, object);
    }

    public void put(String key, byte[] object) {
        mBundle.putByteArray(key, object);
    }

    // char
    public void put(String key, char object) {
        mBundle.putChar(key, object);
    }

    public void put(String key, char[] object) {
        mBundle.putCharArray(key, object);
    }

    // CharSequence
    public void put(String key, CharSequence object) {
        mBundle.putCharSequence(key, object);
    }

    public void put(String key, CharSequence[] object) {
        mBundle.putCharSequenceArray(key, object);
    }

    // int
    public void put(String key, int object) {
        mBundle.putInt(key, object);
    }

    public void put(String key, int[] object) {
        mBundle.putIntArray(key, object);
    }

    // short
    public void put(String key, short object) {
        mBundle.putShort(key, object);
    }

    public void put(String key, short[] object) {
        mBundle.putShortArray(key, object);
    }

    // long
    public void put(String key, long object) {
        mBundle.putLong(key, object);
    }

    public void put(String key, long[] object) {
        mBundle.putLongArray(key, object);
    }

    // float
    public void put(String key, float object) {
        mBundle.putFloat(key, object);
    }

    public void put(String key, float[] object) {
        mBundle.putFloatArray(key, object);
    }

    // double
    public void put(String key, double object) {
        mBundle.putDouble(key, object);
    }

    public void put(String key, double[] object) {
        mBundle.putDoubleArray(key, object);
    }

    // String
    public void put(String key, String object) {
        mBundle.putString(key, object);
    }

    public void put(String key, String[] object) {
        mBundle.putStringArray(key, object);
    }

    //endregion PUT ----------------------------

    //region GET ----------------------------

    // Serializable
    public <T extends Serializable> T get(String key, T object) {
        return (T) mBundle.getSerializable(key);
    }

    // Parcelable
    public <T extends Parcelable> T get(String key, T object) {
        return (T) mBundle.getParcelable(key);
    }

    public <T extends Parcelable> T[] get(String key, T[] object) {
        return (T[]) mBundle.getParcelableArray(key);
    }

    public <T extends Parcelable> SparseArray<T> get(String key, SparseArray<T> object) {
        return mBundle.getSparseParcelableArray(key);
    }

    // Bundle
    public Bundle get(String key, Bundle object) {
        return mBundle.getBundle(key);
    }

    // boolean
    public boolean get(String key, boolean object) {
        return mBundle.getBoolean(key);
    }

    public boolean[] get(String key, boolean[] object) {
        return mBundle.getBooleanArray(key);
    }

    // byte
    public byte get(String key, byte object) {
        return mBundle.getByte(key);
    }

    public byte[] get(String key, byte[] object) {
        return mBundle.getByteArray(key);
    }

    // char
    public char get(String key, char object) {
        return mBundle.getChar(key);
    }

    public char[] get(String key, char[] object) {
        return mBundle.getCharArray(key);
    }

    // CharSequence
    public CharSequence get(String key, CharSequence object) {
        return mBundle.getCharSequence(key);
    }

    public CharSequence[] get(String key, CharSequence[] object) {
        return mBundle.getCharSequenceArray(key);
    }

    // int
    public int get(String key, int object) {
        return mBundle.getInt(key);
    }

    public int[] get(String key, int[] object) {
        return mBundle.getIntArray(key);
    }

    // short
    public short get(String key, short object) {
        return mBundle.getShort(key);
    }

    public short[] get(String key, short[] object) {
        return mBundle.getShortArray(key);
    }

    // long
    public long get(String key, long object) {
        return mBundle.getLong(key);
    }

    public long[] get(String key, long[] object) {
        return mBundle.getLongArray(key);
    }

    // float
    public float get(String key, float object) {
        return mBundle.getFloat(key);
    }

    public float[] get(String key, float[] object) {
        return mBundle.getFloatArray(key);
    }

    // double
    public double get(String key, double object) {
        return mBundle.getDouble(key);
    }

    public double[] get(String key, double[] object) {
        return mBundle.getDoubleArray(key);
    }

    // String
    public String get(String key, String object) {
        return mBundle.getString(key);
    }

    public String[] get(String key, String[] object) {
        return mBundle.getStringArray(key);
    }

    //endregion GET ----------------------------

}
