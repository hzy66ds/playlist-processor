package com.sohu.playlist.processer.service;

import com.sohu.spaces.user.model.Account;
import com.sohu.spaces.user.service.AccountService;
import com.sohu.spaces.videos.model.Playlist;
import com.sohu.spaces.videos.service.PlaylistService;
import com.sohu.tv.elasticsearch.model.QueryParam;
import com.sohu.tv.elasticsearch.model.SearchResult;
import com.sohu.tv.elasticsearch.service.PlaylistInfoSearchService;
import com.sohu.tv.ugc.monitor.constant.MonitorConstant;
import com.sohu.tv.ugc.monitor.model.AuditContent;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.*;
import java.rmi.Naming;
import java.util.ArrayList;
import java.util.List;

@Service
public class PlaylistDealService {

    private final static Logger logger = LoggerFactory.getLogger(PlaylistDealService.class);

    @Resource
    private PlaylistInfoSearchService playlistInfoSearchService;


    /**
     * 主要方法，从本地得到禁词列表，根据禁词拼接参数；
     * 一次查询200条结果，循环处理。若是UGC视频，删除；不是UGC视频，continue
     *
     * @param filePath 本地禁词文件位置。
     */
    public void dealPalyListByKeyWord(String filePath) {
        List<String> keywordList = getKeyWordListFromDisk(filePath);
        for (String keyword : keywordList) {
            logger.info("a keyword start {}", keyword);
            //拼接查询参数
            QueryParam param = formParam(keyword);
            //得到命中的列表(一次查200个)
            SearchResult<AuditContent> searchResult = playlistInfoSearchService.search(param, false, "5m");
            //处理第一次查询的200个结果
            handleSearchResult(searchResult);
            Long page = searchResult.getTotal() / 200;
            for (int m = 0; m < page; m++) {
                param.setFrom(m * 200);
                param.setSize(200);
                searchResult = playlistInfoSearchService.search(param, false, "5m");
                //对200个查询结果处理
                handleSearchResult(searchResult);
            }

        }
    }


    public void deal56PlayListBykeyWord(String filePath) {
        List<String> keywordList = getKeyWordListFromDisk(filePath);
        for (String keyword : keywordList) {
            logger.info("a keyword start {}", keyword);
            QueryParam param = formParamFor56(keyword);
            SearchResult<AuditContent> searchResult = playlistInfoSearchService.search(param, false, "5m");
            handelSearchResultFor56(searchResult);
            Long page = searchResult.getTotal() / 200;
            for (int m = 0; m < page; m++) {
                param.setFrom(m * 200);
                param.setSize(200);
                searchResult = playlistInfoSearchService.search(param, false, "5m");
                handelSearchResultFor56(searchResult);
            }
        }
    }

    public void handelSearchResultFor56(SearchResult<AuditContent> searchResult) {
        for (AuditContent auditContent : searchResult.getResults()) {
            logger.info(auditContent.getPrimaryValue());
        }

    }


    private List<String> getKeyWordListFromDisk(String filePath) {
        List<String> keywordList = new ArrayList<>();
        File file = new File(filePath);
        try (InputStreamReader inputReader = new InputStreamReader(new FileInputStream(file));
             BufferedReader bf = new BufferedReader(inputReader)) {

            // 按行读取字符串
            String str;
            while ((str = bf.readLine()) != null) {
                keywordList.add(str);
            }
        } catch (IOException e) {
            logger.error("读取文件失败！", e);
        }
        return keywordList;
    }

    private QueryParam formParam(String keyword) {
        QueryParam param = new QueryParam();
        //专辑
        param.setType("playlist");
        param.setOrderBy("time");
        param.setKeyword(keyword);
        //默认精确查询
        param.setPrecise(true);
        //查询已通过的
        param.setIsAudited(1);
        param.setFrom(0);
        param.setSize(200);
        return param;
    }

    private boolean isUgcUser(String userId) {
        AccountService accountService = null;
        try {
            accountService = (AccountService) Naming.lookup("//10.19.29.217:8802/AccountRMIService");
        } catch (Exception e) {
            logger.error("调用rmi方法失败！", e);
        }
        Account account = null;
        try {
            account = accountService.getAccount(Long.parseLong(userId));
        } catch (Exception e) {
            logger.error("获取account失败！", e);
        }
        if (null == account) {
            logger.info("cant get account");
            return false;
        }
        return account.getEnterprise() != 20;
    }

    private void handleSearchResult(SearchResult<AuditContent> searchResult) {
        for (AuditContent auditContent : searchResult.getResults()) {
            String userId = auditContent.getUserId();
            //处理是UGC的专辑
            if (StringUtils.isNotBlank(userId) && isUgcUser(userId)) {
                handleVideoInfo(auditContent);
            }
        }
    }

    private void handleVideoInfo(AuditContent auditContent) {

        PlaylistService playlistService = null;
        try {
            playlistService = (PlaylistService) Naming.lookup("//cdn.sync.tv.sohuno.com:8702/PlaylistRMIService");
        } catch (Exception e) {
            logger.error("调用playlistService失败！", e);
        }
        Playlist playlist = null;
        try {
            playlist = playlistService.getPlaylist(Long.parseLong(auditContent.getPrimaryValue()));
        } catch (Exception e) {
            logger.error("获得Playlist失败！", e);
        }
        if (playlist != null) {
            playlist.setStatus(MonitorConstant.STATUS_DELETE);
            try {
                logger.info(String.valueOf(playlist.getId()));
                playlistService.updatePlaylist(playlist);
            } catch (Exception e) {
                logger.error("更新Playlist失败！", e);
            }

        }
    }


    private QueryParam formParamFor56(String keyword) {
        QueryParam param = new QueryParam();
        //56专辑
        param.setType("text-56-playlist");
        param.setOrderBy("time");
        param.setKeyword(keyword);
        //默认精确查询
        param.setPrecise(true);
        //查询已通过的
        param.setIsAudited(1);
        param.setFrom(0);
        param.setSize(200);
        return param;
    }

}

