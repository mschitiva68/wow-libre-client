package com.auth.wow.libre.application.services.realmlist;

import com.auth.wow.libre.domain.ports.in.realmlist.*;
import com.auth.wow.libre.domain.ports.out.realmlist.*;
import com.auth.wow.libre.infrastructure.entities.auth.*;
import org.springframework.stereotype.*;

import java.util.*;

@Service
public class RealmlistService implements RealmlistPort {
    private final ObtainRealmlist obtainRealmlist;

    public RealmlistService(ObtainRealmlist obtainRealmlist) {
        this.obtainRealmlist = obtainRealmlist;
    }

    @Override
    public List<RealmlistEntity> findByAll() {
        return obtainRealmlist.findByAll();
    }
}
