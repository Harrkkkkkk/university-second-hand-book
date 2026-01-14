<!--
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: Statistics.vue
 * Author: WiseBookPal Team Version: 1.0 Date: 2026-01-11
 * Description: Admin statistics dashboard component.
 *              Displays charts for Daily Active Users (DAU) and Transaction Volume.
 *              Supports filtering by date range and exporting data to CSV.
-->
<template>
  <div class="statistics-container">
    <div class="filter-bar">
      <el-radio-group v-model="days" @change="loadData">
        <el-radio-button :label="7">近7天</el-radio-button>
        <el-radio-button :label="30">近30天</el-radio-button>
      </el-radio-group>
      <el-button type="primary" @click="handleExport" :loading="exporting">导出数据</el-button>
    </div>

    <div class="charts-container" v-loading="loading">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <span>日活跃用户 (DAU)</span>
              </div>
            </template>
            <div ref="dauChartRef" style="height: 350px;"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <span>交易统计</span>
              </div>
            </template>
            <div ref="transChartRef" style="height: 350px;"></div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { getDashboardStats, exportStats } from '@/api/adminApi'
import { ElMessage } from 'element-plus'

const days = ref(7)
const loading = ref(false)
const exporting = ref(false)
const dauChartRef = ref(null)
const transChartRef = ref(null)
let dauChart = null
let transChart = null

const initCharts = () => {
  if (dauChartRef.value) {
    dauChart = echarts.init(dauChartRef.value)
  }
  if (transChartRef.value) {
    transChart = echarts.init(transChartRef.value)
  }
}

const resizeCharts = () => {
  dauChart && dauChart.resize()
  transChart && transChart.resize()
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getDashboardStats(days.value)
    // res is array of {date, dau, transactionCount, transactionAmount}
    
    const dates = res.map(item => item.date)
    const daus = res.map(item => item.dau)
    const counts = res.map(item => item.transactionCount)
    const amounts = res.map(item => item.transactionAmount)

    // Render DAU Chart
    dauChart.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'category', boundaryGap: false, data: dates },
      yAxis: { type: 'value' },
      series: [{ 
        name: 'DAU', 
        type: 'line', 
        data: daus, 
        smooth: true,
        areaStyle: { opacity: 0.3 },
        itemStyle: { color: '#409EFF' }
      }]
    })

    // Render Transaction Chart
    transChart.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['交易笔数', '交易金额'] },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'category', data: dates },
      yAxis: [
        { type: 'value', name: '笔数' },
        { type: 'value', name: '金额' }
      ],
      series: [
        { 
          name: '交易笔数', 
          type: 'bar', 
          data: counts, 
          yAxisIndex: 0,
          itemStyle: { color: '#67C23A' }
        },
        { 
          name: '交易金额', 
          type: 'line', 
          data: amounts, 
          yAxisIndex: 1, 
          smooth: true,
          itemStyle: { color: '#E6A23C' }
        }
      ]
    })
  } catch (e) {
    console.error(e)
    ElMessage.error('加载统计数据失败')
  } finally {
    loading.value = false
  }
}

const handleExport = async () => {
  exporting.value = true
  try {
    const blob = await exportStats(days.value)
    const url = window.URL.createObjectURL(new Blob([blob]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `statistics_${days.value}days.csv`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  } catch (e) {
    ElMessage.error('导出失败')
  } finally {
    exporting.value = false
  }
}

onMounted(() => {
  nextTick(() => {
    initCharts()
    loadData()
    window.addEventListener('resize', resizeCharts)
  })
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeCharts)
  if (dauChart) dauChart.dispose()
  if (transChart) transChart.dispose()
})
</script>

<style scoped>
.filter-bar {
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
