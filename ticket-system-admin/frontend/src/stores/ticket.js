import { defineStore } from 'pinia'
import { ref } from 'vue'
import { ticketApi } from '../api'

export const useTicketStore = defineStore('ticket', () => {
  const tickets = ref([])
  const passes = ref([])
  const passesTotal = ref(0)
  const loading = ref(false)
  const error = ref(null)

  const fetchPasses = async (params = {}) => {
    loading.value = true
    error.value = null
    try {
      const response = await ticketApi.getAllPasses(params)
      passes.value = response.data.data || response.data || []
      passesTotal.value = response.data.totalElements || passes.value.length
    } catch (e) {
      error.value = e.message
    } finally {
      loading.value = false
    }
  }

  const fetchTickets = async (params = {}) => {
    loading.value = true
    error.value = null
    try {
      const response = await ticketApi.getAllTickets(params)
      tickets.value = response.data.data || response.data
    } catch (e) {
      error.value = e.message
    } finally {
      loading.value = false
    }
  }

  const createPass = async (data) => {
    loading.value = true
    error.value = null
    try {
      const response = await ticketApi.createPass(data)
      return response.data
    } catch (e) {
      error.value = e.message
      throw e
    } finally {
      loading.value = false
    }
  }

  const activatePass = async (id) => {
    try {
      const response = await ticketApi.activatePass(id)
      return response.data
    } catch (e) {
      error.value = e.message
      throw e
    }
  }

  const suspendPass = async (id) => {
    try {
      const response = await ticketApi.suspendPass(id)
      return response.data
    } catch (e) {
      error.value = e.message
      throw e
    }
  }

  const cancelPass = async (id) => {
    try {
      const response = await ticketApi.cancelPass(id)
      return response.data
    } catch (e) {
      error.value = e.message
      throw e
    }
  }

  const createSingleTickets = async (data) => {
    loading.value = true
    error.value = null
    try {
      const response = await ticketApi.createSingleTickets(data)
      return response.data
    } catch (e) {
      error.value = e.message
      throw e
    } finally {
      loading.value = false
    }
  }

  const verifyTicket = async (data) => {
    try {
      const response = await ticketApi.verifyTicket(data)
      return response.data
    } catch (e) {
      error.value = e.message
      throw e
    }
  }

  return {
    tickets,
    passes,
    passesTotal,
    loading,
    error,
    fetchPasses,
    fetchTickets,
    createPass,
    activatePass,
    suspendPass,
    cancelPass,
    createSingleTickets,
    verifyTicket
  }
})
