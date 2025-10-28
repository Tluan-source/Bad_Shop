/**
 * Vendor Analytics JavaScript
 * Handles charts and analytics visualization
 */

document.addEventListener('DOMContentLoaded', function() {
    initAnalyticsCharts();
    initDateRangePicker();
});

function initAnalyticsCharts() {
    // Initialize monthly revenue chart
    const monthlyChartCanvas = document.getElementById('monthlyRevenueChart');
    if (monthlyChartCanvas && typeof Chart !== 'undefined') {
        createMonthlyRevenueChart(monthlyChartCanvas);
    }
    
    // Initialize daily revenue chart
    const dailyChartCanvas = document.getElementById('dailyRevenueChart');
    if (dailyChartCanvas && typeof Chart !== 'undefined') {
        createDailyRevenueChart(dailyChartCanvas);
    }
    
    // Initialize top products chart
    const topProductsCanvas = document.getElementById('topProductsChart');
    if (topProductsCanvas && typeof Chart !== 'undefined') {
        createTopProductsChart(topProductsCanvas);
    }
}

function createMonthlyRevenueChart(canvas) {
    const ctx = canvas.getContext('2d');
    
    // Get data from data attributes or hidden elements
    const labels = getChartLabels('monthly-labels');
    const data = getChartData('monthly-data');
    
    new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Doanh thu (VNĐ)',
                data: data,
                backgroundColor: 'rgba(54, 162, 235, 0.6)',
                borderColor: 'rgba(54, 162, 235, 1)',
                borderWidth: 2,
                borderRadius: 5
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: true,
                    position: 'top'
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            return 'Doanh thu: ' + formatCurrency(context.parsed.y);
                        }
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        callback: function(value) {
                            return formatCurrencyShort(value);
                        }
                    }
                }
            }
        }
    });
}

function createDailyRevenueChart(canvas) {
    const ctx = canvas.getContext('2d');
    
    const labels = getChartLabels('daily-labels');
    const data = getChartData('daily-data');
    
    new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [{
                label: 'Doanh thu (VNĐ)',
                data: data,
                borderColor: 'rgb(75, 192, 192)',
                backgroundColor: 'rgba(75, 192, 192, 0.1)',
                tension: 0.4,
                fill: true,
                pointRadius: 4,
                pointHoverRadius: 6
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: true,
                    position: 'top'
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            return 'Doanh thu: ' + formatCurrency(context.parsed.y);
                        }
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        callback: function(value) {
                            return formatCurrencyShort(value);
                        }
                    }
                }
            }
        }
    });
}

function createTopProductsChart(canvas) {
    const ctx = canvas.getContext('2d');
    
    const labels = getChartLabels('products-labels');
    const data = getChartData('products-data');
    
    new Chart(ctx, {
        type: 'horizontalBar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Số lượng đã bán',
                data: data,
                backgroundColor: [
                    'rgba(255, 99, 132, 0.6)',
                    'rgba(54, 162, 235, 0.6)',
                    'rgba(255, 206, 86, 0.6)',
                    'rgba(75, 192, 192, 0.6)',
                    'rgba(153, 102, 255, 0.6)',
                    'rgba(255, 159, 64, 0.6)',
                    'rgba(199, 199, 199, 0.6)',
                    'rgba(83, 102, 255, 0.6)',
                    'rgba(255, 99, 255, 0.6)',
                    'rgba(99, 255, 132, 0.6)'
                ],
                borderWidth: 2
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            indexAxis: 'y',
            plugins: {
                legend: {
                    display: false
                }
            },
            scales: {
                x: {
                    beginAtZero: true,
                    ticks: {
                        stepSize: 1
                    }
                }
            }
        }
    });
}

function initDateRangePicker() {
    const fromDate = document.getElementById('fromDate');
    const toDate = document.getElementById('toDate');
    const filterBtn = document.getElementById('applyFilter');
    
    if (filterBtn) {
        filterBtn.addEventListener('click', function() {
            const from = fromDate ? fromDate.value : '';
            const to = toDate ? toDate.value : '';
            
            if (from && to && new Date(from) > new Date(to)) {
                alert('Ngày bắt đầu phải nhỏ hơn ngày kết thúc!');
                return;
            }
            
            applyDateFilter(from, to);
        });
    }
}

function applyDateFilter(fromDate, toDate) {
    const url = new URL(window.location.href);
    
    if (fromDate) {
        url.searchParams.set('fromDate', fromDate);
    } else {
        url.searchParams.delete('fromDate');
    }
    
    if (toDate) {
        url.searchParams.set('toDate', toDate);
    } else {
        url.searchParams.delete('toDate');
    }
    
    window.location.href = url.toString();
}

function getChartLabels(elementId) {
    const element = document.getElementById(elementId);
    if (element) {
        try {
            return JSON.parse(element.textContent || element.value || '[]');
        } catch (e) {
            console.error('Error parsing chart labels:', e);
            return [];
        }
    }
    return [];
}

function getChartData(elementId) {
    const element = document.getElementById(elementId);
    if (element) {
        try {
            return JSON.parse(element.textContent || element.value || '[]');
        } catch (e) {
            console.error('Error parsing chart data:', e);
            return [];
        }
    }
    return [];
}

function formatCurrency(value) {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(value);
}

function formatCurrencyShort(value) {
    if (value >= 1000000000) {
        return (value / 1000000000).toFixed(1) + 'B';
    } else if (value >= 1000000) {
        return (value / 1000000).toFixed(1) + 'M';
    } else if (value >= 1000) {
        return (value / 1000).toFixed(1) + 'K';
    }
    return value.toString();
}

// Export for global use
window.vendorAnalytics = {
    applyDateFilter,
    formatCurrency,
    formatCurrencyShort
};
