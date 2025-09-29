package org.sang.service;

import org.sang.bean.dto.ArticleDTO;
import org.sang.bean.dto.ArticleDetailDTO;
import org.sang.bean.dto.ArticleQueryDTO;
import org.sang.bean.dto.PageResultDTO;

import java.util.List;

/**
 * 文章服务接口
 */
public interface IArticleService {

    /**
     * 添加新文章
     * @param articleDTO 文章DTO
     * @return 影响行数
     */
    int addNewArticle(ArticleDTO articleDTO);

    /**
     * 根据状态获取文章列表
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    PageResultDTO<ArticleDetailDTO> getArticleByState(ArticleQueryDTO queryDTO);

    /**
     * 获取公开文章列表
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    PageResultDTO<ArticleDetailDTO> getPublicArticles(ArticleQueryDTO queryDTO);

    /**
     * 根据ID获取文章详情
     * @param aid 文章ID
     * @return 文章详情DTO
     */
    ArticleDetailDTO getArticleById(Long aid);

    /**
     * 更新文章状态
     * @param aids 文章ID数组
     * @param state 状态值
     * @return 影响行数
     */
    int updateArticleState(Long[] aids, Integer state);

    /**
     * 恢复文章
     * @param articleId 文章ID
     * @return 影响行数
     */
    int restoreArticle(Integer articleId);

    /**
     * 获取文章分类统计
     * @param uid 用户ID
     * @return 分类名称列表
     */
    List<String> getCategories(Long uid);

    /**
     * 获取数据统计
     * @param uid 用户ID
     * @return 统计数据列表
     */
    List<Integer> getDataStatistics(Long uid);
}
