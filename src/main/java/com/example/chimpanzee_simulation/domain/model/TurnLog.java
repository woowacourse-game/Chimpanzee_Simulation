package com.example.chimpanzee_simulation.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TurnLog {

    private final int turn;
    private final List<String> messages = new ArrayList<>();

    public TurnLog(int turn) {
        this.turn = turn;
    }

    public int turn() {
        return turn;
    }

    public List<String> messages() {
        return Collections.unmodifiableList(messages);
    }

    public void add(String message) {
        messages.add(message);
    }
}
