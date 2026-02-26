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

function showSection(sectionId) {
    const sections = ['overview', 'rooms', 'bookings'];
    sections.forEach(s => {
        const el = document.getElementById(`${s}-section`);
        if (el) el.style.display = s === sectionId ? 'block' : 'none';
    });

    // Update active nav item
    document.querySelectorAll('.nav-item').forEach(item => {
        item.classList.remove('active');
        const onclick = item.getAttribute('onclick');
        if (onclick && onclick.includes(sectionId)) {
            item.classList.add('active');
        }
    });

    // Update title
    const titles = {
        'overview': 'Dashboard Overview',
        'rooms': 'Room Management',
        'bookings': 'Reservations'
    };
    const pageTitle = document.getElementById('page-title');
    if (pageTitle) pageTitle.innerText = titles[sectionId];

    // Load data for the section
    if (sectionId === 'overview') loadStats();
    if (sectionId === 'rooms') loadRooms();
    if (sectionId === 'bookings') loadBookings();
}

async function loadStats() {
    try {
        const response = await fetch('admin/stats');
        const data = await response.json();

        const updateEl = (id, val) => {
            const el = document.getElementById(id);
            if (el) el.innerText = val;
        };

        updateEl('total-rooms', data.totalRooms);
        updateEl('active-bookings', data.activeBookings || 0);
        updateEl('total-revenue', `$${(data.totalRevenue || 0).toLocaleString()}`);

        // Detailed room counts
        if (data.availableRooms !== undefined) updateEl('available-rooms', data.availableRooms);
        if (data.occupiedRooms !== undefined) updateEl('occupied-rooms', data.occupiedRooms);
        if (data.maintenanceRooms !== undefined) updateEl('maintenance-rooms', data.maintenanceRooms);

    } catch (err) {
        console.error('Error loading stats:', err);
    }
}

async function loadRooms() {
    try {
        const response = await fetch('admin/rooms');
        const rooms = await response.json();
        const tbody = document.getElementById('rooms-table-body');
        if (!tbody) return;

        tbody.innerHTML = rooms.map(room => `
            <tr style="border-bottom: 1px solid #eee;">
                <td style="padding: 15px;">${room.roomNumber}</td>
                <td style="padding: 15px;">${room.type}</td>
                <td style="padding: 15px;">$${room.price}</td>
                <td style="padding: 15px;"><span class="status-badge status-${room.status.toLowerCase()}">${room.status}</span></td>
                <td style="padding: 15px;">
                    <button class="btn" style="color: var(--primary-color);" onclick="openRoomModal(${JSON.stringify(room).replace(/"/g, '&quot;')})"><i class="fas fa-edit"></i></button>
                    <button class="btn" style="color: var(--error);" onclick="deleteRoom(${room.roomId})"><i class="fas fa-trash"></i></button>
                </td>
            </tr>
        `).join('');
    } catch (err) {
        console.error('Error loading rooms:', err);
    }
}

// Room Modal Management
function openRoomModal(room = null) {
    const modal = document.getElementById('roomModal');
    const form = document.getElementById('roomForm');
    const title = document.getElementById('modalTitle');

    if (room) {
        title.innerText = 'Edit Room';
        form.roomId.value = room.roomId;
        form.roomNumber.value = room.roomNumber;
        form.type.value = room.type;
        form.price.value = room.price;
        form.status.value = room.status;
        form.description.value = room.description;
    } else {
        title.innerText = 'Add New Room';
        form.reset();
        form.roomId.value = '';
    }

    modal.style.display = 'flex';
}

function closeRoomModal() {
    document.getElementById('roomModal').style.display = 'none';
}

async function handleRoomSubmit(e) {
    e.preventDefault();
    const formData = new FormData(e.target);
    const roomId = formData.get('roomId');
    const action = roomId ? 'update' : 'add';

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
            alert('Error: ' + errorText);
        }
    } catch (err) {
        alert('Connection error');
    }
}

async function deleteRoom(id) {
    if (confirm("Are you sure you want to delete this room?")) {
        try {
            const response = await fetch(`admin/rooms?action=delete&roomId=${id}`, { method: 'POST' });
            if (response.status === 409) {
                alert(await response.text());
            } else if (response.ok) {
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

// ... (loadBookings and other functions remain the same)
async function loadBookings() {
    try {
        const response = await fetch('admin/bookings');
        const bookings = await response.json();
        const tbody = document.getElementById('bookings-table-body');
        if (!tbody) return;

        tbody.innerHTML = bookings.map(b => `
            <tr style="border-bottom: 1px solid #eee;">
                <td style="padding: 15px;">${b.guestName}</td>
                <td style="padding: 15px;">${b.roomNumber}</td>
                <td style="padding: 15px;">${b.checkIn}</td>
                <td style="padding: 15px;">${b.checkOut}</td>
                <td style="padding: 15px;"><span class="status-badge">${b.status}</span></td>
                <td style="padding: 15px;">
                    <select onchange="updateBookingStatus(${b.bookingId}, this.value)" style="padding: 5px; border-radius: 5px; border: 1px solid #ddd;">
                        <option value="Confirmed" ${b.status === 'Confirmed' ? 'selected' : ''}>Confirmed</option>
                        <option value="Cancelled" ${b.status === 'Cancelled' ? 'selected' : ''}>Cancelled</option>
                        <option value="Completed" ${b.status === 'Completed' ? 'selected' : ''}>Completed</option>
                    </select>
                </td>
            </tr>
        `).join('');
    } catch (err) {
        console.error('Error loading bookings:', err);
    }
}

async function updateBookingStatus(id, newStatus) {
    try {
        await fetch(`admin/bookings?action=updateStatus&bookingId=${id}&status=${newStatus}`, { method: 'POST' });
        loadBookings();
    } catch (err) {
        alert('Failed to update status');
    }
}

function handleLogout() {
    if (confirm("Are you sure you want to logout?")) {
        window.location.href = 'admin/logout';
    }
}
