package com.taller.ia.model;

public record UpgradeResponse(PlanConfig plan, long tokensLimit, String message) {
}
