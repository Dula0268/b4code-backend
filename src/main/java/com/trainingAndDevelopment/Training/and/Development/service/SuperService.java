package com.trainingAndDevelopment.Training.and.Development.service;
import java.util.List;

public interface SuperService<T, ID> {

    List<T> saveAll(List<T> list);

    T create(T t);

    T findById(ID key);

    List<T> findAll();

}
