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
    const sections = ['overview', 'rooms', 'bookings'];
    sections.forEach(s => {
        const el = document.getElementById(`${s}-section`);
        if (el) el.style.display = (s === sectionId) ? 'block' : 'none';
    });

    document.querySelectorAll('.nav-item').forEach(item => {
        item.classList.remove('active');
        const onclick = item.getAttribute('onclick');
        if (onclick && onclick.includes(sectionId)) {
            item.classList.add('active');
        }
    });

    const titles = {
        'overview': 'Dashboard Overview',
        'rooms': 'Room Management',
        'bookings': 'Reservations'
    };
    const pageTitle = document.getElementById('page-title');
    if (pageTitle) pageTitle.innerText = titles[sectionId];

    if (sectionId === 'overview') loadStats();
    if (sectionId === 'rooms') loadRooms();
    if (sectionId === 'bookings') loadBookings();
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
        updateEl('total-revenue', `$${(data.totalRevenue || 0).toLocaleString()}`);
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

        tbody.innerHTML = rooms.map(room => {
            // Escape the room object to safely pass it into the onclick function
            const roomJson = JSON.stringify(room).replace(/"/g, '&quot;');
            return `
            <tr style="border-bottom: 1px solid #eee;">
                <td style="padding: 15px;">${room.roomNumber}</td>
                <td style="padding: 15px;">${room.type}</td>
                <td style="padding: 15px;">$${room.price}</td>
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

    form.reset(); // Clear previous values

    if (room && room.roomId) {
        title.innerText = 'Edit Room';
        // Ensure your HTML input names match these keys exactly
        form.roomId.value = room.roomId;
        form.roomNumber.value = room.roomNumber;
        form.type.value = room.type;
        form.price.value = room.price;
        form.status.value = room.status;
        form.description.value = room.description || '';
    } else {
        title.innerText = 'Add New Room';
        form.roomId.value = ''; // Ensure hidden ID is empty for new rooms
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

    // Determine if we are adding or updating
    const action = (roomId && roomId !== "") ? 'update' : 'add';

    // Create URL parameters for standard backend compatibility
    const params = new URLSearchParams(formData);
    params.append('action', action);

    try {
        const response = await fetch('admin/rooms', {
            method: 'POST',
            body: params // Sending as URL-encoded form data
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
        alert('Network error. Is the server running?');
        console.error(err);
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

// --- Authentication & Logout ---
function handleLogout() {
    if (confirm("Are you sure you want to logout?")) {
        window.location.href = 'admin/logout';
    }
}