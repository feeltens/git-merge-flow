package com.feeltens.git.service.impl;

import com.feeltens.git.diff.DiffCache;
import com.feeltens.git.diff.DiffCalculator;
import com.feeltens.git.dto.conflict.ConflictFileContent;
import com.feeltens.git.dto.conflict.ConflictSession;
import com.feeltens.git.dto.diff.ThreeWayDiffVO;
import com.feeltens.git.service.ConflictSessionService;
import com.feeltens.git.service.JGitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * DiffRenderServiceImpl单元测试
 *
 * @author feeltens
 */
@ExtendWith(MockitoExtension.class)
class DiffRenderServiceImplTest {

    @Mock
    private JGitService jGitService;

    @Mock
    private DiffCalculator diffCalculator;

    @Mock
    private DiffCache diffCache;

    @Mock
    private ConflictSessionService conflictSessionService;

    @InjectMocks
    private DiffRenderServiceImpl diffRenderService;

    private ConflictSession mockSession;
    private ConflictFileContent mockContent;
    private ThreeWayDiffVO mockDiffVO;

    @BeforeEach
    void setUp() {
        mockSession = ConflictSession.builder()
                .sessionId("test-session-id")
                .localRepoPath("/tmp/test-repo")
                .build();

        mockContent = ConflictFileContent.builder()
                .filePath("test.txt")
                .baseContent("base content")
                .oursContent("ours content")
                .theirsContent("theirs content")
                .build();

        mockDiffVO = ThreeWayDiffVO.builder()
                .filePath("test.txt")
                .baseContent("base content")
                .oursContent("ours content")
                .theirsContent("theirs content")
                .build();
    }

    @Test
    void testCalculateThreeWayDiff_Success() {
        // Given
        String sessionId = "test-session-id";
        String filePath = "test.txt";

        when(conflictSessionService.getSession(sessionId)).thenReturn(mockSession);
        when(diffCache.get(anyString())).thenReturn(null);
        when(jGitService.getConflictFileContent("/tmp/test-repo", filePath)).thenReturn(mockContent);
        when(diffCalculator.calculateThreeWayDiff(
                "base content", "ours content", "theirs content", filePath))
                .thenReturn(mockDiffVO);

        // When
        ThreeWayDiffVO result = diffRenderService.calculateThreeWayDiff(sessionId, filePath);

        // Then
        assertNotNull(result);
        assertEquals("test.txt", result.getFilePath());
        verify(diffCache).put(anyString(), eq(mockDiffVO));
    }

    @Test
    void testCalculateThreeWayDiff_CacheHit() {
        // Given
        String sessionId = "test-session-id";
        String filePath = "test.txt";

        when(diffCache.get(anyString())).thenReturn(mockDiffVO);

        // When
        ThreeWayDiffVO result = diffRenderService.calculateThreeWayDiff(sessionId, filePath);

        // Then
        assertNotNull(result);
        assertEquals("test.txt", result.getFilePath());
        verify(conflictSessionService, never()).getSession(anyString());
        verify(jGitService, never()).getConflictFileContent(anyString(), anyString());
        verify(diffCalculator, never()).calculateThreeWayDiff(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void testCalculateThreeWayDiff_SessionNotFound() {
        // Given
        String sessionId = "invalid-session-id";
        String filePath = "test.txt";

        when(conflictSessionService.getSession(sessionId)).thenReturn(null);

        // When & Then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> diffRenderService.calculateThreeWayDiff(sessionId, filePath)
        );
        assertTrue(exception.getMessage().contains("计算三路diff失败"));
        assertTrue(exception.getCause().getMessage().contains("会话不存在"));
    }

    @Test
    void testCalculateThreeWayDiff_LocalRepoPathEmpty() {
        // Given
        String sessionId = "test-session-id";
        String filePath = "test.txt";

        ConflictSession sessionWithEmptyPath = ConflictSession.builder()
                .sessionId(sessionId)
                .localRepoPath("")
                .build();

        when(conflictSessionService.getSession(sessionId)).thenReturn(sessionWithEmptyPath);

        // When & Then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> diffRenderService.calculateThreeWayDiff(sessionId, filePath)
        );
        assertTrue(exception.getMessage().contains("计算三路diff失败"));
        assertTrue(exception.getCause().getMessage().contains("本地仓库路径为空"));
    }

    @Test
    void testCalculateThreeWayDiff_LocalRepoPathNull() {
        // Given
        String sessionId = "test-session-id";
        String filePath = "test.txt";

        ConflictSession sessionWithNullPath = ConflictSession.builder()
                .sessionId(sessionId)
                .localRepoPath(null)
                .build();

        when(conflictSessionService.getSession(sessionId)).thenReturn(sessionWithNullPath);

        // When & Then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> diffRenderService.calculateThreeWayDiff(sessionId, filePath)
        );
        assertTrue(exception.getMessage().contains("计算三路diff失败"));
        assertTrue(exception.getCause().getMessage().contains("本地仓库路径为空"));
    }

    @Test
    void testCalculateThreeWayDiff_JGitServiceException() {
        // Given
        String sessionId = "test-session-id";
        String filePath = "test.txt";

        when(conflictSessionService.getSession(sessionId)).thenReturn(mockSession);
        when(diffCache.get(anyString())).thenReturn(null);
        when(jGitService.getConflictFileContent("/tmp/test-repo", filePath))
                .thenThrow(new RuntimeException("JGit service error"));

        // When & Then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> diffRenderService.calculateThreeWayDiff(sessionId, filePath)
        );
        assertTrue(exception.getMessage().contains("计算三路diff失败"));
    }

    @Test
    void testCalculateThreeWayDiff_DiffCalculatorException() {
        // Given
        String sessionId = "test-session-id";
        String filePath = "test.txt";

        when(conflictSessionService.getSession(sessionId)).thenReturn(mockSession);
        when(diffCache.get(anyString())).thenReturn(null);
        when(jGitService.getConflictFileContent("/tmp/test-repo", filePath)).thenReturn(mockContent);
        when(diffCalculator.calculateThreeWayDiff(
                anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Diff calculator error"));

        // When & Then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> diffRenderService.calculateThreeWayDiff(sessionId, filePath)
        );
        assertTrue(exception.getMessage().contains("计算三路diff失败"));
    }
}
