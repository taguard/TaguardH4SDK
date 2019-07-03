package com.moko.beaconxpro.able;

public interface ISlotDataAction {
    boolean isValid();

    void sendData();

    void upgdateProgress(int viewId, int progress);
}
