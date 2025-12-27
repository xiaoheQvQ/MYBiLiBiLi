package com.hsx.manyue.modules.music.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hsx.manyue.modules.music.dto.MusicResult;
import com.hsx.manyue.modules.music.entity.*;
import com.hsx.manyue.modules.music.mapper.*;
import com.hsx.manyue.modules.music.service.MusicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MusicServiceImpl implements MusicService {

    @Autowired
    private MusicMapper musicMapper;

    @Autowired
    private ArtistMapper artistMapper;

    @Autowired
    private AlbumMapper albumMapper;

    @Autowired
    private PlaylistMapper playlistMapper;

    @Autowired
    private PlaylistRelationMapper playlistRelationMapper;

    @Autowired
    private ToplistMapper toplistMapper;

    @Autowired
    private ToplistRelationMapper toplistRelationMapper;

    @Override
    public MusicResult getSongInfo(String source, String id) {
        MusicSong song = musicMapper.selectById(id);
        if (song == null) {
            return MusicResult.error("Song not found");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("name", song.getTitle());
        data.put("artist", song.getArtistName());
        data.put("album", song.getAlbumName());
        // Mirror ToneHub structure
        data.put("url", "/api/?source=" + source + "&id=" + id + "&type=url");
        data.put("pic", "/api/?source=" + source + "&id=" + id + "&type=pic");
        data.put("lrc", "/api/?source=" + source + "&id=" + id + "&type=lrc");
        return MusicResult.success(data);
    }

    @Override
    public String getSongUrl(String source, String id, String br) {
        MusicSong song = musicMapper.selectById(id);
        return song != null ? song.getFileUrl() : null;
    }

    @Override
    public String getAlbumPic(String source, String id) {
        MusicSong song = musicMapper.selectById(id);
        return song != null ? song.getCoverUrl() : null;
    }

    @Override
    public String getSongLrc(String source, String id) {
        MusicSong song = musicMapper.selectById(id);
        return song != null ? song.getLyricText() : "";
    }

    @Override
    public MusicResult searchSongs(String source, String keyword, Integer limit) {
        LambdaQueryWrapper<MusicSong> query = new LambdaQueryWrapper<>();
        query.like(MusicSong::getTitle, keyword)
                .or().like(MusicSong::getArtistName, keyword)
                .last("LIMIT " + (limit != null ? limit : 20));
        
        List<MusicSong> songs = musicMapper.selectList(query);
        
        Map<String, Object> resultData = new HashMap<>();
        resultData.put("keyword", keyword);
        resultData.put("total", songs.size());
        
        List<Map<String, Object>> results = songs.stream().map(song -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", song.getId().toString());
            map.put("name", song.getTitle());
            map.put("artist", song.getArtistName());
            map.put("album", song.getAlbumName());
            map.put("url", "/api/?source=" + song.getSourceType() + "&id=" + song.getId() + "&type=url");
            map.put("platform", song.getSourceType());
            return map;
        }).collect(Collectors.toList());
        
        resultData.put("results", results);
        return MusicResult.success(resultData);
    }

    @Override
    public MusicResult aggregateSearch(String keyword) {
        // Reuse search logic for simplicity
        return searchSongs(null, keyword, 30);
    }

    @Override
    public MusicResult getPlaylistDetail(String source, String id) {
        MusicPlaylist playlist = playlistMapper.selectById(id);
        if (playlist == null) {
            return MusicResult.error("Playlist not found");
        }
        
        LambdaQueryWrapper<MusicPlaylistRelation> relationQuery = new LambdaQueryWrapper<>();
        relationQuery.eq(MusicPlaylistRelation::getPlaylistId, id)
                .orderByAsc(MusicPlaylistRelation::getSortOrder);
        List<MusicPlaylistRelation> relations = playlistRelationMapper.selectList(relationQuery);
        
        List<Long> songIds = relations.stream().map(MusicPlaylistRelation::getSongId).collect(Collectors.toList());
        List<MusicSong> songs = songIds.isEmpty() ? Collections.emptyList() : musicMapper.selectBatchIds(songIds);
        
        Map<String, Object> data = new HashMap<>();
        data.put("info", Map.of("name", playlist.getTitle(), "author", "System"));
        data.put("list", songs.stream().map(song -> Map.of(
                "id", song.getId().toString(),
                "name", song.getTitle(),
                "types", List.of("320k", "128k")
        )).collect(Collectors.toList()));
        
        return MusicResult.success(data);
    }

    @Override
    public MusicResult getToplists(String source) {
        LambdaQueryWrapper<MusicToplist> query = new LambdaQueryWrapper<>();
        query.eq(MusicToplist::getIsActive, true);
        List<MusicToplist> lists = toplistMapper.selectList(query);
        
        Map<String, Object> data = new HashMap<>();
        data.put("list", lists.stream().map(list -> Map.of(
                "id", list.getId().toString(),
                "name", list.getName(),
                "updateFrequency", list.getUpdateFrequency()
        )).collect(Collectors.toList()));
        
        return MusicResult.success(data);
    }

    @Override
    public MusicResult getToplistSongs(String source, String id) {
        LambdaQueryWrapper<MusicToplistRelation> relationQuery = new LambdaQueryWrapper<>();
        relationQuery.eq(MusicToplistRelation::getToplistId, id)
                .orderByAsc(MusicToplistRelation::getRankNum);
        List<MusicToplistRelation> relations = toplistRelationMapper.selectList(relationQuery);
        
        List<Long> songIds = relations.stream().map(MusicToplistRelation::getSongId).collect(Collectors.toList());
        List<MusicSong> songs = songIds.isEmpty() ? Collections.emptyList() : musicMapper.selectBatchIds(songIds);
        
        // Sorting songs based on relations rankNum order
        Map<Long, Integer> rankMap = relations.stream().collect(Collectors.toMap(MusicToplistRelation::getSongId, MusicToplistRelation::getRankNum));
        songs.sort(Comparator.comparingInt(s -> rankMap.getOrDefault(s.getId(), 999)));

        Map<String, Object> data = new HashMap<>();
        data.put("list", songs.stream().map(song -> Map.of(
                "id", song.getId().toString(),
                "name", song.getTitle()
        )).collect(Collectors.toList()));
        data.put("source", "local");
        
        return MusicResult.success(data);
    }
}
