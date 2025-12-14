package com.ebuy.payment.validator;

import com.ebuy.payment.enums.PaymentStatus;
import com.ebuy.payment.exception.InvalidPaymentStatusException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Component
public class PaymentStatusValidator {

    private final Map<PaymentStatus, Set<PaymentStatus>> allowedTransitions;

    public PaymentStatusValidator() {
        this.allowedTransitions = new EnumMap<>(PaymentStatus.class);
        initializeTransitionRules();
    }

    /**
     * Initialize the allowed status transition rules
     */
    private void initializeTransitionRules() {
        // PENDING can transition to: AUTHORIZED, CAPTURED, FAILED, VOIDED
        allowedTransitions.put(PaymentStatus.PENDING,
                EnumSet.of(
                        PaymentStatus.AUTHORIZED,
                        PaymentStatus.CAPTURED,
                        PaymentStatus.FAILED,
                        PaymentStatus.VOIDED
                )
        );

        // AUTHORIZED can transition to: CAPTURED, VOIDED, FAILED
        allowedTransitions.put(PaymentStatus.AUTHORIZED,
                EnumSet.of(
                        PaymentStatus.CAPTURED,
                        PaymentStatus.VOIDED,
                        PaymentStatus.FAILED
                )
        );

        // CAPTURED can transition to: REFUNDED
        allowedTransitions.put(PaymentStatus.CAPTURED,
                EnumSet.of(PaymentStatus.REFUNDED)
        );

        // FAILED - terminal state, no transitions allowed
        allowedTransitions.put(PaymentStatus.FAILED, EnumSet.noneOf(PaymentStatus.class));

        // REFUNDED - terminal state, no transitions allowed
        allowedTransitions.put(PaymentStatus.REFUNDED, EnumSet.noneOf(PaymentStatus.class));

        // VOIDED - terminal state, no transitions allowed
        allowedTransitions.put(PaymentStatus.VOIDED, EnumSet.noneOf(PaymentStatus.class));
    }

    /**
     * Validate if the status transition is allowed
     *
     * @param currentStatus Current payment status
     * @param targetStatus Target payment status
     * @throws InvalidPaymentStatusException if transition is not allowed
     */
    public void validateTransition(PaymentStatus currentStatus, PaymentStatus targetStatus) {
        if (currentStatus == null) {
            throw new InvalidPaymentStatusException("Current payment status cannot be null");
        }
        if (targetStatus == null) {
            throw new InvalidPaymentStatusException("Target payment status cannot be null");
        }

        // Check if current status is same as target status
        if (currentStatus == targetStatus) {
            throw new InvalidPaymentStatusException(
                    currentStatus.name(),
                    targetStatus.name(),
                    "Payment is already in the target status"
            );
        }

        // Get allowed transitions for current status
        Set<PaymentStatus> allowed = allowedTransitions.get(currentStatus);

        // Check if transition is allowed
        if (allowed == null || !allowed.contains(targetStatus)) {
            throw new InvalidPaymentStatusException(
                    currentStatus.name(),
                    targetStatus.name(),
                    String.format("Invalid status transition from %s to %s",
                            currentStatus.name(), targetStatus.name())
            );
        }
    }

    /**
     * Validate if the status transition is allowed for a specific payment
     *
     * @param paymentId Payment ID
     * @param currentStatus Current payment status
     * @param targetStatus Target payment status
     * @throws InvalidPaymentStatusException if transition is not allowed
     */
    public void validateTransition(Long paymentId, PaymentStatus currentStatus, PaymentStatus targetStatus) {
        if (currentStatus == null) {
            throw new InvalidPaymentStatusException("Current payment status cannot be null");
        }
        if (targetStatus == null) {
            throw new InvalidPaymentStatusException("Target payment status cannot be null");
        }

        // Check if current status is same as target status
        if (currentStatus == targetStatus) {
            throw new InvalidPaymentStatusException(
                    paymentId,
                    currentStatus.name(),
                    targetStatus.name()
            );
        }

        // Get allowed transitions for current status
        Set<PaymentStatus> allowed = allowedTransitions.get(currentStatus);

        // Check if transition is allowed
        if (allowed == null || !allowed.contains(targetStatus)) {
            throw new InvalidPaymentStatusException(
                    paymentId,
                    currentStatus.name(),
                    targetStatus.name()
            );
        }
    }

    /**
     * Check if a status transition is valid without throwing an exception
     *
     * @param currentStatus Current payment status
     * @param targetStatus Target payment status
     * @return true if transition is valid, false otherwise
     */
    public boolean isValidTransition(PaymentStatus currentStatus, PaymentStatus targetStatus) {
        if (currentStatus == null || targetStatus == null || currentStatus == targetStatus) {
            return false;
        }

        Set<PaymentStatus> allowed = allowedTransitions.get(currentStatus);
        return allowed != null && allowed.contains(targetStatus);
    }

    /**
     * Get all allowed transitions from a given status
     *
     * @param currentStatus Current payment status
     * @return Set of allowed target statuses
     */
    public Set<PaymentStatus> getAllowedTransitions(PaymentStatus currentStatus) {
        if (currentStatus == null) {
            return EnumSet.noneOf(PaymentStatus.class);
        }

        Set<PaymentStatus> allowed = allowedTransitions.get(currentStatus);
        return allowed != null ? EnumSet.copyOf(allowed) : EnumSet.noneOf(PaymentStatus.class);
    }

    /**
     * Check if a status is a terminal state (no further transitions allowed)
     *
     * @param status Payment status to check
     * @return true if terminal state, false otherwise
     */
    public boolean isTerminalState(PaymentStatus status) {
        if (status == null) {
            return false;
        }

        Set<PaymentStatus> allowed = allowedTransitions.get(status);
        return allowed == null || allowed.isEmpty();
    }

    /**
     * Check if a status can be refunded
     *
     * @param status Payment status to check
     * @return true if refundable, false otherwise
     */
    public boolean isRefundable(PaymentStatus status) {
        return status == PaymentStatus.CAPTURED;
    }

    /**
     * Check if a status can be voided
     *
     * @param status Payment status to check
     * @return true if voidable, false otherwise
     */
    public boolean isVoidable(PaymentStatus status) {
        return status == PaymentStatus.PENDING || status == PaymentStatus.AUTHORIZED;
    }

    /**
     * Check if a status represents a successful payment
     *
     * @param status Payment status to check
     * @return true if successful, false otherwise
     */
    public boolean isSuccessful(PaymentStatus status) {
        return status == PaymentStatus.AUTHORIZED || status == PaymentStatus.CAPTURED;
    }
}