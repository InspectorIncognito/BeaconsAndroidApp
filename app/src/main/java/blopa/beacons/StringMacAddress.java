package blopa.beacons;

/**
 * Created by Blopa on 12-01-2017.
 */

public class StringMacAddress {
    String majorMinor;
    String mAddress;

    StringMacAddress(String string, String mAddress){
        this.majorMinor = string;
        this.mAddress = mAddress;
    }

    @Override
    public String toString(){
        return this.majorMinor;
    }
}