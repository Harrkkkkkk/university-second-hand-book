<!--
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: Cart.vue
 * Author: WiseBookPal Team Version: 1.0 Date: 2026-01-02
 * Description: Buyer shopping cart page.
 *              Manages cart items (list, remove, clear) and initiates checkout.
 * History:
 * 1. Date: 2026-01-02
 *    Author: WiseBookPal Team
 *    Modification: Initial implementation
-->
<template>
  <div class="buyer-cart">
    <div class="page-header-card">
       <h2 class="page-title">我的购物车</h2>
       <p class="page-subtitle">管理您的待购教材</p>
    </div>

    <div class="cart-container">
      <el-card class="cart-card" shadow="hover">
        <template #header>
           <div class="card-header">
             <span>共 {{ items.length }} 件商品</span>
             <el-button type="danger" link @click="clear" v-if="items.length > 0">清空购物车</el-button>
           </div>
        </template>
        
        <el-table :data="items" style="width: 100%" :header-cell-style="{background:'#f5f7fa', color:'#606266'}">
          <el-table-column label="教材信息" min-width="300">
            <template #default="scope">
                  <div class="book-info-cell">
                  <div class="book-icon-wrapper">
                    <el-icon><Reading /></el-icon>
                  </div>
                  <div class="book-meta">
                    <span class="book-name" @click="toDetail(scope.row.bookId)">{{ scope.row.bookName }}</span>
                    <span class="book-id">ID: {{ scope.row.bookId }}</span>
                  </div>
               </div>
            </template>
          </el-table-column>
          <el-table-column prop="sellPrice" label="单价" width="150">
             <template #default="scope">
               <span class="price-text">¥{{ scope.row.sellPrice }}</span>
             </template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="120" align="center" />
          <el-table-column label="操作" width="250" fixed="right">
            <template #default="scope">
              <div class="action-buttons">
                <el-button type="primary" size="small" round @click="orderAndPay(scope.row.bookId)">
                  <el-icon><CreditCard /></el-icon> 下单
                </el-button>
                <el-button type="danger" size="small" circle :icon="Delete" @click="removeItem(scope.row)" plain></el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
        
        <div class="cart-empty" v-if="items.length === 0">
           <el-empty description="购物车空空如也，快去选购吧！">
             <el-button type="primary" @click="$router.push('/buyer/home')">去逛逛</el-button>
           </el-empty>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { logoutAndBackToLogin } from '@/utils/auth.js'
import { listCart, removeFromCart, clearCart } from '@/api/cartApi'
import { createOrder, payOrder } from '@/api/orderApi'
import { CreditCard, Delete, Reading } from '@element-plus/icons-vue'

const items = ref([])
const router = useRouter()
const logout = () => logoutAndBackToLogin()

/**
 * Function: load
 * Description: Fetches current user's cart items.
 */
const load = async () => {
  try { items.value = await listCart() || [] } catch { ElMessage.error('加载购物车失败') }
}

const toDetail = (bookId) => {
  router.push(`/book/${bookId}`)
}

/**
 * Function: removeItem
 * Description: Removes an item (or specific quantity) from the cart.
 *              Prompts for quantity if item count > 1.
 * Input: row (Object) - Cart item
 */
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

/**
 * Function: clear
 * Description: Removes all items from the cart.
 */
const clear = async () => {
  try { await clearCart(); ElMessage.success('已清空'); load() } catch { ElMessage.error('清空失败') }
}

/**
 * Function: orderAndPay
 * Description: Creates an order for a single cart item and processes payment.
 * Input: bookId (Number)
 */
const orderAndPay = async (bookId) => {
  // Check identity
  const isVerified = sessionStorage.getItem('isVerified') === '1'
  if (!isVerified) {
    ElMessageBox.confirm(
      '下单前需要先完成实名认证，是否前往认证？',
      '提示',
      {
        confirmButtonText: '去认证',
        cancelButtonText: '取消',
        type: 'warning'
      }
    ).then(() => {
      router.push('/verify-identity?redirect=/buyer/cart')
    }).catch(() => {})
    return
  }

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
.buyer-cart {
  min-height: calc(100vh - 64px);
  background-color: #f5f7fa;
  padding: 20px;
}

.page-header-card {
  background: linear-gradient(135deg, #409EFF 0%, #3a8ee6 100%);
  border-radius: 12px;
  padding: 30px;
  color: white;
  margin-bottom: 24px;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.page-title {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
}

.page-subtitle {
  margin: 8px 0 0;
  opacity: 0.9;
  font-size: 14px;
}

.cart-container {
  max-width: 1200px;
  margin: 0 auto;
}

.cart-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.book-info-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.book-icon-wrapper {
  width: 40px;
  height: 40px;
  background-color: #ecf5ff;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #409EFF;
  font-size: 20px;
}

.book-meta {
  display: flex;
  flex-direction: column;
}

.book-name {
  font-weight: 600;
  color: #303133;
  cursor: pointer;
  transition: color 0.2s;
}

.book-name:hover {
  color: #409EFF;
}

.book-id {
  font-size: 12px;
  color: #909399;
}

.price-text {
  color: #F56C6C;
  font-weight: 700;
  font-size: 16px;
}

.action-buttons {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
