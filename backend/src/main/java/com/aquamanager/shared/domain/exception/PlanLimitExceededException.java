package com.aquamanager.shared.domain.exception;

/** Lançada quando uma ação violaria os limites do plano contratado pela empresa. */
public class PlanLimitExceededException extends BusinessException {
    public PlanLimitExceededException(String message) {
        super("PLAN_LIMIT_EXCEEDED", message);
    }
}
