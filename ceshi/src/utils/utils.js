// 工具类
const Utils = {
  // 获取本地图片路径
  getLocalImage(fileName) {
    // 假设图片存放在public/images目录下
    return `/images/${fileName}`;
  },
  // 页面边距
  bodyPadding: 20,
  // 轮播最多显示的视频数
  carouselMaxCount: 3
};

// 热门排行综合评分权重
const HOT_WEIGHTS = {
  playCount: 1,
  likeCount: 3,
  followCount: 5,
  commentCount: 2,
  coinsInserted: 10
}

const computeHotScore = (video) => {
  const playCount = parseInt(video.playCount) || 0
  const likeCount = parseInt(video.likeCount) || 0
  const followCount = parseInt(video.followCount) || 0
  const commentCount = parseInt(video.commentCount) || 0
  const coinsInserted = parseInt(video.coinsInserted) || 0

  return (
    playCount * HOT_WEIGHTS.playCount +
    likeCount * HOT_WEIGHTS.likeCount +
    followCount * HOT_WEIGHTS.followCount +
    commentCount * HOT_WEIGHTS.commentCount +
    coinsInserted * HOT_WEIGHTS.coinsInserted
  )
}

export default Utils
export { HOT_WEIGHTS, computeHotScore }