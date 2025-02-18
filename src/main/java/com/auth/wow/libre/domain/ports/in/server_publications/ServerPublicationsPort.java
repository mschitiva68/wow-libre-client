package com.auth.wow.libre.domain.ports.in.server_publications;

import com.auth.wow.libre.domain.model.dto.view.*;

import java.util.*;

public interface ServerPublicationsPort {
    List<Card> publications();
}
