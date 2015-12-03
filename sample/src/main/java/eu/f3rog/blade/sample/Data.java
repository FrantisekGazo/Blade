package eu.f3rog.blade.sample;

import java.io.Serializable;

/**
 * Class {@link Data}
 *
 * @author FrantisekGazo
 * @version 2015-11-28
 */
public class Data implements Serializable {

    private int mNumber;
    private String mText;

    public Data(int number, String text) {
        mNumber = number;
        mText = text;
    }

    public int getNumber() {
        return mNumber;
    }

    public String getText() {
        return mText;
    }
}
