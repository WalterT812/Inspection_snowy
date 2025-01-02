import dayjs from 'dayjs'

export const formatDateTime = (date) => {
  if (!date) return '--'
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

export const formatDate = (date) => {
  if (!date) return '--'
  return dayjs(date).format('YYYY-MM-DD')
}

export const formatTime = (date) => {
  if (!date) return '--'
  return dayjs(date).format('HH:mm:ss')
}
