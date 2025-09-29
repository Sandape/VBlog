package org.sang.controller;

import org.apache.commons.io.IOUtils;
import org.sang.bean.RespBean;
import org.sang.bean.dto.ArticleDTO;
import org.sang.bean.dto.ArticleDetailDTO;
import org.sang.bean.dto.ArticleQueryDTO;
import org.sang.bean.dto.PageResultDTO;
import org.sang.service.IArticleService;
import org.sang.utils.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import org.sang.bean.LoginLog;
import org.sang.service.LoginLogService;

/**
 * Created by sang on 2017/12/20.
 */
@RestController
@RequestMapping("/article")
public class ArticleController {

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

    @Autowired
    private IArticleService articleService;

    @Autowired
    private LoginLogService loginLogService;

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public RespBean addNewArticle(ArticleDTO articleDTO) {
        int result = articleService.addNewArticle(articleDTO);
        if (result == 1) {
            return new RespBean("success", articleDTO.getId() + "");
        } else {
            return new RespBean("error", articleDTO.getState() == 0 ? "文章保存失败!" : "文章发表失败!");
        }
    }

    /**
     * 上传图片
     *
     * @return 返回值为图片的地址
     */
    @RequestMapping(value = "/uploadimg", method = RequestMethod.POST)
    public RespBean uploadImg(HttpServletRequest req, MultipartFile image) {
        StringBuffer url = new StringBuffer();
        String filePath = "/blogimg/" + sdf.format(new Date());
        String imgFolderPath = req.getServletContext().getRealPath(filePath);
        File imgFolder = new File(imgFolderPath);
        if (!imgFolder.exists()) {
            imgFolder.mkdirs();
        }
        url.append(req.getScheme())
                .append("://")
                .append(req.getServerName())
                .append(":")
                .append(req.getServerPort())
                .append(req.getContextPath())
                .append(filePath);
        String imgName = UUID.randomUUID() + "_" + image.getOriginalFilename().replaceAll(" ", "");
        try {
            IOUtils.write(image.getBytes(), new FileOutputStream(new File(imgFolder, imgName)));
            url.append("/").append(imgName);
            return new RespBean("success", url.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new RespBean("error", "上传失败!");
    }

    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public PageResultDTO<ArticleDetailDTO> getArticleByState(@RequestParam(value = "state", defaultValue = "-1") Integer state,
                                                           @RequestParam(value = "page", defaultValue = "1") Integer page,
                                                           @RequestParam(value = "count", defaultValue = "6") Integer count,
                                                           String keywords) {
        ArticleQueryDTO queryDTO = new ArticleQueryDTO();
        queryDTO.setState(state);
        queryDTO.setPage(page);
        queryDTO.setCount(count);
        queryDTO.setKeywords(keywords);
        queryDTO.setUid(Util.getCurrentUser().getId().intValue());

        return articleService.getArticleByState(queryDTO);
    }

    /**
     * 公开浏览所有已发表的文章
     */
    @RequestMapping(value = "/public/all", method = RequestMethod.GET)
    public PageResultDTO<ArticleDetailDTO> getPublicArticles(@RequestParam(value = "page", defaultValue = "1") Integer page,
                                                           @RequestParam(value = "count", defaultValue = "6") Integer count,
                                                           String keywords) {
        ArticleQueryDTO queryDTO = new ArticleQueryDTO();
        queryDTO.setPage(page);
        queryDTO.setCount(count);
        queryDTO.setKeywords(keywords);

        return articleService.getPublicArticles(queryDTO);
    }

    @RequestMapping(value = "/{aid}", method = RequestMethod.GET)
    public ArticleDetailDTO getArticleById(@PathVariable Long aid) {
        return articleService.getArticleById(aid);
    }

    /**
     * 公开浏览文章详情
     */
    @RequestMapping(value = "/public/{aid}", method = RequestMethod.GET)
    public ArticleDetailDTO getPublicArticleById(@PathVariable Long aid) {
        return articleService.getArticleById(aid);
    }

    @RequestMapping(value = "/dustbin", method = RequestMethod.PUT)
    public RespBean updateArticleState(Long[] aids, Integer state) {
        if (articleService.updateArticleState(aids, state) == aids.length) {
            return new RespBean("success", "删除成功!");
        }
        return new RespBean("error", "删除失败!");
    }

    @RequestMapping(value = "/restore", method = RequestMethod.PUT)
    public RespBean restoreArticle(Integer articleId) {
        if (articleService.restoreArticle(articleId) == 1) {
            return new RespBean("success", "还原成功!");
        }
        return new RespBean("error", "还原失败!");
    }

    /**
     * 获取登录日志列表
     */
    @RequestMapping(value = "/loginLogs", method = RequestMethod.GET)
    public Map<String, Object> getLoginLogs(@RequestParam(value = "page", defaultValue = "1") Integer page, 
                                       @RequestParam(value = "count", defaultValue = "10") Integer count) {
        // 不按用户ID过滤，显示所有登录记录
        List<LoginLog> loginLogs = loginLogService.getAllLoginLogs(page, count);
        int totalCount = loginLogService.getAllLoginLogCount();
        
        System.out.println("查询到的登录日志数量: " + loginLogs.size());
        System.out.println("总数量: " + totalCount);
        
        Map<String, Object> map = new HashMap<>();
        map.put("totalCount", totalCount);
        map.put("loginLogs", loginLogs);
        return map;
    }

    /**
     * 按用户统计登录次数
     */
    @RequestMapping(value = "/loginStatsByUser", method = RequestMethod.GET)
    public Map<String, Object> getLoginStatsByUser() {
        Map<String, Object> map = new HashMap<>();
        List<String> usernames = loginLogService.getUsernames();
        List<Integer> loginCounts = loginLogService.getLoginCountsByUser();
        map.put("usernames", usernames);
        map.put("loginCounts", loginCounts);
        return map;
    }

    /**
     * 根据用户名搜索登录记录
     */
    @RequestMapping(value = "/searchLoginLogs", method = RequestMethod.GET)
    public Map<String, Object> searchLoginLogs(@RequestParam("username") String username,
                                         @RequestParam(value = "page", defaultValue = "1") Integer page,
                                         @RequestParam(value = "count", defaultValue = "10") Integer count) {
        List<LoginLog> loginLogs = loginLogService.searchLoginLogsByUsername(username, page, count);
        int totalCount = loginLogService.getLoginLogCountByUsername(username);
        Map<String, Object> map = new HashMap<>();
        map.put("totalCount", totalCount);
        map.put("loginLogs", loginLogs);
        return map;
    }

    @RequestMapping("/dataStatistics")
    public Map<String,Object> dataStatistics() {
        Map<String, Object> map = new HashMap<>();
        Long currentUserId = Util.getCurrentUser().getId();
        List<String> categories = articleService.getCategories(currentUserId);
        List<Integer> dataStatistics = articleService.getDataStatistics(currentUserId);

        // 添加登录日志统计
        List<String> loginDates = loginLogService.getLoginDates(currentUserId);
        List<Integer> loginCounts = loginLogService.getLoginCounts(currentUserId);

        map.put("categories", categories);
        map.put("ds", dataStatistics);
        map.put("loginDates", loginDates);
        map.put("loginCounts", loginCounts);
        return map;
    }
}
