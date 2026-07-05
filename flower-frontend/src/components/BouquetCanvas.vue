<template>
  <div :class="['bouquet-canvas', { readonly, compact }]">
    <div class="canvas-shell">
      <div
        ref="boardRef"
        :class="['bouquet-board', packageClass, {
          'has-items': sortedItems.length > 0,
          'drag-ready': dragReady,
          'snap-active': snapActive
        }]"
        @dragover.prevent="handleDragOver"
        @dragenter.prevent="handleDragEnter"
        @dragleave="handleDragLeave"
        @drop.prevent="handleDrop"
        @click="clearSelection"
      >
        <div class="drop-hint">{{ sortedItems.length ? '继续添加花材，靠近束口会自动整理' : '拖拽花材到这里，做一束自己的花' }}</div>
        <div class="snap-halo" aria-hidden="true"></div>
        <div class="wrap-layer wrap-back"></div>
        <div class="wrap-layer wrap-liner left"></div>
        <div class="wrap-layer wrap-liner center"></div>
        <div class="wrap-layer wrap-liner right"></div>
        <div class="stem-bundle wrap-layer"></div>

        <div class="flower-layer">
          <div
            v-for="entry in sortedItems"
            :key="entry._key"
            :class="['placed-flower', {
              selected: !readonly && selectedIndex === entry.index,
              dragging: activeDrag?.index === entry.index
            }]"
            :style="getItemStyle(entry.item)"
            @pointerdown.stop="startMove($event, entry.index)"
            @click.stop="selectItem(entry.index)"
          >
            <img
              class="flower-photo"
              :src="getFlowerImage(entry.item)"
              :alt="entry.item.flowerName || entry.item.name || '花材'"
              draggable="false"
            />
            <button
              v-if="!readonly && selectedIndex === entry.index"
              type="button"
              class="handle-dot"
              title="拖拽旋转"
              @pointerdown.stop="startRotate($event, entry.index)"
            >
              ↻
            </button>
          </div>
        </div>

        <div class="wrap-layer wrap-left"></div>
        <div class="wrap-layer wrap-right"></div>
        <div class="wrap-layer wrap-front"></div>
        <div class="wrap-layer wrap-neck"></div>
        <div class="wrap-layer wrap-ribbon"></div>
        <div v-if="message" class="message-card">{{ message }}</div>
      </div>
    </div>

    <div v-if="!readonly && selectedItem" class="canvas-toolbar">
      <div class="selected-name">
        <span>正在调整</span>
        <strong>{{ selectedItem.flowerName }}</strong>
      </div>
      <label>
        <span>旋转 {{ Math.round(selectedItem.rotation || 0) }}°</span>
        <input type="range" min="-60" max="60" step="1" :value="selectedItem.rotation || 0" @input="updateSelected({ rotation: Number($event.target.value) })" />
      </label>
      <label>
        <span>大小 {{ Math.round((selectedItem.scale || 1) * 100) }}%</span>
        <input type="range" min="70" max="130" step="2" :value="Math.round((selectedItem.scale || 1) * 100)" @input="updateSelected({ scale: Number($event.target.value) / 100 })" />
      </label>
      <div class="toolbar-actions">
        <button type="button" @click="duplicateSelected">复制</button>
        <button type="button" @click="shiftLayer(1)">上移</button>
        <button type="button" @click="shiftLayer(-1)">下移</button>
        <button type="button" class="danger" @click="removeSelected">删除</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, ref } from 'vue'

const BOARD_W = 560
const BOARD_H = 600

const props = defineProps({
  modelValue: { type: Array, default: () => [] },
  packageType: { type: String, default: '圆形包装' },
  message: { type: String, default: '' },
  readonly: { type: Boolean, default: false },
  compact: { type: Boolean, default: false },
  selectedIndex: { type: Number, default: null },
  dragEnabled: { type: Boolean, default: false }
})

const emit = defineEmits(['update:modelValue', 'update:selectedIndex', 'drop-flower'])

const boardRef = ref(null)
const dragReady = ref(false)
const snapActive = ref(false)
const activeDrag = ref(null)
const activeRotate = ref(null)

const assetByName = [
  ['红玫瑰', 'red-rose.webp'],
  ['白玫瑰', 'white-rose.webp'],
  ['粉玫瑰', 'pink-rose.webp'],
  ['黄玫瑰', 'yellow-rose.webp'],
  ['红郁金香', 'red-tulip.webp'],
  ['粉郁金香', 'pink-tulip.webp'],
  ['黄郁金香', 'yellow-tulip.webp'],
  ['白百合', 'white-lily.webp'],
  ['粉百合', 'pink-lily.webp'],
  ['向日葵', 'sunflower.webp'],
  ['康乃馨', 'pink-carnation.webp'],
  ['小雏菊', 'white-daisy.webp'],
  ['满天星', 'baby-breath.webp'],
  ['尤加利叶', 'eucalyptus.webp']
]

const packageClass = computed(() => {
  const name = props.packageType || ''
  if (name.includes('心') || name.includes('豆沙') || name.includes('粉')) return 'package-blush'
  if (name.includes('长') || name.includes('森') || name.includes('绿') || name.includes('雾绿')) return 'package-forest'
  if (name.includes('礼盒') || name.includes('礼赠') || name.includes('紫') || name.includes('灰')) return 'package-gift'
  return 'package-paper'
})

const selectedIndex = computed({
  get: () => props.selectedIndex,
  set: value => emit('update:selectedIndex', value)
})

const selectedItem = computed(() => {
  return selectedIndex.value === null ? null : props.modelValue[selectedIndex.value]
})

const sortedItems = computed(() => {
  return props.modelValue
    .map((item, index) => ({ item, index, _key: item.uid || `${item.flowerId || item.id || 'f'}-${index}` }))
    .sort((a, b) => (a.item.z ?? a.item.y ?? 0) - (b.item.z ?? b.item.y ?? 0))
})

const copyItems = () => props.modelValue.map(item => ({ ...item }))

const emitItems = (items) => emit('update:modelValue', items)

const boardPoint = (event) => {
  const rect = boardRef.value.getBoundingClientRect()
  return {
    x: (event.clientX - rect.left) * BOARD_W / rect.width,
    y: (event.clientY - rect.top) * BOARD_H / rect.height
  }
}

const clamp = (value, min, max) => Math.max(min, Math.min(value, max))

const boardDropPoint = (event) => {
  const point = boardPoint(event)
  return snapToBouquetMouth({
    x: clamp(point.x - 43, 24, BOARD_W - 110),
    y: clamp(point.y - 94, 18, BOARD_H - 220)
  })
}

const snapToBouquetMouth = (point) => {
  const mouth = { x: BOARD_W / 2, y: BOARD_H * 0.66 }
  const stemBase = { x: point.x + 43, y: point.y + 176 }
  const dx = stemBase.x - mouth.x
  const dy = stemBase.y - mouth.y
  const distance = Math.hypot(dx, dy)
  const snapRadius = 112
  if (distance > snapRadius) return { ...point, snapped: false }
  const strength = Math.max(0, 1 - distance / snapRadius)
  const pull = strength * strength
  return {
    x: Math.round(point.x - clamp(dx * pull * 0.36, -18, 18)),
    y: Math.round(point.y - clamp(dy * pull * 0.42, -16, 16)),
    snapped: true
  }
}

const getItemStyle = (item) => {
  const photoWidth = item.photoWidth || 132
  const photoHeight = item.photoHeight || 230
  return {
    left: `${(Number(item.x || 0) / BOARD_W) * 100}%`,
    top: `${(Number(item.y || 0) / BOARD_H) * 100}%`,
    zIndex: item.z ?? Math.round(item.y || 0),
    transform: `rotate(${item.rotation || 0}deg) scale(${item.scale || 1})`,
    '--photo-width': `${(photoWidth / 86) * 100}%`,
    '--photo-height': `${(photoHeight / 190) * 100}%`,
    '--bloom-tilt': `${item.tilt || 0}deg`
  }
}

const encodeUrl = (url) => {
  if (!url) return ''
  const lastSlash = url.lastIndexOf('/')
  if (lastSlash >= 0) {
    return url.substring(0, lastSlash + 1) + encodeURIComponent(url.substring(lastSlash + 1))
  }
  return encodeURIComponent(url)
}

const assetUrlByName = (name) => {
  const hit = assetByName.find(([key]) => (name || '').includes(key))
  return hit ? `/images/diy/${hit[1]}` : ''
}

const getFlowerImage = (item) => {
  return item.assetUrl || assetUrlByName(item.flowerName || item.name) || encodeUrl(item.imageUrl)
}

const selectItem = (index) => {
  if (props.readonly) return
  selectedIndex.value = index
}

const clearSelection = () => {
  if (props.readonly || activeDrag.value || activeRotate.value) return
  selectedIndex.value = null
}

const updateSelected = (patch) => {
  if (selectedIndex.value === null) return
  const items = copyItems()
  items[selectedIndex.value] = { ...items[selectedIndex.value], ...patch }
  emitItems(items)
}

const removeSelected = () => {
  if (selectedIndex.value === null) return
  const items = copyItems()
  items.splice(selectedIndex.value, 1)
  selectedIndex.value = null
  emitItems(items)
}

const duplicateSelected = () => {
  if (selectedIndex.value === null) return
  const items = copyItems()
  const item = items[selectedIndex.value]
  const maxZ = Math.max(0, ...items.map(entry => Number(entry.z || 0)))
  items.push({
    ...item,
    uid: `f${Date.now()}`,
    x: clamp(Number(item.x || 0) + 22, 24, BOARD_W - 110),
    y: clamp(Number(item.y || 0) + 10, 18, BOARD_H - 220),
    rotation: clamp(Number(item.rotation || 0) + 6, -60, 60),
    z: maxZ + 1
  })
  selectedIndex.value = items.length - 1
  emitItems(items)
}

const shiftLayer = (direction) => {
  if (selectedIndex.value === null) return
  const items = copyItems()
  const item = items[selectedIndex.value]
  items[selectedIndex.value] = { ...item, z: Number(item.z || 0) + direction * 10 }
  emitItems(items)
}

const startMove = (event, index) => {
  if (props.readonly) return
  const item = props.modelValue[index]
  const point = boardPoint(event)
  selectedIndex.value = index
  activeDrag.value = {
    index,
    dx: point.x - Number(item.x || 0),
    dy: point.y - Number(item.y || 0)
  }
  event.currentTarget.setPointerCapture?.(event.pointerId)
  window.addEventListener('pointermove', movePointer)
  window.addEventListener('pointerup', stopPointer)
}

const startRotate = (event, index) => {
  if (props.readonly) return
  const item = props.modelValue[index]
  selectedIndex.value = index
  const origin = {
    x: Number(item.x || 0) + 43,
    y: Number(item.y || 0) + 175
  }
  const point = boardPoint(event)
  activeRotate.value = {
    index,
    origin,
    baseRotation: Number(item.rotation || 0),
    startAngle: Math.atan2(point.y - origin.y, point.x - origin.x) * 180 / Math.PI
  }
  window.addEventListener('pointermove', movePointer)
  window.addEventListener('pointerup', stopPointer)
}

const movePointer = (event) => {
  if (activeRotate.value) {
    const state = activeRotate.value
    const point = boardPoint(event)
    const angle = Math.atan2(point.y - state.origin.y, point.x - state.origin.x) * 180 / Math.PI
    const items = copyItems()
    const item = items[state.index]
    items[state.index] = {
      ...item,
      rotation: Math.round(clamp(state.baseRotation + angle - state.startAngle, -60, 60))
    }
    emitItems(items)
    return
  }

  if (!activeDrag.value) return
  const state = activeDrag.value
  const point = boardPoint(event)
  const next = snapToBouquetMouth({
    x: clamp(point.x - state.dx, 24, BOARD_W - 110),
    y: clamp(point.y - state.dy, 18, BOARD_H - 220)
  })
  const items = copyItems()
  const item = items[state.index]
  items[state.index] = {
    ...item,
    x: next.x,
    y: next.y,
    z: Math.round(next.y)
  }
  snapActive.value = next.snapped
  emitItems(items)
}

const stopPointer = () => {
  activeDrag.value = null
  activeRotate.value = null
  snapActive.value = false
  window.removeEventListener('pointermove', movePointer)
  window.removeEventListener('pointerup', stopPointer)
}

const handleDragEnter = () => {
  if (!props.readonly && props.dragEnabled) dragReady.value = true
}

const handleDragOver = () => {
  if (!props.readonly && props.dragEnabled) dragReady.value = true
}

const handleDragLeave = (event) => {
  if (!boardRef.value?.contains(event.relatedTarget)) dragReady.value = false
}

const handleDrop = (event) => {
  if (props.readonly || !props.dragEnabled) return
  dragReady.value = false
  emit('drop-flower', boardDropPoint(event))
}

onBeforeUnmount(() => {
  window.removeEventListener('pointermove', movePointer)
  window.removeEventListener('pointerup', stopPointer)
})
</script>

<style scoped>
.bouquet-canvas {
  width: 100%;
}

.canvas-shell {
  width: min(100%, 560px);
  margin: 0 auto;
  aspect-ratio: 14 / 15;
}

.bouquet-board {
  position: relative;
  width: 100%;
  height: 100%;
  min-height: 420px;
  border: 1px solid rgba(196, 69, 105, 0.14);
  border-radius: 18px;
  overflow: hidden;
  background:
    radial-gradient(circle at 50% 15%, rgba(255,255,255,0.9), transparent 34%),
    linear-gradient(180deg, #fffdf8, #f8f3ed);
  box-shadow: inset 0 0 0 1px rgba(255,255,255,0.56), 0 16px 32px rgba(120, 80, 82, 0.12);
  touch-action: none;
}

.compact .bouquet-board {
  min-height: 360px;
}

.drop-hint {
  position: absolute;
  left: 50%;
  top: 12.3%;
  z-index: 2;
  color: rgba(116, 109, 96, 0.72);
  font-size: 13px;
  transform: translateX(-50%);
  pointer-events: none;
  white-space: nowrap;
}

.bouquet-board.has-items .drop-hint {
  opacity: 0;
}

.bouquet-board.drag-ready {
  border-color: rgba(47, 115, 86, 0.34);
  box-shadow:
    0 0 0 4px rgba(47, 115, 86, 0.08),
    0 16px 32px rgba(120, 80, 82, 0.12);
}

.snap-halo {
  position: absolute;
  left: 38.9%;
  top: 58.7%;
  width: 22.1%;
  height: 10.7%;
  border-radius: 50%;
  border: 1px dashed rgba(47, 115, 86, 0.3);
  background: radial-gradient(ellipse at center, rgba(47, 115, 86, 0.12), transparent 68%);
  opacity: 0;
  transform: scale(0.92);
  transition: opacity 0.18s, transform 0.18s;
  pointer-events: none;
  z-index: 2;
}

.snap-halo::after {
  content: "贴近束口";
  position: absolute;
  left: 50%;
  top: 50%;
  padding: 3px 8px;
  border-radius: 999px;
  background: rgba(255, 252, 243, 0.9);
  color: rgba(47, 86, 64, 0.86);
  font-size: 11px;
  white-space: nowrap;
  transform: translate(-50%, -50%);
  box-shadow: 0 4px 10px rgba(54, 43, 28, 0.12);
}

.bouquet-board.snap-active .snap-halo {
  opacity: 1;
  transform: scale(1);
}

.wrap-layer {
  position: absolute;
  pointer-events: none;
}

.wrap-layer::after {
  content: "";
  position: absolute;
  inset: 0;
  background:
    url("/images/diy/wrapping-paper-texture.webp") center / cover,
    linear-gradient(115deg, transparent 0 24%, rgba(255,255,255,0.28) 25% 27%, transparent 28% 52%, rgba(0,0,0,0.08) 53% 54%, transparent 55%),
    repeating-linear-gradient(112deg, rgba(255,255,255,0.14) 0 2px, transparent 2px 16px),
    linear-gradient(90deg, rgba(255,255,255,0.12), rgba(0,0,0,0.07));
  background-blend-mode: multiply, normal, normal, normal;
  opacity: 0.5;
  mix-blend-mode: soft-light;
}

.wrap-back {
  left: 16.8%;
  top: 35%;
  width: 66.4%;
  height: 53.3%;
  border-radius: 48% 48% 18% 18%;
  background: var(--package-back);
  clip-path: polygon(6% 0, 94% 0, 82% 100%, 18% 100%);
  box-shadow: inset 0 0 0 1px rgba(75, 66, 54, 0.11);
  opacity: 0.94;
  z-index: 1;
}

.wrap-liner {
  width: 29.3%;
  height: 35.7%;
  top: 32.7%;
  background: var(--package-liner);
  clip-path: polygon(50% 0, 100% 22%, 72% 100%, 28% 100%, 0 22%);
  opacity: 0.78;
  box-shadow: inset 0 0 0 1px rgba(255,255,255,0.32);
  z-index: 2;
}

.wrap-liner.left {
  left: 22.9%;
  transform: rotate(-19deg);
}

.wrap-liner.center {
  left: 35.4%;
  top: 30.7%;
  width: 30%;
  height: 37.7%;
  transform: rotate(1deg);
}

.wrap-liner.right {
  right: 22.5%;
  transform: rotate(19deg);
}

.wrap-left,
.wrap-right {
  top: 41.7%;
  width: 33.9%;
  height: 47.7%;
  z-index: 5;
}

.wrap-left {
  left: 20%;
  background: var(--package-left);
  clip-path: polygon(0 0, 100% 23%, 78% 100%, 16% 88%);
  transform: rotate(-8deg);
  box-shadow: -12px 20px 30px rgba(72, 55, 40, 0.14);
}

.wrap-right {
  right: 20%;
  background: var(--package-right);
  clip-path: polygon(0 23%, 100% 0, 84% 88%, 22% 100%);
  transform: rotate(8deg);
  box-shadow: 12px 20px 30px rgba(72, 55, 40, 0.14);
}

.wrap-left::before,
.wrap-right::before {
  content: "";
  position: absolute;
  inset: 5% 12% 8%;
  background:
    linear-gradient(112deg, transparent 0 44%, rgba(255,255,255,0.28) 45% 47%, transparent 48%),
    linear-gradient(74deg, transparent 0 55%, rgba(63, 48, 34, 0.1) 56% 58%, transparent 59%);
  opacity: 0.72;
}

.wrap-front {
  left: 27.5%;
  top: 54%;
  width: 45%;
  height: 39.7%;
  background: var(--package-front);
  clip-path: polygon(6% 0, 94% 0, 76% 100%, 24% 100%);
  box-shadow:
    0 -12px 20px rgba(255,255,255,0.22) inset,
    0 -18px 30px rgba(68, 48, 31, 0.15);
  z-index: 6;
}

.wrap-front::before {
  content: "";
  position: absolute;
  left: 17.5%;
  right: 17.5%;
  top: 2%;
  height: 21%;
  border-radius: 50%;
  background: radial-gradient(ellipse at center, rgba(62, 43, 28, 0.22), transparent 72%);
  filter: blur(3px);
}

.stem-bundle {
  left: 41.4%;
  top: 57.3%;
  width: 17.5%;
  height: 29.3%;
  background:
    linear-gradient(72deg, transparent 0 12%, #315e3e 13% 15%, transparent 16% 26%, #6d995d 27% 29%, transparent 30% 42%, #2c6844 43% 45%, transparent 46% 56%, #789a62 57% 59%, transparent 60% 72%, #486f47 73% 75%, transparent 76%),
    linear-gradient(106deg, transparent 0 18%, rgba(38, 91, 59, 0.72) 19% 21%, transparent 22% 38%, rgba(104, 142, 83, 0.64) 39% 41%, transparent 42%),
    linear-gradient(180deg, rgba(49, 94, 62, 0.86), rgba(79, 113, 61, 0.42));
  clip-path: polygon(9% 0, 91% 0, 72% 100%, 28% 100%);
  opacity: 0.78;
  filter: drop-shadow(0 8px 10px rgba(45, 35, 24, 0.18));
  z-index: 4;
}

.stem-bundle::after {
  display: none;
}

.wrap-neck {
  left: 35%;
  top: 63%;
  width: 30.4%;
  height: 13%;
  border-radius: 46% 46% 42% 42%;
  background:
    radial-gradient(ellipse at 50% 38%, rgba(56, 38, 24, 0.22), transparent 58%),
    repeating-linear-gradient(96deg, rgba(255,255,255,0.22) 0 3px, transparent 3px 17px),
    var(--package-front);
  clip-path: polygon(6% 18%, 94% 16%, 82% 100%, 18% 100%);
  box-shadow:
    inset 0 12px 18px rgba(255,255,255,0.16),
    0 8px 18px rgba(69, 48, 31, 0.18);
  z-index: 6;
}

.wrap-ribbon {
  left: 37.9%;
  top: 73%;
  width: 24.6%;
  height: 5.7%;
  border-radius: 99px;
  background:
    linear-gradient(90deg, transparent 0 16%, rgba(255,255,255,0.36) 17% 23%, transparent 24%),
    var(--ribbon);
  box-shadow: 0 8px 18px rgba(74, 40, 38, 0.2);
  z-index: 7;
}

.wrap-ribbon::before,
.wrap-ribbon::after {
  content: "";
  position: absolute;
  top: 18%;
  width: 32%;
  height: 76%;
  background: var(--ribbon);
  filter: brightness(0.96);
}

.wrap-ribbon::before {
  left: -22%;
  clip-path: polygon(0 0, 100% 20%, 100% 80%, 0 100%, 28% 50%);
}

.wrap-ribbon::after {
  right: -22%;
  clip-path: polygon(0 20%, 100% 0, 72% 50%, 100% 100%, 0 80%);
}

.flower-layer {
  position: absolute;
  inset: 0;
  z-index: 3;
}

.package-paper {
  --package-back: linear-gradient(145deg, #efe2cc, #f8f0dd);
  --package-left: linear-gradient(150deg, #ddc7a7, #f4e7cf);
  --package-right: linear-gradient(210deg, #ead9bd, #f9efd8);
  --package-front: linear-gradient(180deg, #f3e5cd, #d8bd94);
  --package-liner: linear-gradient(160deg, rgba(255,252,241,0.82), rgba(225,205,168,0.72));
  --ribbon: #b44646;
}

.package-forest {
  --package-back: linear-gradient(145deg, #c8d7c1, #eef3df);
  --package-left: linear-gradient(150deg, #8fae8c, #d4e0c7);
  --package-right: linear-gradient(210deg, #b9ceab, #eef3dd);
  --package-front: linear-gradient(180deg, #d7e3ca, #92aa82);
  --package-liner: linear-gradient(160deg, rgba(246,252,233,0.82), rgba(166,192,150,0.7));
  --ribbon: #345f49;
}

.package-blush {
  --package-back: linear-gradient(145deg, #f1c7c2, #fff0ea);
  --package-left: linear-gradient(150deg, #d99899, #f8d5cf);
  --package-right: linear-gradient(210deg, #eab8b1, #fff0ea);
  --package-front: linear-gradient(180deg, #f7d4cb, #cf8a86);
  --package-liner: linear-gradient(160deg, rgba(255,244,238,0.86), rgba(229,172,166,0.72));
  --ribbon: #8e2f45;
}

.package-gift {
  --package-back: linear-gradient(145deg, #d8d1e8, #fbf5ff);
  --package-left: linear-gradient(150deg, #a9a0c7, #e2dcf1);
  --package-right: linear-gradient(210deg, #c7bfdf, #fbf5ff);
  --package-front: linear-gradient(180deg, #e8e1f6, #9b90bf);
  --package-liner: linear-gradient(160deg, rgba(252,247,255,0.84), rgba(191,183,216,0.7));
  --ribbon: #c99736;
}

.placed-flower {
  position: absolute;
  width: 15.36%;
  height: 31.67%;
  transform-origin: 50% 92%;
  cursor: move;
  user-select: none;
  touch-action: none;
  filter: drop-shadow(0 8px 8px rgba(42, 35, 28, 0.16));
  transition: filter 0.16s;
}

.readonly .placed-flower {
  pointer-events: none;
}

.flower-photo {
  position: absolute;
  left: 50%;
  bottom: -2%;
  width: var(--photo-width, 153%);
  height: var(--photo-height, 121%);
  object-fit: contain;
  transform: translateX(-50%) rotate(var(--bloom-tilt, 0deg));
  pointer-events: none;
  user-select: none;
}

.placed-flower.selected {
  outline: 1px dashed rgba(180, 70, 70, 0.56);
  outline-offset: 5px;
  z-index: 999 !important;
  filter: drop-shadow(0 10px 10px rgba(42, 35, 28, 0.2));
}

.placed-flower.dragging {
  filter: drop-shadow(0 16px 14px rgba(42, 35, 28, 0.2));
  transition: none;
}

.handle-dot {
  position: absolute;
  right: -6px;
  top: -22px;
  width: 26px;
  height: 26px;
  border: none;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff6b9d 0%, #c44569 100%);
  color: white;
  cursor: grab;
  box-shadow: 0 6px 14px rgba(196, 69, 105, 0.3);
}

.message-card {
  position: absolute;
  right: 22.1%;
  top: 68%;
  width: 18.2%;
  min-height: 11.3%;
  padding: 10px 8px;
  border-radius: 6px;
  background:
    linear-gradient(180deg, rgba(255,255,255,0.92), rgba(248,240,221,0.96)),
    repeating-linear-gradient(0deg, rgba(151, 123, 84, 0.08) 0 1px, transparent 1px 12px);
  border: 1px solid rgba(105, 84, 56, 0.16);
  box-shadow: 0 12px 22px rgba(64, 45, 27, 0.18);
  color: #5f4f3b;
  font-size: 12px;
  line-height: 1.45;
  text-align: center;
  transform: rotate(7deg);
  z-index: 8;
  pointer-events: none;
}

.canvas-toolbar {
  margin-top: 1rem;
  padding: 1rem;
  border-radius: 14px;
  background: #fff7fa;
  border: 1px solid #ffe1eb;
}

.selected-name {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.8rem;
  color: #777;
}

.selected-name strong {
  color: #c44569;
}

.canvas-toolbar label {
  display: grid;
  grid-template-columns: 115px 1fr;
  align-items: center;
  gap: 0.8rem;
  margin-bottom: 0.7rem;
  color: #666;
  font-size: 0.9rem;
}

.canvas-toolbar input[type="range"] {
  width: 100%;
  accent-color: #c44569;
}

.toolbar-actions {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 0.5rem;
}

.toolbar-actions button {
  border: none;
  border-radius: 10px;
  padding: 0.55rem 0.4rem;
  background: white;
  color: #c44569;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(196, 69, 105, 0.1);
}

.toolbar-actions .danger {
  color: #c62828;
  background: #ffebee;
}

@media (max-width: 640px) {
  .canvas-shell {
    width: 100%;
  }

  .bouquet-board {
    min-height: 330px;
    border-radius: 16px;
  }

  .drop-hint {
    white-space: normal;
    width: 72%;
    text-align: center;
  }

  .canvas-toolbar label {
    grid-template-columns: 1fr;
    gap: 0.25rem;
  }

  .toolbar-actions {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
