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

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class WatchlistServiceImpl implements WatchlistService {

    private final WatchRecordRepository watchRecordRepository;

    @Override
    public Page<WatchRecordVO> listMyWatchlist(Long userId, String status, long page, long size) {
        LambdaQueryWrapper<WatchRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WatchRecord::getUserId, userId);
        if (status != null && !status.isBlank()) {
            wrapper.eq(WatchRecord::getStatus, status);
        }
        wrapper.orderByDesc(WatchRecord::getUpdatedAt);

        Page<WatchRecord> recordPage = watchRecordRepository.selectPage(new Page<>(page, size), wrapper);

        Page<WatchRecordVO> voPage = new Page<>(page, size);
        voPage.setTotal(recordPage.getTotal());
        voPage.setRecords(recordPage.getRecords().stream().map(this::toVO).toList());
        return voPage;
    }

    @Override
    public Page<WatchRecordVO> listMyHistory(Long userId, long page, long size) {
        LambdaQueryWrapper<WatchRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WatchRecord::getUserId, userId)
               .eq(WatchRecord::getStatus, "WATCHED")
               .orderByDesc(WatchRecord::getWatchedAt);

        Page<WatchRecord> recordPage = watchRecordRepository.selectPage(new Page<>(page, size), wrapper);

        Page<WatchRecordVO> voPage = new Page<>(page, size);
        voPage.setTotal(recordPage.getTotal());
        voPage.setRecords(recordPage.getRecords().stream().map(this::toVO).toList());
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
        return toVO(record);
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
        return toVO(record);
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
    public WatchRecordVO getMyStatus(Long userId, Long movieId) {
        WatchRecord record = watchRecordRepository.selectOne(
                new LambdaQueryWrapper<WatchRecord>()
                        .eq(WatchRecord::getUserId, userId)
                        .eq(WatchRecord::getMovieId, movieId));
        return record != null ? toVO(record) : null;
    }

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
