package com.feeltens.git.controller;

import com.feeltens.git.dto.diff.ThreeWayDiffVO;
import com.feeltens.git.service.DiffRenderService;
import com.feeltens.git.vo.base.CloudResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;

/**
 * Diff控制器
 *
 * @author feeltens
 */
@RestController
@RequestMapping("/api/diff")
@Slf4j
public class DiffController {

    @Resource
    private DiffRenderService diffRenderService;

    /**
     * 获取三路diff数据
     *
     * @param sessionId 会话ID
     * @param filePath  文件路径
     * @return 三路diff视图对象
     */
    @GetMapping("/three-way")
    public CloudResponse<ThreeWayDiffVO> getThreeWayDiff(
            @RequestParam String sessionId,
            @RequestParam String filePath) {
        try {
            log.info("获取三路diff: sessionId={}, filePath={}", sessionId, filePath);
            ThreeWayDiffVO diffVO = diffRenderService.calculateThreeWayDiff(sessionId, filePath);
            return CloudResponse.success(diffVO);
        } catch (Exception e) {
            log.error("获取三路diff失败: sessionId={}, filePath={}", sessionId, filePath, e);
            return CloudResponse.fail("获取三路diff失败: " + e.getMessage());
        }
    }

    /**
     * 获取Diff查看器页面
     *
     * @param sessionId 会话ID
     * @param filePath  文件路径
     * @return ModelAndView
     */
    @GetMapping("/viewer")
    public ModelAndView diffViewer(
            @RequestParam String sessionId,
            @RequestParam String filePath) {
        try {
            log.info("访问Diff查看器: sessionId={}, filePath={}", sessionId, filePath);

            ThreeWayDiffVO diffVO = diffRenderService.calculateThreeWayDiff(sessionId, filePath);

            ModelAndView modelAndView = new ModelAndView("diff-viewer");
            modelAndView.addObject("filePath", filePath);
            modelAndView.addObject("baseContent", diffVO.getBaseContent());
            modelAndView.addObject("oursContent", diffVO.getOursContent());
            modelAndView.addObject("theirsContent", diffVO.getTheirsContent());

            return modelAndView;
        } catch (Exception e) {
            log.error("访问Diff查看器失败: sessionId={}, filePath={}", sessionId, filePath, e);
            ModelAndView modelAndView = new ModelAndView("diff-viewer");
            modelAndView.addObject("filePath", filePath);
            modelAndView.addObject("baseContent", "加载失败: " + e.getMessage());
            modelAndView.addObject("oursContent", "");
            modelAndView.addObject("theirsContent", "");
            return modelAndView;
        }
    }

}