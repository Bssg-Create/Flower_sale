<template>
  <div class="ai-assistant">
    <!-- 浮动气泡按钮 -->
    <button class="ai-bubble" type="button" aria-label="打开花店 AI 助手" @click="toggleChat" v-show="!showChat">
      <svg viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="white" stroke-width="2">
        <path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z"/>
      </svg>
    </button>

    <!-- 聊天面板 -->
    <transition name="slide-up">
      <div class="ai-panel" v-show="showChat">
        <div class="ai-header">
          <span>花店AI助手</span>
          <button class="ai-close" @click="showChat = false">×</button>
        </div>

        <div class="ai-messages" ref="msgBox">
          <div class="ai-welcome">
            你好！我是花店 AI 助手，可以帮你选花、搭配花束、解答疑问。
          </div>

          <div v-for="(msg, i) in messages" :key="i" :class="['ai-msg', msg.role]">
            <div class="ai-msg-content">{{ msg.content }}</div>
            <div v-if="msg.role === 'assistant' && msg.streaming" class="ai-cursor">▍</div>
          </div>

          <div v-if="isStreaming" class="ai-typing">思考中...</div>
        </div>

        <div class="ai-input-row">
          <input
            v-model="inputText"
            class="ai-input"
            placeholder="输入问题，比如：送女朋友什么花好？"
            @keyup.enter="sendMessage"
            :disabled="isStreaming"
          />
          <button class="ai-send" @click="sendMessage" :disabled="isStreaming || !inputText.trim()">
            发送
          </button>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'

const showChat = ref(false)
const inputText = ref('')
const messages = ref([])
const isStreaming = ref(false)
const msgBox = ref(null)

const toggleChat = () => {
  showChat.value = true
  nextTick(scrollToBottom)
}

const scrollToBottom = () => {
  nextTick(() => {
    if (msgBox.value) {
      msgBox.value.scrollTop = msgBox.value.scrollHeight
    }
  })
}

const sendMessage = async () => {
  const text = inputText.value.trim()
  if (!text || isStreaming.value) return

  messages.value.push({ role: 'user', content: text })
  inputText.value = ''
  scrollToBottom()

  const assistantMsg = { role: 'assistant', content: '', streaming: true }
  messages.value.push(assistantMsg)
  isStreaming.value = true
  scrollToBottom()

  try {
    const baseUrl = import.meta.env.VITE_API_BASE || ''
    const response = await fetch(`${baseUrl}/api/ai/chat?message=${encodeURIComponent(text)}`)

    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() || ''

      for (const line of lines) {
        if (line.startsWith('data:')) {
          const data = line.substring(5).trim()
          if (data === '[DONE]') {
            assistantMsg.streaming = false
            break
          }
          if (data.startsWith('[ERROR]')) {
            assistantMsg.content = '抱歉，连接出现问题，请稍后再试'
            assistantMsg.streaming = false
            break
          }
          assistantMsg.content += data
        }
      }
      scrollToBottom()
    }
  } catch (e) {
    assistantMsg.content = '网络连接失败，请检查网络后重试'
    assistantMsg.streaming = false
  }

  isStreaming.value = false
  scrollToBottom()
}
</script>

<style scoped>
.ai-assistant {
  position: fixed;
  bottom: 24px;
  right: 24px;
  z-index: 9999;
  font-family: inherit;
}

/* 浮动气泡 */
.ai-bubble {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  border: none;
  color: white;
  background: var(--color-brand);
  box-shadow: 0 10px 26px rgba(132, 45, 74, 0.28);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s, background 0.2s;
}
.ai-bubble:hover {
  transform: translateY(-2px);
  background: var(--color-brand-strong);
  box-shadow: 0 14px 30px rgba(132, 45, 74, 0.34);
}

/* 聊天面板 */
.ai-panel {
  width: 360px;
  height: 500px;
  background: var(--color-surface);
  border: 1px solid var(--color-line);
  border-radius: var(--radius-card);
  box-shadow: var(--shadow-float);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.ai-header {
  background: var(--color-forest);
  color: #fff;
  padding: 14px 18px;
  font-size: 16px;
  font-weight: 600;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.ai-close {
  background: none;
  border: none;
  color: #fff;
  font-size: 22px;
  cursor: pointer;
  line-height: 1;
  padding: 0 4px;
}

/* 消息区 */
.ai-messages {
  flex: 1;
  overflow-y: auto;
  padding: 14px;
  background: var(--color-canvas);
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.ai-welcome {
  background: var(--color-brand-soft);
  color: var(--color-brand-strong);
  padding: 12px 14px;
  border-radius: 12px;
  font-size: 13px;
  line-height: 1.6;
}

.ai-msg {
  display: flex;
  flex-direction: column;
}
.ai-msg.user {
  align-items: flex-end;
}
.ai-msg.assistant {
  align-items: flex-start;
}

.ai-msg-content {
  max-width: 85%;
  padding: 10px 14px;
  border-radius: 14px;
  font-size: 13px;
  line-height: 1.6;
  word-break: break-word;
  white-space: pre-wrap;
}
.ai-msg.user .ai-msg-content {
  background: var(--color-brand);
  color: #fff;
  border-bottom-right-radius: 4px;
}
.ai-msg.assistant .ai-msg-content {
  background: #fff;
  color: var(--color-ink);
  border-bottom-left-radius: 4px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
}

.ai-cursor {
  display: inline;
  color: var(--color-brand);
  animation: blink 0.8s infinite;
  font-size: 13px;
}
@keyframes blink {
  50% { opacity: 0; }
}

.ai-typing {
  font-size: 12px;
  color: var(--color-muted);
  padding-left: 4px;
}

/* 输入区 */
.ai-input-row {
  display: flex;
  padding: 10px 14px;
  border-top: 1px solid var(--color-line);
  gap: 8px;
  background: #fff;
}
.ai-input {
  flex: 1;
  border: 1px solid var(--color-line);
  border-radius: var(--radius-control);
  padding: 8px 16px;
  font-size: 13px;
  outline: none;
  transition: border 0.2s;
}
.ai-input:focus {
  border-color: var(--color-brand);
}
.ai-send {
  background: var(--color-brand);
  color: #fff;
  border: none;
  border-radius: var(--radius-control);
  padding: 8px 18px;
  font-size: 13px;
  cursor: pointer;
  transition: opacity 0.2s;
}
.ai-send:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 动画 */
.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.25s ease;
}
.slide-up-enter-from,
.slide-up-leave-to {
  opacity: 0;
  transform: translateY(20px);
}

@media (max-width: 480px) {
  .ai-assistant { right: 14px; bottom: 14px; }
  .ai-panel { width: calc(100vw - 28px); height: min(520px, calc(100dvh - 28px)); }
}
</style>
