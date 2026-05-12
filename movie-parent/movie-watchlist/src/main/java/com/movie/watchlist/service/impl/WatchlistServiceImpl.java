package com.movie.watchlist.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.movie.common.exception.BusinessException;
import com.movie.watchlist.dto.*;
import com.movie.watchlist.entity.WatchRecord;
import com.movie.watchlist.repository.WatchRecordRepository;
import com.movie.watchlist.service.WatchlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class WatchlistServiceImpl implements WatchlistService {

    private final WatchRecordRepository watchRecordRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public Page<WatchRecordVO> listMyWatchlist(Long userId, String status, long page, long size) {
        LambdaQueryWrapper<WatchRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WatchRecord::getUserId, userId);
        if (status != null && !status.isBlank()) {
            wrapper.eq(WatchRecord::getStatus, status);
        }
        wrapper.orderByDesc(WatchRecord::getUpdatedAt);

        Page<WatchRecord> recordPage = watchRecordRepository.selectPage(new Page<>(page, size), wrapper);

        List<WatchRecordVO> vos = recordPage.getRecords().stream().map(this::toVO).toList();
        enrichMovieInfo(vos);

        Page<WatchRecordVO> voPage = new Page<>(page, size);
        voPage.setTotal(recordPage.getTotal());
        voPage.setRecords(vos);
        return voPage;
    }

    @Override
    public Page<WatchRecordVO> listMyHistory(Long userId, long page, long size) {
        LambdaQueryWrapper<WatchRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WatchRecord::getUserId, userId)
               .eq(WatchRecord::getStatus, "WATCHED")
               .orderByDesc(WatchRecord::getWatchedAt);

        Page<WatchRecord> recordPage = watchRecordRepository.selectPage(new Page<>(page, size), wrapper);

        List<WatchRecordVO> vos = recordPage.getRecords().stream().map(this::toVO).toList();
        enrichMovieInfo(vos);

        Page<WatchRecordVO> voPage = new Page<>(page, size);
        voPage.setTotal(recordPage.getTotal());
        voPage.setRecords(vos);
        return voPage;
    }

    @Override
    @Transactional
    public WatchRecordVO addRecord(Long userId, WatchRecordCreateRequest request) {
        // 检查是否已有记录
        WatchRecord existing = watchRecordRepository.selectOne(
                new LambdaQueryWrapper<WatchRecord>()
                        .eq(WatchRecord::getUserId, userId)
                        .eq(WatchRecord::getMovieId, request.getMovieId()));
        if (existing != null) {
            throw new BusinessException(400, "已有观影记录，请修改而非新增");
        }

        WatchRecord record = new WatchRecord();
        record.setUserId(userId);
        record.setMovieId(request.getMovieId());
        record.setStatus(request.getStatus());
        record.setRating(request.getRating());

        if ("WATCHED".equals(request.getStatus())) {
            record.setWatchedAt(LocalDateTime.now());
        }

        watchRecordRepository.insert(record);
        WatchRecordVO vo = toVO(record);
        enrichMovieInfo(List.of(vo));
        return vo;
    }

    @Override
    @Transactional
    public WatchRecordVO updateRecord(Long userId, Long recordId, WatchRecordUpdateRequest request) {
        WatchRecord record = watchRecordRepository.selectById(recordId);
        if (record == null) {
            throw new BusinessException(404, "观影记录不存在");
        }
        if (!record.getUserId().equals(userId)) {
            throw new BusinessException(403, "只能修改自己的观影记录");
        }

        if (request.getStatus() != null) {
            record.setStatus(request.getStatus());
            if ("WATCHED".equals(request.getStatus()) && record.getWatchedAt() == null) {
                record.setWatchedAt(LocalDateTime.now());
            }
        }
        if (request.getRating() != null) {
            record.setRating(request.getRating());
        }

        watchRecordRepository.updateById(record);
        WatchRecordVO vo = toVO(record);
        enrichMovieInfo(List.of(vo));
        return vo;
    }

    @Override
    @Transactional
    public void deleteRecord(Long userId, Long recordId) {
        WatchRecord record = watchRecordRepository.selectById(recordId);
        if (record == null) {
            throw new BusinessException(404, "观影记录不存在");
        }
        if (!record.getUserId().equals(userId)) {
            throw new BusinessException(403, "只能删除自己的观影记录");
        }
        watchRecordRepository.deleteById(recordId);
    }

    @Override
    public Integer getWatchCount(Long movieId, LocalDate startDate, LocalDate endDate) {
        Integer count = watchRecordRepository.getWatchCountByDate(movieId, startDate, endDate);
        return count != null ? count : 0;
    }

    @Override
    public WatchRecordVO getMyStatus(Long userId, Long movieId) {
        WatchRecord record = watchRecordRepository.selectOne(
                new LambdaQueryWrapper<WatchRecord>()
                        .eq(WatchRecord::getUserId, userId)
                        .eq(WatchRecord::getMovieId, movieId));
        if (record == null) return null;
        WatchRecordVO vo = toVO(record);
        enrichMovieInfo(List.of(vo));
        return vo;
    }

    // ==================== 内部方法 ====================

    private void enrichMovieInfo(List<WatchRecordVO> vos) {
        if (vos.isEmpty()) return;

        List<Long> movieIds = vos.stream()
                .map(WatchRecordVO::getMovieId)
                .distinct()
                .toList();

        Map<Long, MovieBrief> briefMap = fetchMovieBriefs(movieIds);

        for (WatchRecordVO vo : vos) {
            MovieBrief brief = briefMap.get(vo.getMovieId());
            if (brief != null) {
                vo.setMovieTitle(brief.title);
                vo.setMoviePoster(brief.posterUrl);
            }
        }
    }

    private Map<Long, MovieBrief> fetchMovieBriefs(List<Long> movieIds) {
        try {
            String idsParam = movieIds.stream().map(String::valueOf).collect(Collectors.joining(","));
            String url = "http://movie-movie/api/movies/batch?ids=" + idsParam;
            String json = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(json);
            if (root.get("code").asInt() != 200 || root.get("data").isNull()) {
                return Map.of();
            }

            Map<Long, MovieBrief> map = new HashMap<>();
            for (JsonNode node : root.get("data")) {
                Long id = node.get("id").asLong();
                String title = node.has("title") && !node.get("title").isNull()
                        ? node.get("title").asText() : null;
                String posterUrl = node.has("posterUrl") && !node.get("posterUrl").isNull()
                        ? node.get("posterUrl").asText() : null;
                map.put(id, new MovieBrief(title, posterUrl));
            }
            return map;
        } catch (Exception e) {
            return Map.of();
        }
    }

    private record MovieBrief(String title, String posterUrl) {}

    private WatchRecordVO toVO(WatchRecord record) {
        return WatchRecordVO.builder()
                .id(record.getId())
                .movieId(record.getMovieId())
                .status(record.getStatus())
                .rating(record.getRating())
                .watchedAt(record.getWatchedAt())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }
}
