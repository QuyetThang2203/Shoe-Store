package com.example.shoestore.presentation.admin.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoestore.domain.repository.OrderRepository
import com.example.shoestore.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class AdminStatsViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _statsState = MutableStateFlow<StatsData>(StatsData())
    val statsState: StateFlow<StatsData> = _statsState

    // 0: All, 1: Month, 2: Today, 3: This Year (NEW)
    private val _filterType = MutableStateFlow(0)
    val filterType: StateFlow<Int> = _filterType

    init {
        calculateStats()
    }

    fun setFilter(type: Int) {
        _filterType.value = type
        calculateStats()
    }

    private fun calculateStats() {
        viewModelScope.launch {
            orderRepository.getAllOrders().collect { result ->
                if (result is Resource.Success) {
                    val allOrders = result.data ?: emptyList()

                    val filteredOrders = when(_filterType.value) {
                        1 -> allOrders.filter { isSameMonth(it.createdAt, Date()) }
                        2 -> allOrders.filter { isSameDay(it.createdAt, Date()) }
                        3 -> allOrders.filter { isSameYear(it.createdAt, Date()) } // Logic Năm
                        else -> allOrders
                    }

                    val revenue = filteredOrders.sumOf { it.totalPrice }
                    val count = filteredOrders.size
                    val delivered = filteredOrders.count { it.status == "DELIVERED" }

                    _statsState.value = StatsData(
                        totalRevenue = revenue,
                        totalOrders = count,
                        deliveredOrders = delivered,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun isSameMonth(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)
    }

    private fun isSameYear(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
    }
}

data class StatsData(
    val totalRevenue: Double = 0.0,
    val totalOrders: Int = 0,
    val deliveredOrders: Int = 0,
    val isLoading: Boolean = true
)