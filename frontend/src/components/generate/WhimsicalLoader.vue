<template>
  <div class="loader">
    <div class="spinner"></div>
    <slot />
  </div>
</template>

<style scoped>
.loader { display: flex; flex-direction: column; align-items: center; gap: 1rem; padding: 2rem; }
.spinner { width: 2rem; height: 2rem; border: 3px solid #e0e0e0; border-top-color: #3b82f6; border-radius: 50%; animation: spin 0.8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
</style>
