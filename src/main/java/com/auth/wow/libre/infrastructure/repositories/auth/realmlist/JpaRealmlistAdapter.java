package com.auth.wow.libre.infrastructure.repositories.auth.realmlist;

import com.auth.wow.libre.domain.ports.out.realmlist.*;
import com.auth.wow.libre.infrastructure.entities.auth.*;
import org.springframework.stereotype.*;

import java.util.*;

@Repository
public class JpaRealmlistAdapter implements ObtainRealmlist {
    private final RealmlistRepository realmlistRepository;

    public JpaRealmlistAdapter(RealmlistRepository realmlistRepository) {
        this.realmlistRepository = realmlistRepository;
    }


    @Override
    public List<RealmlistEntity> findByAll() {
        return realmlistRepository.findAll();
    }
}
