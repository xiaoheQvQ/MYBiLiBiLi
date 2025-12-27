package com.hsx.manyue.modules.music.service;

import com.hsx.manyue.modules.music.dto.MusicResult;
import java.util.Map;

public interface MusicService {
    /**
     * 获取歌曲基本信息
     */
    MusicResult getSongInfo(String source, String id);

    /**
     * 获取音乐文件链接
     */
    String getSongUrl(String source, String id, String br);

    /**
     * 获取专辑封面
     */
    String getAlbumPic(String source, String id);

    /**
     * 获取歌词
     */
    String getSongLrc(String source, String id);

    /**
     * 搜索歌曲
     */
    MusicResult searchSongs(String source, String keyword, Integer limit);

    /**
     * 聚合搜索
     */
    MusicResult aggregateSearch(String keyword);

    /**
     * 获取歌单详情
     */
    MusicResult getPlaylistDetail(String source, String id);

    /**
     * 获取排行榜列表
     */
    MusicResult getToplists(String source);

    /**
     * 获取排行榜歌曲
     */
    MusicResult getToplistSongs(String source, String id);
}
