<template>
  <div class="data-load-more-list">
    <!-- 数据列表 -->
    <slot v-for="item in dataSource" :key="item.videoId" :data="item"></slot>
    
    <!-- 加载更多 -->
    <div class="load-more-container">
      <el-button 
        type="primary" 
        :loading="loading" 
        @click="handleLoadMore" 
        :disabled="loading"
      >
        {{ loading ? '加载中...' : '加载更多' }}
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue'
import { ElButton } from 'element-plus'

// 定义组件属性
const props = defineProps({
  dataSource: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  }
})

// 定义事件
const emit = defineEmits(['loadData'])

// 处理加载更多
const handleLoadMore = () => {
  emit('loadData')
}
</script>

<style scoped>
.data-load-more-list {
  padding: 10px;
}

.load-more-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  margin-bottom: 20px;
}
</style>
