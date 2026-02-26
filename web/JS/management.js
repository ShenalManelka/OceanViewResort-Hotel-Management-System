document.addEventListener('DOMContentLoaded', () => {
    loadStats();

    // Initial view
    if (document.getElementById('overview-section')) {
        showSection('overview');
    }

    // Modal Form Listener
    const roomForm = document.getElementById('roomForm');
    if (roomForm) {
        roomForm.addEventListener('submit', handleRoomSubmit);
    }
});

// --- Navigation Logic ---
function showSection(sectionId) {
    const sections = ['overview', 'rooms', 'bookings', 'reports', 'bills'];
    sections.forEach(s => {
        const el = document.getElementById(`${s}-section`);
        if (el) el.style.display = (s === sectionId) ? 'block' : 'none';
    });

    document.querySelectorAll('.nav-item').forEach(item => {
        item.classList.remove('active');
        const onclick = item.getAttribute('onclick');
        if (onclick && onclick.includes(`showSection('${sectionId}')`)) {
            item.classList.add('active');
        }
    });

    const titles = {
        'overview': 'Dashboard Overview',
        'rooms': 'Room Management',
        'bookings': 'Reservations',
        'reports': 'Business Reports',
        'bills': 'Payment History'
    };
    const pageTitle = document.getElementById('page-title');
    if (pageTitle) pageTitle.innerText = titles[sectionId] || 'Dashboard';

    if (sectionId === 'overview') loadStats();
    if (sectionId === 'rooms') loadRooms();
    if (sectionId === 'bookings') loadBookings();
    if (sectionId === 'reports') loadReports();
    if (sectionId === 'bills') loadBills();
}

// --- Data Loading ---
async function loadStats() {
    try {
        const response = await fetch('admin/stats');
        const data = await response.json();

        const updateEl = (id, val) => {
            const el = document.getElementById(id);
            if (el) el.innerText = val;
        };

        updateEl('total-rooms', data.totalRooms || 0);
        updateEl('available-rooms', data.availableRooms || 0);
        updateEl('occupied-rooms', data.occupiedRooms || 0);
        updateEl('maintenance-rooms', data.maintenanceRooms || 0);
        updateEl('total-revenue', formatPrice(data.totalRevenue || 0));
    } catch (err) {
        console.error('Error loading stats:', err);
    }
}

function formatPrice(usdAmount) {
    if (currentCurrency === 'LKR') {
        const lkrAmount = usdAmount * EXCHANGE_RATE;
        return `Rs ${lkrAmount.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
    }
    return `$${usdAmount.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
}

function setCurrency(currency) {
    currentCurrency = currency;
    localStorage.setItem('currency', currency);
    updateCurrencyUI();

    // Reload data to reflect new currency
    loadStats();
    loadRooms();
    loadBookings();
    loadBills();
    if (document.getElementById('reports-section').style.display === 'block') {
        loadReports();
    }
}

function updateCurrencyUI() {
    document.querySelectorAll('.currency-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    const activeBtn = document.getElementById(`toggle-${currentCurrency}`);
    if (activeBtn) activeBtn.classList.add('active');
}

async function loadRooms() {
    try {
        const response = await fetch('admin/rooms');
        const rooms = await response.json();
        const tbody = document.getElementById('rooms-table-body');
        if (!tbody) return;

        tbody.innerHTML = rooms.map(room => {
            const roomJson = JSON.stringify(room).replace(/"/g, '&quot;');
            return `
            <tr style="border-bottom: 1px solid #eee;">
                <td style="padding: 15px;">${room.roomNumber}</td>
                <td style="padding: 15px;">${room.type}</td>
                <td style="padding: 15px;">${formatPrice(room.price)}</td>
                <td style="padding: 15px;">
                    <span class="status-badge status-${room.status.toLowerCase()}">${room.status}</span>
                </td>
                <td style="padding: 15px;">
                    <button class="btn" style="color: var(--primary-color); cursor:pointer;" onclick="openRoomModal(${roomJson})">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="btn" style="color: var(--error); cursor:pointer;" onclick="deleteRoom(${room.roomId})">
                        <i class="fas fa-trash"></i>
                    </button>
                </td>
            </tr>`;
        }).join('');
    } catch (err) {
        console.error('Error loading rooms:', err);
    }
}

// --- Room Modal Logic (Add & Update) ---
function openRoomModal(room = null) {
    const modal = document.getElementById('roomModal');
    const form = document.getElementById('roomForm');
    const title = document.getElementById('modalTitle');

    form.reset();

    if (room && room.roomId) {
        title.innerText = 'Edit Room';
        form.roomId.value = room.roomId;
        form.roomNumber.value = room.roomNumber;
        form.type.value = room.type;
        form.price.value = room.price;
        form.status.value = room.status;
        form.description.value = room.description || '';
    } else {
        title.innerText = 'Add New Room';
        form.roomId.value = '';
    }

    modal.style.display = 'grid';
}

function closeRoomModal() {
    document.getElementById('roomModal').style.display = 'none';
}

async function handleRoomSubmit(e) {
    e.preventDefault();
    const formData = new FormData(e.target);
    const roomId = formData.get('roomId');
    const action = (roomId && roomId !== "") ? 'update' : 'add';

    const params = new URLSearchParams(formData);
    params.append('action', action);

    try {
        const response = await fetch('admin/rooms', {
            method: 'POST',
            body: params
        });

        if (response.ok) {
            closeRoomModal();
            loadRooms();
            loadStats();
        } else {
            const errorText = await response.text();
            alert('Operation failed: ' + errorText);
        }
    } catch (err) {
        alert('Network error');
    }
}

async function deleteRoom(id) {
    if (confirm("Are you sure you want to delete this room?")) {
        try {
            const response = await fetch(`admin/rooms?action=delete&roomId=${id}`, { method: 'POST' });
            if (response.ok) {
                loadRooms();
                loadStats();
            } else {
                alert('Deletion failed');
            }
        } catch (err) {
            alert('Connection error');
        }
    }
}

// --- Bookings Logic ---
async function loadBookings() {
    try {
        const response = await fetch('admin/bookings');
        const bookings = await response.json();
        const tbody = document.getElementById('bookings-table-body');
        if (!tbody) return;

        tbody.innerHTML = bookings.map(b => `
            <tr style="border-bottom: 1px solid #eee;">
                <td style="padding: 15px; font-weight: 600; color: var(--primary-color);">#${b.bookingId}</td>
                <td style="padding: 15px;">
                    <div style="font-weight: 600;">${b.guestName}</div>
                    <div style="font-size: 11px; color: var(--text-secondary);">ID: ${b.guestId}</div>
                </td>
                <td style="padding: 15px;">Room ${b.roomNumber}</td>
                <td style="padding: 15px;">${b.checkIn}</td>
                <td style="padding: 15px;">${b.checkOut}</td>
                <td style="padding: 15px;"><span class="status-badge status-${b.status.toLowerCase().replace(' ', '')}">${b.status}</span></td>
                <td style="padding: 15px; display: flex; gap: 10px;">
                    <select onchange="updateBookingStatus(${b.bookingId}, this.value)" class="form-input" style="padding: 5px; font-size: 11px;">
                        <option value="Confirmed" ${b.status === 'Confirmed' ? 'selected' : ''}>Confirmed</option>
                        <option value="Checked-in" ${b.status === 'Checked-in' ? 'selected' : ''}>Checked-in</option>
                        <option value="Completed" ${b.status === 'Completed' ? 'selected' : ''}>Completed</option>
                        <option value="Cancelled" ${b.status === 'Cancelled' ? 'selected' : ''}>Cancelled</option>
                    </select>
                    <button class="btn" style="color: var(--error);" onclick="deleteBooking(${b.bookingId})">
                        <i class="fas fa-trash"></i>
                    </button>
                </td>
            </tr>
        `).join('');
    } catch (err) {
        console.error('Error loading bookings:', err);
    }
}

async function updateBookingStatus(id, newStatus) {
    try {
        const response = await fetch('admin/bookings', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ action: 'updateStatus', bookingId: id, status: newStatus })
        });
        if (response.ok) {
            loadBookings();
            loadStats();
        } else {
            alert('Failed to update status');
        }
    } catch (err) {
        alert('Connection error');
    }
}

async function deleteBooking(id) {
    if (confirm("Are you sure you want to delete this reservation? This cannot be undone.")) {
        try {
            const response = await fetch('admin/bookings', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ action: 'delete', bookingId: id })
            });
            if (response.ok) {
                loadBookings();
                loadStats();
            } else {
                alert('Deletion failed');
            }
        } catch (err) {
            alert('Connection error');
        }
    }
}

// --- Reports Logic ---
async function loadReports() {
    try {
        // Fetch Revenue Data
        const revResp = await fetch('admin/bookings?report=revenue');
        const revenueData = await revResp.json();
        renderRevenueChart(revenueData);

        // Fetch Cancellation Data
        const cancelResp = await fetch('admin/bookings?report=cancellation');
        const cancelData = await cancelResp.json();
        document.getElementById('cancellation-rate').innerText = `${cancelData.rate.toFixed(1)}%`;
        document.getElementById('cancellation-count').innerText = `${cancelData.cancelled} of ${cancelData.total} bookings`;

        // Best Month
        if (revenueData.length > 0) {
            const best = [...revenueData].sort((a, b) => b.revenue - a.revenue)[0];
            document.getElementById('best-month').innerText = `${best.month} (${formatPrice(best.revenue)})`;
        }
    } catch (err) {
        console.error('Error loading reports:', err);
    }
}

function renderRevenueChart(data) {
    const container = document.getElementById('revenue-chart');
    if (!container) return;

    if (data.length === 0) {
        container.innerHTML = '<div style="color: var(--text-secondary); width: 100%; text-align: center;">No revenue data yet</div>';
        return;
    }

    const maxRev = Math.max(...data.map(d => d.revenue));

    container.innerHTML = data.map(d => {
        const height = (d.revenue / maxRev) * 150; // Max height 150px
        const dispRev = d.revenue > 1000 ? (d.revenue / 1000).toFixed(1) + 'k' : d.revenue;
        return `
            <div style="flex: 1; display: flex; flex-direction: column; align-items: center; gap: 10px;">
                <div style="font-size: 10px; color: var(--primary-color); font-weight: 600;">Rs. ${dispRev}k</div>
                <div style="width: 100%; background: var(--primary-color); height: ${height}px; border-radius: 4px 4px 0 0; min-height: 2px; transition: height 0.5s ease;"></div>
                <div style="font-size: 11px; color: var(--text-secondary); white-space: nowrap;">${d.month.substring(0, 3)}</div>
            </div>
        `;
    }).join('');
}

async function loadBills() {
    try {
        const response = await fetch('admin/payments');
        const payments = await response.json();
        const tbody = document.getElementById('bills-table-body');
        if (!tbody) return;

        tbody.innerHTML = payments.map(p => `
            <tr style="border-bottom: 1px solid #eee;">
                <td style="padding: 15px; font-weight: 600;">#${p.paymentId}</td>
                <td style="padding: 15px;">#${p.bookingId}</td>
                <td style="padding: 15px;">${new Date(p.paymentDate).toLocaleString()}</td>
                <td style="padding: 15px; font-weight: 600; color: var(--primary-color);">${formatPrice(p.amount)}</td>
                <td style="padding: 15px;">${p.paymentMethod}</td>
                <td style="padding: 15px;"><span class="status-badge status-available">${p.paymentStatus}</span></td>
            </tr>
        `).join('');
    } catch (err) {
        console.error('Error loading payments:', err);
    }
}

// --- Authentication & Logout ---
function handleLogout() {
    if (confirm("Are you sure you want to logout?")) {
        window.location.href = 'admin/logout';
    }
}
