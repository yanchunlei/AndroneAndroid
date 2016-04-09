package com.sandstormweb.droneone.datamodels;

public class KeyValue
{
    private String key, value;

    public KeyValue(String key, String value)
    {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
