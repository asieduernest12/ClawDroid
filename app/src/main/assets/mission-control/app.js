const API_BASE = '';

async function fetchJSON(url) {
    const res = await fetch(url);
    return res.json();
}

async function postJSON(url) {
    const res = await fetch(url, { method: 'POST' });
    return res.json();
}

function formatUptime(seconds) {
    const h = Math.floor(seconds / 3600);
    const m = Math.floor((seconds % 3600) / 60);
    const s = seconds % 60;
    if (h > 0) return `${h}h ${m}m ${s}s`;
    if (m > 0) return `${m}m ${s}s`;
    return `${s}s`;
}

function updateStatus(data) {
    document.getElementById('server-status').textContent = data.status || '-';
    document.getElementById('uptime').textContent = formatUptime(data.uptimeSeconds || 0);
    document.getElementById('port').textContent = data.port || '-';
    document.getElementById('nano-status').textContent =
        data.nanoClawRunning ? 'Running' : 'Stopped';

    const badge = document.getElementById('connection-status');
    badge.textContent = 'Connected';
    badge.className = 'status-badge connected';
}

function showError(err) {
    console.error('Mission Control error:', err);
    const badge = document.getElementById('connection-status');
    badge.textContent = 'Disconnected';
    badge.className = 'status-badge disconnected';
}

async function pollStatus() {
    try {
        const data = await fetchJSON(`${API_BASE}/api/status`);
        updateStatus(data);
    } catch (err) {
        showError(err);
    }
}

document.addEventListener('DOMContentLoaded', () => {
    pollStatus();
    setInterval(pollStatus, 3000);

    document.getElementById('start-btn').addEventListener('click', async () => {
        await postJSON(`${API_BASE}/api/start`);
        pollStatus();
    });

    document.getElementById('stop-btn').addEventListener('click', async () => {
        await postJSON(`${API_BASE}/api/stop`);
        pollStatus();
    });
});
