package com.feeltens.git.diff;

import com.feeltens.git.dto.diff.DiffBlock;
import com.feeltens.git.dto.diff.DiffLine;
import com.feeltens.git.dto.diff.ThreeWayDiffVO;
import com.github.difflib.DiffUtils;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.DeltaType;
import com.github.difflib.patch.Patch;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Diff计算器
 * 使用java-diff-utils的Myers算法计算diff
 *
 * @author feeltens
 */
@Component
@Slf4j
public class DiffCalculator {

    /**
     * 计算三路diff
     *
     * @param baseContent   Base版本内容
     * @param oursContent   Ours版本内容
     * @param theirsContent Theirs版本内容
     * @param filePath      文件路径
     * @return 三路diff视图对象
     */
    public ThreeWayDiffVO calculateThreeWayDiff(String baseContent,
                                                String oursContent,
                                                String theirsContent,
                                                String filePath) {
        List<String> baseLines = splitLines(baseContent);
        List<String> oursLines = splitLines(oursContent);
        List<String> theirsLines = splitLines(theirsContent);

        log.debug("计算三路diff: filePath={}, baseLines={}, oursLines={}, theirsLines={}",
                filePath, baseLines.size(), oursLines.size(), theirsLines.size());

        // 计算base-ours diff
        Patch<String> baseOursPatch = DiffUtils.diff(baseLines, oursLines);
        List<DiffBlock> baseOursBlocks = convertPatchToBlocks(baseOursPatch,
                DiffBlock.BlockType.MODIFIED, baseLines, oursLines);

        // 计算base-theirs diff
        Patch<String> baseTheirsPatch = DiffUtils.diff(baseLines, theirsLines);
        List<DiffBlock> baseTheirsBlocks = convertPatchToBlocks(baseTheirsPatch,
                DiffBlock.BlockType.MODIFIED, baseLines, theirsLines);

        // 合并diff块
        List<DiffBlock> mergedBlocks = mergeDiffBlocks(baseOursBlocks, baseTheirsBlocks,
                baseLines, oursLines, theirsLines);

        return ThreeWayDiffVO.builder()
                .filePath(filePath)
                .baseContent(baseContent)
                .oursContent(oursContent)
                .theirsContent(theirsContent)
                .diffBlocks(mergedBlocks)
                .baseTotalLines(baseLines.size())
                .oursTotalLines(oursLines.size())
                .theirsTotalLines(theirsLines.size())
                .build();
    }

    /**
     * 计算两路diff
     *
     * @param left  左边内容
     * @param right 右边内容
     * @return Diff块列表
     */
    public List<DiffBlock> calculateTwoWayDiff(String left, String right) {
        List<String> leftLines = splitLines(left);
        List<String> rightLines = splitLines(right);

        Patch<String> patch = DiffUtils.diff(leftLines, rightLines);
        return convertPatchToBlocks(patch, DiffBlock.BlockType.MODIFIED,
                leftLines, rightLines);
    }

    /**
     * 将Patch转换为DiffBlock列表
     *
     * @param patch      Patch对象
     * @param blockType  块类型
     * @param leftLines  左边行列表
     * @param rightLines 右边行列表
     * @return Diff块列表
     */
    private List<DiffBlock> convertPatchToBlocks(Patch<String> patch,
                                                 DiffBlock.BlockType blockType,
                                                 List<String> leftLines,
                                                 List<String> rightLines) {
        List<DiffBlock> blocks = new ArrayList<>();

        for (AbstractDelta<String> delta : patch.getDeltas()) {
            DiffBlock block = convertDeltaToBlock(delta, blockType, leftLines, rightLines);
            if (block != null) {
                blocks.add(block);
            }
        }

        return blocks;
    }

    /**
     * 将Delta转换为DiffBlock
     *
     * @param delta      Delta对象
     * @param blockType  块类型
     * @param leftLines  左边行列表
     * @param rightLines 右边行列表
     * @return Diff块
     */
    private DiffBlock convertDeltaToBlock(AbstractDelta<String> delta,
                                          DiffBlock.BlockType blockType,
                                          List<String> leftLines,
                                          List<String> rightLines) {
        DeltaType type = delta.getType();
        int sourceStart = delta.getSource().getPosition();
        int sourceEnd = sourceStart + delta.getSource().getLines().size();
        int targetStart = delta.getTarget().getPosition();
        int targetEnd = targetStart + delta.getTarget().getLines().size();

        List<DiffLine> lines = new ArrayList<>();

        switch (type) {
            case INSERT:
                // 新增
                for (int i = 0; i < delta.getTarget().getLines().size(); i++) {
                    int lineIndex = targetStart + i;
                    lines.add(DiffLine.builder()
                            .baseLineNumber(null)
                            .oursLineNumber(lineIndex + 1)
                            .theirsLineNumber(null)
                            .content(delta.getTarget().getLines().get(i))
                            .status(DiffLine.LineStatus.ADDED)
                            .build());
                }
                break;

            case DELETE:
                // 删除
                for (int i = 0; i < delta.getSource().getLines().size(); i++) {
                    int lineIndex = sourceStart + i;
                    lines.add(DiffLine.builder()
                            .baseLineNumber(lineIndex + 1)
                            .oursLineNumber(null)
                            .theirsLineNumber(null)
                            .content(delta.getSource().getLines().get(i))
                            .status(DiffLine.LineStatus.DELETED)
                            .build());
                }
                break;

            case CHANGE:
                // 修改
                int maxLines = Math.max(delta.getSource().getLines().size(),
                        delta.getTarget().getLines().size());
                for (int i = 0; i < maxLines; i++) {
                    String sourceLine = i < delta.getSource().getLines().size() ?
                            delta.getSource().getLines().get(i) : null;
                    String targetLine = i < delta.getTarget().getLines().size() ?
                            delta.getTarget().getLines().get(i) : null;

                    lines.add(DiffLine.builder()
                            .baseLineNumber(sourceLine != null ? sourceStart + i + 1 : null)
                            .oursLineNumber(targetLine != null ? targetStart + i + 1 : null)
                            .theirsLineNumber(null)
                            .content(targetLine != null ? targetLine : sourceLine)
                            .status(DiffLine.LineStatus.MODIFIED)
                            .build());
                }
                break;

            default:
                log.warn("未知的Delta类型: {}", type);
                return null;
        }

        return DiffBlock.builder()
                .type(blockType)
                .baseStartLine(sourceStart + 1)
                .baseEndLine(sourceEnd)
                .oursStartLine(targetStart + 1)
                .oursEndLine(targetEnd)
                .theirsStartLine(null)
                .theirsEndLine(null)
                .lines(lines)
                .build();
    }

    /**
     * 合并diff块
     *
     * @param baseOursBlocks   base-ours diff块
     * @param baseTheirsBlocks base-theirs diff块
     * @param baseLines        base行列表
     * @param oursLines        ours行列表
     * @param theirsLines      theirs行列表
     * @return 合并后的diff块
     */
    private List<DiffBlock> mergeDiffBlocks(List<DiffBlock> baseOursBlocks,
                                            List<DiffBlock> baseTheirsBlocks,
                                            List<String> baseLines,
                                            List<String> oursLines,
                                            List<String> theirsLines) {
        // 简化实现：直接合并两个diff块列表
        // 实际实现需要更复杂的逻辑来对齐行号
        List<DiffBlock> merged = new ArrayList<>();
        merged.addAll(baseOursBlocks);
        return merged;
    }

    /**
     * 分割行
     *
     * @param content 内容
     * @return 行列表
     */
    private List<String> splitLines(String content) {
        if (content == null || content.isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.asList(content.split("\n", -1));
    }

}