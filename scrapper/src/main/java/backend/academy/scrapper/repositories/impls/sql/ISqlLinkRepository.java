package backend.academy.scrapper.repositories.impls.sql;

import backend.academy.scrapper.entities.TrackedLink;
import backend.academy.scrapper.repositories.LinkRepository;

interface ISqlLinkRepository extends LinkRepository {
    TrackedLink saveLinkOnly(TrackedLink link);
}
