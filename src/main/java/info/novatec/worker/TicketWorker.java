package info.novatec.worker;

import info.novatec.micronaut.zeebe.client.feature.ZeebeWorker;
import info.novatec.model.Ticket;
import info.novatec.process.VariableHandler;
import info.novatec.service.TicketService;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@Singleton
public class TicketWorker {

    Logger logger = LoggerFactory.getLogger(TicketWorker.class);

    private final TicketService ticketService;

    public TicketWorker(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @ZeebeWorker(type = "generate-ticket")
    public void generateTicket(final JobClient client, final ActivatedJob job) {
        logger.info("generating ticket");
        Ticket ticket = ticketService.generateTickets();
        Map<String, Object> variables = VariableHandler.empty().withTicketId(ticket.getCode()).build();
        client.newCompleteCommand(job.getKey()).variables(variables).send().join();
    }

    @ZeebeWorker(type = "send-ticket")
    public void sendTicket(final JobClient client, final ActivatedJob job) {
        String ticket = VariableHandler.getTicketCode(job);
        String qrCode = VariableHandler.getQrCode(job);
        String userId = VariableHandler.getUserId(job);
        List<String> seats = VariableHandler.getSeats(job);
        String movieId = VariableHandler.getMovieId(job);
        String message = Ticket.getMessage(userId, movieId, String.join(", ", seats), ticket, qrCode);
        logger.info("sending ticket {} to customer", ticket);
        client.newCompleteCommand(job.getKey()).send().join();
        logger.info(message);
    }
}