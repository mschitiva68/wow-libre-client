package com.auth.wow.libre.domain.ports.out.server_publications;

import com.auth.wow.libre.infrastructure.entities.auth.*;

import java.util.*;

public interface ObtainServerPublications {
    List<ServerPublicationsEntity> findAll();
}
