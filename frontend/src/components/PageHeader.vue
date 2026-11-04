<template>
  <div class="page-header">
    <div>
      <h2 class="page-header-title">{{ title }}</h2>
      <p v-if="description" class="page-header-desc">{{ description }}</p>
    </div>
    <div class="page-header-extra">
      <slot name="extra" />
    </div>
  </div>
</template>

<script setup>
defineProps({
  title: { type: String, required: true },
  description: { type: String, default: '' }
})
</script>

<style scoped lang="scss">
.page-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: var(--space-block);

  .page-header-title {
    font-size: 22px;
    font-weight: 600;
    color: var(--text-primary);
    margin: 0;
  }

  .page-header-desc {
    font-size: 13px;
    color: var(--text-secondary);
    margin: 6px 0 0;
  }
}
</style>
