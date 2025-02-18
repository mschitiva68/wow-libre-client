package com.auth.wow.libre.application.services.server_publications;

import com.auth.wow.libre.domain.model.dto.view.*;
import com.auth.wow.libre.domain.ports.in.server_publications.*;
import com.auth.wow.libre.domain.ports.out.server_publications.*;
import com.auth.wow.libre.infrastructure.entities.auth.*;
import org.springframework.stereotype.*;

import java.util.*;

@Service
public class ServerPublicationsService implements ServerPublicationsPort {
    private final ObtainServerPublications obtainServerPublications;

    public ServerPublicationsService(ObtainServerPublications obtainServerPublications) {
        this.obtainServerPublications = obtainServerPublications;
    }

    @Override
    public List<Card> publications() {
        return obtainServerPublications.findAll().stream().map(this::buildCard).toList();
    }


    private Card buildCard(ServerPublicationsEntity publication) {
        return new Card(publication.getImg(), publication.getTitle(), publication.getDescription());
    }
}
