package com.cookbook.processing.interfaces;

public interface Producer {

    void notify(byte[] data);
    void addDecoder(Decoder decoder);
}
