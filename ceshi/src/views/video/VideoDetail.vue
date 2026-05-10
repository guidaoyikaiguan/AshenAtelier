<template>
  <div class="video-detail">
    <!-- 顶部导航栏 -->
    <Header />
    
    <!-- 左侧导航栏 -->
    <Sidebar />
    
    <!-- 主内容区 -->
    <div class="main-content">
      <div class="content-wrapper">
        <!-- 视频播放区域 -->
        <div class="video-player-area">
          <!-- 视频播放器 -->
          <div class="video-player">
            <div id="dplayer" ref="dplayerRef"></div>
            <!-- 星光弹幕按钮 -->
            <div class="danmaku-list-container">
              <div class="danmaku-list-btn" @click="toggleDanmakuList">
                <span>星光弹幕</span>
                <el-icon><ArrowDown /></el-icon>
              </div>
              <!-- 星光弹幕弹窗 -->
              <div v-if="showDanmakuList" class="danmaku-list-popup">
                <div class="danmaku-list-header">
                  <h4>星光弹幕</h4>
                  <el-icon class="close-btn" @click="showDanmakuList = false"><Close /></el-icon>
                </div>
                <div class="danmaku-list-content">
                  <div v-if="danmakuList.length > 0" class="danmaku-item" v-for="(danmaku, index) in danmakuList" :key="index">
                    <div class="danmaku-info">
                      <span class="danmaku-time">{{ formatDanmakuTime(danmaku.time) }}</span>
                      <span class="danmaku-user">{{ danmaku.username || '匿名用户' }}</span>
                      <span class="danmaku-create-time">{{ formatDate(danmaku.createTime) }}</span>
                      <!-- 只有视频发布者可以看到删除按钮 -->
                      <el-button 
                        v-if="loginStore.userInfo?.userId && loginStore.userInfo.userId === authorInfo?.userId" 
                        type="danger" 
                        size="small" 
                        @click="deleteDanmaku(danmaku.id, index)"
                        style="margin-left: 10px;"
                      >
                        删除
                      </el-button>
                      <!-- 用户悬停在自己发送的弹幕后显示删除按钮 -->
                      <el-button 
                        v-if="loginStore.userInfo?.userId && loginStore.userInfo.userId !== authorInfo?.userId && 
                               (danmaku.userId === loginStore.userInfo.userId || danmaku.user_id === loginStore.userInfo.userId || 
                                danmaku.username === (loginStore.userInfo.username || loginStore.userInfo.nickname || loginStore.userInfo.nickName))" 
                        type="danger" 
                        size="small" 
                        @click="deleteDanmaku(danmaku.id, index)"
                        style="margin-left: 10px; display: none;" 
                        class="user-danmaku-delete-btn"
                      >
                        删除
                      </el-button>
                    </div>
                    <div class="danmaku-content">{{ danmaku.content }}</div>
                  </div>
                  <div v-else class="empty-danmaku">暂无星光弹幕</div>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 视频信息区域 -->
          <div class="video-info">
            <h1 class="video-title">{{ videoInfo.title }}</h1>
            <div class="video-stats">
              <span class="view-count">{{ videoInfo.playCount }} 播放</span>
              <span class="date">{{ formatDate(videoInfo.createTime) }}</span>
            </div>
            
            <!-- 视频标签 -->
            <div v-if="videoInfo && videoInfo.tags" class="video-tags">
              <span 
                v-for="(tag, index) in getTagsArray(videoInfo.tags)" 
                :key="index" 
                class="video-tag"
                @click="goToTagSearch(tag)"
              >
                {{ tag }}
              </span>
            </div>
            
            <!-- 作者信息和互动按钮 -->
            <div class="author-interaction">
              <!-- 作者信息 -->
              <div class="author-info">
                <div class="author-avatar" @click="goToUserProfile(authorInfo.userId)">
                  <img :src="authorInfo.avatar" alt="作者头像" style="cursor: pointer;" />
                </div>
                <div class="author-details">
                  <div class="author-name">{{ authorInfo.username }}</div>
                  <div class="author-fans">{{ authorInfo.fansCount }} 粉丝</div>
                </div>
                <button 
                  :class="['follow-btn', { 'followed': isFollowing }]" 
                  @click="followUser"
                >
                  {{ isFollowing ? '旅途中' : '关注这位旅人' }}
                </button>
              </div>
              
              <!-- 互动按钮 -->
              <div class="interaction-buttons">
                <div class="interaction-btn" @click="likeVideo">
                  <el-icon :class="{ 'liked': isLiked }">
                    <MagicStick />
                  </el-icon>
                  <span>{{ videoInfo.likeCount || 0 }}</span>
                </div>
                <div class="interaction-btn" @click="openCoinDialog">
                  <el-icon :class="{ 'coined': isCoined }"><Coin /></el-icon>
                  <span :class="{ 'coined': isCoined }">{{ videoInfo.coinCount }}</span>
                </div>
                <div class="interaction-btn" @click="collectVideo">
                  <el-icon :class="{ 'collected': isCollected }"><Collection /></el-icon>
                  <span :class="{ 'collected': isCollected }">{{ videoInfo.collectCount || 0 }}</span>
                </div>
                <div class="interaction-btn">
                  <el-icon><Share /></el-icon>
                  <span>传递</span>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 视频描述 -->
          <div class="video-description">
            {{ videoInfo.description }}
          </div>
          
          <!-- 评论区域 -->
          <div class="comment-section">
            <div class="comment-header">
              <h3>旅人讨论 ({{ commentCount }})</h3>
              <div class="comment-sort">
                <button 
                  :class="['sort-btn', { 'active': sortord === 1 }]"
                  @click="sortord = 1"
                >
                  最新
                </button>
                <button 
                  :class="['sort-btn', { 'active': sortord === 0 }]"
                  @click="sortord = 0"
                >
                  最热
                </button>
              </div>
            </div>
            
            <!-- 评论输入框 -->
            <div class="comment-input-area">
              <div class="user-avatar">
                <img :src="getUserAvatar()" alt="用户头像" />
              </div>
              <div class="input-container">
                <el-input
                  v-model="commentContent"
                  type="textarea"
                  :rows="3"
                  placeholder="留下你的足迹..."
                ></el-input>
                <div class="comment-actions">
                  <button 
                    class="submit-btn"
                    @click="submitComment"
                    :disabled="!commentContent.trim() || !loginStore.userInfo.userId"
                  >
                    提交旅笺
                  </button>
                </div>
              </div>
            </div>
            
            <!-- 评论列表 -->
            <div class="comment-list">
              <!-- 只显示顶级评论（parentId 为 null 或 0） -->
              <div v-if="topLevelComments.length === 0" class="no-comments">
                <p>此卷暂无旅人留迹，快来留下第一个足迹吧！</p>
              </div>
              <div 
                v-for="comment in topLevelComments" 
                :key="comment?.commentId || Math.random()"
                :class="['comment-item', { 'topped': comment.ifTop === 1 }]"
              >
                <div class="comment-avatar" @click="goToUserProfile(comment.userId)">
                  <img :src="comment.userAvatar ? comment.userAvatar.replace('/avatar/', '/avatar/') : 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=user%20avatar%20profile%20picture&image_size=square'" alt="评论用户头像" style="cursor: pointer;" />
                </div>
                <div class="comment-content-wrapper">
                  <div class="comment-header-info">
                    <span class="comment-username">{{ comment.nickName || comment.username || '未知用户' }}</span>
                    <span v-if="comment.ifTop === 1" class="top-badge">置顶</span>
                    <span class="comment-time">{{ formatDate(comment.commentTime) }}</span>
                  </div>
                  <div class="comment-text">{{ comment.content || '' }}</div>
                  <div class="comment-actions">
                    <button 
                      class="comment-action-btn"
                      @click="replyComment(comment)"
                    >
                      回应
                    </button>
                    <button 
                      :class="['comment-action-btn', { 'liked': comment.isLiked }]"
                      @click="likeComment(comment)"
                    >
                      <el-icon><Star /></el-icon>
                      <span>{{ comment.likeCount || 0 }}</span>
                    </button>
                    <button 
                      v-if="loginStore.userInfo?.userId && loginStore.userInfo.userId === authorInfo?.userId"
                      :class="['comment-action-btn', { 'topped': comment.ifTop === 1 }]"
                      @click="toggleTopComment(comment)"
                    >
                      <el-icon><Top /></el-icon>
                      <span>{{ comment.ifTop === 1 ? '取消置顶' : '置顶' }}</span>
                    </button>
                    <button 
                      v-if="loginStore.userInfo?.userId && (loginStore.userInfo.userId === comment.userId || loginStore.userInfo.userId === authorInfo?.userId)"
                      class="comment-action-btn delete-btn"
                      @click="deleteComment(comment)"
                    >
                      删除
                    </button>
                  </div>
                  
                  <!-- 回复输入框 -->
                  <div v-if="replyTarget === comment.commentId" class="reply-input-area">
                    <div class="reply-input-container">
                      <el-input
                        v-model="replyContent"
                        type="textarea"
                        :rows="2"
                        placeholder="写下你的回应..."
                      ></el-input>
                      <div class="reply-actions">
                        <button 
                          class="cancel-btn"
                          @click="cancelReply"
                        >
                          取消
                        </button>
                        <button 
                          class="submit-btn"
                          @click="submitReply(comment)"
                          :disabled="!replyContent.trim()"
                        >
                          提交回应
                        </button>
                      </div>
                    </div>
                  </div>
                  
                  <!-- 子评论列表（回复） -->
                  <div v-if="comment.subComments && Array.isArray(comment.subComments) && comment.subComments.length > 0" class="sub-comment-list">
                    <!-- 查看更多按钮 -->
                    <div class="view-more-comments">
                      <button 
                        class="view-more-btn"
                        @click="toggleNestedComments(comment.commentId)"
                      >
                        {{ expandedComments[comment.commentId] ? '收起回应' : '展开更多回应 (' + comment.subComments.length + ')' }}
                      </button>
                    </div>
                    
                    <!-- 子评论内容 -->
                    <div v-if="expandedComments[comment.commentId]" class="sub-comment-content">
                      <div 
                        v-for="subComment in comment.subComments" 
                        :key="subComment?.commentId || Math.random()"
                        :class="['sub-comment-item', { 'topped': subComment.ifTop === 1 }]"
                      >
                        <div class="sub-comment-avatar" @click="goToUserProfile(subComment.userId)">
                          <img :src="subComment.userAvatar ? subComment.userAvatar.replace('/avatar/', '/avatar/') : 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=user%20avatar%20profile%20picture&image_size=square'" alt="回复用户头像" style="cursor: pointer;" />
                        </div>
                        <div class="sub-comment-content-wrapper">
                          <div class="sub-comment-header-info">
                            <span class="sub-comment-username">{{ subComment.nickName || subComment.username || '未知用户' }}</span>
                            <span v-if="subComment.ifTop === 1" class="top-badge">置顶</span>
                            <span class="sub-comment-time">{{ formatDate(subComment.commentTime) }}</span>
                          </div>
                          <div class="sub-comment-text">{{ subComment.content || '' }}</div>
                          <div class="sub-comment-actions">
                            <button 
                              class="sub-comment-action-btn"
                              @click="replyComment(subComment)"
                            >
                              回应
                            </button>
                            <button 
                              :class="['sub-comment-action-btn', { 'liked': subComment.isLiked }]"
                              @click="likeComment(subComment)"
                            >
                              <el-icon><Star /></el-icon>
                              <span>{{ subComment.likeCount || 0 }}</span>
                            </button>
                            <button 
                              v-if="loginStore.userInfo?.userId && loginStore.userInfo.userId === authorInfo?.userId"
                              :class="['sub-comment-action-btn', { 'topped': subComment.ifTop === 1 }]"
                              @click="toggleTopComment(subComment)"
                            >
                              <el-icon><Top /></el-icon>
                              <span>{{ subComment.ifTop === 1 ? '取消置顶' : '置顶' }}</span>
                            </button>
                            <button 
                              v-if="loginStore.userInfo?.userId && (loginStore.userInfo.userId === subComment.userId || loginStore.userInfo.userId === authorInfo?.userId)"
                              class="sub-comment-action-btn delete-btn"
                              @click="deleteComment(subComment)"
                            >
                              删除
                            </button>
                          </div>
                          
                          <!-- 子评论回复输入框 -->
                          <div v-if="replyTarget === subComment.commentId" class="reply-input-area">
                            <div class="reply-input-container">
                              <el-input
                                v-model="replyContent"
                                type="textarea"
                                :rows="2"
                                placeholder="写下你的回应..."
                              ></el-input>
                              <div class="reply-actions">
                                <button 
                                  class="cancel-btn"
                                  @click="cancelReply"
                                >
                                  取消
                                </button>
                                <button 
                                  class="submit-btn"
                                  @click="submitReply(subComment)"
                                  :disabled="!replyContent.trim()"
                                >
                                  提交回应
                                </button>
                              </div>
                            </div>
                          </div>
                          
                          <!-- 子评论的子评论 -->
                          <div v-if="subComment.subComments && Array.isArray(subComment.subComments) && subComment.subComments.length > 0" class="nested-sub-comments">
                            <!-- 查看更多按钮 -->
                            <div class="view-more-comments">
                              <button 
                                class="view-more-btn"
                                @click="toggleNestedComments(subComment.commentId)"
                              >
                                {{ expandedComments[subComment.commentId] ? '收起回应' : '展开更多回应 (' + subComment.subComments.length + ')' }}
                              </button>
                            </div>
                            
                            <!-- 嵌套子评论内容 -->
                            <div v-if="expandedComments[subComment.commentId]" class="nested-sub-comment-content">
                              <div 
                                v-for="nestedSubComment in subComment.subComments" 
                                :key="nestedSubComment?.commentId || Math.random()"
                                :class="['nested-sub-comment-item', { 'topped': nestedSubComment.ifTop === 1 }]"
                              >
                                <div class="nested-sub-comment-avatar" @click="goToUserProfile(nestedSubComment.userId)">
                                  <img :src="nestedSubComment.userAvatar ? nestedSubComment.userAvatar.replace('/avatar/', '/avatar/') : 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=user%20avatar%20profile%20picture&image_size=square'" alt="回复用户头像" style="cursor: pointer;" />
                                </div>
                                <div class="nested-sub-comment-content-wrapper">
                                  <div class="nested-sub-comment-header-info">
                                    <span class="nested-sub-comment-username">{{ nestedSubComment.nickName || nestedSubComment.username || '未知用户' }}</span>
                                    <span v-if="nestedSubComment.ifTop === 1" class="top-badge">置顶</span>
                                    <span class="nested-sub-comment-time">{{ formatDate(nestedSubComment.commentTime) }}</span>
                                  </div>
                                  <div class="nested-sub-comment-text">{{ nestedSubComment.content || '' }}</div>
                                  <div class="nested-sub-comment-actions">
                                    <button 
                                      class="nested-sub-comment-action-btn"
                                      @click="replyComment(nestedSubComment)"
                                    >
                                      回应
                                    </button>
                                    <button 
                                      :class="['nested-sub-comment-action-btn', { 'liked': nestedSubComment.isLiked }]"
                                      @click="likeComment(nestedSubComment)"
                                    >
                                      <el-icon><Star /></el-icon>
                                      <span>{{ nestedSubComment.likeCount || 0 }}</span>
                                    </button>
                                    <button 
                                      v-if="loginStore.userInfo?.userId && loginStore.userInfo.userId === authorInfo?.userId"
                                      :class="['nested-sub-comment-action-btn', { 'topped': nestedSubComment.ifTop === 1 }]"
                                      @click="toggleTopComment(nestedSubComment)"
                                    >
                                      <el-icon><Top /></el-icon>
                                      <span>{{ nestedSubComment.ifTop === 1 ? '取消置顶' : '置顶' }}</span>
                                    </button>
                                    <button 
                                      v-if="loginStore.userInfo?.userId && (loginStore.userInfo.userId === nestedSubComment.userId || loginStore.userInfo.userId === authorInfo?.userId)"
                                      class="nested-sub-comment-action-btn delete-btn"
                                      @click="deleteComment(nestedSubComment)"
                                    >
                                      删除
                                    </button>
                                  </div>
                                  
                                  <!-- 嵌套子评论的回复输入框 -->
                                  <div v-if="replyTarget === nestedSubComment.commentId" class="reply-input-area">
                                    <div class="reply-input-container">
                                      <el-input
                                        v-model="replyContent"
                                        type="textarea"
                                        :rows="2"
                                        placeholder="写下你的回应..."
                                      ></el-input>
                                      <div class="reply-actions">
                                        <button 
                                          class="cancel-btn"
                                          @click="cancelReply"
                                        >
                                          取消
                                        </button>
                                        <button 
                                          class="submit-btn"
                                          @click="submitReply(nestedSubComment)"
                                          :disabled="!replyContent.trim()"
                                        >
                                          提交回应
                                        </button>
                                      </div>
                                    </div>
                                  </div>
                                </div>
                              </div>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              
              <!-- 加载更多 -->
              <div v-if="hasMoreComments" class="load-more-comments">
                <button class="load-more-btn" @click="loadMoreComments">
                  查阅更多旅笺
                </button>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 右侧推荐视频 -->
        <div class="recommended-videos">
          <h3 class="recommended-title">旅途中相似的故事</h3>
          <div class="recommended-list">
            <div class="recommended-item" v-for="(item, index) in recommendedVideos" :key="item.id" @click="goToVideoDetail(item)">
              <div class="recommended-cover">
                <img :src="item.cover" alt="视频封面" />
                <div class="video-duration">{{ item.duration }}</div>
              </div>
              <div class="recommended-info">
                <div class="recommended-title">{{ item.title }}</div>
                <div class="recommended-author">{{ item.author }}</div>
                <div class="recommended-stats">{{ item.playCount }} 播放</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
  
  <!-- 投币弹窗 -->
  <el-dialog
    v-model="showCoinDialog"
    title="为旅人投喂魔法币"
    width="300px"
    center
  >
    <div class="coin-dialog-content">
      <h3 class="coin-dialog-title">为旅人投喂 {{ selectedCoinCount }} 枚魔法币</h3>
      
      <div class="coin-options">
        <div 
          class="coin-option" 
          :class="{ 'selected': selectedCoinCount === 1 }"
          @click="selectedCoinCount = 1"
        >
          <div class="coin-icon">
            <el-icon style="font-size: 48px;"><Coin /></el-icon>
          </div>
          <div class="coin-text">1魔法币</div>
        </div>
        
        <div 
          class="coin-option" 
          :class="{ 'selected': selectedCoinCount === 2 }"
          @click="selectedCoinCount = 2"
        >
          <div class="coin-icon">
            <el-icon style="font-size: 48px;"><Coin /></el-icon>
            <el-icon style="font-size: 48px; margin-left: -10px;"><Coin /></el-icon>
          </div>
          <div class="coin-text">2魔法币</div>
        </div>
      </div>
      
      <div class="like-checkbox">
        <el-checkbox v-model="likeWithCoin">同时送上祝福</el-checkbox>
      </div>
    </div>
    
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="showCoinDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmCoinInsert">确定</el-button>
      </span>
    </template>
  </el-dialog>
  
  <!-- 旅途珍藏夹选择弹窗 -->
  <el-dialog
    v-model="showCollectDialog"
    title="选择旅途珍藏夹"
    width="400px"
    center
  >
    <div class="collect-dialog-content">
      <div class="collect-folder-list">
        <div 
          v-for="folder in collectFolders" 
          :key="folder.favorite"
          class="collect-folder-item"
          :class="{ 'selected': selectedFavorite === folder.favorite }"
          @click="selectFavoriteFolder(folder.favorite)"
        >
          <div class="folder-info">
            <el-icon><Folder /></el-icon>
            <span class="folder-name">{{ folder.favoriteName }}</span>
            <span class="folder-count">{{ folder.videoCount || 0 }} 个视频</span>
          </div>
          <div class="folder-actions">
            <el-button 
              type="text" 
              size="small" 
              @click.stop="editFolder(folder)"
              class="edit-btn"
            >
              <el-icon><Edit /></el-icon>
            </el-button>
          </div>
        </div>
      </div>
      
      <div class="collect-actions">
        <el-button type="primary" @click="createNewFolder" class="create-folder-btn">
          <el-icon><Plus /></el-icon>
          编织新珍藏夹
        </el-button>
      </div>
    </div>
    
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="showCollectDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmCollect" :disabled="!selectedFavorite">确定</el-button>
      </span>
    </template>
  </el-dialog>
  
  <!-- 编辑珍藏夹弹窗 -->
  <el-dialog
    v-model="showEditFolderDialog"
    title="编修珍藏夹"
    width="400px"
    center
  >
    <el-form>
      <el-form-item label="珍藏夹名称">
        <el-input v-model="editingFolderName" placeholder="请输入珍藏夹名称"></el-input>
      </el-form-item>
      <el-form-item label="公开状态">
        <el-radio-group v-model="editingFolderIsPublic">
          <el-radio :value="1">公开</el-radio>
          <el-radio :value="0">私有</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="showEditFolderDialog = false">取消</el-button>
        <el-button type="primary" @click="saveFolderEdit">确定</el-button>
      </span>
    </template>
  </el-dialog>
  
  <!-- 新建珍藏夹弹窗 -->
  <el-dialog
    v-model="showNewFolderDialog"
    title="编织新珍藏夹"
    width="400px"
    center
  >
    <el-form>
      <el-form-item label="珍藏夹名称">
        <el-input v-model="newFolderName" placeholder="请输入珍藏夹名称"></el-input>
      </el-form-item>
      <el-form-item label="公开状态">
        <el-radio-group v-model="newFolderIsPublic">
          <el-radio :value="1">公开</el-radio>
          <el-radio :value="0">私有</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="showNewFolderDialog = false">取消</el-button>
        <el-button type="primary" @click="saveNewFolder">确定</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, onMounted, onUnmounted, inject, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { mitter } from '@/eventbus/eventBus'
import Header from '@/components/Header.vue'
import Sidebar from '@/components/Sidebar.vue'
import { MagicStick, Coin, Collection, Share, Folder, Plus, Edit, Top, ArrowDown, Close } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useLoginStore } from '@/stores/loginStore'
import DPlayer from 'dplayer'

const route = useRoute()
const router = useRouter()
const videoId = ref(route.params.videoId)
const dplayerRef = ref(null)
const dp = ref(null)

// 监听路由参数变化，重新加载视频详情
watch(() => route.params.videoId, async (newVideoId) => {
  if (newVideoId && newVideoId !== videoId.value) {
    videoId.value = newVideoId
    // 重新加载视频详情
    await loadVideoDetail()
    // 重新初始化视频播放器
    initDPlayer()
    // 重新加载推荐视频
    loadRecommendedVideos()
    // 重新加载评论
    loadComments()
  }
})

// 定义进度记录定时器变量
let progressRecordTimer = null

// 注入全局工具
const Request = inject('Request', null)
const Api = inject('Api', null)
const loginStore = useLoginStore()

// 视频信息
const videoInfo = ref({
  title: '加载中...',
  videoUrl: '',
  cover: '',
  playCount: 0,
  likeCount: 0,
  coinCount: 0,
  collectCount: 0,
  createTime: new Date(),
  description: '加载中...',
  userId: null,
  tags: [] // 视频标签
})

// 作者信息
const authorInfo = ref({
  username: '加载中...',
  avatar: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=user%20avatar%20profile%20picture&image_size=square',
  fansCount: 0,
  userId: null
})

// 关注状态
const isFollowing = ref(false)

// 点赞状态
const isLiked = ref(false)

// 投币弹窗相关
const showCoinDialog = ref(false)
const selectedCoinCount = ref(2) // 默认选择2个硬币
const likeWithCoin = ref(true) // 默认同时点赞
const isCoined = ref(false) // 投币状态

// 收藏状态
const isCollected = ref(false)

// 收藏夹相关
const showCollectDialog = ref(false)
const showEditFolderDialog = ref(false)
const showNewFolderDialog = ref(false)
const collectFolders = ref([])
const selectedFavorite = ref(null)
const editingFolder = ref(null)
const editingFolderName = ref('')
const editingFolderIsPublic = ref(1)
const newFolderName = ref('')
const newFolderIsPublic = ref(1)

// 评论相关
const comments = ref([])
const commentCount = ref(0)
const commentContent = ref('')
const replyContent = ref('')
const replyTarget = ref(null)
const sortord = ref(1) // 0: 最热, 1: 最新
const hasMoreComments = ref(true)
const expandedComments = ref({}) // 存储展开的子评论ID

// 弹幕列表相关
const danmakuList = ref([])
const showDanmakuList = ref(false)

// 监听sortord变化，重新加载评论
watch(sortord, async (newSortord) => {
  console.log('评论排序方式已更改:', newSortord === 0 ? '最热' : '最新')
  await loadComments()
})

// 计算属性：获取顶级评论（parentId 为 null 或 0 或 undefined）
const topLevelComments = computed(() => {
  // 添加防御性检查，确保 comments.value 是数组且不包含 null 元素
  if (!Array.isArray(comments.value)) {
    console.log('comments.value 不是数组:', comments.value)
    return []
  }
  
  console.log('原始评论数据:', comments.value)
  
  const filteredComments = comments.value.filter(comment => {
    // 确保评论对象不为 null
    if (!comment) {
      return false
    }
    
    // 检查 parentId，宽松处理：null、undefined、0 都视为顶级评论
    const isTopLevel = comment.parentId === null || comment.parentId === undefined || comment.parentId === 0
    console.log('评论 ID:', comment.commentId, 'parentId:', comment.parentId, '是否顶级:', isTopLevel)
    
    return isTopLevel
  })
  
  console.log('过滤后的顶级评论:', filteredComments)
  return filteredComments
})

// 获取视频上传者的详细信息
const getUploaderInfo = async () => {
  // 检查是否获取到作者ID
  if (!authorInfo.value.userId) {
    return
  }
  
  try {
    // 始终使用getEXAttentionUserInfo方法，避免重复增加clicks计数
    // 只有进入个人主页时才使用getAttentionUserInfo方法
    const result = await Request({
      url: Api.getEXAttentionUserInfo,
      method: 'GET',
      params: {
        userId: authorInfo.value.userId
      }
    })
    
    if (result && result.success) {
      const userInfo = result.data
      // 更新作者信息
      if (userInfo) {
        // 支持多种字段名：nickName、nickname、author、username
        const authorName = userInfo.nickName || userInfo.nickname || userInfo.author || userInfo.username || authorInfo.value.username
        
        // 处理头像URL
        let avatarUrl = userInfo.avatar || authorInfo.value.avatar
        if (avatarUrl && !avatarUrl.startsWith('http')) {
          // 如果头像路径不是完整URL，添加前缀
          if (avatarUrl.startsWith('/avatar/')) {
            avatarUrl = `${avatarUrl}`
          } else {
            avatarUrl = `/avatar/${avatarUrl}`
          }
        }
        
        authorInfo.value = {
          ...authorInfo.value,
          username: authorName,
          avatar: avatarUrl,
          fansCount: userInfo.followerCount || userInfo.fans || authorInfo.value.fansCount
        }
      }
    }
  } catch (error) {
    console.error('获取视频上传者信息失败:', error)
  }
}

// 检查关注状态
const checkFollowStatus = async () => {
  // 检查用户是否登录
  if (!loginStore.userInfo.userId) {
    isFollowing.value = false
    return
  }
  
  // 检查是否获取到作者ID
  if (!authorInfo.value.userId) {
    isFollowing.value = false
    return
  }
  
  // 检查是否是自己的视频
  if (loginStore.userInfo.userId === authorInfo.value.userId) {
    isFollowing.value = false
    return
  }
  
  try {
    const result = await Request({
      url: Api.selectIfFollow,
      method: 'GET',
      params: {
        userId: authorInfo.value.userId
      }
    })
    
    if (result && result.success) {
      isFollowing.value = result.data
    } else {
      isFollowing.value = false
    }
  } catch (error) {
    console.error('检查关注状态失败:', error)
    isFollowing.value = false
  }
}

// 检查点赞状态
// 收藏视频
const collectVideo = async () => {
  // 检查用户是否登录
  if (!loginStore.userInfo.userId) {
    ElMessage.warning('请先登录后再收藏')
    return
  }
  
  // 如果已经收藏，打开收藏夹选择弹窗
  if (isCollected.value) {
    // 加载用户的收藏夹列表
    await loadCollectFolders()
    // 打开收藏夹选择弹窗
    showCollectDialog.value = true
    return
  }
  
  // 如果未收藏，直接收藏到默认收藏夹
  try {
    const result = await Request({
      url: Api.increaseCollect,
      method: 'POST',
      params: {
        videoId: videoId.value
      }
    })
    
    if (result) {
      // 简化响应处理逻辑
      const isSuccess = result.success || result.status === 'success' || result.code === 200
      if (isSuccess) {
        // 根据后端返回的消息判断是收藏还是取消收藏
        const message = result.data || result.msg || ''
        console.log('后端返回消息:', message)
        
        if (message.includes('取消收藏成功')) {
          ElMessage.success('取消收藏成功')
          // 更新收藏状态
          isCollected.value = false
          // 更新收藏数
          if (videoInfo.value.collectCount && parseInt(videoInfo.value.collectCount) > 0) {
            videoInfo.value.collectCount = String(parseInt(videoInfo.value.collectCount) - 1)
          } else {
            videoInfo.value.collectCount = '0'
          }
        } else if (message.includes('收藏成功')) {
          ElMessage.success('收藏成功')
          // 更新收藏状态
          isCollected.value = true
          // 更新收藏数
          if (videoInfo.value.collectCount) {
            videoInfo.value.collectCount = String(parseInt(videoInfo.value.collectCount) + 1)
          } else {
            videoInfo.value.collectCount = '1'
          }
        }
      } else {
        ElMessage.error(result.msg || result.data || '操作失败，请重试')
      }
    }
  } catch (error) {
    console.error('收藏操作失败:', error)
    ElMessage.error('操作失败，请重试')
  }
}

// 关注用户
const followUser = async () => {
  // 检查用户是否登录
  if (!loginStore.userInfo.userId) {
    ElMessage.warning('请先登录后再关注用户')
    return
  }
  
  // 检查是否获取到作者ID
  if (!authorInfo.value.userId) {
    ElMessage.error('无法获取作者信息，请刷新页面重试')
    return
  }
  
  // 检查是否关注自己
  if (loginStore.userInfo.userId === authorInfo.value.userId) {
    ElMessage.warning('不能关注自己')
    return
  }
  
  try {
    const result = await Request({
      url: Api.followUser,
      method: 'POST',
      params: {
        userId: authorInfo.value.userId
      }
    })
    
    if (result && result.success) {
      // 根据当前状态切换关注状态
      const newStatus = !isFollowing.value
      isFollowing.value = newStatus
      
      // 显示相应的提示信息
      ElMessage.success(newStatus ? '关注成功' : '取消关注成功')
      
      // 更新粉丝数
      if (newStatus) {
        // 关注：粉丝数+1
        authorInfo.value.fansCount = (authorInfo.value.fansCount || 0) + 1
      } else {
        // 取消关注：粉丝数-1（确保不小于0）
        authorInfo.value.fansCount = Math.max(0, (authorInfo.value.fansCount || 0) - 1)
      }
    } else {
      ElMessage.error('操作失败，请重试')
    }
  } catch (error) {
    console.error('关注/取消关注用户失败:', error)
    ElMessage.error('操作失败，请重试')
  }
}

// 点赞视频
const likeVideo = async () => {
  // 检查用户是否登录
  if (!loginStore.userInfo.userId) {
    ElMessage.warning('请先登录后再点赞')
    return
  }
  
  try {
    const result = await Request({
      url: Api.increaseLikeCount,
      method: 'GET',
      params: {
        videoId: videoId.value
      }
    })
    
    if (result) {
      // 简化响应处理逻辑
      const isSuccess = result.success || result.status === 'success' || result.code === 200
      if (isSuccess) {
        // 根据后端返回的数据判断是点赞还是取消点赞
        const message = result.data || result.msg || ''
        console.log('后端返回消息:', message)
        
        // 先检查取消点赞，避免字符串包含问题
        if (message.includes('取消点赞成功')) {
          ElMessage.success('取消点赞成功')
          // 更新点赞状态
          isLiked.value = false
          // 更新点赞数
          if (videoInfo.value.likeCount && parseInt(videoInfo.value.likeCount) > 0) {
            videoInfo.value.likeCount = String(parseInt(videoInfo.value.likeCount) - 1)
          } else {
            videoInfo.value.likeCount = '0'
          }
        } else if (message.includes('点赞成功')) {
          ElMessage.success('点赞成功')
          // 更新点赞状态
          isLiked.value = true
          // 更新点赞数
          if (videoInfo.value.likeCount) {
            videoInfo.value.likeCount = String(parseInt(videoInfo.value.likeCount) + 1)
          } else {
            videoInfo.value.likeCount = '1'
          }
        }
      } else {
        ElMessage.error(result.msg || result.data || '操作失败，请重试')
      }
    }
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败，请重试')
  }
}

// 推荐视频列表
const recommendedVideos = ref([
  {
    id: 1,
    title: '推荐视频 1',
    cover: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=video%20thumbnail%201&image_size=landscape_4_3',
    author: '作者 1',
    playCount: 1234,
    duration: '03:45'
  },
  {
    id: 2,
    title: '推荐视频 2',
    cover: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=video%20thumbnail%202&image_size=landscape_4_3',
    author: '作者 2',
    playCount: 5678,
    duration: '05:20'
  },
  {
    id: 3,
    title: '推荐视频 3',
    cover: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=video%20thumbnail%203&image_size=landscape_4_3',
    author: '作者 3',
    playCount: 9012,
    duration: '02:15'
  }
])

// 增加视频播放量
const increasePlayCount = async () => {
  try {
    const result = await Request({
      url: Api.increasePlayCount,
      method: 'GET',
      params: {
        videoId: videoId.value
      }
    })
    
    if (result && result.success) {
      console.log('播放量增加成功')
    }
  } catch (error) {
    console.error('增加播放量失败:', error)
  }
}

// 初始化DPlayer
const initDPlayer = () => {
  if (!dplayerRef.value) return

  // 销毁已存在的实例
  if (dp.value) {
    dp.value.destroy()
  }

  const videoUrl = videoInfo.value.videoUrl || ''
  const coverUrl = videoInfo.value.coverUrl || videoInfo.value.cover || ''

  // 获取当前用户的用户名
  const getCurrentUsername = () => {
    if (loginStore.userInfo && loginStore.userInfo.username) {
      return loginStore.userInfo.username;
    }
    if (loginStore.userInfo && loginStore.userInfo.nickname) {
      return loginStore.userInfo.nickname;
    }
    if (loginStore.userInfo && loginStore.userInfo.nickName) {
      return loginStore.userInfo.nickName;
    }
    return '匿名用户';
  };

  const currentUsername = getCurrentUsername();

  // 配置DPlayer
  dp.value = new DPlayer({
    container: dplayerRef.value,
    autoplay: true,
    video: {
      url: videoUrl,
      cover: coverUrl,
      type: 'auto'
    },
    danmaku: {
      id: videoId.value,
      api: '/api/danmaku/dplayer/',
      user: currentUsername,
      bottom: '15%',
      unlimited: true
    },
    control: true,
    speed: false
  });

  // 监听弹幕发送事件（DPlayer 已自动绘制弹幕，此处负责持久化到后端）
  dp.value.on('danmaku_send', function (danmaku) {
    if (!loginStore.userInfo.userId) {
      ElMessage.warning('请先登录后再发送弹幕')
      return
    }

    // DPlayer 类型 0=滚动,1=顶部 → 后端存储 0=顶部,1=滚动
    let backendType = danmaku.type
    if (backendType === 0) backendType = 1
    else if (backendType === 1) backendType = 0

    let backendColor = danmaku.color || '#ffffff'
    if (!backendColor || backendColor === '') {
      backendColor = '#ffffff'
    } else if (typeof backendColor === 'number') {
      backendColor = '#' + backendColor.toString(16).padStart(6, '0')
    } else if (!backendColor.startsWith('#')) {
      backendColor = '#' + backendColor
    }

    Request({
      url: '/api/danmaku/addDanmaku',
      method: 'POST',
      params: {
        videoId: videoId.value,
        userId: loginStore.userInfo.userId,
        username: getCurrentUsername(),
        content: danmaku.text,
        time: danmaku.time || 0,
        color: backendColor,
        type: backendType
      }
    }).then(response => {
      console.log('弹幕发送成功:', response)
    }).catch(error => {
      console.error('弹幕发送失败:', error)
      ElMessage.error('弹幕发送失败，请重试')
    })
  })

  // 实现自定义的速度控制
  if (dp.value && dp.value.video) {
    // 延迟执行，确保DPlayer已经完全初始化
    setTimeout(() => {
      // 查找DPlayer控制栏
      if (dplayerRef.value) {
        const controlBar = dplayerRef.value.querySelector('.dplayer-control-bar');
        if (controlBar) {
        // 创建自定义的速度控制按钮
        const speedButton = document.createElement('div');
        speedButton.className = 'dplayer-button dplayer-speed';
        speedButton.style.marginRight = '10px';
        speedButton.style.color = '#fff';
        speedButton.style.cursor = 'pointer';
        speedButton.style.display = 'flex';
        speedButton.style.alignItems = 'center';
        speedButton.textContent = '1.0x';
        
        // 创建速度菜单
        const speedMenu = document.createElement('div');
        speedMenu.className = 'dplayer-speed-menu';
        speedMenu.style.position = 'absolute';
        speedMenu.style.bottom = '40px';
        speedMenu.style.right = '10px';
        speedMenu.style.backgroundColor = 'rgba(0, 0, 0, 0.8)';
        speedMenu.style.color = '#fff';
        speedMenu.style.borderRadius = '4px';
        speedMenu.style.display = 'none';
        speedMenu.style.zIndex = '1000';
        
        // 速度选项
        const speeds = [0.5, 0.75, 1, 1.25, 1.5, 2];
        speeds.forEach(speed => {
          const option = document.createElement('div');
          option.style.padding = '8px 12px';
          option.style.cursor = 'pointer';
          option.style.whiteSpace = 'nowrap';
          option.textContent = `${speed}x`;
          
          // 添加点击事件
          option.addEventListener('click', function() {
            // 设置视频速度
            dp.value.video.playbackRate = speed;
            // 更新按钮文本
            speedButton.textContent = `${speed}x`;
            // 关闭菜单
            speedMenu.style.display = 'none';
            // 输出日志
            console.log('设置视频速度为:', speed);
            console.log('实际视频速度为:', dp.value.video.playbackRate);
          });
          
          speedMenu.appendChild(option);
        });
        
        // 添加点击事件，显示/隐藏菜单
        speedButton.addEventListener('click', function(e) {
          e.stopPropagation();
          speedMenu.style.display = speedMenu.style.display === 'none' ? 'block' : 'none';
        });
        
        // 点击其他地方关闭菜单
        document.addEventListener('click', function() {
          speedMenu.style.display = 'none';
        });
        
        // 将按钮和菜单添加到控制栏
        controlBar.appendChild(speedButton);
        controlBar.appendChild(speedMenu);
        
        console.log('自定义速度控制按钮已添加');
        }
      }
    }, 1500);

  }
  
  // 重写DPlayer的错误处理方法，完全禁用错误提示
  if (dp.value && dp.value.danmaku) {
    // 重写错误提示方法
    if (dp.value.danmaku.notify) {
      dp.value.danmaku.notify = function() {};
    }

    // 重写错误处理方法
    if (dp.value.danmaku.error) {
      dp.value.danmaku.error = function() {};
    }
  }
  
  // 安全地隐藏错误提示元素
  setTimeout(() => {
    try {
      // 只在DPlayer容器内查找元素
      if (dplayerRef.value) {
        // 查找DPlayer容器内的错误提示元素
        const errorElements = dplayerRef.value.querySelectorAll(
          '.dplayer-notice, .dplayer-notice-error'
        );
        errorElements.forEach(element => {
          element.style.display = 'none';
          element.style.visibility = 'hidden';
          element.style.opacity = '0';
        });
        
        // 查找包含"弹幕加载失败"文本的元素
        const elements = dplayerRef.value.querySelectorAll('*');
        elements.forEach(element => {
          if (element.textContent && element.textContent.includes('弹幕加载失败')) {
            element.style.display = 'none';
            element.style.visibility = 'hidden';
            element.style.opacity = '0';
          }
        });
      }
    } catch (error) {
      console.error('隐藏错误提示时出错:', error);
    }
  }, 300);
  
  // 简化的DOM变化监听
  try {
    const observer = new MutationObserver((mutations) => {
      mutations.forEach((mutation) => {
        mutation.addedNodes.forEach((node) => {
          if (node.nodeType === 1 && node.textContent && node.textContent.includes('弹幕加载失败')) {
            node.style.display = 'none';
            node.style.visibility = 'hidden';
            node.style.opacity = '0';
          }
        });
      });
    });
    
    // 开始观察DPlayer容器
    if (dplayerRef.value) {
      observer.observe(dplayerRef.value, {
        childList: true,
        subtree: true,
        characterData: true
      });
    }
  } catch (error) {
    console.error('创建MutationObserver时出错:', error);
  }
  
}

// 加载视频详情
const loadVideoDetail = async () => {
  try {
    const result = await Request({
      url: `${Api.loadVideo}/${videoId.value}`,
      method: 'GET'
    })
    
    if (result && result.data) {
      // 处理视频数据
      const video = result.data
      
      // 处理视频URL
      if (video.videoUrl && !video.videoUrl.startsWith('http')) {
        video.videoUrl = `${video.videoUrl}`
      }
      
      // 处理封面URL
      if (video.coverUrl && !video.coverUrl.startsWith('http')) {
        video.coverUrl = `${video.coverUrl}`
      }
      
      // 处理头像URL
      if (video.avatar && !video.avatar.startsWith('http')) {
        // 头像路径通常存储在/avatar/目录下
        video.avatar = `/avatar/${video.avatar}`;
      }
      
      videoInfo.value = video
      
      // 将后端的coinsInserted字段映射到前端的coinCount字段
      videoInfo.value.coinCount = video.coinsInserted != null ? video.coinsInserted : 0
      
      // 将后端的followCount字段映射到前端的collectCount字段
      if (video.followCount !== undefined && video.followCount !== null) {
        videoInfo.value.collectCount = video.followCount
      }
      
      // 确保likeCount字段有值
      if (video.likeCount === undefined || video.likeCount === null) {
        videoInfo.value.likeCount = 0
      }
      
      // 从视频数据中提取作者信息
      if (video) {
        // 支持多种字段名：nickName、nickname、author、username
        const authorName = video.nickName || video.nickname || video.author || video.username || '未知作者'
        authorInfo.value = {
          username: authorName,
          avatar: video.avatar || 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=user%20avatar%20profile%20picture&image_size=square',
          fansCount: video.fans || 0,
          userId: video.userId || video.user_id || null
        }

        // 检查关注状态
        await checkFollowStatus()
        // 直接从 loadVideo 返回的数据设置互动状态
        isLiked.value = video.iflike === '1'
        isCoined.value = (video.userCoinsInserted || 0) > 0
        isCollected.value = video.ifcollect === '1'
        // 获取视频上传者的详细信息
        await getUploaderInfo()
      }
      // Emit video context for mascot pet
      mitter.emit('video-context', {
        videoId: videoId.value,
        title: videoInfo.value?.title || '',
        description: videoInfo.value?.description || '',
        tags: getTagsArray(videoInfo.value?.tags).join(', ')
      })
    }
  } catch (error) {
    console.error('Failed to load video detail:', error)
  }
}

// 统一创建视频对象的函数，确保结构一致
const createVideoObject = (video) => {
  // 处理封面URL
  let coverUrl = video.coverUrl || video.cover || ''
  if (coverUrl && !coverUrl.startsWith('http')) {
    coverUrl = `${coverUrl}`
  }
  
  // 提取作者名字，支持多种字段名
  const authorName = video.nickName || video.nickname || video.author || video.username || video.nick_name || video.userName || '未知作者'
  
  // 统一ID字段处理
  const videoId = video.videoId || video.id
  
  return {
    id: videoId,
    title: video.title,
    cover: coverUrl,
    author: authorName,
    playCount: video.playCount || 0,
    duration: video.duration || '00:00',
    videoId: videoId
  }
}

// 加载推荐视频
const loadRecommendedVideos = async () => {
  try {
    // 检查当前视频是否有标签
    const tagsArray = getTagsArray(videoInfo.value.tags)
    
    // 存储所有推荐视频的集合，用于去重
    const allRecommendedVideos = new Set()
    
    if (tagsArray.length > 0) {
      // 使用标签搜索推荐视频
      console.log('使用标签搜索推荐视频:', tagsArray)
      
      // 循环使用每个标签搜索
      for (const tag of tagsArray) {
        console.log('使用标签搜索:', tag)
        
        const result = await Request({
          url: `/api/video/search/byTags`,
          method: 'GET',
          params: {
            tags: tag,
            page: 0,
            size: 10 // 每个标签获取10个视频
          }
        })
        
        if (result && result.data) {
          // 处理推荐视频数据
          const processedVideos = result.data.map(createVideoObject)
          
          // 将处理后的视频添加到集合中（自动去重）
          processedVideos.forEach(video => {
            if (video.videoId && video.videoId !== videoInfo.value.videoId) {
              allRecommendedVideos.add(JSON.stringify(video))
            }
          })
          
          // 如果已经收集到足够的视频，提前结束
          if (allRecommendedVideos.size >= 12) {
            break
          }
        }
      }
    }
    
    // 如果通过标签搜索到的视频不足12个，调用默认推荐接口补充
    if (allRecommendedVideos.size < 12) {
      console.log('标签搜索视频不足，调用默认推荐接口补充')
      
      const result = await Request({
        url: Api.loadRecommendVideo,
        method: 'GET',
        params: {
          pageNo: 1,
          pageSize: 20 // 获取足够的视频来补充
        }
      })
      
      if (result && result.data && result.data.list) {
        // 处理推荐视频数据
        const processedVideos = result.data.list.map(createVideoObject)
        
        // 添加默认推荐视频，直到达到12个
        for (const video of processedVideos) {
          if (video.videoId && video.videoId !== videoInfo.value.videoId) {
            allRecommendedVideos.add(JSON.stringify(video))
            if (allRecommendedVideos.size >= 12) {
              break
            }
          }
        }
      }
    }
    
    // 将集合转换回数组，并限制数量为12个
    recommendedVideos.value = Array.from(allRecommendedVideos)
      .map(videoStr => JSON.parse(videoStr))
      .slice(0, 12)
    
    console.log('最终推荐视频数量:', recommendedVideos.value.length)
  } catch (error) {
    console.error('Failed to load recommended videos:', error)
  }
}

// 跳转到用户个人中心
const goToUserProfile = (userId) => {
  if (userId) {
    router.push(`/profile/${userId}`)
  }
}

// 跳转到标签搜索页面
const goToTagSearch = (tag) => {
  router.push({ path: '/search', query: { tag: tag } })
}

// 跳转到其他视频详情页
const goToVideoDetail = (video) => {
  if (video && video.videoId) {
    router.push(`/video/${video.videoId}`)
  }
}

// 解析视频标签
const getTagsArray = (tags) => {
  if (!tags) return []
  
  // 如果已经是数组，直接返回
  if (Array.isArray(tags)) {
    return tags.filter(tag => tag && typeof tag === 'string' && tag.trim())
  }
  
  // 如果是字符串，需要解析
  let tagsStr = tags.toString().trim()
  
  // 处理格式如"(历史,音乐)"的情况
  if (tagsStr.startsWith('(') && tagsStr.endsWith(')')) {
    tagsStr = tagsStr.substring(1, tagsStr.length - 1)
  }
  
  // 按逗号分割并过滤空标签
  return tagsStr.split(',').map(tag => tag.trim()).filter(tag => tag)
}

// 切换弹幕列表显示/隐藏
const toggleDanmakuList = () => {
  showDanmakuList.value = !showDanmakuList.value
  if (showDanmakuList.value && danmakuList.value.length === 0) {
    loadDanmakuList()
  }
}

// 加载弹幕列表
const loadDanmakuList = async () => {
  try {
    const response = await Request({
      url: '/api/danmaku/getDanmaku',
      method: 'GET',
      params: {
        id: videoId.value
      }
    })
    
    if (response && response.success && response.data) {
      danmakuList.value = response.data
      console.log('弹幕列表加载成功:', response.data)
    }
  } catch (error) {
    console.error('弹幕列表加载失败:', error)
  }
}

// 格式化弹幕在视频中的时间
const formatDanmakuTime = (time) => {
  if (!time && time !== 0) return '00:00'
  const totalSeconds = Math.floor(time)
  const minutes = Math.floor(totalSeconds / 60).toString().padStart(2, '0')
  const seconds = (totalSeconds % 60).toString().padStart(2, '0')
  return `${minutes}:${seconds}`
}



// 删除弹幕
const deleteDanmaku = async (danmakuId, index) => {
  if (!danmakuId) {
    console.warn('弹幕ID不存在，无法删除')
    return
  }
  
  // 确保用户已登录
  if (!loginStore.userInfo?.userId) {
    console.warn('用户未登录，无法删除弹幕')
    return
  }
  
  try {
    const response = await Request({
      url: '/api/danmaku/deleteDanmaku',
      method: 'DELETE',
      params: {
        id: danmakuId,
        user_id: loginStore.userInfo.userId, // 添加user_id参数
        video_id: videoId.value // 添加video_id参数
      }
    })
    
    if (response && response.success) {
      // 从列表中移除删除的弹幕
      danmakuList.value.splice(index, 1)
      console.log('弹幕删除成功')
    } else {
      console.error('弹幕删除失败:', response?.message || '未知错误')
    }
  } catch (error) {
    console.error('删除弹幕时发生错误:', error)
  }
}

// 显示投币弹窗
const openCoinDialog = () => {
  showCoinDialog.value = true
}

// 确认投币
const confirmCoinInsert = async () => {
  // 检查用户是否登录
  if (!loginStore.userInfo.userId) {
    ElMessage.warning('请先登录后再投币')
    showCoinDialog.value = false
    return
  }
  
  try {
    // 调用后端投币接口
    const result = await Request({
      url: Api.insertCoins,
      method: 'POST',
      params: {
        videoId: videoId.value,
        coinsCount: selectedCoinCount.value
      }
    })
    
    if (result && result.success) {
      ElMessage.success('投币成功')
      // 更新本地投币数
      if (videoInfo.value.coinCount) {
        videoInfo.value.coinCount = String(parseInt(videoInfo.value.coinCount) + selectedCoinCount.value)
      } else {
        videoInfo.value.coinCount = String(selectedCoinCount.value)
      }
      
      // 设置投币状态为已投币
      isCoined.value = true
      
      // 如果选择同时点赞，调用点赞方法
      if (likeWithCoin.value && !isLiked.value) {
        await likeVideo()
      }
      
      // 关闭弹窗
      showCoinDialog.value = false
    } else {
      ElMessage.error(result.msg || result.data || '投币失败，请重试')
    }
  } catch (error) {
    console.error('投币失败:', error)
    ElMessage.error('投币失败，请重试')
  }
}

// 加载用户的收藏夹列表（粉圈功能）
const loadCollectFolders = async () => {
  try {
    const result = await Request({
      url: Api.getFavorite,
      method: 'GET'
    })
    
    if (result && result.data) {
      const favoriteList = result.data
      
      // 对收藏夹进行去重，因为后端返回的是所有收藏记录
      const folderMap = new Map()
      favoriteList.forEach(collect => {
        const key = collect.favorite
        if (!folderMap.has(key)) {
          folderMap.set(key, {
            favorite: collect.favorite,
            favoriteName: collect.favoriteName,
            isPublic: collect.isPublic || 1,
            videoCount: 0
          })
        }
      })
      
      collectFolders.value = Array.from(folderMap.values())
      
      // 获取每个收藏夹的视频数量
      for (const folder of collectFolders.value) {
        const countResult = await Request({
          url: Api.getCollectCount,
          method: 'GET',
          params: {
            favoriteNO: folder.favorite
          }
        })
        
        if (countResult && countResult.data !== undefined) {
          folder.videoCount = countResult.data
        }
      }
    }
  } catch (error) {
    console.error('加载收藏夹列表失败:', error)
    ElMessage.error('加载收藏夹列表失败')
  }
}

// 选择收藏夹
const selectFavoriteFolder = (favorite) => {
  selectedFavorite.value = favorite
}

// 确认收藏到选中的收藏夹（调用moveCollect方法）
const confirmCollect = async () => {
  if (!selectedFavorite.value) {
    ElMessage.warning('请选择收藏夹')
    return
  }
  
  try {
    const result = await Request({
      url: Api.moveCollect,
      method: 'GET',
      params: {
        videoId: videoId.value,
        newFavorite: selectedFavorite.value
      }
    })
    
    if (result && result.success) {
      ElMessage.success('移动到收藏夹成功')
      showCollectDialog.value = false
      selectedFavorite.value = null
    } else {
      ElMessage.error(result.msg || '移动失败，请重试')
    }
  } catch (error) {
    console.error('移动到收藏夹失败:', error)
    ElMessage.error('移动失败，请重试')
  }
}

// 编辑收藏夹（蓝圈功能）
const editFolder = (folder) => {
  editingFolder.value = folder
  editingFolderName.value = folder.favoriteName
  editingFolderIsPublic.value = folder.isPublic
  showEditFolderDialog.value = true
}

// 保存收藏夹编辑（调用updateCollectStatus方法）
const saveFolderEdit = async () => {
  if (!editingFolderName.value.trim()) {
    ElMessage.warning('请输入收藏夹名称')
    return
  }
  
  try {
    const result = await Request({
      url: Api.updateCollectStatus,
      method: 'GET',
      params: {
        favorite: editingFolder.value.favorite,
        favoriteName: editingFolderName.value.trim(),
        isPublic: editingFolderIsPublic.value
      }
    })
    
    if (result && result.success) {
      ElMessage.success('修改成功')
      showEditFolderDialog.value = false
      editingFolder.value = null
      editingFolderName.value = ''
      editingFolderIsPublic.value = 1
      
      // 重新加载收藏夹列表
      await loadCollectFolders()
    } else {
      ElMessage.error(result.msg || '修改失败，请重试')
    }
  } catch (error) {
    console.error('修改收藏夹失败:', error)
    ElMessage.error('修改失败，请重试')
  }
}

// 新建收藏夹
const createNewFolder = () => {
  newFolderName.value = ''
  newFolderIsPublic.value = 1
  showNewFolderDialog.value = true
}

// 保存新建收藏夹
const saveNewFolder = async () => {
  if (!newFolderName.value.trim()) {
    ElMessage.warning('请输入收藏夹名称')
    return
  }
  
  try {
    const result = await Request({
      url: Api.addCollect,
      method: 'GET',
      params: {
        favoriteName: newFolderName.value.trim(),
        isPublic: newFolderIsPublic.value
      }
    })
    
    if (result && result.success) {
      ElMessage.success('新建收藏夹成功')
      showNewFolderDialog.value = false
      newFolderName.value = ''
      newFolderIsPublic.value = 1
      
      // 重新加载收藏夹列表
      await loadCollectFolders()
    } else {
      ElMessage.error(result.msg || '新建失败，请重试')
    }
  } catch (error) {
    console.error('新建收藏夹失败:', error)
    ElMessage.error('新建失败，请重试')
  }
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 加载评论列表
const loadComments = async () => {
  try {
    const result = await Request({
      url: Api.getVideoComments,
      method: 'GET',
      params: {
        videoId: videoId.value,
        sortord: sortord.value,
        userId: loginStore.userInfo?.userId || null
      }
    })
    
    if (result && result.success) {
      // 确保 data 是数组
      comments.value = Array.isArray(result.data) ? result.data : []
      
      // 处理评论的isLiked字段
      comments.value.forEach(comment => {
        // 确保isLiked字段有值
        if (comment.isLiked === undefined || comment.isLiked === null) {
          comment.isLiked = false
        }
        
        // 处理子评论的isLiked字段
        if (comment.subComments && Array.isArray(comment.subComments)) {
          comment.subComments.forEach(subComment => {
            if (subComment.isLiked === undefined || subComment.isLiked === null) {
              subComment.isLiked = false
            }
          })
        }
      })
      
      // 对评论进行排序：置顶的评论排在最前面
      comments.value.sort((a, b) => {
        // 首先按置顶状态排序（置顶的在前）
        if (a.ifTop === 1 && b.ifTop !== 1) {
          return -1
        }
        if (a.ifTop !== 1 && b.ifTop === 1) {
          return 1
        }
        // 如果置顶状态相同，按原来的排序方式
        return 0
      })
      
      // 对每个主评论的子评论按时间升序排序（最早的在上面）
      comments.value.forEach(comment => {
        if (comment.subComments && Array.isArray(comment.subComments) && comment.subComments.length > 0) {
          comment.subComments.sort((a, b) => {
            // 首先按置顶状态排序（置顶的在前）
            if (a.ifTop === 1 && b.ifTop !== 1) {
              return -1
            }
            if (a.ifTop !== 1 && b.ifTop === 1) {
              return 1
            }
            // 如果置顶状态相同，按时间升序排序
            const timeA = new Date(a.commentTime).getTime();
            const timeB = new Date(b.commentTime).getTime();
            return timeA - timeB;
          });
        }
      });
      
      commentCount.value = comments.value.length
      hasMoreComments.value = comments.value.length >= 20 // 假设每页20条
    } else {
      comments.value = []
      commentCount.value = 0
    }
  } catch (error) {
    console.error('加载评论失败:', error)
    ElMessage.error('加载评论失败，请重试')
    comments.value = []
    commentCount.value = 0
  }
}

// 提交评论
const submitComment = async () => {
  if (!loginStore.userInfo.userId) {
    ElMessage.warning('请先登录后再评论')
    return
  }
  
  if (!commentContent.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  
  try {
    const result = await Request({
      url: Api.addComment,
      method: 'POST',
      params: {
        videoId: videoId.value,
        content: commentContent.value.trim(),
        userId: loginStore.userInfo.userId,
        parentId: null
      }
    })
    
    if (result && result.success) {
      ElMessage.success('评论成功')
      commentContent.value = ''
      // 重新加载评论列表
      await loadComments()
    } else {
      ElMessage.error('评论失败，请重试')
    }
  } catch (error) {
    console.error('提交评论失败:', error)
    ElMessage.error('评论失败，请重试')
  }
}

// 回复评论
const replyComment = (comment) => {
  replyTarget.value = comment.commentId
  replyContent.value = ''
}

// 取消回复
const cancelReply = () => {
  replyTarget.value = null
  replyContent.value = ''
}

// 切换子评论的展开/收起状态
const toggleNestedComments = (commentId) => {
  expandedComments.value[commentId] = !expandedComments.value[commentId]
}

// 获取用户头像URL
const getUserAvatar = () => {
  if (loginStore.userInfo && loginStore.userInfo.avatar) {
    // 确保头像URL包含完整的后端服务地址
    if (loginStore.userInfo.avatar.startsWith('/avatar/')) {
      return '' + loginStore.userInfo.avatar
    }
    return loginStore.userInfo.avatar
  }
  // 返回默认头像
  return 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=user%20avatar%20profile%20picture&image_size=square'
}

// 提交回复
const submitReply = async (comment) => {
  if (!loginStore.userInfo.userId) {
    ElMessage.warning('请先登录后再回复')
    return
  }
  
  if (!replyContent.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  
  try {
    const result = await Request({
      url: Api.addComment,
      method: 'POST',
      params: {
        videoId: videoId.value,
        content: replyContent.value.trim(),
        userId: loginStore.userInfo.userId,
        parentId: comment.commentId
      }
    })
    
    if (result && result.success) {
      ElMessage.success('回复成功')
      replyTarget.value = null
      replyContent.value = ''
      // 重新加载评论列表
      await loadComments()
    } else {
      ElMessage.error('回复失败，请重试')
    }
  } catch (error) {
    console.error('提交回复失败:', error)
    ElMessage.error('回复失败，请重试')
  }
}

// 点赞评论
const likeComment = async (comment) => {
  if (!loginStore.userInfo.userId) {
    ElMessage.warning('请先登录后再点赞')
    return
  }
  
  try {
    const result = await Request({
      url: Api.likeComment,
      method: 'POST',
      params: {
        commentId: comment.commentId,
        videoId: videoId.value,
        userId: loginStore.userInfo.userId
      }
    })
    
    if (result && result.success) {
      // 根据后端返回的消息判断是点赞还是取消点赞
      const message = result.data || result.msg || ''
      const isCancellingLike = message.includes('取消点赞成功')
      
      // 更新点赞状态
      comment.isLiked = !isCancellingLike
      // 更新点赞数
      if (comment.isLiked) {
        comment.likeCount = (comment.likeCount || 0) + 1
      } else {
        comment.likeCount = Math.max(0, (comment.likeCount || 0) - 1)
      }
      ElMessage.success(message)
    } else {
      ElMessage.error(result.msg || '操作失败，请重试')
    }
  } catch (error) {
    console.error('点赞评论失败:', error)
    ElMessage.error('操作失败，请重试')
  }
}

// 删除评论
const deleteComment = async (comment) => {
  if (!loginStore.userInfo.userId) {
    ElMessage.warning('请先登录后再删除')
    return
  }
  
  try {
    const result = await Request({
      url: Api.deleteComment,
      method: 'POST',
      params: {
        commentId: comment.commentId,
        videoId: videoId.value,
        userId: loginStore.userInfo.userId
      }
    })
    
    if (result && result.success) {
      ElMessage.success('删除成功')
      // 重新加载评论列表
      await loadComments()
    } else {
      ElMessage.error('删除失败，请重试')
    }
  } catch (error) {
    console.error('删除评论失败:', error)
    ElMessage.error('删除失败，请重试')
  }
}

// 置顶/取消置顶评论
const toggleTopComment = async (comment) => {
  if (!loginStore.userInfo.userId) {
    ElMessage.warning('请先登录后再操作')
    return
  }
  
  // 检查是否是视频作者
  if (loginStore.userInfo.userId !== authorInfo.value.userId) {
    ElMessage.warning('只有视频作者才能置顶评论')
    return
  }
  
  try {
    // 判断是置顶还是取消置顶
    const isTopping = comment.ifTop !== 1
    const apiUrl = isTopping ? Api.topComment : Api.untopComment
    const successMessage = isTopping ? '置顶成功' : '取消置顶成功'
    
    const result = await Request({
      url: apiUrl,
      method: 'POST',
      params: {
        commentId: comment.commentId,
        videoId: videoId.value,
        userId: loginStore.userInfo.userId
      }
    })
    
    if (result && result.success) {
      ElMessage.success(successMessage)
      // 重新加载评论列表
      await loadComments()
    } else {
      ElMessage.error(result.msg || '操作失败，请重试')
    }
  } catch (error) {
    console.error('置顶评论失败:', error)
    ElMessage.error('操作失败，请重试')
  }
}

// 加载更多评论
const loadMoreComments = async () => {
  // 这里可以实现分页加载评论的逻辑
  // 暂时简单处理
  ElMessage.info('暂无更多评论')
  hasMoreComments.value = false
}

// 记录观看历史
const recordHistoryProgress = async (progress = 0, watchDuration = 0) => {
  if (!loginStore.userInfo?.userId) {
    return
  }
  
  try {
    const result = await Request({
      url: Api.addVideoHistory,
      method: 'GET',
      params: {
        videoId: videoId.value,
        userId: loginStore.userInfo.userId,
        progress: progress,
        watchDuration: watchDuration
      }
    })
    
    if (result && result.success) {
      console.log('观看历史记录成功')
    } else {
      console.log('观看历史记录失败:', result.msg)
    }
  } catch (error) {
    console.error('记录观看历史失败:', error)
  }
}

onMounted(async () => {
  console.log('VideoDetail mounted with id:', videoId.value)
  // 加载视频详情
  await loadVideoDetail()
  // 初始化DPlayer
  initDPlayer()
  // 增加视频播放量
  await increasePlayCount()
  // 加载推荐视频
  await loadRecommendedVideos()
  // 加载评论列表
  await loadComments()
  // 记录观看历史
  await recordHistoryProgress()
})

// 页面卸载时
onUnmounted(() => {
  // 清除播放速度检查定时器
  if (window.speedCheckInterval) {
    clearInterval(window.speedCheckInterval)
    window.speedCheckInterval = null
  }
  
  // 清除进度记录定时器
  if (progressRecordTimer) {
    clearInterval(progressRecordTimer)
    progressRecordTimer = null
  }
  
  // 最后记录一次观看进度
  if (dp.value && dp.value.video) {
    const progress = Math.floor(dp.value.video.currentTime)
    const watchDuration = Math.floor(dp.value.video.currentTime)
    
    if (progress > 0) {
      recordHistoryProgress(progress, watchDuration)
    }
  }
  
  // 销毁DPlayer实例
  if (dp.value) {
    dp.value.destroy()
    dp.value = null
  }
  // 清除视频上下文
  mitter.emit('video-context', null)
})
</script>

<style lang="scss" scoped>
.video-detail {
  min-height: 100vh;
  background: var(--bg-primary);
}

.main-content {
  margin-top: 60px; /* 60px header */
  margin-left: 200px; /* 200px sidebar */
  padding: 20px;
  min-height: calc(100vh - 60px);
}

.content-wrapper {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

/* 视频播放区域 */
.video-player-area {
  background-color: var(--bg-card);
  border-radius: 8px;
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}

/* 视频播放器 */
.video-player {
  width: 100%;
  background-color: #000;
  position: relative;
  aspect-ratio: 16/9;
}

.video-element {
  width: 100%;
  height: auto;
  aspect-ratio: 16/9;
}

/* DPlayer样式调整 */
#dplayer {
  width: 100%;
  height: 100%;
}

/* 弹幕列表样式 */
.danmaku-list-container {
  position: absolute;
  top: 10px;
  right: 10px;
  z-index: 100;
}

.danmaku-list-btn {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 5px 10px;
  background-color: rgba(0, 0, 0, 0.6);
  color: #fff;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  transition: background-color 0.3s;
}

.danmaku-list-btn:hover {
  background-color: rgba(0, 0, 0, 0.8);
}

.danmaku-list-popup {
  position: absolute;
  top: 100%;
  right: 0;
  margin-top: 5px;
  width: 300px;
  max-height: 400px;
  background-color: rgba(255,255,255,0.04);
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  overflow: hidden;
  z-index: 1000;
}

.danmaku-list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 15px;
  background-color: var(--bg-secondary);
  border-bottom: 1px solid var(--glass-border);
}

.danmaku-list-header h4 {
  margin: 0;
  font-size: 14px;
  font-weight: 500;
}

.danmaku-list-header .close-btn {
  cursor: pointer;
  color: #909399;
  font-size: 16px;
}

.danmaku-list-content {
  max-height: 350px;
  overflow-y: auto;
  padding: 10px 0;
}

.danmaku-item {
  padding: 10px 15px;
  border-bottom: 1px solid var(--glass-border);
  transition: background-color 0.2s;
  position: relative;
}

.danmaku-item:hover {
  background-color: var(--bg-secondary);;
}

/* 悬停时显示用户自己弹幕的删除按钮 */
.danmaku-item:hover .user-danmaku-delete-btn {
  display: inline-block !important;
}

.danmaku-info {
  display: flex;
  gap: 10px;
  font-size: 11px;
  color: #909399;
  margin-bottom: 5px;
}

.danmaku-content {
  font-size: 13px;
  line-height: 1.4;
  word-break: break-word;
}

.empty-danmaku {
  text-align: center;
  padding: 20px;
  color: #909399;
  font-size: 13px;
}

/* 确保弹幕容器在全屏模式下正常显示 */
#dplayer .dplayer-video-wrap {
  position: relative;
  z-index: 1;
}

#dplayer .dplayer-danmaku {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 2;
  pointer-events: auto;
}

/* 全屏模式下的样式 */
#dplayer:-webkit-full-screen {
  width: 100% !important;
  height: 100% !important;
}

#dplayer:-moz-full-screen {
  width: 100% !important;
  height: 100% !important;
}

#dplayer:-ms-fullscreen {
  width: 100% !important;
  height: 100% !important;
}

#dplayer:fullscreen {
  width: 100% !important;
  height: 100% !important;
}

/* 确保弹幕在全屏模式下的层级 */
#dplayer:fullscreen .dplayer-danmaku {
  z-index: 10;
  pointer-events: auto;
}

/* 确保控制栏在全屏模式下正常显示 */
#dplayer .dplayer-controller {
  position: relative;
  z-index: 100;
  opacity: 1;
  transition: opacity 0.3s ease;
}

#dplayer:fullscreen .dplayer-controller {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 100;
  opacity: 1;
  transition: opacity 0.3s ease;
}

/* 鼠标悬停在视频任意位置时显示控制栏 */
#dplayer:fullscreen:hover .dplayer-controller {
  opacity: 1;
}

/* 控制栏隐藏时的样式 */
#dplayer .dplayer-controller.dplayer-controller-hide {
  opacity: 0;
}

/* 增加控制栏的检测区域，确保鼠标移动到底部时能触发显示 */
#dplayer:fullscreen .dplayer-video-wrap {
  position: relative;
  height: 100%;
}

/* 底部热区，确保鼠标移动到底部时能触发控制栏显示 */
#dplayer:fullscreen .dplayer-video-wrap::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 100px;
  z-index: 50;
  pointer-events: none;
}

/* 确保控制栏不会被其他元素覆盖 */
#dplayer .dplayer-video-wrap {
  z-index: 1;
}

#dplayer .dplayer-danmaku {
  z-index: 10;
}

#dplayer .dplayer-controller {
  z-index: 100;
}

/* 完全隐藏弹幕错误提示 */
#dplayer .dplayer-notice {
  display: none !important;
  visibility: hidden !important;
  opacity: 0 !important;
}

#dplayer .dplayer-notice.dplayer-notice-error {
  display: none !important;
  visibility: hidden !important;
  opacity: 0 !important;
}

/* 隐藏所有通知 */
.dplayer-notice {
  display: none !important;
}

/* 视频信息区域 */
.video-info {
  padding: 20px;
}

.video-title {
  font-size: 20px;
  font-weight: bold;
  margin-bottom: 12px;
  color: var(--text-primary);
}

.video-stats {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
  color: var(--text-dim);
  font-size: 14px;
}

/* 视频标签样式 */
.video-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin: 16px 0;
}

.video-tag {
  display: inline-block;
  padding: 4px 12px;
  background-color: #f0f9eb;
  color: #67c23a;
  border-radius: 16px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.3s;
}

.video-tag:hover {
  background-color: #e1f3d8;
}

/* 作者信息和互动按钮 */
.author-interaction {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 0;
  border-top: 1px solid #eee;
  border-bottom: 1px solid var(--glass-border);
}

.author-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.author-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  overflow: hidden;
}

.author-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.author-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.author-name {
  font-weight: 500;
  color: var(--text-primary);
}

.author-fans {
  font-size: 12px;
  color: var(--text-dim);
}

.follow-btn {
  padding: 6px 16px;
  font-size: 13px;
  border: 1px solid #409eff;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s;
  background-color: var(--bg-card);
  color: #409eff;
}

.follow-btn:hover {
  background-color: #ecf5ff;
}

.follow-btn.followed {
  background-color: rgba(255,255,255,0.06);
  color: var(--text-secondary);
  border-color: #dcdfe6;
}

.follow-btn.followed:hover {
  background-color: rgba(255,255,255,0.08);
}

/* 互动按钮 */
.interaction-buttons {
  display: flex;
  gap: 24px;
}

.interaction-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  padding: 8px;
  border-radius: 4px;
  transition: all 0.2s ease;
}

.interaction-btn .el-icon {
  color: var(--text-secondary);
  transition: all 0.2s ease;
}

.interaction-btn:hover .el-icon {
  color: var(--color-primary);
}

.interaction-btn:hover {
  background-color: rgba(255,255,255,0.06);
}

.interaction-btn span {
  font-size: 12px;
  color: var(--text-secondary);
}

/* 已点赞状态 */
.interaction-btn .el-icon.liked {
  color: var(--color-primary);
}

/* 视频描述 */
.video-description {
  padding: 20px;
  color: var(--text-primary);
  line-height: 1.6;
}

/* 右侧推荐视频 */
.recommended-videos {
  background-color: var(--bg-card);
  border-radius: 8px;
  padding: 16px;
  box-shadow: var(--shadow-sm);
}

.recommended-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 16px;
  color: var(--text-primary);
}

.recommended-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.recommended-item {
  display: flex;
  gap: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
  padding: 8px;
  border-radius: 4px;
}

.recommended-item:hover {
  background-color: rgba(255,255,255,0.06);
}

.recommended-cover {
  position: relative;
  width: 120px;
  height: 68px;
  flex-shrink: 0;
}

.recommended-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 4px;
}

.video-duration {
  position: absolute;
  bottom: 4px;
  right: 4px;
  background-color: rgba(0, 0, 0, 0.7);
  color: white;
  font-size: 10px;
  padding: 2px 4px;
  border-radius: 2px;
}

.recommended-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.recommended-info .recommended-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  margin-bottom: 0;
}

.recommended-author {
  font-size: 12px;
  color: var(--text-dim);
}

.recommended-stats {
  font-size: 12px;
  color: var(--text-dim);
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .content-wrapper {
    grid-template-columns: 1fr;
  }
  
  .recommended-videos {
    order: -1;
  }
}

@media (max-width: 992px) {
  .main-content {
    margin-left: 60px; /* 60px sidebar (collapsed) */
  }
}

@media (max-width: 768px) {
  .main-content {
    margin-left: 0; /* 导航栏隐藏 */
    padding: 12px;
  }
  
  .author-interaction {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .interaction-buttons {
    width: 100%;
    justify-content: space-around;
  }
}

/* 投币弹窗样式 */
.coin-dialog-content {
  text-align: center;
}

.coin-dialog-title {
  margin-bottom: 24px;
  color: var(--text-primary);
}

.coin-options {
  display: flex;
  justify-content: space-around;
  margin-bottom: 24px;
}

.coin-option {
  width: 120px;
  height: 150px;
  border: 2px dashed #ccc;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.coin-option:hover {
  border-color: var(--color-primary);
  background-color: #f0f9ff;
}

.coin-option.selected {
  border-color: var(--color-primary);
  border-style: solid;
  background-color: rgba(147, 112, 219, 0.15);
}

.coin-icon {
  margin-bottom: 12px;
}

.coin-text {
  font-size: 14px;
  color: var(--text-secondary);
}

.coin-option.selected .coin-text {
  color: var(--color-primary);
  font-weight: bold;
}

.like-checkbox {
  margin-bottom: 24px;
  text-align: left;
}

.coin-dialog-footer {
  text-align: center;
}

/* 投币状态样式 */
.coined {
  color: var(--color-primary) !important;
}

/* 已收藏状态 */
.interaction-btn .el-icon.collected {
  color: var(--color-primary);
}

/* 收藏夹弹窗样式 */
.collect-dialog-content {
  max-height: 400px;
  overflow-y: auto;
}

.collect-folder-list {
  margin-bottom: 20px;
}

.collect-folder-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border: 1px solid var(--glass-border);
  border-radius: 4px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.collect-folder-item:hover {
  background-color: var(--bg-secondary);
}

.collect-folder-item.selected {
  background-color: rgba(147, 112, 219, 0.15);
  border-color: var(--color-primary-light);
}

.collect-folder-item .folder-info {
  display: flex;
  align-items: center;
  flex: 1;
}

.collect-folder-item .folder-info .el-icon {
  margin-right: 8px;
  color: var(--color-primary-light);
}

.collect-folder-item .folder-name {
  flex: 1;
  font-size: 14px;
  color: var(--text-primary);
}

.collect-folder-item .folder-count {
  font-size: 12px;
  color: var(--text-dim);
  margin-right: 16px;
}

.collect-folder-item .folder-actions {
  display: flex;
  align-items: center;
}

.collect-folder-item .edit-btn {
  color: var(--text-secondary);
  padding: 4px;
}

.collect-folder-item .edit-btn:hover {
  color: var(--color-primary-light);
}

.collect-actions {
  display: flex;
  justify-content: center;
  padding-top: 10px;
}

.collect-actions .create-folder-btn {
  width: 100%;
}

.collected {
  color: var(--color-primary) !important;
}

/* 评论区域样式 */
.comment-section {
  padding: 20px;
  border-top: 1px solid #eee;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.comment-header h3 {
  font-size: 18px;
  font-weight: bold;
  color: var(--text-primary);
  margin: 0;
}

.comment-sort {
  display: flex;
  gap: 12px;
}

.sort-btn {
  padding: 6px 12px;
  border: 1px solid var(--glass-border);
  border-radius: 4px;
  background-color: rgba(255,255,255,0.04);
  cursor: pointer;
  transition: all 0.2s ease;
}

.sort-btn:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}

.sort-btn.active {
  background-color: var(--color-primary);
  color: #fff;
  border-color: var(--color-primary);
}

/* 评论输入区域 */
.comment-input-area {
  display: flex;
  gap: 12px;
  margin-bottom: 30px;
  padding: 16px;
  background-color: var(--bg-secondary);;
  border-radius: 8px;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.input-container {
  flex: 1;
}

.input-container .el-textarea {
  margin-bottom: 10px;
}

.comment-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.submit-btn {
  padding: 8px 16px;
  background-color: var(--color-primary);
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.submit-btn:hover:not(:disabled) {
  background-color: #00b5e5;
}

.submit-btn:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

/* 评论列表 */
.comment-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.comment-item {
  display: flex;
  gap: 12px;
  padding: 16px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.comment-item.topped {
  background-color: rgba(212, 175, 55, 0.1);
  border: 1px solid #ffd591;
}

.comment-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
}

.comment-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.comment-content-wrapper {
  flex: 1;
}

.comment-header-info {
  display: flex;
  gap: 12px;
  margin-bottom: 8px;
  align-items: center;
}

.top-badge {
  background-color: #faad14;
  color: white;
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 2px;
  font-weight: 500;
}

.comment-username {
  font-weight: 500;
  color: var(--text-primary);
  font-size: 14px;
}

.comment-time {
  font-size: 12px;
  color: var(--text-dim);
}

.comment-text {
  margin-bottom: 12px;
  line-height: 1.5;
  color: var(--text-primary);
}

.comment-actions {
  display: flex;
  gap: 20px;
}

.comment-action-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  background: none;
  border: none;
  color: var(--text-dim);
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.comment-action-btn:hover {
  color: var(--color-primary);
}

.comment-action-btn.liked {
  color: var(--color-primary);
}

.sub-comment-action-btn.liked {
  color: var(--color-primary);
}

.nested-sub-comment-action-btn.liked {
  color: var(--color-primary);
}

.comment-action-btn.topped {
  color: #faad14;
}

.comment-action-btn.delete-btn:hover {
  color: #ff4d4f;
}

/* 回复输入区域 */
.reply-input-area {
  margin-top: 12px;
  margin-left: 48px;
  padding: 12px;
  background-color: var(--bg-secondary);
  border-radius: 6px;
}

.reply-input-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.reply-input-container .el-textarea {
  margin-bottom: 0;
}

.reply-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.cancel-btn {
  padding: 6px 12px;
  background-color: rgba(255,255,255,0.04);
  color: var(--text-secondary);
  border: 1px solid var(--glass-border);
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.cancel-btn:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}

/* 子评论列表 */
.sub-comment-list {
  margin-top: 12px;
  margin-left: 48px;
  display: flex;
  flex-direction: column;
  gap: 12px;
.sub-comment-list {
  margin-top: 15px;
  padding-left: 50px;
}

.sub-comment-content {
  margin-top: 10px;
}

.nested-sub-comment-content {
  margin-top: 10px;
}  display: flex;
  gap: 10px;
}

/* 嵌套子评论 */
.nested-sub-comments {
  margin-top: 15px;
  padding-left: 50px;
}

.nested-sub-comment-item {
  margin-top: 10px;
  display: flex;
  gap: 8px;
}

.nested-sub-comment-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
}

.nested-sub-comment-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.nested-sub-comment-content-wrapper {
  flex: 1;
  background-color: var(--bg-secondary);;
  padding: 10px;
  border-radius: 8px;
}

.nested-sub-comment-header-info {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 5px;
}

.nested-sub-comment-username {
  font-size: 12px;
  font-weight: 500;
  color: var(--text-primary);
}

.nested-sub-comment-time {
  font-size: 10px;
  color: var(--text-dim);
}

.nested-sub-comment-text {
  font-size: 12px;
  line-height: 1.4;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.nested-sub-comment-actions {
  display: flex;
  gap: 15px;
  align-items: center;
}

.nested-sub-comment-action-btn {
  font-size: 10px;
  color: var(--text-secondary);
  background: none;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 2px;
  padding: 0;
}

.nested-sub-comment-action-btn:hover {
  color: var(--color-primary);
}

.nested-sub-comment-action-btn.liked {
  color: #ff6b6b;
}

.nested-sub-comment-action-btn.topped {
  color: #faad14;
}

.nested-sub-comment-action-btn.delete-btn {
  color: #ff6b6b;
}

/* 查看更多按钮 */
.view-more-comments {
  margin-top: 10px;
  text-align: left;
  padding-left: 32px;
}

.view-more-btn {
  font-size: 12px;
  color: var(--color-primary);
  background: none;
  border: none;
  cursor: pointer;
  padding: 0;
}

.view-more-btn:hover {
  text-decoration: underline;
}

.sub-comment-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
}

.sub-comment-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.sub-comment-content-wrapper {
  flex: 1;
}

.sub-comment-header-info {
  display: flex;
  gap: 8px;
  margin-bottom: 6px;
  align-items: center;
}

.sub-comment-username {
  font-weight: 500;
  color: var(--text-primary);
  font-size: 13px;
}

.sub-comment-time {
  font-size: 11px;
  color: var(--text-dim);
}

.sub-comment-text {
  margin-bottom: 8px;
  line-height: 1.4;
  color: var(--text-primary);
  font-size: 14px;
}

.sub-comment-actions {
  display: flex;
  gap: 16px;
}

.sub-comment-action-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  background: none;
  border: none;
  color: var(--text-dim);
  font-size: 11px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.sub-comment-action-btn:hover {
  color: var(--color-primary);
}

.sub-comment-action-btn.liked {
  color: var(--color-primary);
}

.sub-comment-action-btn.topped {
  color: #faad14;
}

.sub-comment-action-btn.delete-btn:hover {
  color: #ff4d4f;
}

/* 加载更多 */
.load-more-comments {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.load-more-btn {
  padding: 8px 20px;
  background-color: rgba(255,255,255,0.04);
  color: var(--text-secondary);
  border: 1px solid var(--glass-border);
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.load-more-btn:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}

/* 无评论提示 */
.no-comments {
  text-align: center;
  padding: 40px 20px;
  color: var(--text-dim);
  background-color: var(--bg-secondary);;
  border-radius: 8px;
  margin: 20px 0;
}

.no-comments p {
  margin: 0;
  font-size: 14px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .comment-section {
    padding: 12px;
  }
  
  .comment-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .comment-input-area {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .user-avatar {
    align-self: flex-start;
  }
  
  .reply-input-area {
    margin-left: 0;
  }
  
  .sub-comment-list {
    margin-left: 0;
  }
}
</style>
