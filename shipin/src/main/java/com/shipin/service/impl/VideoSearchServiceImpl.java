package com.shipin.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.shipin.entity.document.VideoDocument;
import com.shipin.entity.po.User;
import com.shipin.entity.query.UserQuery;
import com.shipin.entity.po.Video;
import com.shipin.mappers.UserMapper;
import com.shipin.mappers.VideoMapper;
import com.shipin.service.VideoSearchService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class VideoSearchServiceImpl implements VideoSearchService {

    @Resource
    private ElasticsearchClient client;

    @Resource
    private VideoMapper videoMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private com.shipin.service.UserService userService;

    @Override
    public void saveVideo(VideoDocument videoDocument) {
        try {
            IndexRequest<VideoDocument> request = IndexRequest.of(i -> i
                .index("video_index")
                .id(videoDocument.getVideoId().toString())
                .document(videoDocument)
            );
            client.index(request);
        } catch (IOException e) {
            throw new RuntimeException("保存视频失败", e);
        }
    }

    @Override
    public void deleteVideo(Integer videoId) {
        try {
            DeleteRequest request = DeleteRequest.of(d -> d
                .index("video_index")
                .id(videoId.toString())
            );
            client.delete(request);
        } catch (IOException e) {
            throw new RuntimeException("删除视频失败", e);
        }
    }

    @Override
    public VideoDocument findById(Integer videoId) {
        try {
            GetRequest request = GetRequest.of(g -> g
                .index("video_index")
                .id(videoId.toString())
            );
            GetResponse<VideoDocument> response = client.get(request, VideoDocument.class);
            return response.found() ? response.source() : null;
        } catch (IOException e) {
            throw new RuntimeException("查询视频失败", e);
        }
    }

    @Override
    public List<VideoDocument> searchByKeyword(String keyword, int page, int size) {
        try {
            SearchRequest request = SearchRequest.of(s -> s
                .index("video_index")
                .query(q -> q
                    .multiMatch(m -> m
                        .query(keyword)
                        .fields("title", "description", "tags")
                    )
                )
                .sort(sort -> sort
                    .field(f -> f
                        .field("createTime")
                        .order(SortOrder.Desc)
                    )
                )
                .from(page * size)
                .size(size)
            );
            return executeSearch(request);
        } catch (IOException e) {
            throw new RuntimeException("搜索视频失败", e);
        }
    }

    @Override
    public List<User> searchUsersByKeyword(String keyword, int page, int size) {
        try {
            UserQuery userQuery = new UserQuery();
            userQuery.setNickNameFuzzy(keyword);
            userQuery.setOrderBy("user_id desc");
            userQuery.setPageNo(page + 1);
            userQuery.setPageSize(size);
            List<User> users = userMapper.selectList(userQuery);
            
            // 为每个用户添加粉丝数
            for (User user : users) {
                Integer followerCount = userService.getFollowerCount(user.getUserId());
                user.setFollowerCount(followerCount != null ? followerCount : 0);
            }
            
            return users;
        } catch (Exception e) {
            throw new RuntimeException("搜索用户失败", e);
        }
    }
    //获取推荐视频，按照播放量和点赞数排序，状态为1（审核通过）
    @Override
    public List<VideoDocument> getRecommendedVideos(int page, int size) {
        try {
            SearchRequest request = SearchRequest.of(s -> s
                .index("video_index")
                .query(q -> q
                    .term(t -> t
                        .field("status")
                        .value("1")
                    )
                )
                .sort(sort -> sort
                    .field(f -> f
                        .field("playCount")
                        .order(SortOrder.Desc)
                    )
                )
                .sort(sort -> sort
                    .field(f -> f
                        .field("likeCount")
                        .order(SortOrder.Desc)
                    )
                )
                .from(page * size)
                .size(size)
            );
            return executeSearch(request);
        } catch (IOException e) {
            throw new RuntimeException("获取推荐视频失败", e);
        }
    }

    @Override
    public List<VideoDocument> searchByCategory(Integer categoryId, int page, int size) {
        try {
            SearchRequest request = SearchRequest.of(s -> s
                .index("video_index")
                .query(q -> q
                    .bool(b -> b
                        .must(m -> m
                            .term(t -> t
                                .field("categoryId")
                                .value(categoryId)
                            )
                        )
                        .must(m -> m
                            .term(t -> t
                                .field("status")
                                .value("1")
                            )
                        )
                    )
                )
                .sort(sort -> sort
                    .field(f -> f
                        .field("createTime")
                        .order(SortOrder.Desc)
                    )
                )
                .from(page * size)
                .size(size)
            );
            return executeSearch(request);
        } catch (IOException e) {
            throw new RuntimeException("搜索分类视频失败", e);
        }
    }

    @Override
    public List<VideoDocument> searchByTags(String tags, int page, int size) {
        try {
            // 使用完整的标签进行搜索
            String trimmedTags = tags.trim();
            
            SearchRequest request = SearchRequest.of(s -> s
                .index("video_index")
                .query(q -> q
                    .bool(b -> b
                        .must(m -> m
                            .multiMatch(multi -> multi
                                .query(trimmedTags)
                                .fields("tags")
                            )
                        )
                        .must(m -> m
                            .term(t -> t
                                .field("status")
                                .value("1")
                            )
                        )
                    )
                )
                .sort(sort -> sort
                    .field(f -> f
                        .field("createTime")
                        .order(SortOrder.Desc)
                    )
                )
                .sort(sort -> sort
                       .field(f -> f
                            .field("playCount")
                             .order(SortOrder.Desc)
                            )
                    )
                .from(page * size)
                .size(size)
            );
            return executeSearch(request);
        } catch (IOException e) {
            throw new RuntimeException("搜索标签视频失败", e);
        }
    }

    //逻辑方法结束了，下面的是具体的实现方法
    @Override
    public void syncVideoFromDatabase(Integer videoId) {
        Video video = (Video) videoMapper.selectByVideoId(videoId);
        if (video != null) {
            VideoDocument videoDocument = convertToDocument(video);
            saveVideo(videoDocument);
        }
    }

    @Override
    public void syncAllVideosFromDatabase() {
        try {
            List<Video> videos = videoMapper.selectAllApprovedVideos();
            System.out.println("开始同步视频数据，共 " + videos.size() + " 个视频");
            
            for (int i = 0; i < videos.size(); i++) {
                Video video = videos.get(i);
                System.out.println("同步第 " + (i + 1) + " 个视频: " + video.getTitle());
                
                try {
                    VideoDocument videoDocument = convertToDocument(video);
                    saveVideo(videoDocument);
                    System.out.println("视频 " + video.getTitle() + " 同步成功");
                } catch (Exception e) {
                    System.err.println("视频 " + video.getTitle() + " 同步失败: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            System.out.println("视频数据同步完成");
        } catch (Exception e) {
            System.err.println("同步视频数据失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private List<VideoDocument> executeSearch(SearchRequest request) throws IOException {
        SearchResponse<VideoDocument> response = client.search(request, VideoDocument.class);
        List<VideoDocument> results = new ArrayList<>();
        for (Hit<VideoDocument> hit : response.hits().hits()) {
            if (hit.source() != null) {
                results.add(hit.source());
            }
        }
        return results;
    }

    private VideoDocument convertToDocument(Video video) {
        VideoDocument document = new VideoDocument();
        BeanUtils.copyProperties(video, document);
        document.setCategoryId(video.getCategroyId());
        
        // 复制标签字段
        document.setTags(video.getTags());

        // 添加作者信息
        if (video.getUserId() != null) {
            User user = (User) userMapper.selectByUserId(video.getUserId());
            if (user != null) {
                document.setNickName(user.getNickName());
                document.setAvatar(user.getAvatar());
            }
        }

        // 安全转换字符串为长整型
        try {
            if (video.getPlayCount() != null && !video.getPlayCount().isEmpty()) {
                document.setPlayCount(Long.parseLong(video.getPlayCount()));
            }
            if (video.getLikeCount() != null && !video.getLikeCount().isEmpty()) {
                document.setLikeCount(Long.parseLong(video.getLikeCount()));
            }
            if (video.getCommentCount() != null && !video.getCommentCount().isEmpty()) {
                document.setCommentCount(Long.parseLong(video.getCommentCount()));
            }
            if (video.getCoinsInserted() != null && !video.getCoinsInserted().isEmpty()) {
                document.setCoinsInserted(Long.parseLong(video.getCoinsInserted()));
            }
            if (video.getFollowCount() != null && !video.getFollowCount().isEmpty()) {
                document.setFollowCount(Long.parseLong(video.getFollowCount()));
            }
        } catch (NumberFormatException e) {
            System.err.println("视频数据转换失败: " + e.getMessage());
            e.printStackTrace();
        }

        return document;
    }
}
