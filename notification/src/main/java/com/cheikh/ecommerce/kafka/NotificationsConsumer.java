package com.cheikh.ecommerce.kafka;

import com.cheikh.ecommerce.config.WebSocketNotificationService;
import com.cheikh.ecommerce.email.EmailService;
import com.cheikh.ecommerce.kafka.order.OrderConfirmation;
import com.cheikh.ecommerce.kafka.payment.PaymentConfirmation;
import com.cheikh.ecommerce.notification.Notification;
import com.cheikh.ecommerce.notification.NotificationRepository;
import com.cheikh.ecommerce.notification.NotificationType;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static java.lang.String.format;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationsConsumer {

    private final NotificationRepository repository;
    private final EmailService emailService;
    private final WebSocketNotificationService webSocketNotificationService;


    @KafkaListener(topics = "payment-topic")
    public void consumePaymentSuccessNotifications(PaymentConfirmation paymentConfirmation) throws MessagingException {
        log.info(format("Consuming the message from payment-topic Topic:: %s", paymentConfirmation));

        repository.save(
                Notification.builder()
                        .type(NotificationType.PAYMENT_CONFIRMATION)
                        .notificationDate(LocalDateTime.now())
                        .paymentConfirmation(paymentConfirmation)
                        .build()
        );

        // Send email notification
        var customerName = paymentConfirmation.customerFirstname() + " " + paymentConfirmation.customerLastname();
        emailService.sendPaymentSuccessEmail(
                paymentConfirmation.customerEmail(),
                customerName,
                paymentConfirmation.paymentMethod(),
                paymentConfirmation.amount(),
                paymentConfirmation.orderReference()
        );

        var notificationMessage = String.format(
                "Payment received: %s %s paid %s via %s for order %s.",
                paymentConfirmation.customerFirstname(),
                paymentConfirmation.customerLastname(),
                paymentConfirmation.amount(),
                paymentConfirmation.paymentMethod(),
                paymentConfirmation.orderReference()
        );
        webSocketNotificationService.sendNotification(notificationMessage);
    }

    @KafkaListener(topics = "order-topic")
    public void consumeOrderConfirmationNotifications(OrderConfirmation orderConfirmation) throws MessagingException {
        log.info(format("Consuming the message from order-topic Topic:: %s", orderConfirmation));

        // Save notification to the repository
        repository.save(
                Notification.builder()
                        .type(NotificationType.ORDER_CONFIRMATION)
                        .notificationDate(LocalDateTime.now())
                        .orderConfirmation(orderConfirmation)
                        .build()
        );

        // Send email notification
        var customerName = orderConfirmation.customer().firstname() + " " + orderConfirmation.customer().lastname();
        emailService.sendOrderConfirmationEmail(
                orderConfirmation.customer().email(),
                customerName,
                orderConfirmation.totalAmount(),
                orderConfirmation.orderReference(),
                orderConfirmation.products()
        );

        // Send WebSocket notification
        var notificationMessage = String.format(
                "Order confirmed: %s %s placed an order (reference: %s) worth %s.",
                orderConfirmation.customer().firstname(),
                orderConfirmation.customer().lastname(),
                orderConfirmation.orderReference(),
                orderConfirmation.totalAmount()
        );
        webSocketNotificationService.sendNotification(notificationMessage);
    }
}
