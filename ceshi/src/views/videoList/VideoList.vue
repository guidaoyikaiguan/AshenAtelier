<template>
  <div class="data-list">
    <DataLoadMoreList :dataSource="dataSource" :loading="loadingData" @loadData="loadDataList">
      <template #default="{ data }">
        <VideoItem :data="data" :marginTop="20" ref="videoItemRef"></VideoItem>
      </template>
    </DataLoadMoreList>
  </div>
</template>

<script setup>
import { useCategoryStore } from '@/stores/categoryStore.js'
const categoryStore = useCategoryStore()
import VideoItem from '@/components/VideoItem.vue'
import DataLoadMoreList from '@/components/DataLoadMoreList.vue'

import { mitter } from '@/eventbus/eventBus.js'
import {
  ref,
  reactive,
  nextTick,
  watch,
  inject,
  onMounted,
  onUnmounted,
} from 'vue'
import { useRoute, useRouter } from 'vue-router'
const route = useRoute()
const router = useRouter()

// 注入全局工具
const Request = inject('Request')
const Api = inject('Api')
const Utils = inject('Utils')

// 定义props
const props = defineProps({
  categoryId: {
    type: String,
    default: ''
  }
})

const categoryMap = categoryStore.categoryMap
const categoryIdInfo = ref({})

const convertCode2Id = (pCategoryCode, categoryCode) => {
  let pCategoryId = ''
  let categoryId = ''
  if (pCategoryCode) {
    pCategoryId = categoryMap[pCategoryCode]
      ? categoryMap[pCategoryCode].categoryId
      : ''
  }
  if (categoryCode) {
    categoryId = categoryMap[categoryCode]
      ? categoryMap[categoryCode].categoryId
      : ''
  }
  categoryIdInfo.value = {
    pCategoryId,
    categoryId,
  }
}
const loadingData = ref(false)
const dataSource = ref({ list: [], pageNo: 1 })
const loadDataList = async () => {
  let params = {
    categoryId: props.categoryId
  }
  loadingData.value = true
  console.log('Loading videos by category, categoryId:', props.categoryId)
  let result = await Request({
    url: Api.loadVideoByCategory,
    params,
  })
  loadingData.value = false
  if (!result) {
    return
  }
  // 处理后端返回的数据格式
  const videoList = result.data || []
  dataSource.value = {
    list: videoList,
    pageNo: 1
  }
}

const initData = () => {
  if (props.categoryId) {
    // 如果通过props传递了categoryId，直接使用它
    console.log('Initializing with categoryId:', props.categoryId)
    loadDataList()
  } else {
    // 否则从路由参数获取
    console.log('Initializing from route params:', route.params)
  }
}

// 监听路由参数变化
watch(
  () => route.params,
  (newVal, oldVal) => {
    if (newVal) {
      initData()
    }
  },
  { immediate: true, deep: true }
)

// 监听props.categoryId变化
watch(
  () => props.categoryId,
  (newVal, oldVal) => {
    if (newVal) {
      // 当categoryId变化时，重置页码并重新加载数据
      dataSource.value = { list: [], pageNo: 1 }
      initData()
    }
  },
  { immediate: true }
)
</script>

<style lang="scss" scoped>
.video-list-page { background: var(--bg-primary); min-height: 100vh; }
</style>
