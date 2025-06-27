package Project_ITSS.CancelOrder.Service;

import Project_ITSS.CancelOrder.Entity.Order;
import Project_ITSS.CancelOrder.Strategy.PaymentStrategy.RefundResult;

/**
 * Interface for notification services
 * Implements Dependency Inversion Principle - depends on abstraction, not concrete implementation
 */
public interface INotificationService {
    
    /**
     * Send cancellation notification
     * @param order The cancelled order
     * @param refundResult The refund result
     */
    void sendCancellationNotification(Order order, RefundResult refundResult);
    
    /**
     * Send success notification
     * @param email The recipient email
     * @param subject The email subject
     * @param content The email content
     */
    void sendSuccessEmail(String email, String subject, String content);
} 