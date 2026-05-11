<template>
  <div class="mascot-chat" :class="{ visible }" :style="chatPosition">
    <div class="chat-header" @pointerdown="onHeaderDown">
      <span class="chat-title">与伊蕾娜对话</span>
      <el-icon class="chat-close" @click.stop="$emit('close')"><Close /></el-icon>
    </div>
    <div class="chat-messages" ref="messagesRef">
      <div class="chat-welcome" v-if="messages.length === 0">
        <p>旅人你好~ 我是灰之魔女伊蕾娜。</p>
        <p>有什么想聊的尽管说吧！</p>
      </div>
      <div v-for="(msg, i) in messages" :key="i" :class="['chat-msg', msg.role]">
        <div class="msg-content">{{ msg.content }}</div>
      </div>
      <div v-if="streaming" class="chat-msg mascot streaming">
        <div class="msg-content">{{ streamingText }}<span class="cursor">|</span></div>
      </div>
    </div>
    <div class="chat-input">
      <div class="mic-btn" :class="{ recording }" @click="toggleRecording" :title="recording ? '点击停止' : '语音输入'">
        <el-icon><Microphone /></el-icon>
      </div>
      <el-input
        v-model="inputText"
        placeholder="想说些什么..."
        @keyup.enter="sendMessage"
        :disabled="streaming || recording"
      />
      <el-button type="primary" size="small" @click="sendMessage" :disabled="streaming || !inputText.trim()">
        <el-icon><Promotion /></el-icon>
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, nextTick, watch, onUnmounted } from 'vue'
import { Close, Microphone, Promotion } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  visible: Boolean
})

const emit = defineEmits(['close', 'send'])

const inputText = ref('')
const messages = ref([])
const streaming = ref(false)
const streamingText = ref('')
const messagesRef = ref(null)
const position = reactive({ right: 30, bottom: 320 })

const chatPosition = computed(() => ({
  right: `${position.right}px`,
  bottom: `${position.bottom}px`
}))

// ---- drag ----
let dragInfo = null

const onHeaderDown = (e) => {
  dragInfo = {
    startX: e.clientX,
    startY: e.clientY,
    origRight: position.right,
    origBottom: position.bottom
  }
  window.addEventListener('pointermove', onHeaderMove)
  window.addEventListener('pointerup', onHeaderUp)
}

const onHeaderMove = (e) => {
  if (!dragInfo) return
  const dx = dragInfo.startX - e.clientX
  const dy = dragInfo.startY - e.clientY
  const w = window.innerWidth
  const h = window.innerHeight
  position.right = Math.max(0, Math.min(w - 320, dragInfo.origRight + dx))
  position.bottom = Math.max(0, Math.min(h - 80, dragInfo.origBottom + dy))
}

const onHeaderUp = () => {
  window.removeEventListener('pointermove', onHeaderMove)
  window.removeEventListener('pointerup', onHeaderUp)
  dragInfo = null
}

onUnmounted(() => {
  window.removeEventListener('pointermove', onHeaderMove)
  window.removeEventListener('pointerup', onHeaderUp)
})

const sendMessage = () => {
  const text = inputText.value.trim()
  if (!text || streaming.value) return
  messages.value.push({ role: 'user', content: text })
  inputText.value = ''
  streaming.value = true
  streamingText.value = ''
  emit('send', text)
  nextTick(scrollToBottom)
}

const addMascotMessage = (mood, reply) => {
  messages.value.push({ role: 'mascot', content: reply, mood })
  streaming.value = false
  streamingText.value = ''
  nextTick(scrollToBottom)
}

const appendStreamToken = (token) => {
  streamingText.value += token
  nextTick(scrollToBottom)
}

const scrollToBottom = () => {
  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  }
}

// ---- voice recording ----
const recording = ref(false)
let mediaRecorder = null
let audioChunks = []

const toggleRecording = async () => {
  if (recording.value) {
    mediaRecorder?.stop()
    return
  }
  try {
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
    const mimeType = MediaRecorder.isTypeSupported('audio/webm;codecs=opus')
      ? 'audio/webm;codecs=opus'
      : 'audio/webm'
    mediaRecorder = new MediaRecorder(stream, { mimeType })
    audioChunks = []
    mediaRecorder.ondataavailable = (e) => { if (e.data.size > 0) audioChunks.push(e.data) }
    mediaRecorder.onstop = async () => {
      stream.getTracks().forEach(t => t.stop())
      recording.value = false
      if (audioChunks.length === 0) return
      const blob = new Blob(audioChunks, { type: mimeType })
      try {
        const form = new FormData()
        form.append('audio', blob, 'recording.webm')
        const resp = await fetch('/api/ai/mascot/asr', { method: 'POST', body: form })
        const json = await resp.json()
        if (json.data?.text) {
          inputText.value = json.data.text
          nextTick(() => sendMessage())
        } else {
          ElMessage.warning('没有识别到语音内容')
        }
      } catch (_) {
        ElMessage.error('语音识别失败，请重试')
      }
    }
    mediaRecorder.start()
    recording.value = true
  } catch (_) {
    ElMessage.warning('请在浏览器设置中允许麦克风权限')
  }
}

const loadMessages = (history) => {
  messages.value = history || []
  nextTick(scrollToBottom)
}

defineExpose({ addMascotMessage, appendStreamToken, streaming, loadMessages })

watch(() => props.visible, (v) => {
  if (v) nextTick(scrollToBottom)
})
</script>

<style scoped>
.mascot-chat {
  position: fixed;
  width: 320px;
  height: 420px;
  background: var(--glass-bg-card, rgba(41, 35, 80, 0.55));
  backdrop-filter: blur(16px);
  border: 1px solid var(--glass-border, rgba(255, 255, 255, 0.08));
  border-radius: var(--radius-lg, 16px);
  display: flex;
  flex-direction: column;
  z-index: 91;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3), 0 0 60px rgba(147, 112, 219, 0.1);
  opacity: 0;
  transform: translateY(12px) scale(0.96);
  pointer-events: none;
  transition: opacity 0.25s ease, transform 0.25s ease;
}

.mascot-chat.visible {
  opacity: 1;
  transform: translateY(0) scale(1);
  pointer-events: auto;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: grab;
  padding: 14px 18px;
  border-bottom: 1px solid var(--glass-border, rgba(255,255,255,0.06));
}

.chat-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-champagne, #D3AF36);
}

.chat-close {
  cursor: pointer;
  color: var(--text-muted, #909399);
  font-size: 16px;
  transition: color 0.2s;
}

.chat-close:hover {
  color: var(--text-primary, #fff);
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.chat-messages::-webkit-scrollbar {
  width: 4px;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: rgba(147, 112, 219, 0.3);
  border-radius: 2px;
}

.chat-welcome {
  text-align: center;
  padding: 30px 16px;
}

.chat-welcome p {
  color: var(--text-dim, #909399);
  font-size: 13px;
  line-height: 1.8;
  margin: 0;
}

.chat-msg {
  max-width: 85%;
}

.chat-msg.user {
  align-self: flex-end;
}

.chat-msg.user .msg-content {
  background: linear-gradient(135deg, var(--color-primary, #926FDB), #7B5EC7);
  color: #fff;
  border-radius: 14px 14px 4px 14px;
  padding: 10px 14px;
  font-size: 13px;
  line-height: 1.5;
}

.chat-msg.mascot {
  align-self: flex-start;
}

.chat-msg.mascot .msg-content {
  background: rgba(255, 255, 255, 0.06);
  color: var(--text-secondary, #cfd3dc);
  border-radius: 14px 14px 14px 4px;
  padding: 10px 14px;
  font-size: 13px;
  line-height: 1.5;
}

.cursor {
  animation: blink 0.8s infinite;
  color: var(--color-champagne, #D3AF36);
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

.chat-input {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  border-top: 1px solid var(--glass-border, rgba(255,255,255,0.06));
}

.chat-input :deep(.el-input__inner) {
  background: rgba(255,255,255,0.04);
  border: 1px solid var(--glass-border, rgba(255,255,255,0.08));
  color: var(--text-primary);
  border-radius: 20px;
  font-size: 13px;
}

.chat-input :deep(.el-input__inner)::placeholder {
  color: var(--text-dim);
}

.chat-input .el-button {
  border-radius: 50%;
  width: 36px;
  height: 36px;
  padding: 0;
  min-width: 36px;
}

.mic-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  cursor: pointer;
  color: var(--text-muted, #909399);
  background: rgba(255,255,255,0.04);
  border: 1px solid var(--glass-border, rgba(255,255,255,0.08));
  transition: all 0.25s;
  flex-shrink: 0;
}

.mic-btn:hover {
  color: var(--color-champagne, #D3AF36);
  border-color: var(--color-champagne, #D3AF36);
}

.mic-btn.recording {
  color: #fff;
  background: #e74c3c;
  border-color: #e74c3c;
  animation: micPulse 0.8s infinite;
}

@keyframes micPulse {
  0%, 100% { box-shadow: 0 0 0 0 rgba(231, 76, 60, 0.6); }
  50% { box-shadow: 0 0 0 8px rgba(231, 76, 60, 0); }
}
</style>
