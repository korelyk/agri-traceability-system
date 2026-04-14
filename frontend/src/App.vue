<template>
  <router-view />
</template>

<script>
export default {
  name: 'App'
}
</script>

<style>
:root {
  --agri-bg: #f5f7f2;
  --agri-surface: rgba(255, 255, 255, 0.92);
  --agri-surface-strong: #ffffff;
  --agri-line: rgba(35, 67, 52, 0.1);
  --agri-shadow: 0 20px 45px rgba(28, 47, 39, 0.08);
  --agri-shadow-soft: 0 12px 24px rgba(28, 47, 39, 0.06);
  --agri-text: #193026;
  --agri-text-soft: #607065;
  --agri-primary: #2f7d58;
  --agri-primary-deep: #1f5c41;
  --agri-accent: #c48a43;
  --agri-warm: #f6efe2;
  --agri-cool: #edf6f2;
  --agri-danger: #c95b53;
}

* {
  box-sizing: border-box;
}

html,
body,
#app {
  min-height: 100%;
}

body {
  margin: 0;
  color: var(--agri-text);
  background:
    radial-gradient(circle at top left, rgba(98, 166, 119, 0.14), transparent 28%),
    radial-gradient(circle at top right, rgba(196, 138, 67, 0.12), transparent 22%),
    linear-gradient(180deg, #f6f9f4 0%, #eef4ef 100%);
  font-family: "HarmonyOS Sans SC", "Alibaba PuHuiTi 3.0", "PingFang SC", "Microsoft YaHei", sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

a {
  color: inherit;
  text-decoration: none;
}

code {
  font-family: "JetBrains Mono", "Cascadia Code", "Consolas", monospace;
}

.page-container,
.page-shell {
  width: 100%;
}

.page-shell {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.page-hero,
.section-card,
.glass-card {
  border: 1px solid var(--agri-line);
  border-radius: 24px;
  background: var(--agri-surface);
  box-shadow: var(--agri-shadow);
  backdrop-filter: blur(14px);
}

.page-hero {
  position: relative;
  overflow: hidden;
  padding: 28px 30px;
}

.page-hero::before,
.page-hero::after {
  content: '';
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
}

.page-hero::before {
  width: 220px;
  height: 220px;
  top: -120px;
  right: -50px;
  background: radial-gradient(circle, rgba(47, 125, 88, 0.18) 0%, rgba(47, 125, 88, 0) 72%);
}

.page-hero::after {
  width: 180px;
  height: 180px;
  left: -40px;
  bottom: -100px;
  background: radial-gradient(circle, rgba(196, 138, 67, 0.16) 0%, rgba(196, 138, 67, 0) 72%);
}

.hero-eyebrow {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 14px;
  border-radius: 999px;
  background: rgba(47, 125, 88, 0.12);
  color: var(--agri-primary-deep);
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.04em;
}

.hero-title {
  margin: 16px 0 10px;
  font-size: clamp(30px, 4vw, 40px);
  line-height: 1.15;
  letter-spacing: -0.02em;
}

.hero-subtitle {
  max-width: 760px;
  margin: 0;
  color: var(--agri-text-soft);
  line-height: 1.8;
  font-size: 15px;
}

.section-card {
  padding: 22px 24px;
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.section-title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
}

.section-desc {
  margin: 8px 0 0;
  color: var(--agri-text-soft);
  line-height: 1.7;
  font-size: 14px;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 16px;
}

.metric-card {
  padding: 18px 20px;
  border-radius: 20px;
  border: 1px solid rgba(47, 125, 88, 0.12);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.95), rgba(244, 248, 244, 0.95));
  box-shadow: var(--agri-shadow-soft);
}

.metric-label {
  color: var(--agri-text-soft);
  font-size: 13px;
}

.metric-value {
  margin-top: 10px;
  font-size: 30px;
  font-weight: 700;
  line-height: 1;
}

.metric-note {
  margin-top: 12px;
  color: var(--agri-text-soft);
  font-size: 13px;
  line-height: 1.6;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(12, minmax(0, 1fr));
  gap: 20px;
}

.detail-grid > * {
  grid-column: span 12;
}

.chip-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.info-chip {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid rgba(47, 125, 88, 0.12);
  color: var(--agri-text-soft);
  font-size: 13px;
}

.el-card {
  border: 1px solid var(--agri-line);
  border-radius: 22px;
  box-shadow: var(--agri-shadow-soft);
}

.el-card__header {
  padding: 20px 22px 0;
  border-bottom: none;
}

.el-card__body {
  padding: 22px;
}

.el-button {
  border-radius: 14px;
  font-weight: 600;
}

.el-button--primary {
  background: linear-gradient(135deg, var(--agri-primary), var(--agri-primary-deep));
  border-color: transparent;
}

.el-button--primary:hover,
.el-button--primary:focus {
  background: linear-gradient(135deg, #338963, #204f39);
  border-color: transparent;
}

.el-button--success {
  background: linear-gradient(135deg, #52936f, #386f53);
  border-color: transparent;
}

.el-tag {
  border-radius: 999px;
  font-weight: 600;
}

.el-input__wrapper,
.el-select__wrapper,
.el-textarea__inner {
  border-radius: 14px;
  box-shadow: 0 0 0 1px rgba(47, 125, 88, 0.08) inset;
}

.el-dialog {
  border-radius: 24px;
  overflow: hidden;
}

.el-dialog__header {
  padding: 22px 24px 0;
}

.el-dialog__body {
  padding: 20px 24px 24px;
}

.el-table {
  border-radius: 18px;
  overflow: hidden;
}

.el-table th.el-table__cell {
  background: #f5f8f4;
  color: var(--agri-text);
  font-weight: 700;
}

.el-descriptions {
  overflow: hidden;
  border-radius: 18px;
}

.el-descriptions__label {
  font-weight: 600;
  color: var(--agri-text-soft);
}

.el-alert {
  border-radius: 18px;
}

.text-success {
  color: #4d8c66;
}

.text-warning {
  color: #b98436;
}

.text-danger {
  color: var(--agri-danger);
}

.text-info {
  color: var(--agri-text-soft);
}

@media (max-width: 992px) {
  .page-hero,
  .section-card,
  .el-card__body {
    padding: 20px;
  }
}
</style>
