package com.example.etfsj.service;

import com.example.etfsj.domain.User;
import org.springframework.stereotype.Component;

@Component
public class ScoreService {

    public int calculateUserScore(User user) {
        int score = 0;

        if ("beginner".equals(user.getExperience())) score -= 2;
        if ("aggressive".equals(user.getRisk())) score += 2;
        if ("income".equals(user.getGoal())) score -= 1;

        return score;
    }
}
