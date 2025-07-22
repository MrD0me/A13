package RemoteCCC.App.util;

import testrobotchallenge.commons.models.dto.score.basic.CoverageDTO;
import testrobotchallenge.commons.models.dto.score.basic.JacocoScoreDTO;
import testrobotchallenge.commons.models.dto.score.JacocoCoverageDTO;
import testrobotchallenge.commons.util.ExtractScore;

public class BuildResponse {

    public static JacocoScoreDTO buildDTO(String xmlContent) {
        int[][] scores = ExtractScore.fromJacoco(xmlContent);
        JacocoScoreDTO responseBody = new JacocoScoreDTO();

        responseBody.setLineCoverageDTO(new CoverageDTO(
                scores[0][0], scores[0][1]
        ));
        responseBody.setBranchCoverageDTO(new CoverageDTO(
                scores[1][0], scores[1][1]
        ));
        responseBody.setInstructionCoverageDTO(new CoverageDTO(
                scores[2][0], scores[2][1]
        ));

        return responseBody;
    }

    public static JacocoCoverageDTO buildExtendedDTO(String xmlContent, String outCompile, boolean errors) {
        JacocoCoverageDTO responseBody = new JacocoCoverageDTO();
        responseBody.setCoverage(xmlContent);
        responseBody.setErrors(errors);
        responseBody.setOutCompile(outCompile);
        responseBody.setJacocoScoreDTO(buildDTO(xmlContent));
        return responseBody;
    }
}
