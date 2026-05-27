import { defineStore } from 'pinia'
import { ref } from 'vue'
import { otaApi } from '../api'

export const useOtaStore = defineStore('ota', () => {
  const orders = ref([])
  const loading = ref(false)
  const error = ref(null)

  const fetchPendingOrders = async (channel = null) => {
    loading.value = true
    error.value = null
    try {
      const response = channel
        ? await otaApi.getPendingOrders(channel)
        : await otaApi.getAllPendingOrders()
      orders.value = response.data.data || response.data
    } catch (e) {
      error.value = e.message
    } finally {
      loading.value = false
    }
  }

  const syncOrder = async (data) => {
    loading.value = true
    error.value = null
    try {
      const response = await otaApi.syncOrder(data)
      return response.data
    } catch (e) {
      error.value = e.message
      throw e
    } finally {
      loading.value = false
    }
  }

  const generateTickets = async (orderId) => {
    loading.value = true
    error.value = null
    try {
      const response = await otaApi.generateTickets(orderId)
      return response.data
    } catch (e) {
      error.value = e.message
      throw e
    } finally {
      loading.value = false
    }
  }

  return {
    orders,
    loading,
    error,
    fetchPendingOrders,
    syncOrder,
    generateTickets
  }
})
