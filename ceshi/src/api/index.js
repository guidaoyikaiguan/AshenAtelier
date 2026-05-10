// 接口地址必须和Java后端的接口路径一一对应！！！
export default {
  // 用户相关接口
  checkCode: '/api/user/getCheckCode', // 验证码接口
  login: '/api/user/login', // 登录接口
  register: '/api/user/register', // 注册接口
  getUserInfo: '/api/user/getUserInfo', // 获取用户信息接口
  getAttentionUserInfo: '/api/user/getAttentionUserInfo', // 获取关注者信息接口
  updateUserInfo: '/api/user/updateUserInfo', // 更新用户信息接口
  uploadAvatar: '/api/user/uploadAvatar', // 上传头像接口
  getFollowingList: '/api/user/getFollowingList', // 获取关注列表接口
  getFollowerList: '/api/user/getFollowerList', // 获取粉丝列表接口
  followUser: '/api/user/followUser', // 关注用户接口
  selectIfFollow: '/api/user/selectIfFollow', // 查询是否关注用户接口
  getEXAttentionUserInfo: '/api/user/getEXAttentionUserInfo', // 查询视频上传者用户信息接口
  getUserVideos: '/api/user/getUserVideos', // 获取用户投稿视频接口
  getUserVideoCount: '/api/user/getUserVideoCount', // 获取用户投稿视频总数接口
  getUserTotalLikes: '/api/user/getUserTotalLikes', // 获取用户总获赞数接口
  getUserTotalPlays: '/api/user/getUserTotalPlays', // 获取用户总播放数接口
  getMyVideoCount: '/api/user/getMyVideoCount', // 获取当前用户视频数量接口
  updateMyVideo: '/api/user/updateMyVideo', // 更新当前用户视频接口
  deleteMyVideo: '/api/user/deleteMyVideo', // 删除当前用户视频接口
  // 管理员相关接口
  adminLogin: '/api/admin/login', // 管理员登录接口
  adminLogout: '/api/admin/logout', // 管理员退出登录接口
  getAdminInfo: '/api/admin/getAdminInfo', // 获取管理员信息接口
  
  // 视频相关接口
  loadRecommendVideo: '/api/video/loadRecommendVideo', // 加载推荐视频接口
  loadVideoByCategory: '/api/video/loadVideoByCategory', // 按分类加载视频接口
  loadVideo: '/api/video/loadVideo', // 加载视频详情接口
  loadAllVideo: '/api/video/loadAllVideo', // 管理员端加载全部视频接口
  loadMyVideos: '/api/video/loadMyVideos', // 加载我的视频列表接口
  uploadVideoFile: '/api/video/uploadVideoFile', // 上传视频文件接口
  uploadVideoChunk: '/api/video/uploadVideoChunk', // 上传视频分块接口
  mergeVideoChunks: '/api/video/mergeVideoChunks', // 合并视频分块接口
  uploadVideoCover: '/api/video/uploadVideoCover', // 上传视频封面接口
  uploadVideo: '/api/video/uploadVideo', // 上传视频信息接口
  increasePlayCount: '/api/video/increasePlayCount', // 增加视频播放量接口
  increaseLikeCount: '/api/video/increaseLikeCount', // 增加视频点赞数接口
  insertCoins: '/api/video/insertCoins', // 用户投币接口
  increaseCollect: '/api/video/increaseCollect', // 收藏视频接口
  
  // 收藏相关接口
  getCollectList: '/api/collect/getCollectList', // 分页查询收藏视频接口
  getCollectCount: '/api/collect/getCollectCount', // 获取收藏夹视频数量接口
  getFavorite: '/api/collect/getFavorite', // 获取收藏夹列表接口
  updateCollectStatus: '/api/collect/updateCollectStatus', // 修改收藏夹状态接口
  updateFavoritePublicStatus: '/api/collect/updateFavoritePublicStatus', // 修改收藏夹公开状态接口
  addCollect: '/api/collect/addCollect', // 新增收藏夹接口
  moveCollect: '/api/collect/moveCollect', // 移动视频到收藏夹接口
  // 其他用户收藏相关接口
  getOtherUserCollect: '/api/collect/getOtherUserCollect', // 获取其他用户的收藏视频接口
  getOtherUserCollectCount: '/api/collect/getOtherUserCollectCount', // 获取其他用户的收藏夹视频数量接口
  getOtherUserFavorite: '/api/collect/getOtherUserFavorite', // 获取其他用户的收藏夹列表接口
  
  // 分类相关接口
  loadCategories: '/api/category/loadCategories', // 加载分类列表接口
  loadCategoryInfo: '/api/category/loadCategoryInfo', // 加载分类详情接口
  
  // 合集相关接口
  getExCompilationsVideo: '/api/video/getExCompilationsVideo',
  addVideo2CompilationsId: '/api/video/addVideo2CompilationsId',
  getCompilationsList: '/api/compilations/getCompilationsList',
  getCompilations: '/api/compilations/getCompilations',
  getCompilationsCount: '/api/compilations/getCompilationsCount',
  addCompilations: '/api/compilations/addCompilations',
  updateCompilationsName: '/api/compilations/updateCompilationsName',
  deleteCompilations: '/api/compilations/deleteCompilations',
  deleteCompilationsVideo: '/api/compilations/deleteCompilationsVideo',
  
  // 评论相关接口
  addComment: '/api/comment/addComment', // 添加评论接口
  getVideoComments: '/api/comment/getVideoComments', // 获取视频评论列表接口
  likeComment: '/api/comment/likeComment', // 点赞评论接口
  deleteComment: '/api/comment/deleteComment', // 删除评论接口
  topComment: '/api/comment/topComment', // 置顶评论接口
  untopComment: '/api/comment/untopComment', // 取消置顶评论接口
  
  // 历史记录相关接口
  getVideoHistory: '/api/videoHistory/getVideoHistory', // 获取观看历史记录接口
  addVideoHistory: '/api/videoHistory/addVideoHistory', // 添加观看历史记录接口
  deleteVideoHistory: '/api/videoHistory/deleteVideoHistory', // 删除观看历史记录接口
  clearVideoHistory: '/api/videoHistory/clearVideoHistory', // 清空观看历史记录接口
  updateVideoHistory: '/api/videoHistory/updateVideoHistory', // 更新观看历史记录接口
  searchVideoHistory: '/api/videoHistory/searchVideoHistory', // 搜索观看历史记录接口
  deleteBatchVideoHistory: '/api/videoHistory/deleteBatchVideoHistory', // 批量删除观看历史记录接口
  
  // 聊天相关接口
  getSessions: '/api/chat/getSessions', // 获取聊天会话列表接口
  getMessages: '/api/chat/getMessages', // 获取聊天消息接口
  sendMessage: '/api/chat/sendMessage', // 发送消息接口
  addChatSession: '/api/chat/addChatSession', // 添加聊天会话接口
  
  // 动态相关接口
  getQiniuToken: '/api/moment/getQiniuToken', // 获取七牛云上传令牌接口
  publishMoment: '/api/moment/publishMoment', // 发布动态接口
  getMomentList: '/api/moment/getMomentList', // 获取动态列表接口
  likeMoment: '/api/moment/likeMoment', // 点赞动态接口
  commentMoment: '/api/moment/commentMoment', // 评论动态接口
  getFollowedMomentList: '/api/moment/getFollowedMomentList', // 获取关注用户动态列表接口
  
  // 轮播图相关接口
  getCarousel: '/api/carousel/getCarousel', // 获取主页轮播图
  getCarouselByCategory: '/api/carousel/getCarouselByCategory', // 按分类获取轮播图

  // AI 智能服务
  aiTagsSuggest: '/api/ai/tagging/suggest', // AI标签建议
  mascotChat: '/api/ai/mascot/chat', // 桌宠聊天
  videoTranscript: '/api/ai/video/transcript', // 视频语音转文字

  // 系统相关
  sourcePath: '' // 资源路径前缀
};