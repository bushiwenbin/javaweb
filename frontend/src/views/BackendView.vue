<script setup lang="ts">
import { ref } from 'vue'
import { backendApi, type EnvInfo } from '@/api/backend'

const loading = ref(false)
const error = ref('')
const helloResult = ref('')
const envResult = ref<EnvInfo | null>(null)

async function callHello() {
  loading.value = true
  error.value = ''
  try {
    const res = await backendApi.hello()
    helloResult.value = res.data
  } catch (e) {
    error.value = e instanceof Error ? e.message : String(e)
  } finally {
    loading.value = false
  }
}

async function callEnv() {
  loading.value = true
  error.value = ''
  try {
    const res = await backendApi.env()
    envResult.value = res.data
  } catch (e) {
    error.value = e instanceof Error ? e.message : String(e)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="demo">
    <h1>前后端联调自检</h1>
    <p class="sub">
      下面两个按钮会通过 Vite 代理请求 <code>/api/*</code>，由 Spring Boot 后端返回结果。
      能点出内容就说明「Vue 前端 → 代理 → Java 后端」整条链路是通的。
    </p>

    <div class="actions">
      <button type="button" :disabled="loading" @click="callHello">调用 /api/hello</button>
      <button type="button" :disabled="loading" @click="callEnv">调用 /api/env</button>
    </div>

    <p v-if="loading" class="state">请求中…</p>
    <p v-if="error" class="state err">
      {{ error }} —— 后端没起来？先在项目根目录跑 <code>mvn spring-boot:run</code>
    </p>

    <div v-if="helloResult" class="block">
      <h2>后端说</h2>
      <p class="out">{{ helloResult }}</p>
    </div>

    <div v-if="envResult" class="block">
      <h2>后端运行环境</h2>
      <dl>
        <dt>Java 版本</dt>
        <dd>{{ envResult.javaVersion }}</dd>
        <dt>JDK 供应商</dt>
        <dd>{{ envResult.javaVendor }}</dd>
        <dt>JAVA_HOME</dt>
        <dd>{{ envResult.javaHome }}</dd>
        <dt>操作系统</dt>
        <dd>{{ envResult.osName }}</dd>
      </dl>
    </div>
  </div>
</template>

<style scoped>
.demo {
  max-width: 640px;
  margin: 0 auto;
  padding: 0 1rem;
}

h1 {
  font-size: 1.35rem;
  margin-bottom: 0.6rem;
}

h2 {
  font-size: 0.95rem;
  margin: 0 0 0.5rem;
  color: var(--color-heading);
}

.sub {
  color: var(--color-text);
  opacity: 0.8;
  font-size: 0.9rem;
  line-height: 1.7;
  margin-bottom: 1.4rem;
}

code {
  background: var(--color-background-mute);
  padding: 0.1rem 0.35rem;
  border-radius: 4px;
  font-size: 0.85em;
}

.actions {
  display: flex;
  gap: 0.7rem;
  flex-wrap: wrap;
}

button {
  font: inherit;
  font-size: 0.9rem;
  padding: 0.5rem 1.1rem;
  border-radius: 8px;
  border: 1px solid var(--color-border-hover);
  background: var(--color-background-soft);
  color: var(--color-text);
  cursor: pointer;
  transition: border-color 0.2s;
}

button:hover:not(:disabled) {
  border-color: #42b883;
}

button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.state {
  font-size: 0.85rem;
  margin-top: 0.9rem;
}

.err {
  color: #d03050;
}

.block {
  margin-top: 1.5rem;
  padding: 1rem 1.2rem;
  border: 1px solid var(--color-border);
  border-radius: 10px;
  background: var(--color-background-soft);
}

.out {
  font-size: 0.95rem;
  margin: 0;
  color: #42b883;
  font-weight: 600;
}

dl {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 0.45rem 1.2rem;
  margin: 0;
  font-size: 0.85rem;
}

dt {
  opacity: 0.65;
}

dd {
  margin: 0;
  font-family: Consolas, Monaco, monospace;
}
</style>
