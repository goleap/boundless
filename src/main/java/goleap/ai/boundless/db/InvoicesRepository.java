package goleap.ai.boundless.db;

import goleap.ai.boundless.model.Invoices.Invoice;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoicesRepository extends CrudRepository<Invoice, String> {}
