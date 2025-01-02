import { createApp } from 'vue'
import Antd from 'ant-design-vue'
import { createPinia } from 'pinia'

import './style/index.less'
import snowy from './snowy'
import i18n from './locales'
import router from './router'
import App from './App.vue'
import './tailwind.css'
import { formatDateTime, formatDate, formatTime } from '@/utils/dateUtil'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.use(Antd)
app.use(i18n)
app.use(snowy)

app.config.globalProperties.$formatDateTime = formatDateTime
app.config.globalProperties.$formatDate = formatDate
app.config.globalProperties.$formatTime = formatTime

// 挂载app
app.mount('#app')
