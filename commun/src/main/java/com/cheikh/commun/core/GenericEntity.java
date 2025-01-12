package com.cheikh.commun.core;

public interface GenericEntity<T> {
    // update current instance with provided data
    void update(T source);

    Long getId();

    T createNewInstance();
}
