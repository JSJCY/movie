package com.movie.watchlist.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.movie.watchlist.dto.*;

public interface WatchlistService {
    Page<WatchRecordVO> listMyWatchlist(Long userId, String status, long page, long size);
    Page<WatchRecordVO> listMyHistory(Long userId, long page, long size);
    WatchRecordVO addRecord(Long userId, WatchRecordCreateRequest request);
    WatchRecordVO updateRecord(Long userId, Long recordId, WatchRecordUpdateRequest request);
    void deleteRecord(Long userId, Long recordId);
    WatchRecordVO getMyStatus(Long userId, Long movieId);
}
