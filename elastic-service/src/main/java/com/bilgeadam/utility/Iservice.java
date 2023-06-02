package com.bilgeadam.utility;

import java.util.List;
import java.util.Optional;

/**
 *
 * @param <T> -> Entity nin adı Type
 * @param <ID> -> Entity nin @Id ile işaretlenmiş alanını DataType ı
 */
public interface Iservice<T, ID> {
    T save(T t);
    Iterable<T> saveAll(Iterable<T> entities);
    T update(T t);
    void delete(T t);
    void deleteById(ID id);
    Optional<T> findById(ID id);
    Iterable<T> findAll();
}
