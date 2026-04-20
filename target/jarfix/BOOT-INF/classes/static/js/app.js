/* Food Waste Management System - Main JS */
document.addEventListener('DOMContentLoaded', function() {
    initToasts();
    initNotificationPolling();
    initSidebarToggle();
    initDeleteConfirmations();
    initSearchDebounce();
});

function initToasts() {
    var alerts = document.querySelectorAll('.alert-dismissible');
    alerts.forEach(function(alert) {
        setTimeout(function() {
            alert.style.transition = 'opacity 0.5s ease';
            alert.style.opacity = '0';
            setTimeout(function() { alert.remove(); }, 500);
        }, 4000);
    });
}

function initNotificationPolling() {
    setInterval(function() {
        fetch('/api/notifications/count')
            .then(function(r) { return r.json(); })
            .then(function(data) {
                var badge = document.getElementById('notif-badge');
                if (badge && data.unreadCount > 0) {
                    badge.textContent = data.unreadCount;
                    badge.style.display = 'flex';
                } else if (badge) {
                    badge.style.display = 'none';
                }
            }).catch(function() {});
    }, 15000);
}

function initSidebarToggle() {
    var toggler = document.getElementById('sidebar-toggle');
    var sidebar = document.getElementById('sidebar');
    if (toggler && sidebar) {
        toggler.addEventListener('click', function() {
            sidebar.classList.toggle('show');
        });
    }
}

function initDeleteConfirmations() {
    document.querySelectorAll('.btn-delete-confirm').forEach(function(btn) {
        btn.addEventListener('click', function(e) {
            if (!confirm('Are you sure you want to delete this item?')) {
                e.preventDefault();
            }
        });
    });
}

function initSearchDebounce() {
    var searchInput = document.getElementById('search-input');
    if (searchInput) {
        var timeout;
        searchInput.addEventListener('input', function() {
            clearTimeout(timeout);
            timeout = setTimeout(function() {
                searchInput.closest('form').submit();
            }, 600);
        });
    }
}

function showToast(message, type) {
    var container = document.querySelector('.toast-container');
    if (!container) {
        container = document.createElement('div');
        container.className = 'toast-container';
        document.body.appendChild(container);
    }
    var toast = document.createElement('div');
    toast.className = 'toast-custom';
    toast.style.borderLeftColor = type === 'error' ? '#dc3545' : '#52b788';
    toast.innerHTML = '<strong>' + (type === 'error' ? 'Error' : 'Success') + '</strong><br>' + message;
    container.appendChild(toast);
    setTimeout(function() {
        toast.style.opacity = '0';
        toast.style.transition = 'opacity 0.4s';
        setTimeout(function() { toast.remove(); }, 400);
    }, 4000);
}
