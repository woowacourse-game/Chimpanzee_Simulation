package com.example.chimpanzee_simulation.service.ui;

import com.example.chimpanzee_simulation.domain.model.Chimpanzee;
import com.example.chimpanzee_simulation.domain.model.SimulationState;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.function.Function;

public class ChimpanzeeTableScreen {

    private final Scanner scanner;

    public ChimpanzeeTableScreen(Scanner scanner) {
        this.scanner = scanner;
    }

    /** 표 전용 화면: 대체 스크린 버퍼에서 표를 렌더링하고 내부 입력 루프를 돈다. */
    public void open(SimulationState state) {
        ConsoleUI.enterAltScreen();
        ConsoleUI.hideCursor();
        try {
            int page = 1;
            int pageSize = 20;
            boolean onlyAlive = false;

            while (true) {
                ConsoleUI.clear();
                int totalPages = totalPages(state, onlyAlive, pageSize);
                System.out.printf("침팬지 현황표 %s  페이지 %d / %d%n",
                        (onlyAlive ? "[생존만]" : "[전체]"), page, Math.max(totalPages, 1));
                System.out.println();

                renderTable(state, onlyAlive, page, pageSize);

                System.out.println();
                System.out.print("[n: 다음 / p: 이전 / a: 생존만 토글 / s: 돌아가기] > ");
                String cmd = scanner.nextLine().trim().toLowerCase();

                switch (cmd) {
                    case "n" -> page++;
                    case "p" -> page = Math.max(1, page - 1);
                    case "a" -> { onlyAlive = !onlyAlive; page = 1; }
                    case "s" -> { return; } // 표 화면 종료 → 원래 로그 화면으로 복귀
                    default -> { /* 무시 */ }
                }
            }
        } finally {
            ConsoleUI.showCursor();
            ConsoleUI.exitAltScreen();
        }
    }

    private int totalPages(SimulationState state, boolean onlyAlive, int pageSize) {
        List<Chimpanzee> list = new ArrayList<>(state.chimpanzees());
        if (onlyAlive) list.removeIf(c -> !c.isAlive());
        return (int) Math.ceil(Math.max(1, list.size()) / (double) pageSize);
    }

    /** 실제 표 렌더링 */
    private void renderTable(SimulationState state, boolean onlyAlive, int page, int pageSize) {
        List<Chimpanzee> all = new ArrayList<>(state.chimpanzees());
        if (onlyAlive) all.removeIf(c -> !c.isAlive());

        if (all.isEmpty()) {
            System.out.println(onlyAlive ? "생존한 침팬지가 없습니다." : "등록된 침팬지가 없습니다.");
            return;
        }

        // 정렬: id → age(desc) → health(desc)
        all.sort(Comparator.comparing(Chimpanzee::getId)
                .thenComparing(Chimpanzee::getAge, Comparator.reverseOrder())
                .thenComparing(Chimpanzee::getHealth, Comparator.reverseOrder()));

        int total = all.size();
        int totalPages = Math.max(1, (int) Math.ceil(total / (double) pageSize));
        int p = Math.min(Math.max(1, page), totalPages);
        int from = (p - 1) * pageSize;
        int to = Math.min(from + pageSize, total);
        List<Chimpanzee> list = all.subList(from, to);

        // === 컬럼 정의 ===
        String[] headers = {
                "ID","성별","나이","연령대","건강","힘","민첩",
                "번식률","수명","알파","생존","사망사유","임신","분만예정","아버지ID"
        };

        // 각 셀 값 생성기 (프로젝트 실제 게터에 맞춤)
        Function<Chimpanzee, String[]> rowMapper = c -> new String[]{
                String.valueOf(c.getId()),
                String.valueOf(c.getSex()),
                String.valueOf(c.getAge()),
                String.valueOf(c.getAgeCategory()),
                String.valueOf(c.getHealth()),
                String.valueOf(c.getStrength()),
                String.valueOf(c.getAgility()),
                String.format("%.2f", c.getReproductionRate()),
                String.valueOf(c.getLongevity()),
                c.isAlpha() ? "Y" : "N",
                c.isAlive() ? "Y" : "N",
                c.isAlive() ? "" : String.valueOf(c.getDeathReason()),
                c.isPregnant() ? "Y" : "N",
                c.isPregnant() ? String.valueOf(c.getPregnancyDueTurn()) : "",
                (c.isPregnant() && c.getPregnancyFatherId()!=null) ? String.valueOf(c.getPregnancyFatherId()) : ""
        };

        // 폭 계산
        int cols = headers.length;
        int[] widths = new int[cols];
        for (int i = 0; i < cols; i++) widths[i] = headers[i].length();
        List<String[]> rows = new ArrayList<>();
        for (Chimpanzee c : list) {
            String[] r = rowMapper.apply(c);
            rows.add(r);
            for (int i = 0; i < cols; i++) {
                String v = (r[i] == null ? "" : r[i]);
                widths[i] = Math.max(widths[i], v.length());
            }
        }

        String sep = buildSep(widths);
        System.out.println(sep);
        // 헤더
        System.out.println(buildRow(headers, widths));
        System.out.println(sep);
        // 데이터
        for (String[] r : rows) {
            System.out.println(buildRow(r, widths));
        }
        System.out.println(sep);
        System.out.printf("표시: %d ~ %d / 총 %d 마리%n", from + 1, to, total);
    }

    private String buildSep(int[] widths) {
        StringBuilder sb = new StringBuilder("+");
        for (int w : widths) sb.append("-".repeat(w + 2)).append("+");
        return sb.toString();
    }

    private String buildRow(String[] values, int[] widths) {
        StringBuilder sb = new StringBuilder("|");
        for (int i = 0; i < widths.length; i++) {
            String v = (i < values.length && values[i] != null) ? values[i] : "";
            sb.append(" ").append(pad(v, widths[i])).append(" |");
        }
        return sb.toString();
    }

    private String pad(String s, int w) {
        if (s.length() >= w) return s;
        return s + " ".repeat(w - s.length());
    }
}
