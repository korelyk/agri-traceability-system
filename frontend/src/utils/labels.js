export const PRODUCT_CATEGORY_LABELS = {
  FR: '水果',
  FRUIT: '水果',
  VE: '蔬菜',
  VEGETABLE: '蔬菜',
  GR: '粮食',
  GRAIN: '粮食',
  TE: '茶叶',
  TEA: '茶叶',
  ME: '肉类',
  MEAT: '肉类',
  AQ: '水产',
  AQUATIC: '水产',
  OT: '其他',
  OTHER: '其他'
}

export const STATUS_LABELS = {
  CREATED: '已创建',
  PRODUCE: '生产中',
  PROCESS: '加工中',
  TRANSPORT: '运输中',
  STORAGE: '仓储中',
  SALE: '销售中',
  INSPECT: '检测中',
  PACKAGE: '包装中'
}

export const OPERATION_TYPE_LABELS = {
  PRODUCE: '生产',
  PROCESS: '加工',
  TRANSPORT: '运输',
  STORAGE: '仓储',
  SALE: '销售',
  INSPECT: '检测',
  PACKAGE: '包装'
}

export const USER_TYPE_LABELS = {
  PRODUCER: '生产者',
  PROCESSOR: '加工商',
  LOGISTICS: '物流商',
  RETAILER: '销售商',
  INSPECTOR: '检测机构',
  ADMIN: '管理员'
}

export const ROLE_LABELS = {
  ADMIN: '管理员',
  OPERATOR: '业务操作员',
  USER: '普通用户'
}

export function readableText(value) {
  return value && typeof value === 'string' && value.trim() && !/^\?+$/.test(value)
    ? value.trim()
    : ''
}

function labelOf(map, value) {
  if (!value) {
    return '-'
  }
  return map[value] || value
}

export function productCategoryLabel(value) {
  return labelOf(PRODUCT_CATEGORY_LABELS, value)
}

export function statusLabel(value) {
  return labelOf(STATUS_LABELS, value)
}

export function operationTypeLabel(value) {
  return labelOf(OPERATION_TYPE_LABELS, value)
}

export function userTypeLabel(value) {
  return labelOf(USER_TYPE_LABELS, value)
}

export function operationTypeDisplay(name, type) {
  return readableText(name) || operationTypeLabel(type)
}

export function userTypeDisplay(name, type) {
  return readableText(name) || userTypeLabel(type)
}

export function roleLabel(value) {
  return labelOf(ROLE_LABELS, value)
}
