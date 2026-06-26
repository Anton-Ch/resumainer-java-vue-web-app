<template>
  <div class="account-tab">
    <div class="detail-field">
      <span class="detail-label">{{ $t('admin.userDetails.accountEmail') }}</span>
      <span class="detail-value read-only">{{ account.accountEmail }}</span>
    </div>
    <div class="detail-field">
      <span class="detail-label">{{ $t('admin.userDetails.role') }}</span>
      <Select
        v-model="dirty.roleCode"
        :options="roleOptions"
        optionLabel="label"
        optionValue="value"
        class="detail-select"
        :disabled="disableRole"
      />
      <span v-if="disableRole" class="self-hint">{{ $t('admin.userDetails.selfDemotionHint') }}</span>
    </div>
    <div class="detail-field">
      <span class="detail-label">{{ $t('admin.userDetails.status') }}</span>
      <Select
        v-model="dirty.statusCode"
        :options="statusOptions"
        optionLabel="label"
        optionValue="value"
        class="detail-select"
        :disabled="disableStatus"
      />
      <span v-if="disableStatus" class="self-hint">{{ $t('admin.userDetails.selfBlockHint') }}</span>
    </div>
    <div class="detail-field">
      <span class="detail-label">{{ $t('admin.userDetails.permission') }}</span>
      <Select
        v-model="dirty.permissionCode"
        :options="permissionOptions"
        optionLabel="label"
        optionValue="value"
        class="detail-select"
      />
    </div>
    <div class="detail-field">
      <span class="detail-label">{{ $t('admin.userDetails.isPrivileged') }}</span>
      <div class="privileged-control">
        <InputSwitch v-model="dirty.isPrivileged" />
        <span class="privileged-label">{{ dirty.isPrivileged ? $t('admin.userDetails.privileged') : $t('admin.userDetails.nonPrivileged') }}</span>
      </div>
    </div>
    <div class="tab-actions">
      <Button
        :label="$t('admin.userDetails.save')"
        icon="pi pi-check"
        class="p-button-success"
        :disabled="!hasChanges || saving"
        :loading="saving"
        @click="onSave"
      />
      <Button
        v-if="hasChanges"
        :label="$t('admin.userDetails.cancel')"
        icon="pi pi-times"
        class="p-button-text"
        :disabled="saving"
        @click="resetChanges"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import Select from 'primevue/select'
import InputSwitch from 'primevue/inputswitch'
import Button from 'primevue/button'
import type { AdminUserAccount, AdminUserAccessUpdateRequest } from '@/types/admin'

const props = defineProps<{
  account: AdminUserAccount
  isCurrentAdmin: boolean
  saving: boolean
}>()

const emit = defineEmits<{
  save: [request: AdminUserAccessUpdateRequest]
  'update:dirty': [isDirty: boolean]
}>()

const { t, locale } = useI18n()

const roleOptions = computed(() => [
  { value: 'USER', label: locale.value === 'ru' ? 'Пользователь' : 'User' },
  { value: 'ADMIN', label: locale.value === 'ru' ? 'Администратор' : 'Admin' },
])

const statusOptions = computed(() => [
  { value: 'ACTIVE', label: locale.value === 'ru' ? 'Активен' : 'Active' },
  { value: 'BLOCKED', label: locale.value === 'ru' ? 'Заблокирован' : 'Blocked' },
])

const permissionOptions = computed(() => [
  { value: 'ALLOWED', label: locale.value === 'ru' ? 'Разрешено' : 'Allowed' },
  { value: 'FORBIDDEN', label: locale.value === 'ru' ? 'Запрещено' : 'Forbidden' },
])

const disableRole = computed(() =>
  props.isCurrentAdmin && props.account.roleCode === 'ADMIN'
)

const disableStatus = computed(() =>
  props.isCurrentAdmin && props.account.statusCode === 'ACTIVE'
)

const initial = reactive({ roleCode: '', statusCode: '', permissionCode: '', isPrivileged: false })
const dirty = reactive({ roleCode: '', statusCode: '', permissionCode: '', isPrivileged: false })

watch(() => props.account, (a) => {
  initial.roleCode = a.roleCode
  initial.statusCode = a.statusCode
  initial.permissionCode = a.permissionCode
  initial.isPrivileged = a.isPrivileged
  dirty.roleCode = a.roleCode
  dirty.statusCode = a.statusCode
  dirty.permissionCode = a.permissionCode
  dirty.isPrivileged = a.isPrivileged
}, { immediate: true })

const hasChanges = computed(() =>
  dirty.roleCode !== initial.roleCode
  || dirty.statusCode !== initial.statusCode
  || dirty.permissionCode !== initial.permissionCode
  || dirty.isPrivileged !== initial.isPrivileged
)

watch(hasChanges, (val) => {
  emit('update:dirty', val)
})

function resetChanges() {
  dirty.roleCode = initial.roleCode
  dirty.statusCode = initial.statusCode
  dirty.permissionCode = initial.permissionCode
  dirty.isPrivileged = initial.isPrivileged
}

function onSave() {
  if (!hasChanges.value || props.saving) return
  emit('save', {
    roleCode: dirty.roleCode,
    statusCode: dirty.statusCode,
    permissionCode: dirty.permissionCode,
    isPrivileged: dirty.isPrivileged
  })
}
</script>

<style scoped>
.account-tab {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  max-width: 480px;
}
.detail-field {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}
.detail-label {
  font-size: 0.75rem;
  font-weight: 600;
  color: #8091A7;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}
.detail-value {
  font-size: 0.9rem;
  color: #10233F;
  padding: 0.5rem 0;
}
.detail-value.read-only {
  font-weight: 600;
}
.detail-select {
  max-width: 280px;
}
.privileged-control {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}
.privileged-label {
  font-size: 0.85rem;
  color: #10233F;
}
.self-hint {
  font-size: 0.75rem;
  color: #D97706;
  font-style: italic;
}
.tab-actions {
  display: flex;
  gap: 0.5rem;
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid #E5E7EB;
}
</style>
