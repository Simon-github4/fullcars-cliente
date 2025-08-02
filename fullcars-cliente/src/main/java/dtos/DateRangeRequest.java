package dtos;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DateRangeRequest {
    private LocalDate start;
    private LocalDate end;

    public DateRangeRequest() {}

    public DateRangeRequest(LocalDate start, LocalDate end) {
        this.start = start;
        this.end = end;
    }

}

