package com.example.chimpanzee_simulation.service.ui;

public final class ConsoleUI {
    private static final String ESC = "\u001b[";

    private ConsoleUI() {}

    /** 화면 전체 지우고 커서 홈으로 */
    public static void clear() {
        System.out.print(ESC + "2J" + ESC + "H");
        System.out.flush();
    }

    /** 대체 스크린 버퍼 진입 (원래 화면은 보존됨) */
    public static void enterAltScreen() {
        System.out.print(ESC + "?1049h");
        System.out.flush();
    }

    /** 대체 스크린 버퍼 종료 (원래 화면 복귀) */
    public static void exitAltScreen() {
        System.out.print(ESC + "?1049l");
        System.out.flush();
    }

    public static void hideCursor() {
        System.out.print(ESC + "?25l");
        System.out.flush();
    }

    public static void showCursor() {
        System.out.print(ESC + "?25h");
        System.out.flush();
    }
}
