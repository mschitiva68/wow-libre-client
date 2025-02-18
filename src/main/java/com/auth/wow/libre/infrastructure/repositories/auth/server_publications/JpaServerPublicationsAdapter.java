package com.auth.wow.libre.infrastructure.repositories.auth.server_publications;

import com.auth.wow.libre.domain.ports.out.server_publications.*;
import com.auth.wow.libre.infrastructure.entities.auth.*;
import org.springframework.stereotype.*;

import java.util.*;

@Repository
public class JpaServerPublicationsAdapter implements ObtainServerPublications {
    private final ServerPublicationsRepository serverPublicationsRepository;

    public JpaServerPublicationsAdapter(ServerPublicationsRepository serverPublicationsRepository) {
        this.serverPublicationsRepository = serverPublicationsRepository;
    }

    @Override
    public List<ServerPublicationsEntity> findAll() {
        return serverPublicationsRepository.findAll();
    }
}
