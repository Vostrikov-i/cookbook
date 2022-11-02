package com.cookbook.processing.interfaces;

public interface Consumer<T> {

    void notify(T data);

}
