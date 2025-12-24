<template>
  <div class="buyer-cart">
    <page-header title="买家中心 - 我的购物车">
      <el-button type="text" @click="logout" style="color: #e64340;">退出登录</el-button>
    </page-header>

    <el-table :data="items" border style="margin-top: 20px;">
      <el-table-column prop="bookId" label="教材ID" width="100" />
      <el-table-column prop="bookName" label="教材名称" />
      <el-table-column prop="sellPrice" label="售价" width="120" />
      <el-table-column prop="quantity" label="数量" width="100" />
      <el-table-column label="操作" width="440">
        <template #default="scope">
          <el-button type="text" size="small" @click="toDetail(scope.row.bookId)">查看详情</el-button>
          <el-button type="danger" size="small" @click="removeItem(scope.row)" style="margin-left:8px;">移除</el-button>
          <el-button type="primary" size="small" @click="orderAndPay(scope.row.bookId)" style="margin-left:8px;">下单并付款</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div style="margin-top: 10px;">
      <el-button type="warning" @click="clear">清空购物车</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import PageHeader from '@/components/PageHeader.vue'
import { logoutAndBackToLogin } from '@/utils/auth.js'
import { listCart, removeFromCart, clearCart } from '@/api/cartApi'
import { createOrder, payOrder } from '@/api/orderApi'

const items = ref([])
const router = useRouter()
const logout = () => logoutAndBackToLogin()

const load = async () => {
  try { items.value = await listCart() || [] } catch { ElMessage.error('加载购物车失败') }
}

const toDetail = (bookId) => {
  router.push(`/book/${bookId}`)
}

const removeItem = async (row) => {
  const bookId = row?.bookId
  const qty = Number(row?.quantity || 0)
  if (!bookId) return
  if (qty > 1) {
    try {
      const { value } = await ElMessageBox.prompt('请输入要移除的数量', '移除教材数量', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputType: 'number',
        inputValue: '1',
        inputValidator: (v) => {
          const n = Number(v)
          if (!Number.isInteger(n)) return '请输入整数'
          if (n <= 0) return '数量必须大于 0'
          if (n > qty) return `最多可移除 ${qty}`
          return true
        }
      })
      const count = Number(value)
      await removeFromCart(bookId, count)
      ElMessage.success('移除成功')
      load()
    } catch {}
    return
  }
  try {
    await removeFromCart(bookId)
    ElMessage.success('移除成功')
    load()
  } catch {
    ElMessage.error('移除失败')
  }
}

const clear = async () => {
  try { await clearCart(); ElMessage.success('已清空'); load() } catch { ElMessage.error('清空失败') }
}

const orderAndPay = async (bookId) => {
  try {
    const order = await createOrder(bookId)
    if (!order || !order.id) { ElMessage.error('下单失败'); return }
    const paid = await payOrder(order.id)
    if (!paid || paid.status !== 'paid') { ElMessage.error('付款失败'); return }
    ElMessage.success('下单并付款成功')
    load()
  } catch {
    ElMessage.error('下单或付款失败')
  }
}

onMounted(load)
</script>

<style scoped>
.buyer-cart { max-width: 1200px; margin: 0 auto; padding: 20px; }
</style>
