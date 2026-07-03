package com.feeltens.git.parser;

import com.feeltens.git.dto.conflict.ConflictBlock;
import com.feeltens.git.dto.conflict.ParsedConflict;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Diff3Parser单元测试
 *
 * @author feeltens
 */
class Diff3ParserTest {

    private Diff3Parser diff3Parser;

    @BeforeEach
    void setUp() {
        diff3Parser = new Diff3Parser();
    }

    @Test
    void testParseSimpleConflict() {
        String content = "line1\n" +
                "<<<<<<< HEAD\n" +
                "ours content\n" +
                "=======\n" +
                "theirs content\n" +
                ">>>>>>> feature\n" +
                "line2";

        ParsedConflict result = diff3Parser.parseConflict(content, "test.txt");

        assertEquals(1, result.getTotalConflicts());
        assertEquals(1, result.getConflictBlocks().size());

        ConflictBlock block = result.getConflictBlocks().get(0);
        assertEquals("ours content", block.getOursContent());
        assertEquals("theirs content", block.getTheirsContent());
        assertNull(block.getBaseContent());
    }

    @Test
    void testParseDiff3Conflict() {
        String content = "line1\n" +
                "<<<<<<< HEAD\n" +
                "ours content\n" +
                "||||||| base\n" +
                "base content\n" +
                "=======\n" +
                "theirs content\n" +
                ">>>>>>> feature\n" +
                "line2";

        ParsedConflict result = diff3Parser.parseConflict(content, "test.txt");

        assertEquals(1, result.getTotalConflicts());

        ConflictBlock block = result.getConflictBlocks().get(0);
        assertEquals("ours content", block.getOursContent());
        assertEquals("base content", block.getBaseContent());
        assertEquals("theirs content", block.getTheirsContent());
    }

    @Test
    void testParseMultipleConflicts() {
        String content = "<<<<<<< HEAD\n" +
                "ours 1\n" +
                "=======\n" +
                "theirs 1\n" +
                ">>>>>>> feature\n" +
                "middle\n" +
                "<<<<<<< HEAD\n" +
                "ours 2\n" +
                "=======\n" +
                "theirs 2\n" +
                ">>>>>>> feature";

        ParsedConflict result = diff3Parser.parseConflict(content, "test.txt");

        assertEquals(2, result.getTotalConflicts());
        assertEquals(2, result.getConflictBlocks().size());
    }

    @Test
    void testParseEmptyContent() {
        ParsedConflict result = diff3Parser.parseConflict("", "test.txt");

        assertEquals(0, result.getTotalConflicts());
        assertEquals(0, result.getConflictBlocks().size());
        assertEquals(0, result.getNonConflictSegments().size());
    }

    @Test
    void testParseNullContent() {
        ParsedConflict result = diff3Parser.parseConflict(null, "test.txt");

        assertEquals(0, result.getTotalConflicts());
        assertNull(result.getOriginalContent());
    }

    @Test
    void testParseNoConflict() {
        String content = "line1\nline2\nline3";

        ParsedConflict result = diff3Parser.parseConflict(content, "test.txt");

        assertEquals(0, result.getTotalConflicts());
        assertEquals(1, result.getNonConflictSegments().size());
        assertEquals(content, result.getNonConflictSegments().get(0));
    }

    @Test
    void testGenerateResolvedContent() {
        String content = "line1\n" +
                "<<<<<<< HEAD\n" +
                "ours content\n" +
                "=======\n" +
                "theirs content\n" +
                ">>>>>>> feature\n" +
                "line2";

        ParsedConflict parsed = diff3Parser.parseConflict(content, "test.txt");

        List<ConflictBlock> resolutions = new ArrayList<>();
        ConflictBlock resolution = ConflictBlock.builder()
                .resolvedContent("resolved content")
                .build();
        resolutions.add(resolution);

        String result = diff3Parser.generateResolvedContent(parsed, resolutions);

        assertFalse(diff3Parser.hasConflictMarkers(result));
        assertTrue(result.contains("resolved content"));
        assertTrue(result.contains("line1"));
        assertTrue(result.contains("line2"));
    }

    @Test
    void testGenerateResolvedContentWithNullParsed() {
        String result = diff3Parser.generateResolvedContent(null, new ArrayList<>());
        assertEquals("", result);
    }

    @Test
    void testGenerateResolvedContentWithEmptyConflicts() {
        ParsedConflict parsed = ParsedConflict.builder()
                .originalContent("no conflict here")
                .conflictBlocks(new ArrayList<>())
                .build();

        String result = diff3Parser.generateResolvedContent(parsed, new ArrayList<>());
        assertEquals("no conflict here", result);
    }

    @Test
    void testHasConflictMarkers() {
        assertTrue(diff3Parser.hasConflictMarkers("<<<<<<< HEAD\ncontent\n=======\n>>>>>>>"));
        assertFalse(diff3Parser.hasConflictMarkers("no conflict here"));
        assertFalse(diff3Parser.hasConflictMarkers(null));
        assertFalse(diff3Parser.hasConflictMarkers(""));
    }

    @Test
    void testConflictBlockLineNumbers() {
        String content = "line1\n" +
                "<<<<<<< HEAD\n" +
                "ours content\n" +
                "=======\n" +
                "theirs content\n" +
                ">>>>>>> feature\n" +
                "line2";

        ParsedConflict result = diff3Parser.parseConflict(content, "test.txt");

        ConflictBlock block = result.getConflictBlocks().get(0);
        assertEquals(2, block.getStartLine()); // 1-based, <<<<<<< is at line 2
        assertEquals(6, block.getEndLine());   // 1-based, >>>>>>> is at line 6
    }

    @Test
    void testNonConflictSegments() {
        String content = "before\n" +
                "<<<<<<< HEAD\n" +
                "ours\n" +
                "=======\n" +
                "theirs\n" +
                ">>>>>>> feature\n" +
                "after";

        ParsedConflict result = diff3Parser.parseConflict(content, "test.txt");

        assertEquals(1, result.getTotalConflicts());
        assertEquals(2, result.getNonConflictSegments().size());
        assertEquals("before", result.getNonConflictSegments().get(0));
        assertEquals("after", result.getNonConflictSegments().get(1));
    }

    @Test
    void testConflictWithBranchNames() {
        String content = "<<<<<<< feature-branch\n" +
                "ours content\n" +
                "=======\n" +
                "theirs content\n" +
                ">>>>>>> main-branch";

        ParsedConflict result = diff3Parser.parseConflict(content, "test.txt");

        assertEquals(1, result.getTotalConflicts());
        ConflictBlock block = result.getConflictBlocks().get(0);
        assertEquals("ours content", block.getOursContent());
        assertEquals("theirs content", block.getTheirsContent());
    }

    @Test
    void testMultiLineConflictContent() {
        String content = "<<<<<<< HEAD\n" +
                "line 1\n" +
                "line 2\n" +
                "line 3\n" +
                "=======\n" +
                "line A\n" +
                "line B\n" +
                ">>>>>>> feature";

        ParsedConflict result = diff3Parser.parseConflict(content, "test.txt");

        assertEquals(1, result.getTotalConflicts());
        ConflictBlock block = result.getConflictBlocks().get(0);
        assertEquals("line 1\nline 2\nline 3", block.getOursContent());
        assertEquals("line A\nline B", block.getTheirsContent());
    }

}