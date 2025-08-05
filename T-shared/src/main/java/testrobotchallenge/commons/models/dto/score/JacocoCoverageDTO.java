package testrobotchallenge.commons.models.dto.score;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import testrobotchallenge.commons.models.dto.score.basic.JacocoScoreDTO;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class JacocoCoverageDTO {
    private JacocoScoreDTO jacocoScoreDTO;
    private String outCompile;
    private String coverage;
    private boolean errors;
}
