/* Socket Client - Polls the REST bridge for real-time donation alerts */
(function() {
    var lastAlertId = null;
    var alertContainer = document.getElementById('socket-alerts');

    function pollAlerts() {
        fetch('/api/socket/alerts')
            .then(function(r) { return r.json(); })
            .then(function(data) {
                if (data.latestAlert && data.latestAlert !== lastAlertId) {
                    lastAlertId = data.latestAlert;
                    try {
                        var alert = JSON.parse(data.latestAlert);
                        if (alert.type === 'NEW_DONATION') {
                            displayDonationAlert(alert);
                        }
                    } catch (e) {
                        // ignore parse errors
                    }
                }
                var clientInfo = document.getElementById('socket-clients');
                if (clientInfo) {
                    clientInfo.textContent = data.connectedClients || 0;
                }
            }).catch(function() {});
    }

    function displayDonationAlert(alert) {
        showToast('New donation available: ' + alert.food + ' (' + alert.qty + ' kg) at ' + alert.location, 'success');

        if (alertContainer) {
            var item = document.createElement('div');
            item.className = 'alert alert-success-custom alert-custom';
            item.innerHTML = '<i class="fas fa-bell"></i> <div><strong>New Donation!</strong> ' +
                alert.food + ' - ' + alert.qty + ' kg at ' + alert.location + '</div>';
            alertContainer.prepend(item);
            setTimeout(function() { item.remove(); }, 10000);
        }
    }

    // Poll every 5 seconds
    if (document.querySelector('[data-socket-poll]')) {
        setInterval(pollAlerts, 5000);
        pollAlerts();
    }
})();
