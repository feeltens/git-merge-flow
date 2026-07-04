package com.feeltens.git.util;

import com.feeltens.git.config.JGitConfig;
import com.feeltens.git.config.RepoCacheConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * GitPathBuilder 单元测试
 *
 * @author feeltens
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GitPathBuilderTest {

    @Mock
    private JGitConfig jgitConfig;

    @Mock
    private RepoCacheConfig repoCacheConfig;

    @InjectMocks
    private GitPathBuilder gitPathBuilder;

    private static final String TEMP_REPO_PATH = "/tmp/git-merge-flow/conflict-cache";
    private static final String CACHE_PATH = "/tmp/git-merge-flow/repo-cache";
    private static final String PROJECT_NAME = "demo-git";
    private static final String SESSION_ID = "abc123def456";

    @BeforeEach
    void setUp() {
        when(jgitConfig.getTempRepoPath()).thenReturn(TEMP_REPO_PATH);
        when(repoCacheConfig.getCachePath()).thenReturn(CACHE_PATH);
    }

    @Test
    void testGetCacheRepoPath() {
        String expected = CACHE_PATH + "/" + PROJECT_NAME;
        String actual = gitPathBuilder.getCacheRepoPath(PROJECT_NAME);
        assertEquals(expected, actual);
    }

    @Test
    void testGetSessionRepoPath() {
        String expected = TEMP_REPO_PATH + "/" + PROJECT_NAME + "/" + SESSION_ID;
        String actual = gitPathBuilder.getSessionRepoPath(PROJECT_NAME, SESSION_ID);
        assertEquals(expected, actual);
    }

    @Test
    void testGetProjectPath() {
        String expected = TEMP_REPO_PATH + "/" + PROJECT_NAME;
        String actual = gitPathBuilder.getProjectPath(PROJECT_NAME);
        assertEquals(expected, actual);
    }

    @Test
    void testGetCustomPathWithSubPath() {
        String customRoot = "/custom/path";
        String subPath = "subdir";
        String expected = customRoot + "/" + PROJECT_NAME + "/" + subPath;
        String actual = gitPathBuilder.getCustomPath(customRoot, PROJECT_NAME, subPath);
        assertEquals(expected, actual);
    }

    @Test
    void testGetCustomPathWithoutSubPath() {
        String customRoot = "/custom/path";
        String expected = customRoot + "/" + PROJECT_NAME;
        String actual = gitPathBuilder.getCustomPath(customRoot, PROJECT_NAME, null);
        assertEquals(expected, actual);
    }

    @Test
    void testGetCustomPathWithEmptySubPath() {
        String customRoot = "/custom/path";
        String expected = customRoot + "/" + PROJECT_NAME;
        String actual = gitPathBuilder.getCustomPath(customRoot, PROJECT_NAME, "");
        assertEquals(expected, actual);
    }

    @Test
    void testGetCacheRepoPathWithDifferentProjectName() {
        String differentProject = "another-project";
        String expected = CACHE_PATH + "/" + differentProject;
        String actual = gitPathBuilder.getCacheRepoPath(differentProject);
        assertEquals(expected, actual);
    }

    @Test
    void testGetSessionRepoPathWithDifferentSessionId() {
        String differentSession = "xyz789";
        String expected = TEMP_REPO_PATH + "/" + PROJECT_NAME + "/" + differentSession;
        String actual = gitPathBuilder.getSessionRepoPath(PROJECT_NAME, differentSession);
        assertEquals(expected, actual);
    }

    @Test
    void testPathConsistency() {
        // 测试不同方法返回的路径格式一致性
        String cachePath = gitPathBuilder.getCacheRepoPath(PROJECT_NAME);
        String sessionPath = gitPathBuilder.getSessionRepoPath(PROJECT_NAME, SESSION_ID);
        String projectPath = gitPathBuilder.getProjectPath(PROJECT_NAME);

        // 验证路径格式
        assertEquals(CACHE_PATH + "/" + PROJECT_NAME, cachePath);
        assertEquals(TEMP_REPO_PATH + "/" + PROJECT_NAME + "/" + SESSION_ID, sessionPath);
        assertEquals(TEMP_REPO_PATH + "/" + PROJECT_NAME, projectPath);

        // 验证项目路径与会话路径的关系
        assertTrue(sessionPath.startsWith(projectPath));
    }

    @Test
    void testGetCustomPathConsistencyWithSessionPath() {
        // 测试自定义路径与会话路径的一致性
        String customPath = gitPathBuilder.getCustomPath(TEMP_REPO_PATH, PROJECT_NAME, SESSION_ID);
        String sessionPath = gitPathBuilder.getSessionRepoPath(PROJECT_NAME, SESSION_ID);

        assertEquals(customPath, sessionPath);
    }

}