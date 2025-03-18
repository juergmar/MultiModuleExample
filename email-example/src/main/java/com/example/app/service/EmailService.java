package com.example.app.service;

import com.example.app.model.OrderItem;
import com.example.email.core.model.Email;
import com.example.email.core.sender.EmailSender;
import com.example.email.example.generated.ExampleEmailService;
import com.example.email.example.generated.OrderConfirmationParams;
import com.example.email.example.generated.OrderConfirmationOrderSummaryParams;
import com.example.email.example.generated.OrderConfirmationShippingParams;
import com.example.email.example.generated.OrderConfirmationTrackingParams;
import com.example.email.example.generated.PasswordResetParams;
import com.example.email.example.generated.WelcomeParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final ExampleEmailService emailTemplateService;
    private final EmailSender emailSender;

    public EmailService(ExampleEmailService emailTemplateService, EmailSender emailSender) {
        this.emailTemplateService = emailTemplateService;
        this.emailSender = emailSender;
    }

    /**
     * Send a welcome email to a new user.
     *
     * @param email The user's email address
     * @param name  The user's name
     */
    public void sendWelcomeEmail(String email, String name) {
        try {
            String token = UUID.randomUUID().toString();
            String activationUrl = "http://localhost:8080/activate?token=" + token;

            WelcomeParams params = WelcomeParams.builder()
                    .userName(name)
                    .email(email)
                    .activationUrl(activationUrl)
                    .expiryHours(24)
                    .build();

            Email emailMessage = emailTemplateService.createWelcomeEmail(params)
                    .to(email)
                    .build();

            emailSender.send(emailMessage);
            logger.info("Welcome email sent to: {}", email);
        } catch (EmailSender.EmailSendException e) {
            logger.error("Failed to send welcome email", e);
            throw new RuntimeException("Failed to send welcome email", e);
        }
    }

    /**
     * Send a password reset email.
     *
     * @param email The user's email address
     */
    public void sendPasswordResetEmail(String email) {
        try {
            String token = UUID.randomUUID().toString();
            String resetUrl = "http://localhost:8080/reset-password?token=" + token;

            PasswordResetParams params = PasswordResetParams.builder()
                    .resetUrl(resetUrl)
                    .expiryMinutes(30)
                    .build();

            Email emailMessage = emailTemplateService.createPasswordResetEmail(params)
                    .to(email)
                    .build();

            emailSender.send(emailMessage);
            logger.info("Password reset email sent to: {}", email);
        } catch (EmailSender.EmailSendException e) {
            logger.error("Failed to send password reset email", e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    /**
     * Send an order confirmation email using the new section-based approach.
     *
     * @param email          The customer's email address
     * @param name           The customer's name
     * @param orderItems     List of items in the order
     * @param shippingAddress The shipping address
     * @param shippingMethod The shipping method (e.g., "Standard", "Express")
     */
    public void sendOrderConfirmationEmail(String email, String name, List<OrderItem> orderItems,
                                           String shippingAddress, String shippingMethod) {
        try {
            String orderNumber = "ORD-" + System.currentTimeMillis();
            String trackingUrl = "http://localhost:8080/orders/" + orderNumber;

            BigDecimal orderTotal = orderItems.stream()
                    .map(OrderItem::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            int deliveryDays = "Express".equalsIgnoreCase(shippingMethod) ? 2 : 5;
            LocalDate estimatedDelivery = LocalDate.now().plusDays(deliveryDays);
            String formattedDeliveryDate = estimatedDelivery.format(
                    DateTimeFormatter.ofPattern("MMMM d, yyyy"));

            OrderConfirmationOrderSummaryParams orderSummaryParams =
                    OrderConfirmationOrderSummaryParams.builder()
                            .items(orderItems)
                            .orderTotal(orderTotal)
                            .build();

            OrderConfirmationShippingParams shippingParams =
                    OrderConfirmationShippingParams.builder()
                            .shippingAddress(shippingAddress)
                            .shippingMethod(shippingMethod)
                            .estimatedDelivery(formattedDeliveryDate)
                            .build();

            OrderConfirmationTrackingParams trackingParams =
                    OrderConfirmationTrackingParams.builder()
                            .trackingUrl(trackingUrl)
                            .build();

            OrderConfirmationParams params = OrderConfirmationParams.builder()
                    .customerName(name)
                    .orderNumber(orderNumber)
                    .orderSummaryParams(orderSummaryParams)
                    .shippingParams(shippingParams)
                    .trackingParams(trackingParams)
                    .subject("Your Order #" + orderNumber + " is Confirmed!")
                    .build();

            Email emailMessage = emailTemplateService.createOrderConfirmationEmail(params)
                    .to(email)
                    .build();

            emailSender.send(emailMessage);
            logger.info("Order confirmation email sent to: {}", email);
        } catch (EmailSender.EmailSendException e) {
            logger.error("Failed to send order confirmation email", e);
            throw new RuntimeException("Failed to send order confirmation email", e);
        }
    }
}
