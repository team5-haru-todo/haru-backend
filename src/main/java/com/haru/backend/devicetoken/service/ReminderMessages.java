package com.haru.backend.devicetoken.service;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 예약 알림 문구 모음.
 * 각 케이스마다 여러 문구를 두고, 발송 시 사용자별로 하나를 랜덤으로 골라 보낸다.
 */
public final class ReminderMessages {

    private ReminderMessages() {
    }

    /** 오전 9시 - 아직 '오늘의 한 개'를 정하지 않은 사용자. */
    public static final List<ReminderMessage> MORNING_NO_TASK = List.of(
            new ReminderMessage("오늘은 어떤 하루로 만들까요? ☀️", "오늘 꼭 해내고 싶은 한 가지를 적어보세요."),
            new ReminderMessage("오늘의 한 개, 정해볼까요?", "거창하지 않아도 괜찮아요. 지금 하나만 골라보세요."),
            new ReminderMessage("하루의 첫 체크포인트 📌", "오늘 가장 중요한 한 가지를 미리 정해두세요."),
            new ReminderMessage("오늘의 나에게 작은 약속", "오늘 끝내고 싶은 일을 한 개만 적어볼까요?"),
            new ReminderMessage("좋은 아침이에요!", "오늘의 목표 하나를 정하면 하루가 조금 선명해져요."),
            new ReminderMessage("오늘도 한 개부터 시작해요", "해야 할 일 중 가장 먼저 떠오르는 것을 적어보세요."),
            new ReminderMessage("오늘 무엇을 완료하고 싶나요?", "지금 정한 한 가지가 오늘의 방향이 되어줄 거예요."),
            new ReminderMessage("딱 하나만 고른다면?", "오늘 반드시 끝내고 싶은 일은 무엇인가요?")
    );

    /** 오후 9시 ① - 오늘 하루 '오늘의 한 개'를 정하지 않은 사용자. */
    public static final List<ReminderMessage> EVENING_NO_TASK = List.of(
            new ReminderMessage("오늘이 지나기 전, 한 개만 🌙", "지금 할 수 있는 작은 일 하나를 정해보세요."),
            new ReminderMessage("오늘의 한 개가 아직 비어 있어요", "늦지 않았어요. 가볍게 하나 적고 시작해볼까요?"),
            new ReminderMessage("오늘을 그냥 보내기 아쉽다면", "10분 안에 할 수 있는 일 하나를 골라보세요."),
            new ReminderMessage("남은 시간, 하나면 충분해요", "오늘 끝낼 수 있는 가장 작은 일을 적어보세요."),
            new ReminderMessage("지금 시작해도 괜찮아요", "완벽한 계획 대신 작은 행동 하나를 남겨보세요."),
            new ReminderMessage("오늘의 마지막 작은 도전", "잠들기 전 완료할 수 있는 일을 하나 정해볼까요?"),
            new ReminderMessage("오늘의 한 칸을 채워볼까요?", "사소한 일도 좋아요. 오늘의 한 개를 남겨보세요."),
            new ReminderMessage("오늘을 완료로 마무리해요", "물 한 잔 마시기처럼 작은 목표도 충분해요.")
    );

    /** 오후 9시 ② - '오늘의 한 개'를 정했지만 아직 완료하지 않은 사용자. */
    public static final List<ReminderMessage> EVENING_UNCOMPLETED = List.of(
            new ReminderMessage("오늘의 한 개, 조금만 더!", "시작했던 일을 마무리하고 오늘을 완료해보세요."),
            new ReminderMessage("목표까지 한 걸음 남았어요", "지금 10분만 집중하면 완료할 수 있을지도 몰라요."),
            new ReminderMessage("오늘의 약속을 기억하고 있나요?", "완벽하게 하지 않아도 괜찮아요. 가능한 만큼 이어가 보세요."),
            new ReminderMessage("거의 다 왔을지도 몰라요 👀", "오늘의 한 개를 다시 확인하고 마지막 힘을 내보세요."),
            new ReminderMessage("오늘의 한 개가 기다리고 있어요", "잠깐만 집중해서 완료 표시까지 남겨볼까요?"),
            new ReminderMessage("작은 마무리가 큰 만족으로", "시작한 일을 끝내고 가벼운 마음으로 하루를 마쳐보세요."),
            new ReminderMessage("오늘 정한 일을 놓치지 마세요", "할 수 있는 만큼 마무리하고 오늘의 기록을 남겨보세요."),
            new ReminderMessage("완료 버튼까지 함께 가요", "조금 남았다면 지금 끝내고 기분 좋게 체크해보세요.")
    );

    /** 오후 9시 ③ - '오늘의 한 개'는 완료했지만 추가로 완료하지 않은 할 일이 남아있는 사용자. */
    public static final List<ReminderMessage> EVENING_COMPLETED_WITH_REMAINING = List.of(
            new ReminderMessage("오늘의 한 개, 해냈어요! 🎉", "여유가 있다면 남은 일도 하나 더 정리해볼까요?"),
            new ReminderMessage("오늘의 중요한 일은 완료!", "충분히 잘했어요. 남은 일은 가볍게 확인해보세요."),
            new ReminderMessage("오늘의 목표를 지켰네요 👏", "힘이 남아 있다면 미완료 할 일을 하나 더 끝내보세요."),
            new ReminderMessage("오늘도 한 개 성공!", "여기서 마무리해도 좋아요. 조금 더 할 수 있다면 한 걸음만 더!"),
            new ReminderMessage("가장 중요한 일을 완료했어요", "남은 일 중 금방 끝낼 수 있는 일이 있는지 살펴보세요."),
            new ReminderMessage("오늘의 약속을 완료했어요", "수고했어요. 남은 할 일은 내일로 옮길지 결정해보세요."),
            new ReminderMessage("오늘 할 일을 멋지게 해냈어요", "남은 일까지 정리하면 내일이 조금 더 가벼워져요."),
            new ReminderMessage("오늘의 한 개, 체크 완료 ✅", "아직 에너지가 남아 있다면 작은 일 하나를 더 완료해보세요.")
    );

    /** 주어진 문구 목록에서 하나를 랜덤으로 고른다. */
    public static ReminderMessage pickRandom(List<ReminderMessage> pool) {
        return pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
    }
}
