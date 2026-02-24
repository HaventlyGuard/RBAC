package Repositories.Interfaces;

import java.util.List;
import java.util.Optional;

public interface IRepository<T> {

    void add(T item);

    boolean remove(T item);

    boolean removeById(String id);

    Optional<T> findById(String id);

    List<T> findAll();

    int count();

    void clear();

    boolean exists(String id);
}
