<template>
  <div class="video-item">
    <div class="video-cover">
      <img :src="data?.videoCover || '/placeholder.jpg'" alt="视频封面">
    </div>
    <div class="video-info">
      <h3 class="video-title">{{ data?.videoName || '未命名视频' }}</h3>
      <div class="video-meta">
        <span v-if="data?.viewCount">播放 {{ data.viewCount }}</span>
        <span v-if="data?.publishTime">发布于 {{ data.publishTime }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { defineProps } from 'vue'

// 定义组件属性
const props = defineProps({
  data: {
    type: Object,
    required: true
  },
  marginTop: {
    type: Number,
    default: 0
  }
})
</script>

<style scoped>
.video-item {
  margin-top: v-bind(marginTop + 'px');
  cursor: pointer;
}

.video-cover {
  width: 100%;
  height: 0;
  padding-bottom: 56.25%; /* 16:9 宽高比 */
  position: relative;
  overflow: hidden;
  background-color: var(--bg-tertiary);
  border-radius: var(--radius-md) var(--radius-md) 0 0;
}

.video-cover img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.video-info {
  padding: 8px;
}

.video-title {
  font-size: 14px;
  font-weight: 500;
  margin: 0 0 4px 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.video-meta {
  font-size: 12px;
  color: var(--text-dim);
  display: flex;
  gap: 12px;
}
</style>
