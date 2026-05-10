<template>
  <div v-if="visible" class="mascot-container" :style="containerStyle">
    <MascotChat
      ref="chatRef"
      :visible="chatVisible"
      @close="chatVisible = false"
      @send="handleChatSend"
    />

    <div
      class="mascot-body"
      :class="{ idle: !chatVisible && !isDragging, dragging: isDragging }"
      @pointerdown="onPointerDown"
      @click="onClick"
    >
      <img
        v-if="!imgFailed"
        :src="`/mascot/${expression}.png`"
        class="mascot-img"
        :class="{ idle: !chatVisible && !isDragging }"
        @error="imgFailed = true"
        draggable="false"
        alt="伊蕾娜"
      />
      <div v-else class="mascot-placeholder" :class="expression">
        <div class="placeholder-body">
          <div class="witch-hat">
            <div class="hat-brim"></div>
            <div class="hat-cone"></div>
          </div>
          <div class="face">
            <div class="eyes">
              <span class="eye left"></span>
              <span class="eye right"></span>
            </div>
            <div class="mouth"></div>
          </div>
          <div class="robe"></div>
        </div>
        <span class="placeholder-label">{{ expressionLabel }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, inject } from 'vue'
import MascotChat from './MascotChat.vue'
import { mitter } from '@/eventbus/eventBus'
import { ElMessage } from 'element-plus'

const Request = inject('Request')
const Api = inject('Api')

const POS_KEY = 'mascot_pet_position'

// ---- state ----
const visible = ref(true)
const chatVisible = ref(false)
const chatRef = ref(null)
const expression = ref('default')
const isDragging = ref(false)
const imgFailed = ref(false)
const chatHistory = ref([])
const position = reactive({ x: 30, y: 80 })
const activeAudio = ref(null)  // keep reference to prevent GC
const videoContext = ref(null)  // current video { videoId, title, description, tags }
const videoTranscript = ref('')  // cached transcript

const expressionLabel = computed(() => {
  const labels = {
    default: '伊蕾娜', happy: '开心', angry: '生气', sad: '难过',
    surprised: '惊讶', shy: '害羞', thinking: '思考中'
  }
  return labels[expression.value] || '伊蕾娜'
})

const containerStyle = computed(() => ({
  right: `${position.x}px`,
  bottom: `${position.y}px`
}))

// ---- drag ----
let dragInfo = null

const onPointerDown = (e) => {
  dragInfo = {
    startX: e.clientX,
    startY: e.clientY,
    origRight: position.x,
    origBottom: position.y,
    moved: false
  }
  window.addEventListener('pointermove', onPointerMove)
  window.addEventListener('pointerup', onPointerUp)
}

const onPointerMove = (e) => {
  if (!dragInfo) return
  const dx = dragInfo.startX - e.clientX
  const dy = dragInfo.startY - e.clientY
  if (Math.abs(dx) > 2 || Math.abs(dy) > 2) {
    dragInfo.moved = true
    isDragging.value = true
  }
  if (dragInfo.moved) {
    const w = window.innerWidth
    const h = window.innerHeight
    position.x = Math.max(10, Math.min(w - 140, dragInfo.origRight + dx))
    position.y = Math.max(10, Math.min(h - 340, dragInfo.origBottom + dy))
  }
}

const onPointerUp = () => {
  window.removeEventListener('pointermove', onPointerMove)
  window.removeEventListener('pointerup', onPointerUp)
  if (dragInfo && dragInfo.moved) {
    try { localStorage.setItem(POS_KEY, JSON.stringify({ x: position.x, y: position.y })) } catch (_) {}
  }
  dragInfo = null
  setTimeout(() => { isDragging.value = false }, 50)
}

const onClick = () => {
  if (dragInfo && dragInfo.moved) return
  chatVisible.value = !chatVisible.value
}

// ---- chat ----
const handleChatSend = async (message) => {
  expression.value = 'thinking'
  try {
    const response = await fetch('/api/ai/mascot/chat', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        message,
        conversation_history: chatHistory.value.slice(-10),
        video_context: videoContext.value || null,
        transcript: videoTranscript.value || null
      })
    })

    if (!response.ok) throw new Error('Network error')

    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''
    let fullReply = ''
    let fullReplyJa = ''
    let currentMood = 'default'
    let ttsAudio = null

    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() || ''
      for (const line of lines) {
        if (line.startsWith('data: ')) {
          try {
            const data = JSON.parse(line.slice(6))
            if (data.type === 'mood') {
              currentMood = data.expression
              expression.value = currentMood
              fullReplyJa = data.reply_ja || ''
              if (fullReplyJa) {
                if (activeAudio.value) { activeAudio.value.pause(); activeAudio.value.src = '' }
                ttsAudio = new Audio(`/api/ai/mascot/tts?text=${encodeURIComponent(fullReplyJa)}&text_lang=ja&mood=${currentMood}`)
                ttsAudio.volume = 0.7
                ttsAudio.preload = 'auto'
                activeAudio.value = ttsAudio
              }
            } else if (data.type === 'token') {
              fullReply += data.content
            } else if (data.type === 'error') {
              ElMessage.error('伊蕾娜暂时无法回应...')
            }
          } catch (_) {}
        }
      }
    }

    // Wait for TTS to be ready, then play synced
    if (ttsAudio && fullReplyJa) {
      try {
        await new Promise((resolve, reject) => {
          ttsAudio.oncanplaythrough = () => resolve()
          ttsAudio.onerror = () => reject(new Error('audio failed'))
          // Timeout after 60s — give up on audio
          setTimeout(() => resolve(), 60000)
        })
        await typeWithAudio(fullReply, ttsAudio, currentMood)
      } catch (_) {
        await typeWithoutAudio(fullReply, currentMood)
      }
    } else {
      await typeWithoutAudio(fullReply, currentMood)
    }

    expression.value = 'default'

    chatHistory.value.push({ role: 'user', content: message })
    chatHistory.value.push({ role: 'assistant', content: fullReply })
  } catch (e) {
    chatRef.value?.addMascotMessage('default', '抱歉旅人...我的魔法好像出了点问题，等会儿再试试吧~')
    expression.value = 'sad'
  }
}

// ---- synced typing ----
const PUNCTUATIONS = /[,.!?，。！？、；：~]/

const typeWithAudio = async (text, audio, mood) => {
  const duration = audio.duration || (text.length * 0.15)
  let totalWeight = 0
  for (let i = 0; i < text.length; i++) {
    totalWeight += PUNCTUATIONS.test(text[i]) ? 4 : 1
  }
  const msPerWeight = (duration * 1000) / totalWeight

  audio.play().catch(() => {})
  for (let i = 0; i < text.length; i++) {
    const charDelay = PUNCTUATIONS.test(text[i]) ? msPerWeight * 4 : msPerWeight
    chatRef.value?.appendStreamToken(text[i])
    await new Promise(r => setTimeout(r, charDelay))
  }
  chatRef.value?.addMascotMessage(mood, text)
}

const typeWithoutAudio = async (text, mood) => {
  const charDelay = Math.min(50, (text.length * 150) / text.length)
  for (let i = 0; i < text.length; i++) {
    chatRef.value?.appendStreamToken(text[i])
    await new Promise(r => setTimeout(r, charDelay))
  }
  chatRef.value?.addMascotMessage(mood, text)
}

// ---- transcript ----
const fetchTranscript = async (videoId) => {
  try {
    const resp = await fetch(`/api/ai/video/transcript?video_id=${videoId}`, {
      method: 'POST'
    })
    if (!resp.ok) return
    const data = await resp.json()
    if (data.data?.transcript) {
      videoTranscript.value = data.data.transcript
    }
  } catch (_) {}
}

// ---- lifecycle ----
const loadPosition = () => {
  try {
    const raw = localStorage.getItem(POS_KEY)
    if (raw) {
      const { x, y } = JSON.parse(raw)
      if (typeof x === 'number') position.x = x
      if (typeof y === 'number') position.y = y
    }
  } catch (_) {}
}

onMounted(() => {
  loadPosition()
  mitter.on('toggle-mascot', () => { visible.value = !visible.value })
  mitter.on('video-context', (ctx) => {
    videoContext.value = ctx
    if (ctx && ctx.videoId) {
      fetchTranscript(ctx.videoId)
    } else {
      videoTranscript.value = ''
    }
  })
})

onUnmounted(() => {
  mitter.off('toggle-mascot')
  mitter.off('video-context')
})
</script>

<style scoped>
.mascot-container {
  position: fixed;
  z-index: 90;
  pointer-events: none;
}

.mascot-container > .mascot-body {
  pointer-events: auto;
}

.mascot-body {
  cursor: pointer;
  user-select: none;
  -webkit-user-drag: none;
  position: relative;
  z-index: 0;
}

.mascot-img {
  height: 280px;
  width: auto;
  user-select: none;
  -webkit-user-drag: none;
  transition: opacity 0.25s ease;
}

.mascot-img.idle {
  animation: mascotFloat 3s ease-in-out infinite, mascotBreathe 4s ease-in-out infinite;
}

.mascot-body.dragging {
  cursor: grabbing;
}

.mascot-body.dragging .mascot-img {
  animation: none;
  filter: brightness(1.15);
}

@keyframes mascotFloat {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

@keyframes mascotBreathe {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.025); }
}

/* ---- placeholder ---- */
.mascot-placeholder {
  width: 140px;
  height: 200px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-end;
  position: relative;
}

.mascot-placeholder.idle {
  animation: mascotFloat 3s ease-in-out infinite, mascotBreathe 4s ease-in-out infinite;
}

.placeholder-body {
  width: 100px;
  height: 170px;
  position: relative;
}

.witch-hat {
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
}

.hat-cone {
  width: 0;
  height: 0;
  border-left: 28px solid transparent;
  border-right: 28px solid transparent;
  border-bottom: 55px solid #7B5EC7;
  position: relative;
  margin: 0 auto;
}

.hat-brim {
  width: 72px;
  height: 8px;
  background: #6A4FB8;
  border-radius: 4px;
  margin: 0 auto;
  position: relative;
  top: -8px;
}

.face {
  position: absolute;
  top: 48px;
  left: 50%;
  transform: translateX(-50%);
  width: 50px;
  height: 50px;
  background: #f5e6d3;
  border-radius: 50%;
}

.eyes {
  display: flex;
  justify-content: center;
  gap: 12px;
  padding-top: 16px;
}

.eye {
  display: block;
  width: 6px;
  height: 7px;
  background: #333;
  border-radius: 50%;
}

.mascot-placeholder.happy .eye,
.mascot-placeholder.surprised .eye {
  width: 7px;
  height: 7px;
}

.mascot-placeholder.angry .eyes {
  gap: 8px;
}

.mascot-placeholder.angry .eye {
  background: #c0392b;
}

.mascot-placeholder.sad .eye {
  height: 5px;
}

.mascot-placeholder.surprised .eye {
  width: 8px;
  height: 8px;
}

.mascot-placeholder.shy .face {
  background: #fce4d6;
}

.mouth {
  width: 12px;
  height: 5px;
  border-bottom: 2px solid #c99;
  border-radius: 0 0 8px 8px;
  margin: 6px auto 0;
}

.mascot-placeholder.happy .mouth {
  width: 14px;
  height: 7px;
  border-bottom-width: 2.5px;
}

.mascot-placeholder.angry .mouth {
  border-bottom: none;
  border-top: 2px solid #c0392b;
  border-radius: 8px 8px 0 0;
  margin-top: 8px;
}

.mascot-placeholder.sad .mouth {
  transform: rotate(180deg);
  border-bottom-color: #88a;
}

.mascot-placeholder.surprised .mouth {
  width: 9px;
  height: 9px;
  border-radius: 50%;
  border: 2px solid #c99;
  background: transparent;
  border-bottom: none;
  margin-top: 4px;
}

.mascot-placeholder.shy .mouth {
  width: 10px;
  border-bottom-color: #f8a;
}

.mascot-placeholder.thinking .eyes {
  transform: translateY(-2px);
}

.mascot-placeholder.thinking .mouth {
  border-bottom-color: #99b;
  width: 10px;
  margin-left: 8px;
}

.robe {
  position: absolute;
  top: 90px;
  left: 50%;
  transform: translateX(-50%);
  width: 70px;
  height: 80px;
  background: linear-gradient(180deg, #7B5EC7 0%, #5A3FA0 100%);
  border-radius: 18px 18px 4px 4px;
}

.placeholder-label {
  font-size: 11px;
  color: var(--text-muted, #909399);
  margin-top: 4px;
  text-align: center;
}

/* ---- media ---- */
@media (max-width: 768px) {
  .mascot-img { height: 180px; }
  .mascot-placeholder { width: 100px; height: 150px; }
  .placeholder-body { transform: scale(0.7); transform-origin: center bottom; }
}
</style>
