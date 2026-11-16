package rdhxb.whitelistservice.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.stereotype.Component;
import rdhxb.common.base.RepliesModel;
import rdhxb.common.base.Source;
import rdhxb.common.base.Status;
import rdhxb.common.nip.NIPMessage;

@Component
@RequiredArgsConstructor
@Slf4j
public class WlMessageRecoverer implements MessageRecoverer {
    private final RabbitTemplate template;
    private final MessageConverter converter;

    @Override
    public void recover(Message message, Throwable cause) {
        try {
            NIPMessage nipMessage = (NIPMessage) converter.fromMessage(message);

            RepliesModel replies = new RepliesModel();
            replies.setRequestId(nipMessage.getRequestid());
            replies.setSource(Source.WL);
            replies.setStatus(Status.FAILED);
            replies.setErrorMessage(rootMessage(cause));

            template.convertAndSend("", RabbitMqConfig.REPLIES, replies);
        } catch (Exception e) {
            log.error("Failed to send FAILED reply to raport-service", e);
        }

        // Always reject (even if the reply failed) so the message is not lost but goes to the DLQ
        throw new AmqpRejectAndDontRequeueException(cause);
    }

    private String rootMessage(Throwable cause) {
        Throwable inner = cause.getCause();
        return inner != null ? inner.getMessage() : cause.getMessage();
    }
}
