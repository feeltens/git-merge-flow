package com.feeltens.git.integration;

import com.feeltens.git.detector.ConflictTypeDetector;
import com.feeltens.git.diff.DiffCalculator;
import com.feeltens.git.dto.conflict.ConflictType;
import com.feeltens.git.dto.conflict.GitIndexEntry;
import com.feeltens.git.dto.conflict.ParsedConflict;
import com.feeltens.git.dto.diff.ThreeWayDiffVO;
import com.feeltens.git.parser.Diff3Parser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 集成测试 - 测试整个Diff流程
 *
 * @author feeltens
 */
class DiffIntegrationTest {

    private Diff3Parser diff3Parser;
    private DiffCalculator diffCalculator;
    private ConflictTypeDetector conflictTypeDetector;

    @BeforeEach
    void setUp() {
        diff3Parser = new Diff3Parser();
        diffCalculator = new DiffCalculator();
        conflictTypeDetector = new ConflictTypeDetector();
    }

    @Test
    void testFullDiffWorkflow() {
        // 1. 准备diff3格式冲突内容
        String conflictContent = "line1\n" +
                "<<<<<<< HEAD\n" +
                "ours content\n" +
                "||||||| base\n" +
                "base content\n" +
                "=======\n" +
                "theirs content\n" +
                ">>>>>>> feature\n" +
                "line2";

        // 2. 解析冲突
        ParsedConflict parsedConflict = diff3Parser.parseConflict(conflictContent, "test.txt");
        assertNotNull(parsedConflict);
        assertEquals(1, parsedConflict.getTotalConflicts());
        assertEquals("base content", parsedConflict.getConflictBlocks().get(0).getBaseContent());

        // 3. 模拟Git索引数据
        Map<Integer, GitIndexEntry> indexMap = new HashMap<>();
        indexMap.put(1, GitIndexEntry.builder().path("test.txt").stage(1).objectId("base123").build());
        indexMap.put(2, GitIndexEntry.builder().path("test.txt").stage(2).objectId("ours123").build());
        indexMap.put(3, GitIndexEntry.builder().path("test.txt").stage(3).objectId("theirs123").build());

        // 4. 检测冲突类型
        ConflictType conflictType = conflictTypeDetector.detectConflictType(indexMap);
        assertEquals(ConflictType.DEFAULT, conflictType);

        // 5. 计算三路diff
        String baseContent = "line1\nbase content\nline2";
        String oursContent = "line1\nours content\nline2";
        String theirsContent = "line1\ntheirs content\nline2";

        ThreeWayDiffVO diffVO = diffCalculator.calculateThreeWayDiff(
                baseContent, oursContent, theirsContent, "test.txt");

        assertNotNull(diffVO);
        assertEquals("test.txt", diffVO.getFilePath());
        assertEquals(3, diffVO.getBaseTotalLines());
        assertEquals(3, diffVO.getOursTotalLines());
        assertEquals(3, diffVO.getTheirsTotalLines());
    }

    @Test
    void testConflictTypeDetection() {
        // 测试标准冲突（双方修改）
        Map<Integer, GitIndexEntry> standardConflict = new HashMap<>();
        standardConflict.put(1, GitIndexEntry.builder().stage(1).build());
        standardConflict.put(2, GitIndexEntry.builder().stage(2).build());
        standardConflict.put(3, GitIndexEntry.builder().stage(3).build());
        assertEquals(ConflictType.DEFAULT, conflictTypeDetector.detectConflictType(standardConflict));

        // 测试双方添加
        Map<Integer, GitIndexEntry> addedAdded = new HashMap<>();
        addedAdded.put(2, GitIndexEntry.builder().stage(2).build());
        addedAdded.put(3, GitIndexEntry.builder().stage(3).build());
        assertEquals(ConflictType.ADDED_ADDED, conflictTypeDetector.detectConflictType(addedAdded));

        // 测试修改删除
        Map<Integer, GitIndexEntry> modifiedDeleted = new HashMap<>();
        modifiedDeleted.put(1, GitIndexEntry.builder().stage(1).build());
        modifiedDeleted.put(2, GitIndexEntry.builder().stage(2).build());
        assertEquals(ConflictType.MODIFIED_DELETED, conflictTypeDetector.detectConflictType(modifiedDeleted));

        // 测试删除修改
        Map<Integer, GitIndexEntry> deletedModified = new HashMap<>();
        deletedModified.put(1, GitIndexEntry.builder().stage(1).build());
        deletedModified.put(3, GitIndexEntry.builder().stage(3).build());
        assertEquals(ConflictType.DELETED_MODIFIED, conflictTypeDetector.detectConflictType(deletedModified));

        // 测试双方删除
        Map<Integer, GitIndexEntry> deletedDeleted = new HashMap<>();
        deletedDeleted.put(1, GitIndexEntry.builder().stage(1).build());
        assertEquals(ConflictType.DELETED_DELETED, conflictTypeDetector.detectConflictType(deletedDeleted));
    }

    @Test
    void testDiff3ParserWithMultipleConflicts() {
        String content = "<<<<<<< HEAD\n" +
                "ours 1\n" +
                "=======\n" +
                "theirs 1\n" +
                ">>>>>>> feature\n" +
                "middle\n" +
                "<<<<<<< HEAD\n" +
                "ours 2\n" +
                "||||||| base\n" +
                "base 2\n" +
                "=======\n" +
                "theirs 2\n" +
                ">>>>>>> feature";

        ParsedConflict result = diff3Parser.parseConflict(content, "test.txt");

        assertEquals(2, result.getTotalConflicts());
        assertNull(result.getConflictBlocks().get(0).getBaseContent());
        assertEquals("base 2", result.getConflictBlocks().get(1).getBaseContent());
    }

    @Test
    void testDiffCalculatorWithLargeFiles() {
        StringBuilder baseBuilder = new StringBuilder();
        StringBuilder oursBuilder = new StringBuilder();
        StringBuilder theirsBuilder = new StringBuilder();

        for (int i = 0; i < 500; i++) {
            baseBuilder.append("line ").append(i).append("\n");
            if (i % 10 == 0) {
                oursBuilder.append("line ").append(i).append(" modified\n");
            } else {
                oursBuilder.append("line ").append(i).append("\n");
            }
            theirsBuilder.append("line ").append(i).append("\n");
        }

        ThreeWayDiffVO diffVO = diffCalculator.calculateThreeWayDiff(
                baseBuilder.toString(), oursBuilder.toString(), theirsBuilder.toString(), "test.txt");

        assertNotNull(diffVO);
        assertEquals(501, diffVO.getBaseTotalLines());
        assertEquals(501, diffVO.getOursTotalLines());
        assertEquals(501, diffVO.getTheirsTotalLines());
    }

    @Test
    void testGenerateResolvedContent() {
        String conflictContent = "before\n" +
                "<<<<<<< HEAD\n" +
                "ours\n" +
                "=======\n" +
                "theirs\n" +
                ">>>>>>> feature\n" +
                "after";

        ParsedConflict parsed = diff3Parser.parseConflict(conflictContent, "test.txt");
        String resolved = diff3Parser.generateResolvedContent(parsed,
                java.util.Collections.singletonList(
                        com.feeltens.git.dto.conflict.ConflictBlock.builder()
                                .resolvedContent("resolved")
                                .build()
                ));

        assertFalse(diff3Parser.hasConflictMarkers(resolved));
        assertTrue(resolved.contains("resolved"));
        assertTrue(resolved.contains("before"));
        assertTrue(resolved.contains("after"));
    }

}