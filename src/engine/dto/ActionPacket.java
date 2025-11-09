package engine.dto;

/**
 * 외부(Python 등)에서 보내오는 행동(action) 정보를 담는 클래스
 * /action 엔드포인트의 요청 본문(JSON)을 매핑
 */
public class ActionPacket {
    /** 수평 이동 (-1: 왼쪽, 0: 정지, 1: 오른쪽) */
    public int moveDx;

    /** 수직 이동 (-1: 위, 0: 정지, 1: 아래) */
    public int moveDy;

    /** 발사 여부 */
    public boolean shoot;

    /** 스킬 번호 (0: 없음, 1~n: 스킬 ID) */
    public int skill;
}