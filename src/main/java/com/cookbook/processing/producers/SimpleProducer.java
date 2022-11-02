package com.cookbook.processing.producers;

import com.cookbook.processing.interfaces.Decoder;
import com.cookbook.processing.interfaces.Producer;

import java.util.ArrayList;
import java.util.List;

public class SimpleProducer implements Producer {

    private List<Decoder> decoderList = new ArrayList<>();

    public SimpleProducer(){}


    @Override
    public void notify(byte[] data) {
        for(Decoder decoder : decoderList) {
            decoder.setData(data);
        }
    }

    @Override
    public void addDecoder(Decoder decoder) {
        this.decoderList.add(decoder);
    }
}
