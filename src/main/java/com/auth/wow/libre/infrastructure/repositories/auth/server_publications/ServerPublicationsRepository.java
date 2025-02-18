package com.auth.wow.libre.infrastructure.repositories.auth.server_publications;

import com.auth.wow.libre.infrastructure.entities.auth.*;
import org.springframework.data.repository.*;

import java.util.*;

public interface ServerPublicationsRepository extends CrudRepository<ServerPublicationsEntity, Long> {
    @Override
    List<ServerPublicationsEntity> findAll();
}
