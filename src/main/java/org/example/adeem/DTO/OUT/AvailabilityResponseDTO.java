package org.example.adeem.DTO.OUT;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Getter
@AllArgsConstructor
public class AvailabilityResponseDTO {
    private Long id;
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
}