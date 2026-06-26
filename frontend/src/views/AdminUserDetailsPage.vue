<template>
  <div class="page">
    <AppHeader />

    <main class="page-main">
      <div v-if="loading" class="skeleton-block">
        <Skeleton width="200px" height="24px" />
        <Skeleton width="100%" height="300px" style="margin-top:1rem;" />
      </div>

      <div v-else-if="notFound" class="error-state">
        <i class="pi pi-exclamation-circle" style="font-size:2.5rem;color:#8091A7;"></i>
        <h2>{{ $t('admin.userDetails.notFound') }}</h2>
        <router-link to="/admin/users">
          <Button :label="$t('admin.userDetails.backToUsers')" icon="pi pi-arrow-left" class="p-button-text" />
        </router-link>
      </div>

      <div v-else-if="error" class="inline-error">
        <i class="pi pi-exclamation-triangle"></i>
        <span>{{ error }}</span>
        <Button icon="pi pi-refresh" :label="$t('admin.userDetails.retry')" class="p-button-text p-button-sm" @click="loadDetails" />
      </div>

      <template v-else-if="details">
        <div class="details-header">
          <router-link to="/admin/users" class="back-link">
            <i class="pi pi-arrow-left"></i> {{ $t('admin.userDetails.backToUsers') }}
          </router-link>
          <h1>{{ headerName }}</h1>
          <p class="subtitle">{{ details.account.accountEmail }}</p>
          <Tag v-if="details.isCurrentAdmin" :value="$t('admin.userDetails.you')" severity="info" />
        </div>

        <Tabs :key="tabsKey" :value="activeTab" @update:value="onTabChange">
          <TabList>
            <Tab value="0">{{ $t('admin.userDetails.tabContacts') }}</Tab>
            <Tab value="1">{{ $t('admin.userDetails.tabAccount') }}</Tab>
            <Tab value="2">{{ $t('admin.userDetails.tabAdditional') }}</Tab>
            <Tab value="3">{{ $t('admin.userDetails.tabResumes') }}</Tab>
          </TabList>
          <TabPanels>
            <TabPanel value="0">
              <AdminUserContactsTab :contacts="details.contacts" />
            </TabPanel>
            <TabPanel value="1">
              <AdminUserAccountTab
                :account="details.account"
                :isCurrentAdmin="details.isCurrentAdmin"
                :saving="savingAccess"
                @save="onSaveAccess"
                @update:dirty="accountDirty = $event"
              />
            </TabPanel>
            <TabPanel value="2">
              <AdminUserAdditionalInfoTab :info="details.additionalInfo" :username="details.account.username" />
            </TabPanel>
            <TabPanel value="3">
              <AdminUserResumesTab :userId="details.id" />
            </TabPanel>
          </TabPanels>
        </Tabs>

        <!-- Delete user section -->
        <div class="delete-section" v-if="!details.isCurrentAdmin">
          <h3>{{ $t('admin.userDetails.dangerZone') }}</h3>
          <p>{{ $t('admin.userDetails.deleteDescription') }}</p>
          <Button
            :label="$t('admin.userDetails.deleteUser')"
            icon="pi pi-trash"
            class="p-button-danger"
            :loading="deleting"
            :disabled="deleting"
            @click="confirmDelete"
          />
        </div>
        <div v-else class="delete-section disabled">
          <h3>{{ $t('admin.userDetails.dangerZone') }}</h3>
          <p class="self-delete-hint">{{ $t('admin.userDetails.selfDeleteHint') }}</p>
        </div>
      </template>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useToast } from 'primevue/usetoast'
import { useConfirm } from 'primevue/useconfirm'
import { useI18n } from 'vue-i18n'
import AppHeader from '@/components/AppHeader.vue'
import AdminUserAccountTab from '@/components/admin/AdminUserAccountTab.vue'
import AdminUserContactsTab from '@/components/admin/AdminUserContactsTab.vue'
import AdminUserAdditionalInfoTab from '@/components/admin/AdminUserAdditionalInfoTab.vue'
import AdminUserResumesTab from '@/components/admin/AdminUserResumesTab.vue'
import Button from 'primevue/button'
import Skeleton from 'primevue/skeleton'
import Tabs from 'primevue/tabs'
import TabList from 'primevue/tablist'
import Tab from 'primevue/tab'
import TabPanels from 'primevue/tabpanels'
import TabPanel from 'primevue/tabpanel'
import Tag from 'primevue/tag'
import { getAdminUserDetails, updateAdminUserAccess, deleteAdminUser } from '@/services/adminService'
import type { AdminUserDetails, AdminUserAccessUpdateRequest } from '@/types/admin'

const route = useRoute()
const router = useRouter()
const toast = useToast()
const confirm = useConfirm()
const { t } = useI18n()

const userId = computed(() => route.params.userId as string)

const details = ref<AdminUserDetails | null>(null)
const loading = ref(true)
const error = ref<string | null>(null)
const notFound = ref(false)
const savingAccess = ref(false)
const deleting = ref(false)
const activeTab = ref('0')
const accountDirty = ref(false)

const headerName = computed(() => {
  if (!details.value) return ''
  const fullName = details.value.contacts?.fullName
  const email = details.value.account.accountEmail
  return fullName || email || details.value.id
})

async function loadDetails() {
  loading.value = true
  error.value = null
  notFound.value = false
  details.value = null
  try {
    details.value = await getAdminUserDetails(userId.value)
  } catch (e: any) {
    if (e.message?.includes('404') || e.message?.includes('not found')) {
      notFound.value = true
    } else {
      error.value = e.message || t('admin.userDetails.error')
    }
  } finally {
    loading.value = false
  }
}

const tabsKey = ref(0)

function onTabChange(newTab: string | number) {
  const requestedTab = String(newTab)
  if (requestedTab === activeTab.value) return

  const leavingDirtyAccount = activeTab.value === '1' && accountDirty.value && requestedTab !== '1'

  if (leavingDirtyAccount) {
    confirm.require({
      header: t('admin.userDetails.unsavedTitle'),
      message: t('admin.userDetails.unsavedText'),
      rejectLabel: t('admin.userDetails.stay'),
      acceptLabel: t('admin.userDetails.leave'),
      reject: () => {
        activeTab.value = '1'
        tabsKey.value += 1
      },
      accept: () => {
        accountDirty.value = false
        activeTab.value = requestedTab
        tabsKey.value += 1
      }
    })
    return
  }

  activeTab.value = requestedTab
}

async function onSaveAccess(request: AdminUserAccessUpdateRequest) {
  if (savingAccess.value) return

  // Confirmation before save
  confirm.require({
    header: t('admin.userDetails.saveConfirmTitle'),
    message: t('admin.userDetails.saveConfirmText'),
    rejectLabel: t('admin.userDetails.cancel'),
    acceptLabel: t('admin.userDetails.confirmSave'),
    accept: async () => {
      savingAccess.value = true
      try {
        const updated = await updateAdminUserAccess(userId.value, request)
        details.value = updated
        accountDirty.value = false
        toast.add({ severity: 'success', summary: '', detail: t('admin.userDetails.saveSuccess'), life: 3000 })
      } catch (e: any) {
        toast.add({ severity: 'error', summary: '', detail: e.message || t('admin.userDetails.saveFailed'), life: 3000 })
      } finally {
        savingAccess.value = false
      }
    }
  })
}

function confirmDelete() {
  confirm.require({
    header: t('admin.userDetails.deleteConfirmTitle'),
    message: t('admin.userDetails.deleteConfirmText'),
    rejectLabel: t('admin.userDetails.cancel'),
    acceptLabel: t('admin.userDetails.confirm'),
    accept: async () => {
      if (deleting.value) return
      deleting.value = true
      try {
        await deleteAdminUser(userId.value)
        toast.add({ severity: 'success', summary: '', detail: t('admin.userDetails.deleteSuccess'), life: 3000 })
        router.push('/admin/users')
      } catch (e: any) {
        toast.add({ severity: 'error', summary: '', detail: e.message || t('admin.userDetails.deleteFailed'), life: 3000 })
      } finally {
        deleting.value = false
      }
    }
  })
}

onMounted(() => {
  loadDetails()
})
</script>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: #F6F7FB;
}
.page-main {
  flex: 1;
  max-width: 960px;
  width: 100%;
  margin: 0 auto;
  padding: 1.5rem 1.5rem 2rem;
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}
.details-header {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}
.details-header h1 {
  font-family: 'Manrope', sans-serif;
  font-size: 1.5rem;
  font-weight: 700;
  color: #10233F;
  margin: 0;
}
.subtitle {
  margin: 0;
  color: #5D718B;
  font-size: 0.9rem;
}
.back-link {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  color: #2F6BFF;
  text-decoration: none;
  font-size: 0.85rem;
  margin-bottom: 0.25rem;
}
.back-link:hover { text-decoration: underline; }
.skeleton-block {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
  padding: 3rem 2rem;
  text-align: center;
}
.error-state h2 {
  font-family: 'Manrope', sans-serif;
  font-size: 1.2rem;
  color: #10233F;
  margin: 0;
}
.inline-error {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem;
  background: #FFF7ED;
  border: 1px solid #FDE68A;
  border-radius: 8px;
  color: #92400E;
  font-size: 0.9rem;
}
.delete-section {
  padding: 1.25rem;
  border: 1px solid #FCA5A5;
  border-radius: 8px;
  background: #FEF2F2;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}
.delete-section h3 {
  font-family: 'Manrope', sans-serif;
  font-size: 1rem;
  font-weight: 700;
  color: #991B1B;
  margin: 0;
}
.delete-section p {
  margin: 0;
  color: #7F1D1D;
  font-size: 0.85rem;
}
.delete-section.disabled {
  border-color: #E5E7EB;
  background: #F9FAFB;
}
.delete-section.disabled h3 { color: #8091A7; }
.self-delete-hint {
  font-style: italic;
  color: #5D718B !important;
}
</style>
