package com.feeltens.git.service.impl;

import com.feeltens.git.dto.conflict.ConflictBlock;
import com.feeltens.git.dto.conflict.ParsedConflict;
import com.feeltens.git.service.ConflictParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 冲突解析器实现
 *
 * @author feeltens
 */
@Service
@Slf4j
public class ConflictParserImpl implements ConflictParser {

    // 冲突标记正则表达式
    private static final Pattern CONFLICT_START = Pattern.compile("^<<<<<<<\\s*(.*)$");
    private static final Pattern CONFLICT_BASE = Pattern.compile("^\\|\\|\\|\\|\\|\\|\\|\\s*(.*)$");
    private static final Pattern CONFLICT_SEP = Pattern.compile("^=======$");
    private static final Pattern CONFLICT_END = Pattern.compile("^>>>>>>>\\s*(.*)$");

    // 简单检测冲突标记
    private static final String CONFLICT_MARKER_START = "<<<<<<<";
    private static final String CONFLICT_MARKER_END = ">>>>>>>";

    @Override
    public ParsedConflict parseConflict(String conflictContent, String filePath) {
        if (conflictContent == null || conflictContent.isEmpty()) {
            return ParsedConflict.builder()
                    .filePath(filePath)
                    .originalContent(conflictContent)
                    .conflictBlocks(new ArrayList<>())
                    .nonConflictSegments(new ArrayList<>())
                    .totalConflicts(0)
                    .build();
        }

        List<ConflictBlock> conflictBlocks = new ArrayList<>();
        List<String> nonConflictSegments = new ArrayList<>();
        String[] lines = conflictContent.split("\n", -1);

        int blockIndex = 0;
        int lineNum = 0;
        StringBuilder currentNonConflict = new StringBuilder();

        while (lineNum < lines.length) {
            String line = lines[lineNum];
            Matcher startMatcher = CONFLICT_START.matcher(line);

            if (startMatcher.matches()) {
                // 保存之前的非冲突内容
                if (currentNonConflict.length() > 0) {
                    nonConflictSegments.add(currentNonConflict.toString());
                    currentNonConflict = new StringBuilder();
                }

                // 解析冲突块
                int startLine = lineNum + 1; // 1-based
                StringBuilder oursContent = new StringBuilder();
                StringBuilder baseContent = new StringBuilder();
                StringBuilder theirsContent = new StringBuilder();

                lineNum++; // 跳过 <<<<<<< 行
                boolean inOurs = true;
                boolean inBase = false;
                boolean hasBase = false;

                while (lineNum < lines.length) {
                    line = lines[lineNum];

                    if (CONFLICT_BASE.matcher(line).matches()) {
                        // 进入 base 区域（diff3 格式）
                        inOurs = false;
                        inBase = true;
                        hasBase = true;
                        lineNum++;
                        continue;
                    }

                    if (CONFLICT_SEP.matcher(line).matches()) {
                        // 进入 theirs 区域
                        inOurs = false;
                        inBase = false;
                        lineNum++;
                        continue;
                    }

                    if (CONFLICT_END.matcher(line).matches()) {
                        // 冲突块结束
                        int endLine = lineNum + 1; // 1-based

                        ConflictBlock block = ConflictBlock.builder()
                                .blockIndex(blockIndex++)
                                .startLine(startLine)
                                .endLine(endLine)
                                .oursContent(trimTrailingNewline(oursContent.toString()))
                                .theirsContent(trimTrailingNewline(theirsContent.toString()))
                                .baseContent(hasBase ? trimTrailingNewline(baseContent.toString()) : null)
                                .resolved(false)
                                .build();

                        conflictBlocks.add(block);
                        lineNum++;
                        break;
                    }

                    // 收集内容
                    if (inOurs) {
                        if (oursContent.length() > 0) {
                            oursContent.append("\n");
                        }
                        oursContent.append(line);
                    } else if (inBase) {
                        if (baseContent.length() > 0) {
                            baseContent.append("\n");
                        }
                        baseContent.append(line);
                    } else {
                        if (theirsContent.length() > 0) {
                            theirsContent.append("\n");
                        }
                        theirsContent.append(line);
                    }

                    lineNum++;
                }
            } else {
                // 非冲突行
                if (currentNonConflict.length() > 0) {
                    currentNonConflict.append("\n");
                }
                currentNonConflict.append(line);
                lineNum++;
            }
        }

        // 保存最后的非冲突内容
        if (currentNonConflict.length() > 0) {
            nonConflictSegments.add(currentNonConflict.toString());
        }

        return ParsedConflict.builder()
                .filePath(filePath)
                .originalContent(conflictContent)
                .conflictBlocks(conflictBlocks)
                .nonConflictSegments(nonConflictSegments)
                .totalConflicts(conflictBlocks.size())
                .build();
    }

    @Override
    public String generateResolvedContent(ParsedConflict parsedConflict, List<ConflictBlock> resolutions) {
        if (parsedConflict == null || parsedConflict.getConflictBlocks().isEmpty()) {
            return parsedConflict != null ? parsedConflict.getOriginalContent() : "";
        }

        // 重新解析原始内容，替换冲突块
        String[] lines = parsedConflict.getOriginalContent().split("\n", -1);
        StringBuilder result = new StringBuilder();
        int lineNum = 0;
        int resolutionIndex = 0;

        while (lineNum < lines.length) {
            String line = lines[lineNum];

            if (CONFLICT_START.matcher(line).matches()) {
                // 找到冲突块，使用解决方案替换
                if (resolutionIndex < resolutions.size()) {
                    ConflictBlock resolution = resolutions.get(resolutionIndex);
                    String resolvedContent = resolution.getResolvedContent();

                    if (resolvedContent != null && !resolvedContent.isEmpty()) {
                        if (result.length() > 0 && !result.toString().endsWith("\n")) {
                            result.append("\n");
                        }
                        result.append(resolvedContent);
                    }
                    resolutionIndex++;
                }

                // 跳过整个冲突块
                lineNum++;
                while (lineNum < lines.length && !CONFLICT_END.matcher(lines[lineNum]).matches()) {
                    lineNum++;
                }
                lineNum++; // 跳过 >>>>>>> 行
            } else {
                if (result.length() > 0) {
                    result.append("\n");
                }
                result.append(line);
                lineNum++;
            }
        }

        return result.toString();
    }

    @Override
    public boolean hasConflictMarkers(String content) {
        if (content == null || content.isEmpty()) {
            return false;
        }
        return content.contains(CONFLICT_MARKER_START) && content.contains(CONFLICT_MARKER_END);
    }

    private String trimTrailingNewline(String str) {
        if (str == null) {
            return null;
        }
        if (str.endsWith("\n")) {
            return str.substring(0, str.length() - 1);
        }
        return str;
    }

}