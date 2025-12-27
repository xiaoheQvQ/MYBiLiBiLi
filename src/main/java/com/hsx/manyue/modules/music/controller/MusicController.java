package com.hsx.manyue.modules.music.controller;

import com.hsx.manyue.modules.music.dto.MusicResult;
import com.hsx.manyue.modules.music.service.MusicService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class MusicController {

    @Autowired
    private MusicService musicService;

    @GetMapping("/")
    public Object handleApi(
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String id,
            @RequestParam String type,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "20") Integer limit,
            @RequestParam(required = false, defaultValue = "320k") String br,
            HttpServletResponse response
    ) throws IOException {
        switch (type) {
            case "info":
                return musicService.getSongInfo(source, id);
            case "url":
                String url = musicService.getSongUrl(source, id, br);
                if (url != null) {
                    response.sendRedirect(url);
                    return null;
                }
                return MusicResult.error("URL not found");
            case "pic":
                String pic = musicService.getAlbumPic(source, id);
                if (pic != null) {
                    response.sendRedirect(pic);
                    return null;
                }
                return MusicResult.error("Picture not found");
            case "lrc":
                return musicService.getSongLrc(source, id);
            case "search":
                return musicService.searchSongs(source, keyword, limit);
            case "aggregateSearch":
                return musicService.aggregateSearch(keyword);
            case "playlist":
                return musicService.getPlaylistDetail(source, id);
            case "toplists":
                return musicService.getToplists(source);
            case "toplist":
                return musicService.getToplistSongs(source, id);
            default:
                return MusicResult.error("Invalid type");
        }
    }

    /**
     * 本地扩展接口：获取歌手详情
     */
    @GetMapping("/artist/{id}")
    public MusicResult getArtistInfo(@PathVariable String id) {
        // This is a local extension
        return MusicResult.success("Artist info for " + id);
    }
}
