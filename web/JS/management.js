document.addEventListener('DOMContentLoaded', () => {
    loadStats();

    // Initial view
    if (document.getElementById('overview-section')) {
        showSection('overview');
    }

    // Modal Form Listener
});

// --- Navigation Logic ---
function showSection(sectionId) {
    const sections = ['overview', 'rooms', 'bookings', 'reports', 'bills', 'staff', 'guests', 'help'];
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
        'bills': 'Payment History',
        'help': 'System Guidelines',
        'staff': 'Staff Management',
        'guests': 'Guest Directory'
    };
    const pageTitle = document.getElementById('page-title');
    if (pageTitle) pageTitle.innerText = titles[sectionId] || 'Dashboard';

    if (sectionId === 'overview') loadStats();
    if (sectionId === 'rooms') loadRooms();
    if (sectionId === 'bookings') loadBookings();
    if (sectionId === 'reports') loadReports();
    if (sectionId === 'bills') loadBills();
    if (sectionId === 'staff') loadStaff();
    if (sectionId === 'guests') loadGuests();
}

// --- Guests Logic ---
let allGuests = [];

async function loadGuests(query = '') {
    const tbody = document.getElementById('guests-table-body');
    if (!tbody) return;

    try {
        const response = await fetch(`admin/users${query ? `?query=${encodeURIComponent(query)}` : ''}`);
        allGuests = await response.json();
        renderGuests(allGuests);
    } catch (error) {
        console.error('Error loading guests:', error);
    }
}

function renderGuests(guests) {
    const tbody = document.getElementById('guests-table-body');
    if (!tbody) return;

    if (guests.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" style="text-align: center; padding: 20px;">No guests found</td></tr>';
        return;
    }

    tbody.innerHTML = guests.map(g => `
        <tr style="border-bottom: 1px solid #eee;">
            <td style="padding: 15px;">#${g.userId}</td>
            <td style="padding: 15px; font-weight: 600;">${g.firstName} ${g.lastName}</td>
            <td style="padding: 15px;">${g.email}</td>
            <td style="padding: 15px;">${g.phone}</td>
            <td style="padding: 15px;">${g.nicPassport}</td>
            <td style="padding: 15px; text-align: center; display: flex; gap: 8px; justify-content: center;">
                <button class="btn btn-primary" style="padding: 5px 10px; font-size: 11px;" onclick="openGuestModal(${g.userId})">
                    <i class="fas fa-edit"></i> Edit
                </button>
                <button class="btn" style="padding: 5px 10px; font-size: 11px; color: var(--error);" onclick="deleteGuest(${g.userId})">
                    <i class="fas fa-trash"></i> Delete
                </button>
            </td>
        </tr>
    `).join('');
}

// --- Guest CRUD Functions ---
function openGuestModal(userId = null) {
    const modal = document.getElementById('guestModal');
    const form = document.getElementById('guestForm');
    const title = document.getElementById('guestModalTitle');
    const passLabel = document.getElementById('passwordLabel');
    const passInput = document.getElementById('guestPasswordInput');

    if (userId) {
        title.innerText = 'Edit Guest Details';
        passLabel.innerText = 'New Password (leave blank to keep current)';
        passInput.required = false;

        const guest = allGuests.find(g => g.userId == userId);

        if (guest) {
            form.userId.value = guest.userId;
            form.firstName.value = guest.firstName;
            form.lastName.value = guest.lastName;
            form.email.value = guest.email;
            form.phone.value = guest.phone;
            form.nicPassport.value = guest.nicPassport;
            form.address.value = guest.address || '';
        }
    } else {
        title.innerText = 'Add New Guest';
        passLabel.innerText = 'Password';
        passInput.required = true;
        form.reset();
        form.userId.value = '';
    }
    modal.style.display = 'grid';
}

function closeGuestModal() {
    document.getElementById('guestModal').style.display = 'none';
    document.getElementById('guestForm').reset();
}

async function handleGuestSubmit(e) {
    e.preventDefault();
    const formData = new FormData(e.target);
    const data = Object.fromEntries(formData);
    const isUpdate = !!data.userId;
    data.action = isUpdate ? 'update' : 'add';

    try {
        const response = await fetch('admin/users', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });

        if (response.ok) {
            closeGuestModal();
            loadGuests();
            alert(`Guest ${isUpdate ? 'updated' : 'added'} successfully!`);
        } else {
            alert('Failed to save guest: ' + response.statusText);
        }
    } catch (err) {
        alert('Connection error');
    }
}

async function deleteGuest(userId) {
    if (!confirm('Are you sure you want to delete this guest? This will also remove all their bookings.')) return;

    try {
        const response = await fetch('admin/users', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ action: 'delete', userId: userId })
        });

        if (response.ok) {
            loadGuests();
        } else {
            alert('Failed to delete guest');
        }
    } catch (err) {
        alert('Connection error');
    }
}

const handleGuestSearch = debounce((query) => {
    loadGuests(query);
}, 300);

// Debounce Utility
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
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

function formatPrice(lkrAmount) {
    return `Rs. ${lkrAmount.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
}

let allRooms = [];

async function loadRooms() {
    try {
        const response = await fetch('admin/rooms');
        allRooms = await response.json();
        const tbody = document.getElementById('rooms-table-body');
        if (!tbody) return;

        tbody.innerHTML = allRooms.map(room => `
            <tr style="border-bottom: 1px solid #eee;">
                <td style="padding: 15px;">${room.roomNumber}</td>
                <td style="padding: 15px;">${room.type}</td>
                <td style="padding: 15px;">${formatPrice(room.price)}</td>
                <td style="padding: 15px;">
                    <span class="status-badge status-${room.status.toLowerCase()}">${room.status}</span>
                </td>
                <td style="padding: 15px;">
                    <button class="btn btn-primary" style="padding: 5px 10px; font-size: 11px;" onclick="openRoomModal(${room.roomId})">
                        <i class="fas fa-edit"></i> Edit
                    </button>
                    ${room.status === 'Occupied'
                ? `<button class="btn" style="padding: 5px 10px; font-size: 11px; opacity: 0.5; cursor: not-allowed;" title="Cannot delete an occupied room" disabled>
                            <i class="fas fa-trash"></i> Delete
                           </button>`
                : `<button class="btn" style="padding: 5px 10px; font-size: 11px; color: var(--error);" onclick="deleteRoom(${room.roomId})">
                            <i class="fas fa-trash"></i> Delete
                           </button>`
            }
                </td>
            </tr>
        `).join('');
    } catch (err) {
        console.error('Error loading rooms:', err);
    }
}

// --- Room Modal Logic (Add & Update) ---
function openRoomModal(roomId = null) {
    const modal = document.getElementById('roomModal');
    const form = document.getElementById('roomForm');
    const title = document.getElementById('modalTitle');

    form.reset();

    if (roomId) {
        title.innerText = 'Edit Room';
        const room = allRooms.find(r => r.roomId == roomId);
        if (room) {
            form.roomId.value = room.roomId;
            form.roomNumber.value = room.roomNumber;
            form.type.value = room.type;
            form.price.value = room.price;
            form.status.value = room.status;
            form.description.value = room.description || '';
        }
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
    const data = Object.fromEntries(formData);
    const action = data.roomId ? 'update' : 'add';
    data.action = action;

    try {
        const response = await fetch('admin/rooms', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });

        if (response.ok) {
            closeRoomModal();
            loadRooms();
            loadStats();
            showToast(`Room ${action === 'add' ? 'added' : 'updated'} successfully!`, 'success');
        } else {
            const errorText = await response.text();
            showToast('Operation failed: ' + errorText, 'error');
        }
    } catch (err) {
        showToast('Network error', 'error');
    }
}

async function deleteRoom(id) {
    if (!confirm("Are you sure you want to delete this room?")) return;

    try {
        const response = await fetch('admin/rooms', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ action: 'delete', roomId: id })
        });

        if (response.ok) {
            loadRooms();
            loadStats();
            showToast('Room deleted successfully!', 'success');
        } else {
            const errorText = await response.text();
            showToast('Deletion failed: ' + errorText, 'error');
        }
    } catch (err) {
        showToast('Connection error', 'error');
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
        // ── Fetch stats (booking counts, guests, revenue) ──────────────
        const statsResp = await fetch('admin/stats');
        const stats = await statsResp.json();

        // KPI cards
        const setEl = (id, val) => { const e = document.getElementById(id); if (e) e.innerText = val; };
        setEl('rpt-revenue', formatPrice(stats.totalRevenue || 0));
        setEl('rpt-completed', stats.completedBookings || 0);
        setEl('rpt-guests', stats.totalGuests || 0);

        // ── Fetch cancellation stats ───────────────────────────────────
        const cancelResp = await fetch('admin/bookings?report=cancellation');
        const cancel = await cancelResp.json();
        const cancelRate = cancel.total > 0 ? (cancel.cancelled / cancel.total * 100).toFixed(1) : '0.0';
        setEl('rpt-cancel-rate', `${cancelRate}%`);
        setEl('rpt-cancel-count', `${cancel.cancelled} of ${cancel.total} bookings`);

        // ── Booking Status Breakdown chart (horizontal bars) ──────────
        const statusData = [
            { label: 'Confirmed', count: stats.confirmedBookings || 0, color: 'var(--primary-color)' },
            { label: 'Checked-in', count: stats.checkedInBookings || 0, color: 'var(--warning)' },
            { label: 'Completed', count: stats.completedBookings || 0, color: 'var(--success)' },
            { label: 'Cancelled', count: stats.cancelledBookings || 0, color: 'var(--error)' },
        ];
        const totalBookings = statusData.reduce((s, d) => s + d.count, 0) || 1;
        const statusChart = document.getElementById('booking-status-chart');
        if (statusChart) {
            statusChart.innerHTML = statusData.map(d => {
                const pct = ((d.count / totalBookings) * 100).toFixed(1);
                return `
                <div>
                    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:8px;">
                        <span style="font-size:13px; font-weight:600; color:${d.color};">${d.label}</span>
                        <span style="font-size:13px; color:var(--text-secondary);">${d.count} bookings &nbsp;(${pct}%)</span>
                    </div>
                    <div style="background:#f1f3f9; border-radius:8px; height:14px; overflow:hidden;">
                        <div style="width:${pct}%; height:100%; background:${d.color}; border-radius:8px;
                             transition:width 0.8s ease; min-width:${d.count > 0 ? '4px' : '0'};"></div>
                    </div>
                </div>`;
            }).join('');
        }

        // ── Monthly Revenue Chart ──────────────────────────────────────
        const revResp = await fetch('admin/bookings?report=revenue');
        const revenueData = await revResp.json();
        renderRevenueChart(revenueData);

        // Best Month
        if (revenueData.length > 0) {
            const best = [...revenueData].sort((a, b) => b.revenue - a.revenue)[0];
            setEl('rpt-best-month', `${best.month}`);
        } else {
            setEl('rpt-best-month', '-');
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
                <div style="font-size: 10px; color: var(--primary-color); font-weight: 600;">Rs. ${dispRev}</div>
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
// --- Staff Management ---
async function loadStaff() {
    const tbody = document.getElementById('staff-table-body');
    if (!tbody) return;

    try {
        const response = await fetch('admin/staff');
        const data = await response.json();

        if (!data.length) {
            tbody.innerHTML = '<tr><td colspan="5" style="text-align:center;padding:30px;color:var(--text-secondary);">No staff records found.</td></tr>';
            return;
        }

        tbody.innerHTML = data.map(s => `
            <tr>
                <td style="padding: 15px;">#${s.id}</td>
                <td style="padding: 15px; font-weight: 600;">${s.fullName}</td>
                <td style="padding: 15px;">${s.email}</td>
                <td style="padding: 15px;"><span class="status-badge" style="background:${s.role === 'ADMIN'
                ? 'rgba(67,97,238,0.1); color:var(--primary-color)'
                : 'rgba(6,214,160,0.1); color:var(--success)'
            };">${s.role}</span></td>
                <td style="padding: 15px;">${s.role === 'RECEPTIONIST'
                ? `<button class="btn" style="padding:6px 14px;font-size:12px;color:var(--error);border:1px solid var(--error);" onclick="deleteStaff(${s.id}, '${s.fullName}')">
                        <i class="fas fa-trash"></i> Remove
                       </button>`
                : '<span style="color:var(--text-secondary);font-size:12px;">Protected</span>'}
                </td>
            </tr>
        `).join('');
    } catch (err) {
        tbody.innerHTML = '<tr><td colspan="5" style="text-align:center;padding:30px;color:var(--error);">Failed to load staff data.</td></tr>';
    }
}

function openStaffModal() {
    const form = document.getElementById('staffForm');
    if (form) form.reset();
    const modal = document.getElementById('staffModal');
    if (modal) modal.style.display = 'grid';
}

function closeStaffModal() {
    const modal = document.getElementById('staffModal');
    if (modal) modal.style.display = 'none';
}

async function submitStaff(e) {
    e.preventDefault();
    const fullName = document.getElementById('staff-fullname').value.trim();
    const email = document.getElementById('staff-email').value.trim();
    const password = document.getElementById('staff-password').value.trim();

    try {
        const response = await fetch('admin/staff', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ action: 'add', fullName, email, password })
        });

        const data = await response.json();
        if (response.ok) {
            closeStaffModal();
            loadStaff();
            showToast('Receptionist added successfully!', 'success');
        } else {
            showToast(data.error || 'Failed to add receptionist.', 'error');
        }
    } catch (err) {
        showToast('Network error. Please try again.', 'error');
    }
}

async function deleteStaff(id, name) {
    if (!confirm(`Remove receptionist "${name}" from the system?`)) return;

    try {
        const response = await fetch('admin/staff', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ action: 'delete', id })
        });

        const data = await response.json();
        if (response.ok) {
            loadStaff();
            showToast('Staff member removed.', 'success');
        } else {
            showToast(data.error || 'Failed to remove staff.', 'error');
        }
    } catch (err) {
        showToast('Network error. Please try again.', 'error');
    }
}

function showToast(message, type) {
    const existing = document.getElementById('toast-msg');
    if (existing) existing.remove();
    const toast = document.createElement('div');
    toast.id = 'toast-msg';
    toast.textContent = message;
    toast.style.cssText = `
        position:fixed; bottom:28px; right:28px; z-index:9999;
        padding:14px 22px; border-radius:12px; font-size:14px; font-weight:600;
        color:white; box-shadow:0 8px 20px rgba(0,0,0,0.15);
        background:${type === 'success' ? 'var(--success)' : 'var(--error)'};
        animation:fadeInUp 0.3s ease;
    `;
    document.body.appendChild(toast);
    setTimeout(() => toast.remove(), 3500);
}
