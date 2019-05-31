package com.sohu.playlist.processer.controller;

import com.sohu.playlist.processer.service.PlaylistDealService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/playlist")
public class PlayListDealController {

    private final static Logger logger = LoggerFactory.getLogger(PlayListDealController.class);

    @GetMapping("/deal")
    public boolean deal(String filePath) {
        playlistDealService.dealPalyListByKeyWord(filePath);
        return true;
    }

    @GetMapping("/deal56")
    public boolean deal56(String filePath){
        playlistDealService.deal56PlayListBykeyWord(filePath);
        return true;
    }

    @Resource
    private PlaylistDealService playlistDealService;
}
