package info.novatec.worker;

import info.novatec.exception.PaymentException;
import info.novatec.model.Reservation;
import info.novatec.service.PaymentService;
import info.novatec.micronaut.zeebe.client.feature.ZeebeWorker;
import info.novatec.process.ProcessVariableHandler;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class MoneyWorker {

    private final String ERROR_CODE = "Transaction_Error";
    private final Logger logger = LoggerFactory.getLogger(MoneyWorker.class);
    private final PaymentService paymentService;

    public MoneyWorker(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @ZeebeWorker(type = "get-money")
    public void getMoney(final JobClient client, final ActivatedJob job) {
        logger.info("withdrawing money");
        Reservation reservation = ProcessVariableHandler.getReservation(job);
        if (reservation != null) {
            try {
                paymentService.issueMoney(reservation.getPrice(), "DE12345678901234", "VOBA123456XX");
                client.newCompleteCommand(job.getKey()).send().join();
            } catch (PaymentException e) {
                client.newThrowErrorCommand(job.getKey()).errorCode(ERROR_CODE).errorMessage(e.getMessage()).send().join();
            }
        } else {
            client.newFailCommand(job.getKey()).retries(0).errorMessage("no reservation set").send().join();
        }
    }
}