package com.feeltens.git.diff;

import com.feeltens.git.dto.diff.DiffBlock;
import com.feeltens.git.dto.diff.DiffLine;
import com.feeltens.git.dto.diff.ThreeWayDiffVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * DiffCalculator单元测试
 *
 * @author feeltens
 */
class DiffCalculatorTest {

    private DiffCalculator diffCalculator;

    @BeforeEach
    void setUp() {
        diffCalculator = new DiffCalculator();
    }

    @Test
    void testCalculateThreeWayDiff() {
        String baseContent = "line1\nline2\nline3";
        String oursContent = "line1\nline2 modified\nline3";
        String theirsContent = "line1\nline2\nline3 added";

        ThreeWayDiffVO result = diffCalculator.calculateThreeWayDiff(
                baseContent, oursContent, theirsContent, "test.txt");

        assertNotNull(result);
        assertEquals("test.txt", result.getFilePath());
        assertEquals(baseContent, result.getBaseContent());
        assertEquals(oursContent, result.getOursContent());
        assertEquals(theirsContent, result.getTheirsContent());
        assertEquals(3, result.getBaseTotalLines());
        assertEquals(3, result.getOursTotalLines());
        assertEquals(3, result.getTheirsTotalLines());
        assertNotNull(result.getDiffBlocks());
    }

    @Test
    void testCalculateTwoWayDiff() {
        String left = "line1\nline2\nline3";
        String right = "line1\nline2 modified\nline3";

        List<DiffBlock> result = diffCalculator.calculateTwoWayDiff(left, right);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void testEmptyContent() {
        ThreeWayDiffVO result = diffCalculator.calculateThreeWayDiff("", "", "", "test.txt");

        assertNotNull(result);
        assertEquals(0, result.getBaseTotalLines());
        assertEquals(0, result.getOursTotalLines());
        assertEquals(0, result.getTheirsTotalLines());
    }

    @Test
    void testNullContent() {
        ThreeWayDiffVO result = diffCalculator.calculateThreeWayDiff(null, null, null, "test.txt");

        assertNotNull(result);
        assertEquals(0, result.getBaseTotalLines());
        assertEquals(0, result.getOursTotalLines());
        assertEquals(0, result.getTheirsTotalLines());
    }

    @Test
    void testSimpleDiff() {
        String baseContent = "line1\nline2\nline3";
        String oursContent = "line1\nline2 modified\nline3";
        String theirsContent = "line1\nline2\nline3";

        ThreeWayDiffVO result = diffCalculator.calculateThreeWayDiff(
                baseContent, oursContent, theirsContent, "test.txt");

        assertNotNull(result);
        assertNotNull(result.getDiffBlocks());
        // 应该检测到line2被修改
        assertTrue(result.getDiffBlocks().size() > 0);
    }

    @Test
    void testComplexDiff() {
        String baseContent = "line1\nline2\nline3\nline4\nline5";
        String oursContent = "line1\nline2 modified\nline3\nline4 removed\nline5";
        String theirsContent = "line1\nline2\nline3 added\nline4\nline5";

        ThreeWayDiffVO result = diffCalculator.calculateThreeWayDiff(
                baseContent, oursContent, theirsContent, "test.txt");

        assertNotNull(result);
        assertNotNull(result.getDiffBlocks());
        assertEquals(5, result.getBaseTotalLines());
        assertEquals(5, result.getOursTotalLines());
        assertEquals(5, result.getTheirsTotalLines());
    }

    @Test
    void testAddedLines() {
        String baseContent = "line1\nline2";
        String oursContent = "line1\nline2\nline3 added";
        String theirsContent = "line1\nline2";

        ThreeWayDiffVO result = diffCalculator.calculateThreeWayDiff(
                baseContent, oursContent, theirsContent, "test.txt");

        assertNotNull(result);
        assertEquals(2, result.getBaseTotalLines());
        assertEquals(3, result.getOursTotalLines());
    }

    @Test
    void testDeletedLines() {
        String baseContent = "line1\nline2\nline3";
        String oursContent = "line1\nline3";
        String theirsContent = "line1\nline2\nline3";

        ThreeWayDiffVO result = diffCalculator.calculateThreeWayDiff(
                baseContent, oursContent, theirsContent, "test.txt");

        assertNotNull(result);
        assertEquals(3, result.getBaseTotalLines());
        assertEquals(2, result.getOursTotalLines());
    }

    @Test
    void testNoDiff() {
        String baseContent = "line1\nline2\nline3";
        String oursContent = "line1\nline2\nline3";
        String theirsContent = "line1\nline2\nline3";

        ThreeWayDiffVO result = diffCalculator.calculateThreeWayDiff(
                baseContent, oursContent, theirsContent, "test.txt");

        assertNotNull(result);
        // 没有diff时，diffBlocks应该为空
        assertTrue(result.getDiffBlocks().isEmpty());
    }

    @Test
    void testDiffLineStatus() {
        String left = "line1\nline2\nline3";
        String right = "line1\nline2 modified\nline3";

        List<DiffBlock> result = diffCalculator.calculateTwoWayDiff(left, right);

        assertNotNull(result);
        if (!result.isEmpty()) {
            DiffBlock block = result.get(0);
            assertNotNull(block.getLines());
            if (!block.getLines().isEmpty()) {
                DiffLine line = block.getLines().get(0);
                assertNotNull(line.getStatus());
            }
        }
    }

    @Test
    void testLargeFileDiff() {
        StringBuilder baseBuilder = new StringBuilder();
        StringBuilder oursBuilder = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            baseBuilder.append("line ").append(i).append("\n");
            if (i == 500) {
                oursBuilder.append("line ").append(i).append(" modified\n");
            } else {
                oursBuilder.append("line ").append(i).append("\n");
            }
        }

        ThreeWayDiffVO result = diffCalculator.calculateThreeWayDiff(
                baseBuilder.toString(), oursBuilder.toString(), baseBuilder.toString(), "test.txt");

        assertNotNull(result);
        assertEquals(1001, result.getBaseTotalLines());
        assertEquals(1001, result.getOursTotalLines());
    }

}