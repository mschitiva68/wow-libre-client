package com.auth.wow.libre.infrastructure.repositories.auth.realmlist;

import com.auth.wow.libre.infrastructure.entities.auth.*;
import org.springframework.data.repository.*;

import java.util.*;

public interface RealmlistRepository extends CrudRepository<RealmlistEntity, Long> {
    @Override
    List<RealmlistEntity> findAll();
}
