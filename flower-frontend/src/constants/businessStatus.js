export const ORDER_STATUS = Object.freeze({
  PENDING: 'pending',
  PAID: 'paid',
  SHIPPED: 'shipped',
  COMPLETED: 'completed',
  CANCELED: 'canceled'
})

export const PAYMENT_STATUS = Object.freeze({
  UNPAID: 'unpaid',
  PAID: 'paid',
  REFUNDED: 'refunded'
})

export const DIY_STATUS = Object.freeze({
  SAVED: 'saved',
  ORDERED: 'ordered'
})

export const normalizeOrderStatus = (value) => {
  const normalized = String(value || '').trim().toLowerCase()
  if (normalized === 'payed') return ORDER_STATUS.PAID
  if (normalized === 'cancelled') return ORDER_STATUS.CANCELED
  return normalized
}

export const normalizePaymentStatus = (value) => String(value || '').trim().toLowerCase()

export const normalizeDiyStatus = (value) => {
  const normalized = String(value || '').trim().toLowerCase()
  if (normalized === '1') return DIY_STATUS.SAVED
  if (normalized === '2') return DIY_STATUS.ORDERED
  return normalized
}

export const orderStatusText = (value) => ({
  [ORDER_STATUS.PENDING]: '待支付',
  [ORDER_STATUS.PAID]: '已支付',
  [ORDER_STATUS.SHIPPED]: '已发货',
  [ORDER_STATUS.COMPLETED]: '已完成',
  [ORDER_STATUS.CANCELED]: '已取消'
}[normalizeOrderStatus(value)] || value || '未知状态')

export const paymentStatusText = (value) => ({
  [PAYMENT_STATUS.UNPAID]: '未支付',
  [PAYMENT_STATUS.PAID]: '已支付',
  [PAYMENT_STATUS.REFUNDED]: '已退款'
}[normalizePaymentStatus(value)] || value || '未知状态')

export const diyStatusText = (value) => ({
  [DIY_STATUS.SAVED]: '已保存',
  [DIY_STATUS.ORDERED]: '已下单'
}[normalizeDiyStatus(value)] || value || '未知状态')

export const isDiyOrdered = (value) => normalizeDiyStatus(value) === DIY_STATUS.ORDERED
