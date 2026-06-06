import { createApp } from 'vue'
import PrimeVue from 'primevue/config'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Dialog from 'primevue/dialog'
import Button from 'primevue/button'
import MultiSelect from 'primevue/multiselect'
import Calendar from 'primevue/calendar'
import Tag from 'primevue/tag'
import Accordion from 'primevue/accordion'
import AccordionTab from 'primevue/accordiontab'
import Skeleton from 'primevue/skeleton'
import InputText from 'primevue/inputtext'
import Tooltip from 'primevue/tooltip'

import 'primevue/resources/themes/lara-light-green/theme.css'
import 'primevue/resources/primevue.min.css'
import 'primeicons/primeicons.css'

import router from './router.js'
import App from './App.vue'
import './assets/styles.css'

const app = createApp(App)

app.use(PrimeVue, { ripple: true })
app.use(router)

app.component('p-datatable', DataTable)
app.component('p-column', Column)
app.component('p-dialog', Dialog)
app.component('p-button', Button)
app.component('p-multiselect', MultiSelect)
app.component('p-calendar', Calendar)
app.component('p-tag', Tag)
app.component('p-accordion', Accordion)
app.component('p-accordion-tab', AccordionTab)
app.component('p-skeleton', Skeleton)
app.component('p-inputtext', InputText)

app.directive('tooltip', Tooltip)

app.mount('#app')
