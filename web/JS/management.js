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
        if (item.getAttribute('onclick').includes(sectionId)) {
            item.classList.add('active');
        }
    });

    // Update title
    const titles = {
        'overview': 'Dashboard Overview',
        'rooms': 'Room Management',
        'bookings': 'Reservations'
    };
    document.getElementById('page-title').innerText = titles[sectionId];

    // Load data for the section
    if (sectionId === 'overview') loadStats();
    if (sectionId === 'rooms') loadRooms();
    if (sectionId === 'bookings') loadBookings();
}

async function loadStats() {
    try {
        const response = await fetch('admin/stats');
        const data = await response.json();
        document.getElementById('total-rooms').innerText = data.totalRooms;
        document.getElementById('active-bookings').innerText = data.activeBookings;
        document.getElementById('total-revenue').innerText = `$${data.totalRevenue.toLocaleString()}`;
    } catch (err) {
        console.error('Error loading stats:', err);
    }
}

async function loadRooms() {
    try {
        const response = await fetch('admin/rooms');
        const rooms = await response.json();
        const tbody = document.getElementById('rooms-table-body');
        tbody.innerHTML = rooms.map(room => `
            <tr style="border-bottom: 1px solid #eee;">
                <td style="padding: 15px;">${room.roomNumber}</td>
                <td style="padding: 15px;">${room.type}</td>
                <td style="padding: 15px;">$${room.price}</td>
                <td style="padding: 15px;"><span class="status-badge status-${room.status.toLowerCase()}">${room.status}</span></td>
                <td style="padding: 15px;">
                    <button class="btn" style="color: var(--primary-color);" onclick="editRoom(${room.roomId})"><i class="fas fa-edit"></i></button>
                    <button class="btn" style="color: var(--error);" onclick="deleteRoom(${room.roomId})"><i class="fas fa-trash"></i></button>
                </td>
            </tr>
        `).join('');
    } catch (err) {
        console.error('Error loading rooms:', err);
    }
}

async function loadBookings() {
    try {
        const response = await fetch('admin/bookings');
        const bookings = await response.json();
        const tbody = document.getElementById('bookings-table-body');
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

// Room CRUD placeholders (would ideally use a modal)
function openRoomModal() {
    const num = prompt("Room Number:");
    const type = prompt("Type (Single/Double/Suite):");
    const price = prompt("Price:");
    const status = prompt("Status (Available/Maintenance):");
    const desc = prompt("Description:");

    if (num && type && price && status) {
        const params = new URLSearchParams({
            action: 'add',
            roomNumber: num,
            type: type,
            price: price,
            status: status,
            description: desc
        });
        fetch('admin/rooms', { method: 'POST', body: params }).then(loadRooms);
    }
}

async function deleteRoom(id) {
    if (confirm("Delete this room?")) {
        await fetch(`admin/rooms?action=delete&roomId=${id}`, { method: 'POST' });
        loadRooms();
    }
}
