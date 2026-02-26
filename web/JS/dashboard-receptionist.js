let allRooms = [];
let availableRooms = [];
let guests = [];

document.addEventListener('DOMContentLoaded', () => {
    loadStats();

    // Initial view
    if (document.getElementById('overview-section')) {
        showSection('overview');
    }

    // Form Submissions
    document.getElementById('bookingForm')?.addEventListener('submit', handleBookingSubmit);
    document.getElementById('guestForm')?.addEventListener('submit', handleGuestSubmit);
});

function showSection(sectionId) {
    const sections = ['overview', 'rooms', 'bookings', 'billing'];
    sections.forEach(s => {
        const el = document.getElementById(`${s}-section`);
        if (el) el.style.display = s === sectionId ? 'block' : 'none';
        else console.warn(`Section ${s}-section not found`);
    });

    // Update active nav item
    document.querySelectorAll('.nav-item').forEach(item => {
        item.classList.remove('active');
        const onclick = item.getAttribute('onclick');
        if (onclick && onclick.includes(`showSection('${sectionId}')`)) {
            item.classList.add('active');
        } else if (onclick && onclick.includes(`showSection("${sectionId}")`)) {
            item.classList.add('active');
        }
    });

    // Update title
    const titles = {
        'overview': 'Reception Overview',
        'rooms': 'Room Availability',
        'bookings': 'Check-ins',
        'billing': 'Guest Payments'
    };
    const pageTitle = document.getElementById('page-title');
    if (pageTitle) pageTitle.innerText = titles[sectionId] || 'Reception';

    // Load data for the section
    if (sectionId === 'overview') loadStats();
    if (sectionId === 'rooms') loadRooms();
    if (sectionId === 'bookings') loadBookings();
    if (sectionId === 'billing') cancelBilling();
}

async function loadStats() {
    try {
        const response = await fetch('admin/stats');
        const data = await response.json();
        const totalRoomsEl = document.getElementById('total-rooms');
        const activeBookingsEl = document.getElementById('active-bookings');

        if (totalRoomsEl) totalRoomsEl.innerText = data.availableRooms || 0;
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
                <select onchange="updateRoomStatus(${room.roomId}, this.value)" 
                        class="form-input" 
                        style="padding: 5px; font-size: 12px;"
                        ${room.status === 'Maintenance' ? 'disabled' : ''}>
                    ${room.status === 'Maintenance' ? `<option value="Maintenance" selected>Maintenance</option>` : ''}
                    <option value="Available" ${room.status === 'Available' ? 'selected' : ''}>Available</option>
                    <option value="Occupied" ${room.status === 'Occupied' ? 'selected' : ''}>Occupied</option>
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
    try {
        const response = await fetch('admin/rooms', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ action: 'update', roomId: id, status: newStatus })
        });
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

// --- Booking Logic ---

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
                <td style="padding: 15px; display: flex; gap: 5px;">
                    ${getActionButtons(b)}
                </td>
            </tr>
        `).join('');
    } catch (err) {
        console.error('Error loading bookings:', err);
    }
}

function getActionButtons(b) {
    if (b.status === 'Confirmed') {
        return `
            <button class="btn btn-primary" style="padding: 5px 10px; font-size: 11px;" onclick="updateBookingStatus(${b.bookingId}, 'Checked-in')">Check-in</button>
            <button class="btn" style="padding: 5px 10px; font-size: 11px; color: var(--error);" onclick="updateBookingStatus(${b.bookingId}, 'Cancelled')">Cancel</button>
        `;
    } else if (b.status === 'Checked-in') {
        return `
            <button class="btn btn-primary" style="padding: 5px 10px; font-size: 11px;" onclick="updateBookingStatus(${b.bookingId}, 'Completed')">Check-out</button>
        `;
    }
    return `<span style="color: var(--text-secondary); font-size: 11px;">No actions</span>`;
}

async function openBookingModal() {
    document.getElementById('bookingModal').style.display = 'grid';
    await fetchGuests();
    await fetchAvailableRooms();
}

function closeBookingModal() {
    document.getElementById('bookingModal').style.display = 'none';
    document.getElementById('bookingForm').reset();
    document.getElementById('totalAmountDisplay').innerText = '$0.00';
}

async function fetchGuests() {
    try {
        const response = await fetch('admin/users');
        guests = await response.json();
        const select = document.getElementById('guestSelect');
        const currentVal = select.value;
        select.innerHTML = '<option value="">-- Choose Guest --</option>' +
            guests.map(g => `<option value="${g.userId}">${g.firstName} ${g.lastName} (${g.nicPassport})</option>`).join('');
        select.value = currentVal;
    } catch (err) { console.error('Error fetching users:', err); }
}

async function fetchAvailableRooms() {
    try {
        const response = await fetch('admin/rooms');
        const rooms = await response.json();
        availableRooms = rooms.filter(r => r.status === 'Available');
        const select = document.getElementById('roomSelect');
        select.innerHTML = '<option value="">-- Choose Available Room --</option>' +
            availableRooms.map(r => `<option value="${r.roomId}">${r.roomNumber} - ${r.type} ($${r.price})</option>`).join('');
    } catch (err) { console.error('Error fetching rooms:', err); }
}

function calculateTotal() {
    const roomId = document.getElementById('roomSelect').value;
    const checkIn = document.getElementById('checkIn').value;
    const checkOut = document.getElementById('checkOut').value;

    if (!roomId || !checkIn || !checkOut) return;

    const room = availableRooms.find(r => r.roomId == roomId);
    if (!room) return;

    const start = new Date(checkIn);
    const end = new Date(checkOut);
    const diffTime = end - start;
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

    if (diffDays <= 0) {
        document.getElementById('totalAmountDisplay').innerText = 'Invalid Dates';
        document.getElementById('totalPriceInput').value = 0;
        return;
    }

    const total = diffDays * room.price;
    document.getElementById('totalAmountDisplay').innerText = `$${total.toFixed(2)}`;
    document.getElementById('totalPriceInput').value = total;
}

async function handleBookingSubmit(e) {
    e.preventDefault();
    const formData = new FormData(e.target);
    const data = Object.fromEntries(formData);

    if (data.totalPrice <= 0) {
        alert('Check-out must be after Check-in');
        return;
    }

    // If checkIn is today, set status to Checked-in
    const checkInDate = new Date(data.checkIn);
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    checkInDate.setHours(0, 0, 0, 0);

    if (checkInDate.getTime() === today.getTime()) {
        data.status = 'Checked-in';
    } else {
        data.status = 'Confirmed';
    }

    try {
        const response = await fetch('admin/bookings', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });

        if (response.ok) {
            closeBookingModal();
            loadBookings();
            loadStats();
        } else {
            alert('Failed to save check-in');
        }
    } catch (err) { alert('Connection error'); }
}

function openGuestModal() {
    document.getElementById('guestModal').style.display = 'grid';
}

function closeGuestModal() {
    document.getElementById('guestModal').style.display = 'none';
    document.getElementById('guestForm').reset();
}

async function handleGuestSubmit(e) {
    e.preventDefault();
    const formData = new FormData(e.target);
    const data = Object.fromEntries(formData);
    // Ensure action is set if backend requires it, but UserServlet seems to just handle POST as add
    data.action = 'add';
    data.password = 'guest@123'; // Default password for guest users

    try {
        const response = await fetch('admin/users', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });

        if (response.ok) {
            const user = await response.json();
            await fetchGuests();
            document.getElementById('guestSelect').value = user.userId;
            closeGuestModal();
        } else {
            alert('Failed to register guest');
        }
    } catch (err) { alert('Connection error'); }
}

async function updateBookingStatus(id, newStatus) {
    if (!confirm(`Are you sure you want to set this booking as ${newStatus}?`)) return;

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
    } catch (err) { alert('Connection error'); }
}

function handleLogout() {
    if (confirm("Are you sure you want to logout?")) {
        window.location.href = 'admin/logout';
    }
}
// --- Payment Logic ---
let currentBillingBooking = null;

async function searchForBilling() {
    const id = document.getElementById('billSearchId').value;
    if (!id) return;

    try {
        const response = await fetch(`admin/payments?bookingId=${id}`);
        const data = await response.json();

        if (data.error) {
            alert(data.error);
            return;
        }

        if (data.paymentId) {
            alert('A payment has already been recorded for this booking.');
            return;
        }

        if (data.found) {
            currentBillingBooking = data.booking;
            showBillPreview(data.booking);
        }
    } catch (err) {
        alert('Error searching for booking');
    }
}

function showBillPreview(booking) {
    document.getElementById('billing-preview').style.display = 'block';

    document.getElementById('bill-guest-name').innerText = booking.guestName;
    document.getElementById('bill-guest-email').innerText = booking.guestEmail;
    document.getElementById('bill-booking-id').innerText = '#' + booking.bookingId;
    document.getElementById('bill-date').innerText = new Date().toLocaleDateString();
    document.getElementById('bill-room-num').innerText = booking.roomNumber;

    const basePrice = booking.totalPrice;
    document.getElementById('bill-base-price').innerText = `$${basePrice.toFixed(2)}`;

    recalculateBill();
}

function recalculateBill() {
    if (!currentBillingBooking) return;

    const basePrice = currentBillingBooking.totalPrice;
    const tax = basePrice * 0.10;
    const discount = parseFloat(document.getElementById('billDiscountInput').value) || 0;
    const total = basePrice + tax - discount;

    document.getElementById('bill-tax').innerText = `$${tax.toFixed(2)}`;
    document.getElementById('bill-discount').innerText = `-$${discount.toFixed(2)}`;
    document.getElementById('discount-row').style.display = discount > 0 ? 'table-row' : 'none';
    document.getElementById('bill-total').innerText = `$${total.toFixed(2)}`;
}

async function finalizeAndPrint() {
    if (!currentBillingBooking) return;

    const tax = currentBillingBooking.totalPrice * 0.10;
    const discount = parseFloat(document.getElementById('billDiscountInput').value) || 0;
    const total = currentBillingBooking.totalPrice + tax - discount;
    const paymentMethod = document.getElementById('paymentMethod').value;

    const paymentData = {
        bookingId: currentBillingBooking.bookingId,
        amount: total,
        paymentMethod: paymentMethod,
        paymentStatus: 'Paid'
    };

    try {
        const response = await fetch('admin/payments', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(paymentData)
        });

        if (response.ok) {
            // Hide controls before printing
            document.getElementById('bill-controls').style.display = 'none';
            window.print();
            // Restore controls after print
            document.getElementById('bill-controls').style.display = 'grid';

            alert('Payment recorded successfully!');
            cancelBilling();
            loadRooms();
            loadBookings();
        } else {
            const errData = await response.json();
            alert('Failed to record payment: ' + (errData.error || 'Server error'));
        }
    } catch (err) {
        alert('Error saving payment');
    }
}

function cancelBilling() {
    currentBillingBooking = null;
    document.getElementById('billing-preview').style.display = 'none';
    document.getElementById('billSearchId').value = '';
}
