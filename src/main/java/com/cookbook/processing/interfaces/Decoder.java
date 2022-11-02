package com.cookbook.processing.interfaces;

public interface Decoder<T> {

    void setData(byte[] data);
    void setConsumer(Consumer<T> consumer);
}
