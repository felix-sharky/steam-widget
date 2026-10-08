package codes.sharky.steamwidget.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class YearEndResponse {

    /** Year being wrapped up, or {@code null} outside the year-end window. */
    private Integer wrapYear;

}
