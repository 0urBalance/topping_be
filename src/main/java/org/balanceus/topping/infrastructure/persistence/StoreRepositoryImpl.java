package org.balanceus.topping.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.balanceus.topping.domain.model.Store;
import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.domain.repository.StoreRepository;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StoreRepositoryImpl implements StoreRepository {

    private final StoreJpaRepository storeJpaRepository;

    @Override
    public Optional<Store> findByUser(User user) {
        return storeJpaRepository.findByUser(user);
    }

    @Override
    public Optional<Store> findByUserUuid(UUID userUuid) {
        return storeJpaRepository.findByUserUuid(userUuid);
    }

    @Override
    public Optional<Store> findByName(String name) {
        return storeJpaRepository.findByName(name);
    }

    @Override
    public boolean existsByName(String name) {
        return storeJpaRepository.existsByName(name);
    }

    @Override
    public boolean existsByUser(User user) {
        return storeJpaRepository.existsByUser(user);
    }

    @Override
    public <S extends Store> S save(S entity) {
        return storeJpaRepository.save(entity);
    }

    @Override
    public <S extends Store> List<S> saveAll(Iterable<S> entities) {
        return storeJpaRepository.saveAll(entities);
    }

    @Override
    public Optional<Store> findById(UUID id) {
        return storeJpaRepository.findById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return storeJpaRepository.existsById(id);
    }

    @Override
    public List<Store> findAll() {
        return storeJpaRepository.findAll();
    }

    @Override
    public List<Store> findAllById(Iterable<UUID> ids) {
        return storeJpaRepository.findAllById(ids);
    }

    @Override
    public long count() {
        return storeJpaRepository.count();
    }

    @Override
    public void deleteById(UUID id) {
        storeJpaRepository.deleteById(id);
    }

    @Override
    public void delete(Store entity) {
        storeJpaRepository.delete(entity);
    }

    @Override
    public void deleteAllById(Iterable<? extends UUID> ids) {
        storeJpaRepository.deleteAllById(ids);
    }

    @Override
    public void deleteAll(Iterable<? extends Store> entities) {
        storeJpaRepository.deleteAll(entities);
    }

    @Override
    public void deleteAll() {
        storeJpaRepository.deleteAll();
    }
}