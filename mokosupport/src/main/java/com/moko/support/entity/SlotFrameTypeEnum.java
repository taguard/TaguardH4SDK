package com.moko.support.entity;

import java.io.Serializable;

public enum SlotFrameTypeEnum implements Serializable {
    UID("Eddystone-UID", "00"),
    URL("Eddystone-URL", "10"),
    TLM("Eddystone-TLM", "20"),
    DEVICE("Device Info", "40"),
    IBEACON("iBeacon", "50"),
    AXIS("3-axis Accelerometer", "60"),
    TH("H&T", "70"),
    NO_DATA("NO DATA", "FF");
    private String frameType;
    private String showName;

    SlotFrameTypeEnum(String showName, String frameType) {
        this.frameType = frameType;
        this.showName = showName;
    }


    public String getFrameType() {
        return frameType;
    }

    public String getShowName() {
        return showName;
    }

    public static SlotFrameTypeEnum fromFrameType(int frameType) {
        for (SlotFrameTypeEnum frameTypeEnum : SlotFrameTypeEnum.values()) {
            if (Integer.parseInt(frameTypeEnum.getFrameType(), 16) == frameType) {
                return frameTypeEnum;
            }
        }
        return null;
    }

    public static SlotFrameTypeEnum fromEnumOrdinal(int ordinal) {
        for (SlotFrameTypeEnum frameTypeEnum : SlotFrameTypeEnum.values()) {
            if (frameTypeEnum.ordinal() == ordinal) {
                return frameTypeEnum;
            }
        }
        return null;
    }
}
