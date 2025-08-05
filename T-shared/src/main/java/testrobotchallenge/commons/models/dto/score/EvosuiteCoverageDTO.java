package testrobotchallenge.commons.models.dto.score;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import testrobotchallenge.commons.models.dto.score.basic.EvosuiteScoreDTO;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class EvosuiteCoverageDTO {
    private EvosuiteScoreDTO evosuiteScoreDTO;
    private String resultFileContent;
}
