package com.auth.wow.libre.domain.ports.in.realmlist;

import com.auth.wow.libre.infrastructure.entities.auth.*;

import java.util.*;

public interface RealmlistPort {
    List<RealmlistEntity> findByAll();

}
