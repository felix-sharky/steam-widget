package codes.sharky.steamwidget.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BooleanResultResponse {

    private String steamId;
    private boolean result;

}
