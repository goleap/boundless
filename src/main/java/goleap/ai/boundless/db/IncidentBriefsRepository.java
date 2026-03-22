package goleap.ai.boundless.db;

import goleap.ai.boundless.model.IncidentBriefs.IncidentBrief;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IncidentBriefsRepository extends CrudRepository<IncidentBrief, String> {}
