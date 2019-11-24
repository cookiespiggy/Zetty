package com.zrzhen.zetty.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.zrzhen.zetty.core.mvc.ContentTypeEnum;
import com.zrzhen.zetty.core.mvc.Model;
import com.zrzhen.zetty.core.mvc.anno.*;
import com.zrzhen.zetty.core.util.JsonUtil;
import com.zrzhen.zetty.dao.ArticleDao;
import com.zrzhen.zetty.pojo.result.Result;
import com.zrzhen.zetty.pojo.result.ResultCode;
import com.zrzhen.zetty.pojo.result.ResultGen;
import com.zrzhen.zetty.service.ArticleService;
import com.zrzhen.zetty.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author chenanlian
 */
@Controller("/article/")
public class ArticleController {

    private static final Logger log = LoggerFactory.getLogger(IndexController.class);

    /**
     * 查看一篇文章
     *
     * @param id
     * @return
     */
    @ContentType(ContentTypeEnum.HTML)
    @RequestMapping("detail/{id}")
    public Model detail(@PathVariable Integer id) {
        Model model = new Model();

        Map<String, Object> result = ArticleDao.oneByKey(id);
        if (null != result) {
            if (ArticleService.permission(result)) {
                model.setPath("article.html");
                model.setMap(result);
            } else {
                model.setPath("403.html");
            }
        } else {
            model.setPath("404.html");
        }
        return model;
    }


    /**
     * 返回更新文章页面
     *
     * @param id
     * @return
     */
    @BeforeAdviceAction(id = "loginBeforeAdvice")
    @ContentType(ContentTypeEnum.HTML)
    @RequestMapping("update/{id}")
    public Model update(@PathVariable Integer id) {
        Model model = new Model();

        Map<String, Object> result = ArticleDao.oneByKey(id);
        if (null != result) {
            if (ArticleService.permission(result)) {
                model.setPath("articleUpdate.html");
                model.setMap(result);
            } else {
                model.setPath("403.html");
            }
        } else {
            model.setPath("404.html");
        }

        return model;
    }

    /**
     * 分页查询
     *
     * @param pageNum
     * @param pageSide
     * @param tag
     * @return
     */
    @ContentType(ContentTypeEnum.JSON)
    @RequestMapping("page")
    public Result page(@RequestParam(name = "pageNum", defaultValue = "1") Integer pageNum,
                       @RequestParam(name = "pageSide", defaultValue = "15") Integer pageSide,
                       @RequestParam(name = "tag") String tag,
                       @RequestParam(name = "userId", required = false) Long userId) {

        List<Map<String, Object>> list;
        if (StringUtils.isBlank(tag)) {
            if (userId == null) {
                list = ArticleDao.getList2((pageNum - 1) * pageSide, pageSide);
            } else {
                list = ArticleDao.getList2((pageNum - 1) * pageSide, pageSide, userId);
            }
        } else {
            if (userId == null) {
                list = ArticleDao.listByPageTag(tag, (pageNum - 1) * pageSide, pageSide);
            } else {
                list = ArticleDao.listByPageTag(tag, (pageNum - 1) * pageSide, pageSide, userId);
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("data", list);
        data.put("pageNum", pageNum);
        data.put("pageSide", pageSide);

        return ResultGen.genResult(ResultCode.SUCCESS, data);
    }

    /**
     * 分页查询
     *
     * @param pageNum
     * @param pageSide
     * @return
     */
    @BeforeAdviceAction(id = "loginBeforeAdvice,userCenterBeforeAdvice")
    @ContentType(ContentTypeEnum.JSON)
    @RequestMapping("private")
    public Result privateActicle(@RequestParam(name = "pageNum", defaultValue = "1") Integer pageNum,
                                 @RequestParam(name = "pageSide", defaultValue = "15") Integer pageSide,
                                 @RequestParam(name = "userId", required = true) Long userId) {

        List<Map<String, Object>> list = ArticleDao.listPrivate((pageNum - 1) * pageSide, pageSide, userId);

        Map<String, Object> data = new HashMap<>(5);
        data.put("data", list);
        data.put("pageNum", pageNum);
        data.put("pageSide", pageSide);

        return ResultGen.genResult(ResultCode.SUCCESS, data);
    }

    /**
     * 新增文章
     *
     * @param params
     * @return
     */
    @BeforeAdviceAction(id = "loginBeforeAdvice")
    @ContentType(ContentTypeEnum.JSON)
    @RequestMapping("add")
    public Result add(@RequestJsonBody JsonNode params) {

        Long userid = UserService.getUserid();

        String title = JsonUtil.getString(params, "title");
        String content = JsonUtil.getString(params, "content");
        String status = JsonUtil.getString(params, "status");

        if (StringUtils.isBlank(title)) {
            return ResultGen.genResult(ResultCode.ARG_NEED, "title");
        } else if (StringUtils.isBlank(content)) {
            return ResultGen.genResult(ResultCode.ARG_NEED, "content");
        } else {
            Map<String, Object> valueMap = new HashMap<>();
            valueMap.put("title", title);
            valueMap.put("content", content);
            valueMap.put("userid", userid);
            if (StringUtils.isNotBlank(status)) {
                valueMap.put("status", Integer.valueOf(status));
            }
            return ResultGen.genResult(ResultCode.SUCCESS, ArticleDao.insert(ArticleDao.tableName, valueMap));
        }
    }

    /**
     * 更新文章
     *
     * @param params
     * @return
     */
    @BeforeAdviceAction(id = "loginBeforeAdvice")
    @ContentType(ContentTypeEnum.JSON)
    @RequestMapping("update2Db")
    public Result update2Db(@RequestJsonBody JsonNode params) {

        Long userid = UserService.getUserid();
        String id = JsonUtil.getString(params, "id").trim();
        String title = JsonUtil.getString(params, "title");
        String content = JsonUtil.getString(params, "content");
        String status = JsonUtil.getString(params, "status");

        if (StringUtils.isBlank(id)) {
            return ResultGen.genResult(ResultCode.ARG_NEED, "id");
        } else if (StringUtils.isBlank(title)) {
            return ResultGen.genResult(ResultCode.ARG_NEED, "title");
        } else if (StringUtils.isBlank(content)) {
            return ResultGen.genResult(ResultCode.ARG_NEED, "content");
        } else {
            Integer articleId = Integer.valueOf(id);

            Map<String, Object> articleInDb = ArticleDao.oneByKey(articleId);

            if (articleInDb == null) {
                return ResultGen.genResult(ResultCode.FAIL, "文章不存在");

            } else if (!articleInDb.get("userid").toString().equals(userid.toString())) {
                return ResultGen.genResult(ResultCode.FAIL, "权限不足");

            } else {
                Map<String, Object> valueMap = new HashMap<>();
                valueMap.put("title", title);
                valueMap.put("content", content);

                if (StringUtils.isNotBlank(status)) {
                    valueMap.put("status", Integer.valueOf(status));
                }

                Map<String, Object> whereMap = new HashMap<>();
                whereMap.put("id", articleId);
                ArticleDao.update(ArticleDao.tableName, valueMap, whereMap);
                return ResultGen.genResult(ResultCode.SUCCESS, articleId);

            }
        }
    }


}
