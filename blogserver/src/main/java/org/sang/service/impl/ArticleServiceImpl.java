package org.sang.service.impl;

import org.sang.bean.Article;
import org.sang.bean.Tags;
import org.sang.bean.dto.ArticleDTO;
import org.sang.bean.dto.ArticleDetailDTO;
import org.sang.bean.dto.ArticleQueryDTO;
import org.sang.bean.dto.PageResultDTO;
import org.sang.mapper.ArticleMapper;
import org.sang.mapper.CategoryMapper;
import org.sang.mapper.TagsMapper;
import org.sang.mapper.UserMapper;
import org.sang.service.IArticleService;
import org.sang.utils.Util;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * 文章服务实现类
 */
@Service
@Transactional
public class ArticleServiceImpl implements IArticleService {

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private TagsMapper tagsMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public int addNewArticle(ArticleDTO articleDTO) {
        Article article = new Article();
        BeanUtils.copyProperties(articleDTO, article);

        // 处理文章摘要
        if (article.getSummary() == null || "".equals(article.getSummary())) {
            String stripHtml = stripHtml(article.getHtmlContent());
            article.setSummary(stripHtml.substring(0, Math.min(50, stripHtml.length())));
        }

        if (article.getId() == null || article.getId() == -1) {
            // 添加操作
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            if (article.getState() == 1) {
                article.setPublishDate(timestamp);
            }
            article.setEditTime(timestamp);
            article.setUid(Util.getCurrentUser().getId());

            int result = articleMapper.addNewArticle(article);
            if (result > 0) {
                // 处理标签
                String[] dynamicTags = articleDTO.getDynamicTags();
                if (dynamicTags != null && dynamicTags.length > 0) {
                    int tagsResult = addTagsToArticle(dynamicTags, article.getId().longValue());
                    if (tagsResult == -1) {
                        return -1;
                    }
                }
            }
            return result;
        } else {
            // 更新操作
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            if (article.getState() == 1) {
                article.setPublishDate(timestamp);
            }
            article.setEditTime(timestamp);

            int result = articleMapper.updateArticle(article);
            if (result > 0) {
                // 处理标签
                String[] dynamicTags = articleDTO.getDynamicTags();
                if (dynamicTags != null && dynamicTags.length > 0) {
                    int tagsResult = addTagsToArticle(dynamicTags, article.getId().longValue());
                    if (tagsResult == -1) {
                        return -1;
                    }
                }
            }
            return result;
        }
    }

    @Override
    public PageResultDTO<ArticleDetailDTO> getArticleByState(ArticleQueryDTO queryDTO) {
        int start = (queryDTO.getPage() - 1) * queryDTO.getCount();
        Long uid = Util.getCurrentUser().getId();

        int totalCount = articleMapper.getArticleCountByState(queryDTO.getState(), uid, queryDTO.getKeywords());
        List<Article> articles = articleMapper.getArticleByState(queryDTO.getState(), start, queryDTO.getCount(), uid, queryDTO.getKeywords());

        List<ArticleDetailDTO> detailDTOs = new ArrayList<>();
        for (Article article : articles) {
            ArticleDetailDTO detailDTO = convertToDetailDTO(article);
            detailDTOs.add(detailDTO);
        }

        return new PageResultDTO<>(detailDTOs, totalCount, queryDTO.getPage(), queryDTO.getCount());
    }

    @Override
    public PageResultDTO<ArticleDetailDTO> getPublicArticles(ArticleQueryDTO queryDTO) {
        int start = (queryDTO.getPage() - 1) * queryDTO.getCount();

        int totalCount = articleMapper.getArticleCountByState(1, null, queryDTO.getKeywords());
        List<Article> articles = articleMapper.getArticleByState(-2, start, queryDTO.getCount(), null, queryDTO.getKeywords());

        List<ArticleDetailDTO> detailDTOs = new ArrayList<>();
        for (Article article : articles) {
            ArticleDetailDTO detailDTO = convertToDetailDTO(article);
            detailDTOs.add(detailDTO);
        }

        return new PageResultDTO<>(detailDTOs, totalCount, queryDTO.getPage(), queryDTO.getCount());
    }

    @Override
    public ArticleDetailDTO getArticleById(Long aid) {
        Article article = articleMapper.getArticleById(aid);
        if (article != null) {
            // 增加浏览量
            articleMapper.pvIncrement(aid);
            return convertToDetailDTO(article);
        }
        return null;
    }

    @Override
    public int updateArticleState(Long[] aids, Integer state) {
        if (state == 2) {
            return articleMapper.deleteArticleById(aids);
        } else {
            return articleMapper.updateArticleState(aids, 2);
        }
    }

    @Override
    public int restoreArticle(Integer articleId) {
        return articleMapper.updateArticleStateById(articleId, 1);
    }

    @Override
    public List<String> getCategories(Long uid) {
        return articleMapper.getCategories(uid);
    }

    @Override
    public List<Integer> getDataStatistics(Long uid) {
        return articleMapper.getDataStatistics(uid);
    }

    /**
     * 每日PV统计
     */
    public void pvStatisticsPerDay() {
        articleMapper.pvStatisticsPerDay();
    }

    /**
     * 转换文章实体为详情DTO
     */
    private ArticleDetailDTO convertToDetailDTO(Article article) {
        ArticleDetailDTO detailDTO = new ArticleDetailDTO();
        BeanUtils.copyProperties(article, detailDTO);

        // 查询并设置关联数据
        if (article.getUid() != null) {
            org.sang.bean.User user = userMapper.getUserById(article.getUid());
            if (user != null) {
                detailDTO.setNickname(user.getNickname());
            }
        }

        if (article.getCid() != null) {
            org.sang.bean.Category category = categoryMapper.getCategoryById(article.getCid());
            if (category != null) {
                detailDTO.setCateName(category.getCateName());
            }
        }

        // 查询文章标签
        List<org.sang.bean.Tags> tags = tagsMapper.getTagsByAid(article.getId());
        if (tags != null && !tags.isEmpty()) {
            List<org.sang.bean.dto.TagsDTO> tagsDTOs = new ArrayList<>();
            for (org.sang.bean.Tags tag : tags) {
                org.sang.bean.dto.TagsDTO tagsDTO = new org.sang.bean.dto.TagsDTO();
                tagsDTO.setId(tag.getId());
                tagsDTO.setTagName(tag.getTagName());
                tagsDTOs.add(tagsDTO);
            }
            detailDTO.setTags(tagsDTOs);
        }

        return detailDTO;
    }

    /**
     * 添加标签到文章
     */
    private int addTagsToArticle(String[] dynamicTags, Long aid) {
        // 1.删除该文章目前所有的标签
        tagsMapper.deleteTagsByAid(aid);
        // 2.将上传上来的标签全部存入数据库
        tagsMapper.saveTags(dynamicTags);
        // 3.查询这些标签的id
        List<Long> tIds = tagsMapper.getTagsIdByTagName(dynamicTags);
        // 4.重新给文章设置标签
        int i = tagsMapper.saveTags2ArticleTags(tIds, aid);
        return i == dynamicTags.length ? i : -1;
    }

    /**
     * 去除HTML标签
     */
    private String stripHtml(String content) {
        if (content == null) {
            return "";
        }
        content = content.replaceAll("<p .*?>", "");
        content = content.replaceAll("<br\\s*/?>", "");
        content = content.replaceAll("\\<.*?>", "");
        return content;
    }
}
