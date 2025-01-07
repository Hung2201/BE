package com.example.sourcebase.service;

import java.util.Map;

public interface IRatedRankService {
    Map<Long, Double> getMapManagerRatingPointToUser(Long userId);
}
