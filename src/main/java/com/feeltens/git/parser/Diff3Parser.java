package com.feeltens.git.parser;

import com.feeltens.git.dto.conflict.ConflictBlock;
import com.feeltens.git.dto.conflict.ParsedConflict;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Diff3格式冲突解析器
 * <pre>
 *
 * 支持的格式：
 * 标准格式：
 * <<<<<<< ours
 * ours content
 * =======
 * theirs content
 * >>>>>>> theirs
 *
 * diff3格式：
 * <<<<<<< ours
 * ours content
 * ||||||| base
 * base content
 * =======
 * theirs content
 * >>>>>>> theirs
 *
 * </pre>
 *
 * @author feeltens
 */
@Component
@Slf4j
public class Diff3Parser {

    // 冲突标记正则表达式
    private static final Pattern CONFLICT_START = Pattern.compile("^<<<<<<<\\s*(.*)$");
    private static final Pattern CONFLICT_BASE = Pattern.compile("^\\|\\|\\|\\|\\|\\|\\|\\s*(.*)$");
    private static final Pattern CONFLICT_SEP = Pattern.compile("^=======+$");
    private static final Pattern CONFLICT_END = Pattern.compile("^>>>>>>>\\s*(.*)$");

    /**
     * 解析diff3格式冲突
     *
     * @param content  冲突内容
     * @param filePath 文件路径
     * @return 解析后的冲突信息
     */
    public ParsedConflict parseConflict(String content, String filePath) {
        if (content == null || content.isEmpty()) {
            return ParsedConflict.builder()
                    .filePath(filePath)
                    .originalContent(content)
                    .conflictBlocks(new ArrayList<>())
                    .nonConflictSegments(new ArrayList<>())
                    .totalConflicts(0)
                    .build();
        }

        List<ConflictBlock> conflictBlocks = new ArrayList<>();
        List<String> nonConflictSegments = new ArrayList<>();
        List<String> lines = splitLines(content);

        int lineIndex = 0;
        int totalConflicts = 0;

        while (lineIndex < lines.size()) {
            String line = lines.get(lineIndex);
            Matcher startMatcher = CONFLICT_START.matcher(line);

            if (startMatcher.find()) {
                ConflictBlock block = parseConflictBlock(lines, lineIndex, filePath, totalConflicts);
                if (block != null) {
                    conflictBlocks.add(block);
                    lineIndex = block.getEndLine();
                    totalConflicts++;
                } else {
                    lineIndex++;
                }
            } else {
                StringBuilder segment = new StringBuilder();
                while (lineIndex < lines.size()) {
                    String currentLine = lines.get(lineIndex);
                    if (CONFLICT_START.matcher(currentLine).find()) {
                        break;
                    }
                    segment.append(currentLine).append("\n");
                    lineIndex++;
                }
                if (segment.length() > 0) {
                    nonConflictSegments.add(trimTrailingNewline(segment.toString()));
                }
            }
        }

        return ParsedConflict.builder()
                .filePath(filePath)
                .originalContent(content)
                .conflictBlocks(conflictBlocks)
                .nonConflictSegments(nonConflictSegments)
                .totalConflicts(totalConflicts)
                .build();
    }

    /**
     * 解析单个冲突块
     *
     * @param lines      行列表
     * @param startIndex 起始索引
     * @param filePath   文件路径
     * @param blockIndex 冲突块索引
     * @return 冲突块
     */
    private ConflictBlock parseConflictBlock(List<String> lines, int startIndex,
                                             String filePath, int blockIndex) {
        String oursMarker = lines.get(startIndex);
        Matcher startMatcher = CONFLICT_START.matcher(oursMarker);
        String oursBranch = startMatcher.find() ? startMatcher.group(1).trim() : "ours";

        int i = startIndex + 1;
        List<String> oursLines = new ArrayList<>();
        List<String> baseLines = new ArrayList<>();
        List<String> theirsLines = new ArrayList<>();

        // 解析ours内容
        boolean hasBase = false;
        while (i < lines.size()) {
            String line = lines.get(i);
            Matcher baseMatcher = CONFLICT_BASE.matcher(line);
            Matcher sepMatcher = CONFLICT_SEP.matcher(line);

            if (baseMatcher.find()) {
                hasBase = true;
                break;
            } else if (sepMatcher.find()) {
                break;
            } else {
                oursLines.add(line);
                i++;
            }
        }

        // 解析base内容（如果有）
        if (hasBase) {
            i++;
            while (i < lines.size()) {
                String line = lines.get(i);
                if (CONFLICT_SEP.matcher(line).find()) {
                    break;
                } else {
                    baseLines.add(line);
                    i++;
                }
            }
        }

        i++;

        // 解析theirs内容
        while (i < lines.size()) {
            String line = lines.get(i);
            Matcher endMatcher = CONFLICT_END.matcher(line);
            if (endMatcher.find()) {
                break;
            } else {
                theirsLines.add(line);
                i++;
            }
        }

        if (i >= lines.size()) {
            log.warn("冲突块未正确结束: filePath={}, blockIndex={}", filePath, blockIndex);
            return null;
        }

        String theirsMarker = lines.get(i);
        Matcher endMatcher = CONFLICT_END.matcher(theirsMarker);
        String theirsBranch = endMatcher.find() ? endMatcher.group(1).trim() : "theirs";

        return ConflictBlock.builder()
                .blockIndex(blockIndex)
                .startLine(startIndex + 1) // 1-based
                .endLine(i + 1) // 1-based
                .oursContent(String.join("\n", oursLines))
                .baseContent(hasBase ? String.join("\n", baseLines) : null)
                .theirsContent(String.join("\n", theirsLines))
                .resolved(false)
                .build();
    }

    /**
     * 生成解决后的内容
     *
     * @param parsedConflict 解析后的冲突
     * @param resolutions    解决方案列表
     * @return 解决后的内容
     */
    public String generateResolvedContent(ParsedConflict parsedConflict,
                                          List<ConflictBlock> resolutions) {
        if (parsedConflict == null || parsedConflict.getConflictBlocks().isEmpty()) {
            return parsedConflict != null ? parsedConflict.getOriginalContent() : "";
        }

        String[] lines = parsedConflict.getOriginalContent().split("\n", -1);
        StringBuilder result = new StringBuilder();
        int lineNum = 0;
        int resolutionIndex = 0;

        while (lineNum < lines.length) {
            String line = lines[lineNum];

            if (CONFLICT_START.matcher(line).find()) {
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
                while (lineNum < lines.length && !CONFLICT_END.matcher(lines[lineNum]).find()) {
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

    /**
     * 检查内容是否包含冲突标记
     *
     * @param content 内容
     * @return 是否包含冲突标记
     */
    public boolean hasConflictMarkers(String content) {
        if (content == null || content.isEmpty()) {
            return false;
        }
        return content.contains("<<<<<<<") && content.contains(">>>>>>>");
    }

    /**
     * 分割行（保留空行）
     *
     * @param content 内容
     * @return 行列表
     */
    private List<String> splitLines(String content) {
        List<String> lines = new ArrayList<>();
        int start = 0;
        int end;
        while ((end = content.indexOf('\n', start)) != -1) {
            lines.add(content.substring(start, end));
            start = end + 1;
        }
        if (start < content.length()) {
            lines.add(content.substring(start));
        }
        return lines;
    }

    /**
     * 去除尾部换行符
     *
     * @param str 字符串
     * @return 处理后的字符串
     */
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