let allRooms = [];

document.addEventListener('DOMContentLoaded', () => {
    loadStats();

    // Initial view
    if (document.getElementById('overview-section')) {
        showSection('overview');
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
        'overview': 'Reception Overview',
        'rooms': 'Room Availability',
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
        const totalRoomsEl = document.getElementById('total-rooms');
        const activeBookingsEl = document.getElementById('active-bookings');

        if (totalRoomsEl) totalRoomsEl.innerText = data.availableRooms || 0; // Receptionist sees available rooms as "Total" in overview
        if (activeBookingsEl) activeBookingsEl.innerText = data.activeBookings || 0;
    } catch (err) {
        console.error('Error loading stats:', err);
    }
}

async function loadRooms() {
    try {
        const response = await fetch('admin/rooms');
        allRooms = await response.json();
        renderRooms(allRooms);
    } catch (err) {
        console.error('Error loading rooms:', err);
    }
}

function renderRooms(rooms) {
    const tbody = document.getElementById('rooms-table-body');
    if (!tbody) return;

    tbody.innerHTML = rooms.map(room => `
        <tr style="border-bottom: 1px solid #eee;">
            <td style="padding: 15px;">${room.roomNumber}</td>
            <td style="padding: 15px;">${room.type}</td>
            <td style="padding: 15px;">$${room.price}</td>
            <td style="padding: 15px;"><span class="status-badge status-${room.status.toLowerCase()}">${room.status}</span></td>
            <td style="padding: 15px;">
                <select onchange="updateRoomStatus(${room.roomId}, this.value)" class="form-input" style="padding: 5px; font-size: 12px;">
                    <option value="Available" ${room.status === 'Available' ? 'selected' : ''}>Available</option>
                    <option value="Booked" ${room.status === 'Booked' ? 'selected' : ''}>Booked</option>
                    <option value="Occupied" ${room.status === 'Occupied' ? 'selected' : ''}>Occupied</option>
                    <option value="Cleaning" ${room.status === 'Cleaning' ? 'selected' : ''}>Cleaning</option>
                </select>
            </td>
        </tr>
    `).join('');
}

function filterRooms() {
    const num = document.getElementById('searchRoomNumber').value.toLowerCase();
    const type = document.getElementById('searchType').value;
    const availableOnly = document.getElementById('availableOnly').checked;

    const filtered = allRooms.filter(r => {
        const matchesNum = r.roomNumber.toLowerCase().includes(num);
        const matchesType = type === "" || r.type === type;
        const matchesAvail = !availableOnly || r.status === 'Available';
        return matchesNum && matchesType && matchesAvail;
    });

    renderRooms(filtered);
}

async function updateRoomStatus(id, newStatus) {
    const params = new URLSearchParams({
        action: 'update',
        roomId: id,
        status: newStatus
    });

    try {
        const response = await fetch('admin/rooms', { method: 'POST', body: params });
        if (response.ok) {
            loadRooms();
            loadStats();
        } else {
            alert('Failed to update room status');
        }
    } catch (err) {
        alert('Connection error');
    }
}

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
                    <button class="btn btn-primary" style="padding: 5px 10px; font-size: 12px;" onclick="updateBookingStatus(${b.bookingId}, 'Completed')">Check-out</button>
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
