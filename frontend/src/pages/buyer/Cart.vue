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
    <div class="cart-header">
      <div class="header-content">
        <h2 class="title">我的购物车</h2>
        <span class="subtitle">共 {{ items.length }} 件待购商品</span>
      </div>
      <el-button 
        type="danger" 
        plain 
        round
        :icon="Delete" 
        @click="clear" 
        v-if="items.length > 0"
        class="clear-btn"
      >
        清空购物车
      </el-button>
    </div>

    <div class="cart-list" v-loading="loading">
      <transition-group name="list">
        <div v-for="item in items" :key="item.bookId" class="cart-item">
          <!-- Checkbox placeholder for future batch select -->
          <!-- <el-checkbox v-model="item.checked" class="item-checkbox" /> -->
          
          <div class="item-cover" @click="toDetail(item.bookId)">
            <img :src="item.coverUrl || `https://picsum.photos/seed/${item.bookId}/180/240`" alt="cover" loading="lazy" />
          </div>
          
          <div class="item-info">
            <div class="info-main">
              <h3 class="book-title" @click="toDetail(item.bookId)">{{ item.bookName }}</h3>
              <div class="book-tags">
                <el-tag size="small" type="info" effect="plain">ID: {{ item.bookId }}</el-tag>
                <el-tag size="small" type="success" effect="plain" v-if="item.stock > 0">有货</el-tag>
                <el-tag size="small" type="danger" effect="plain" v-else>暂时无货</el-tag>
              </div>
            </div>
            
            <div class="info-price">
              <span class="currency">¥</span>
              <span class="amount">{{ item.sellPrice }}</span>
            </div>
          </div>

          <div class="item-actions">
            <div class="quantity-box">
              <span class="qty-label">数量</span>
              <span class="qty-val">x{{ item.quantity }}</span>
            </div>
            
            <div class="btn-group">
              <el-button 
                type="primary" 
                round 
                class="buy-btn"
                @click="orderAndPay(item.bookId)"
              >
                立即购买
              </el-button>
              <el-button 
                type="danger" 
                circle 
                plain
                :icon="Delete" 
                class="del-btn"
                @click="removeItem(item)" 
              />
            </div>
          </div>
        </div>
      </transition-group>

      <div class="empty-state" v-if="items.length === 0 && !loading">
         <el-empty description="购物车空空如也" :image-size="200">
           <template #extra>
             <el-button type="primary" size="large" round @click="$router.push('/buyer/home')">
               去选购心仪教材
             </el-button>
           </template>
         </el-empty>
      </div>
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
import { Delete } from '@element-plus/icons-vue'

const items = ref([])
const loading = ref(false)
const router = useRouter()
const logout = () => logoutAndBackToLogin()

/**
 * Function: load
 * Description: Fetches current user's cart items.
 */
const load = async () => {
  loading.value = true
  try { items.value = await listCart() || [] } 
  catch { ElMessage.error('加载购物车失败') }
  finally { loading.value = false }
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
  
  const confirmRemove = async (count = null) => {
    try {
      if (count !== null) {
        await removeFromCart(bookId, count)
      } else {
        await removeFromCart(bookId)
      }
      ElMessage.success('移除成功')
      load()
    } catch {
      ElMessage.error('移除失败')
    }
  }

  if (qty > 1) {
    try {
      const { value } = await ElMessageBox.prompt('请输入要移除的数量', '移除教材', {
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
      confirmRemove(Number(value))
    } catch {}
  } else {
    ElMessageBox.confirm('确定要移除这本教材吗？', '提示', {
      confirmButtonText: '移除',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      confirmRemove()
    }).catch(() => {})
  }
}

/**
 * Function: clear
 * Description: Removes all items from the cart.
 */
const clear = async () => {
  ElMessageBox.confirm('确定要清空购物车吗？', '警告', {
    confirmButtonText: '清空',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try { await clearCart(); ElMessage.success('已清空'); load() } catch { ElMessage.error('清空失败') }
  }).catch(() => {})
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
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
  min-height: 80vh;
}

.cart-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 24px;
  padding: 0 10px;
}

.header-content .title {
  font-size: 28px;
  color: #303133;
  margin: 0 0 4px 0;
  font-weight: 600;
}

.header-content .subtitle {
  color: #909399;
  font-size: 14px;
}

.cart-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.cart-item {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 24px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid transparent;
}

.cart-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  border-color: #ecf5ff;
}

.item-cover {
  width: 90px;
  height: 120px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.item-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s;
}

.item-cover:hover img {
  transform: scale(1.08);
}

.item-info {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-right: 20px;
}

.info-main {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.book-title {
  margin: 0;
  font-size: 18px;
  color: #303133;
  cursor: pointer;
  font-weight: 600;
  line-height: 1.4;
}

.book-title:hover {
  color: #409EFF;
}

.book-tags {
  display: flex;
  gap: 8px;
}

.info-price {
  text-align: right;
  color: #f56c6c;
  font-weight: 700;
}

.info-price .currency {
  font-size: 14px;
  margin-right: 2px;
}

.info-price .amount {
  font-size: 24px;
}

.item-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 16px;
  padding-left: 24px;
  border-left: 1px solid #f0f0f0;
}

.quantity-box {
  color: #909399;
  font-size: 14px;
}

.btn-group {
  display: flex;
  align-items: center;
  gap: 12px;
}

.buy-btn {
  padding: 10px 24px;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
  transition: all 0.3s;
}

.buy-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.4);
}

.del-btn {
  font-size: 16px;
}

.list-enter-active,
.list-leave-active {
  transition: all 0.4s ease;
}

.list-enter-from,
.list-leave-to {
  opacity: 0;
  transform: translateX(20px);
}

@media (max-width: 768px) {
  .cart-item {
    flex-direction: column;
    align-items: flex-start;
    padding: 16px;
  }
  
  .item-info {
    width: 100%;
    margin: 0;
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .item-actions {
    width: 100%;
    flex-direction: row;
    justify-content: space-between;
    align-items: center;
    padding-left: 0;
    border-left: none;
    border-top: 1px solid #f0f0f0;
    padding-top: 12px;
  }
}
</style>
