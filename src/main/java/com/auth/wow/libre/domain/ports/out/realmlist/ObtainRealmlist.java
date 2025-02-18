package com.auth.wow.libre.domain.ports.out.realmlist;

import com.auth.wow.libre.infrastructure.entities.auth.*;

import java.util.*;

public interface ObtainRealmlist {

    List<RealmlistEntity> findByAll();
}
