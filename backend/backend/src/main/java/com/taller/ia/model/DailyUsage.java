package com.taller.ia.model;

import java.time.LocalDate;

public record DailyUsage(LocalDate date, long tokens) {
}
