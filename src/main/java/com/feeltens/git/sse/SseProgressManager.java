package com.feeltens.git.sse;

import com.feeltens.git.vo.resp.InitProgressVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SSE进度推送管理器
 *
 * @author feeltens
 */
@Component
@Slf4j
public class SseProgressManager {

    /**
     * SSE连接缓存 (sessionId -> SseEmitter)
     */
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    /**
     * SSE连接超时时间（毫秒）
     */
    private static final long SSE_TIMEOUT = 300000L; // 5分钟

    /**
     * 创建SSE连接
     *
     * @param sessionId 会话ID
     * @return SSE发射器
     */
    public SseEmitter createEmitter(String sessionId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        // 完成回调
        emitter.onCompletion(() -> {
            emitters.remove(sessionId);
            log.info("SSE连接完成: sessionId={}", sessionId);
        });

        // 超时回调
        emitter.onTimeout(() -> {
            emitters.remove(sessionId);
            log.warn("SSE连接超时: sessionId={}", sessionId);
        });

        // 错误回调
        emitter.onError((ex) -> {
            emitters.remove(sessionId);
            log.error("SSE连接错误: sessionId={}", sessionId, ex);
        });

        emitters.put(sessionId, emitter);
        log.info("创建SSE连接: sessionId={}, 当前连接数={}", sessionId, emitters.size());
        return emitter;
    }

    /**
     * 推送进度
     *
     * @param sessionId 会话ID
     * @param progress  进度信息
     */
    public void sendProgress(String sessionId, InitProgressVO progress) {
        SseEmitter emitter = emitters.get(sessionId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("progress")
                        .data(progress)
                        .id(String.valueOf(System.currentTimeMillis())));

                log.debug("推送进度成功: sessionId={}, status={}, progress={}",
                        sessionId, progress.getStatus(), progress.getProgress());

                // 如果是最终状态，完成连接
                if (progress.getStatus() == com.feeltens.git.enums.InitProgressStatus.READY ||
                        progress.getStatus() == com.feeltens.git.enums.InitProgressStatus.FAILED) {
                    emitter.complete();
                    log.info("SSE连接完成(最终状态): sessionId={}, status={}",
                            sessionId, progress.getStatus());
                }
            } catch (IOException e) {
                log.error("推送进度失败: sessionId={}", sessionId, e);
                emitter.completeWithError(e);
            }
        } else {
            log.warn("SSE连接不存在，无法推送进度: sessionId={}", sessionId);
        }
    }

    /**
     * 获取活跃连接数
     *
     * @return 活跃连接数
     */
    public int getActiveConnections() {
        return emitters.size();
    }

    /**
     * 移除指定会话的SSE连接
     *
     * @param sessionId 会话ID
     */
    public void removeEmitter(String sessionId) {
        SseEmitter emitter = emitters.remove(sessionId);
        if (emitter != null) {
            emitter.complete();
            log.info("主动移除SSE连接: sessionId={}", sessionId);
        }
    }

    /**
     * 清理所有连接
     */
    public void clearAll() {
        emitters.values().forEach(SseEmitter::complete);
        emitters.clear();
        log.info("清理所有SSE连接");
    }

}