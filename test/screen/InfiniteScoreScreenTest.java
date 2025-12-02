package screen;

import engine.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
class InfiniteScoreScreenTest {
    private MockedStatic<Core> coreMock;
    private FileManager fileManagerMock;
    private GameState gameStateMock;
    private Cooldown cooldownMock;

    @BeforeEach
    void setUp() {
        // 1. Core 클래스의 정적 메서드들을 가로채기 위해 MockedStatic 생성
        coreMock = Mockito.mockStatic(Core.class);

        // 2. 의존성 Mock 객체 생성
        fileManagerMock = mock(FileManager.class);
        gameStateMock = mock(GameState.class);
        cooldownMock = mock(Cooldown.class);
        DrawManager drawManagerMock = mock(DrawManager.class);
        InputManager inputManagerMock = mock(InputManager.class);

        // 3. Core가 요청받을 때 가짜 객체(Mock)를 반환하도록 설정
        coreMock.when(Core::getFileManager).thenReturn(fileManagerMock);
        coreMock.when(Core::getDrawManager).thenReturn(drawManagerMock);
        coreMock.when(Core::getInputManager).thenReturn(inputManagerMock);
        coreMock.when(Core::getLogger).thenReturn(Logger.getGlobal());
        coreMock.when(() -> Core.getCooldown(anyInt())).thenReturn(cooldownMock);

        // 4. GameState 기본 설정 (생성자에서 사용)
        when(gameStateMock.getScore()).thenReturn(1000);
        when(gameStateMock.getLivesRemaining()).thenReturn(3);
        when(gameStateMock.getBulletsShot()).thenReturn(50);
        when(gameStateMock.getShipsDestroyed()).thenReturn(20);
    }

    @AfterEach
    void tearDown() {
        // 정적 Mock 해제 (메모리 누수 방지 및 다른 테스트 간섭 방지)
        coreMock.close();
    }

    @Test
    @DisplayName("생성자: 점수가 기존 기록보다 높으면 신기록(isNewRecord=true)이어야 한다")
    void testConstructor_NewRecord() throws IOException, NoSuchFieldException, IllegalAccessException {
        // Given: 기존 1등 점수가 500점이고, 현재 점수(GameState)는 1000점인 상황
        List<Score> lowScores = new ArrayList<>();
        lowScores.add(new Score("TEST", 500));
        when(fileManagerMock.loadInfiniteHighScores()).thenReturn(lowScores);

        // When: 화면 객체 생성
        InfiniteScoreScreen screen = new InfiniteScoreScreen(800, 600, 60, gameStateMock);

        // Then: isNewRecord 필드가 true여야 함
        // (private 필드이므로 Reflection을 사용하여 값 확인)
        Field field = InfiniteScoreScreen.class.getDeclaredField("isNewRecord");
        field.setAccessible(true);
        boolean isNewRecord = (boolean) field.get(screen);

        assertTrue(isNewRecord, "기존 점수보다 높으므로 신기록이어야 합니다.");
    }

    @Test
    @DisplayName("생성자: 점수가 기존 기록보다 낮으면 신기록이 아니어야(isNewRecord=false) 한다")
    void testConstructor_NoNewRecord() throws IOException, NoSuchFieldException, IllegalAccessException {
        // Given: 기존 1등 점수가 5000점이고, 현재 점수는 1000점인 상황
        List<Score> highScores = new ArrayList<>();
        // 7개의 높은 점수로 채움
        for(int i=0; i<7; i++) {
            highScores.add(new Score("USER", 5000));
        }
        when(fileManagerMock.loadInfiniteHighScores()).thenReturn(highScores);

        // When
        InfiniteScoreScreen screen = new InfiniteScoreScreen(800, 600, 60, gameStateMock);

        // Then
        Field field = InfiniteScoreScreen.class.getDeclaredField("isNewRecord");
        field.setAccessible(true);
        boolean isNewRecord = (boolean) field.get(screen);

        assertFalse(isNewRecord, "기존 점수보다 낮으므로 신기록이 아니어야 합니다.");
    }

    @Test
    @DisplayName("saveScore: 점수 저장 시 FileManager가 호출되어야 한다")
    void testSaveScore() throws Exception {
        // Given: 초기화
        when(fileManagerMock.loadInfiniteHighScores()).thenReturn(new ArrayList<>()); // 빈 리스트 반환
        InfiniteScoreScreen screen = new InfiniteScoreScreen(800, 600, 60, gameStateMock);

        // private 메서드인 saveScore를 호출하기 위해 Reflection 사용
        // (또는 update() 메서드를 통해 간접 호출할 수도 있지만, 로직 단위 테스트를 위해 직접 호출)
        Method saveMethod = InfiniteScoreScreen.class.getDeclaredMethod("saveScore");
        saveMethod.setAccessible(true);

        // When: 저장 실행
        saveMethod.invoke(screen);

        // Then: FileManager.saveInfiniteHighScores()가 호출되었는지 검증
        verify(fileManagerMock, times(1)).saveInfiniteHighScores(anyList());
    }
}