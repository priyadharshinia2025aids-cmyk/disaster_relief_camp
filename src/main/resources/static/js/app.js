// ============================================================
// 1. BASIC HELPERS
// ============================================================

// Get an HTML element by its ID
const $ = id => document.getElementById(id);


// ============================================================
// 2. FRONTEND → BACKEND CONNECTION
// ============================================================

// THIS IS THE MAIN CONNECTION TO THE SPRING BOOT BACKEND

const api = async (path, opt = {}) => {

    const response = await fetch('/api' + path, {

        headers: {
            'Content-Type': 'application/json',
            ...(opt.headers || {})
        },

        ...opt
    });


    // Handle backend errors
    if (!response.ok) {

        let message = 'Request failed';

        try {
            const error = await response.json();
            message = error.message || error.error || message;

        } catch (_) {

            message = await response.text() || message;
        }

        throw new Error(message);
    }


    // If backend returns HTTP 204,
    // there is no response body
    return response.status === 204
        ? null
        : response.json();
};


// ============================================================
// 3. HELPER FUNCTIONS
// ============================================================

const esc = value =>
    String(value ?? '').replace(
        /[&<>'"]/g,
        character => ({
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;',
            "'": '&#39;',
            '"': '&quot;'
        }[character])
    );


const pretty = value =>
    String(value || '')
        .replaceAll('_', ' ')
        .toLowerCase()
        .replace(/\b\w/g, x => x.toUpperCase());


// ============================================================
// 4. FRONTEND DATA VARIABLES
// ============================================================

let camps = [];
let supplies = [];
let families = [];
let distributions = [];

let campPageNo = 0;
let campsPage = [];

let toastTimer;


// ============================================================
// 5. TOAST / MESSAGE
// ============================================================

function toast(message, error = false) {

    const toastElement = $('toast');

    toastElement.textContent = message;

    toastElement.className =
        'toast show' + (error ? ' error' : '');

    clearTimeout(toastTimer);

    toastTimer = setTimeout(() => {
        toastElement.className = 'toast';
    }, 3200);
}


// ============================================================
// 6. PAGE / SECTION NAVIGATION
// ============================================================

function go(section) {

    document
        .querySelectorAll('.section')
        .forEach(s =>
            s.classList.toggle(
                'active',
                s.id === section
            )
        );


    document
        .querySelectorAll('.nav-item')
        .forEach(n =>
            n.classList.toggle(
                'active',
                n.dataset.section === section
            )
        );


    const label =
        section[0].toUpperCase() +
        section.slice(1);


    $('pageTitle').textContent = label;


    $('pageHeading').textContent =
        section === 'home'
            ? 'Good morning, Operations Team'
            : section === 'dashboard'
                ? 'Relief operations at a glance'
                : label;


    $('sidebar').classList.remove('open');


    window.scrollTo({
        top: 0,
        behavior: 'smooth'
    });


    // Load required backend data
    // when a particular page is opened

    if (section === 'dashboard')
        loadDashboard();

    if (section === 'camps')
        loadCamps();

    if (section === 'inventory')
        loadSupplies();

    if (section === 'families')
        loadFamilies();

    if (section === 'distributions')
        loadDistributions();

    if (section === 'reports')
        renderReports();

    if (section === 'notifications')
        renderAlerts();
}


// Navigation button events
document
    .querySelectorAll('.nav-item')
    .forEach(button => {

        button.onclick = () =>
            go(button.dataset.section);
    });


// Mobile menu
$('menuBtn').onclick = () =>
    $('sidebar').classList.toggle('open');


// ============================================================
// 7. MODAL / FORM HANDLING
// ============================================================

function openForm(templateId) {

    const box = $('modalContent');

    box.innerHTML =
        $(templateId).innerHTML;


    $('modal').classList.add('show');


    if (
        ['supplyForm', 'distributionForm', 'checkInForm']
            .includes(templateId)
    ) {

        fillCampSelects();
    }


    if (templateId === 'distributionForm') {

        refreshDistributionOptions();
    }
}


function closeModal() {

    $('modal').classList.remove('show');
}


$('modal').addEventListener('click', event => {

    if (event.target.id === 'modal') {

        closeModal();
    }
});


// ============================================================
// 8. DASHBOARD
// ============================================================

// FRONTEND → GET /api/dashboard

async function loadDashboard() {

    try {

        const data = await api('/dashboard');


        // Display dashboard statistics

        $('statCamps').textContent =
            data.camps;

        $('statActive').textContent =
            data.activeCamps;

        $('statFamilies').textContent =
            data.families;

        $('statChecked').textContent =
            data.checkedInFamilies;

        $('statUnits').textContent =
            data.totalSupplyUnits;

        $('statDist').textContent =
            data.distributionsToday;


        $('homeChecked').textContent =
            data.checkedInFamilies;

        $('homeUnits').textContent =
            data.totalSupplyUnits;

        $('homeDist').textContent =
            data.distributionsToday;


        $('todayLabel').textContent =
            new Date().toLocaleDateString(
                'en-IN',
                {
                    day: '2-digit',
                    month: 'short',
                    year: 'numeric'
                }
            );


        await Promise.all([
            loadAllForSelects(),
            loadCampsData(),
            loadSuppliesData(),
            loadFamiliesData(),
            loadDistributionsData()
        ]);


        renderDashboardWidgets();

        renderReports();

        renderAlerts();

    } catch (error) {

        toast(error.message, true);
    }
}


// ============================================================
// 9. CAMP DATA
// ============================================================

// GET /api/camps

async function loadCampsData() {

    try {

        const data = await api(
            '/camps?page=0&size=100&sort=name&direction=asc'
        );


        camps = data.content || [];

        fillCampSelects();

    } catch (_) {}
}


async function loadAllForSelects() {

    try {

        await loadCampsData();

    } catch (_) {}
}


// ============================================================
// 10. LOAD CAMPS PAGE
// ============================================================

// GET /api/camps

async function loadCamps() {

    try {

        const data = await api(
            `/camps?page=${campPageNo}&size=8&sort=name&direction=asc`
        );


        campsPage =
            data.content || [];


        renderCamps(campsPage);


        $('campPageLabel').textContent =
            `Page ${data.number + 1} of ${Math.max(data.totalPages, 1)}`;


        await loadCampsData();

        renderDashboardWidgets();

    } catch (error) {

        toast(error.message, true);
    }
}


// Display camps in table

function renderCamps(data = campsPage) {

    const search =
        ($('campSearch')?.value || '')
            .toLowerCase();


    const list =
        data.filter(c =>
            (c.name + ' ' + c.location)
                .toLowerCase()
                .includes(search)
        );


    $('campRows').innerHTML =
        list.length

            ? list.map(c => {

                const percentage =
                    c.capacity
                        ? Math.round(
                            c.currentOccupancy /
                            c.capacity * 100
                        )
                        : 0;


                return `
                    <tr>

                        <td>
                            <b>${esc(c.name)}</b>
                        </td>

                        <td>
                            ${esc(c.location)}
                        </td>

                        <td>
                            ${c.capacity}
                        </td>

                        <td>
                            ${c.currentOccupancy}
                        </td>

                        <td>
                            <b>${c.availableCapacity}</b>
                        </td>

                        <td>
                            <span class="badge ${
                    percentage >= 90
                        ? 'red'
                        : percentage >= 70
                            ? 'orange'
                            : 'green'
                }">
                                ${esc(c.status)}
                            </span>
                        </td>

                        <td>
                            <div class="actions">

                                <button
                                    class="icon-btn"
                                    onclick="editCamp(${c.id})">
                                    Edit
                                </button>

                                <button
                                    class="icon-btn danger"
                                    onclick="deleteCamp(${c.id})">
                                    Delete
                                </button>

                            </div>
                        </td>

                    </tr>
                `;

            }).join('')

            : `
                <tr>
                    <td colspan="7" class="empty">
                        No camps match your search.
                    </td>
                </tr>
            `;
}


// Camp pagination

function campPage(delta) {

    campPageNo =
        Math.max(
            0,
            campPageNo + delta
        );

    loadCamps();
}


// ============================================================
// 11. CAMP DROPDOWN
// ============================================================

function fillCampSelects() {

    [
        'inventoryCampFilter',
        'familyCampFilter',
        'supplyCamp',
        'distCamp',
        'checkInCamp'
    ].forEach(id => {

        const element = $(id);

        if (!element)
            return;


        const current =
            element.value;


        const isFilter =
            id.includes('Filter');


        element.innerHTML =
            (
                isFilter
                    ? '<option value="">All camps</option>'
                    : '<option value="">Select camp</option>'
            )
            +
            camps.map(c =>
                `<option value="${c.id}">
                    ${esc(c.name)} — ${esc(c.location)}
                </option>`
            ).join('');


        if (current)
            element.value = current;
    });
}


// ============================================================
// 12. SUPPLY / INVENTORY
// ============================================================

// GET /api/supplies

async function loadSuppliesData() {

    try {

        supplies =
            await api('/supplies');

    } catch (error) {

        supplies = [];
    }
}


// Load inventory page

async function loadSupplies() {

    try {

        const id =
            $('inventoryCampFilter')?.value;


        supplies =
            await api(
                '/supplies' +
                (id ? `?campId=${id}` : '')
            );


        renderSupplies();

        renderInventoryCards();

        renderDashboardWidgets();

    } catch (error) {

        toast(error.message, true);
    }
}


// Display supplies

function renderSupplies() {

    const search =
        ($('inventorySearch')?.value || '')
            .toLowerCase();


    const list =
        supplies.filter(s =>
            (
                s.type +
                ' ' +
                s.campName +
                ' ' +
                s.unit
            )
                .toLowerCase()
                .includes(search)
        );


    $('supplyRows').innerHTML =
        list.length

            ? list.map(s => {

                const level =
                    s.quantity === 0
                        ? 'red'
                        : s.quantity < 20
                            ? 'orange'
                            : 'green';


                return `
                    <tr>

                        <td>
                            <b>${pretty(s.type)}</b>
                        </td>

                        <td>
                            ${esc(s.campName)}
                        </td>

                        <td>
                            <b>${s.quantity}</b>
                        </td>

                        <td>
                            ${esc(s.unit)}
                        </td>

                        <td>
                            <span class="badge ${level}">
                                ${
                    s.quantity === 0
                        ? 'Out of stock'
                        : s.quantity < 20
                            ? 'Low stock'
                            : 'Healthy'
                }
                            </span>
                        </td>

                        <td>
                            <button
                                class="icon-btn danger"
                                onclick="deleteSupply(${s.id})">
                                Delete
                            </button>
                        </td>

                    </tr>
                `;

            }).join('')

            : `
                <tr>
                    <td colspan="6" class="empty">
                        No inventory records found.
                    </td>
                </tr>
            `;
}


// Inventory summary cards

function renderInventoryCards() {

    const sum = type =>
        supplies
            .filter(s => s.type === type)
            .reduce(
                (total, item) =>
                    total + (item.quantity || 0),
                0
            );


    $('waterStock').textContent =
        sum('WATER');

    $('foodStock').textContent =
        sum('FOOD');

    $('medicalStock').textContent =
        sum('MEDICAL_KITS');


    $('otherStock').textContent =
        supplies
            .filter(s =>
                ![
                    'WATER',
                    'FOOD',
                    'MEDICAL_KITS'
                ].includes(s.type)
            )
            .reduce(
                (total, item) =>
                    total + (item.quantity || 0),
                0
            );
}


// ============================================================
// 13. FAMILY
// ============================================================

// GET /api/families

async function loadFamiliesData() {

    try {

        families =
            await api('/families');

    } catch (error) {

        families = [];
    }
}


// Load family page

async function loadFamilies() {

    try {

        const id =
            $('familyCampFilter')?.value;


        families =
            await api(
                '/families' +
                (id ? `?campId=${id}` : '')
            );


        renderFamilies();

        renderFamilySummary();

        renderDashboardWidgets();

    } catch (error) {

        toast(error.message, true);
    }
}


// Display families

function renderFamilies() {

    const search =
        ($('familySearch')?.value || '')
            .toLowerCase();


    const status =
        $('familyStatusFilter')?.value || '';


    const list =
        families.filter(f =>
            (
                f.headName +
                ' ' +
                f.contactNumber +
                ' ' +
                (f.campName || '')
            )
                .toLowerCase()
                .includes(search)
            &&
            (!status || f.status === status)
        );


    $('familyRows').innerHTML =
        list.length

            ? list.map(f => `

                <tr>

                    <td>
                        <b>${esc(f.headName)}</b>
                    </td>

                    <td>
                        ${f.memberCount}
                    </td>

                    <td>
                        ${esc(f.contactNumber)}
                    </td>

                    <td>
                        ${esc(f.campName || 'Not checked in')}
                    </td>

                    <td>
                        <span class="badge ${
                f.status === 'CHECKED_IN'
                    ? 'green'
                    : f.status === 'CHECKED_OUT'
                        ? 'blue'
                        : 'orange'
            }">
                            ${pretty(f.status)}
                        </span>
                    </td>

                    <td>

                        <div class="actions">

                            ${
                f.status === 'REGISTERED'
                    ? `
                                        <button
                                            class="icon-btn"
                                            onclick="checkIn(${f.id})">
                                            Check in
                                        </button>
                                    `
                    : ''
            }

                            ${
                f.status === 'CHECKED_IN'
                    ? `
                                        <button
                                            class="icon-btn"
                                            onclick="checkOut(${f.id})">
                                            Check out
                                        </button>
                                    `
                    : ''
            }

                            <button
                                class="icon-btn"
                                onclick="editFamily(${f.id})">
                                Edit
                            </button>

                            <button
                                class="icon-btn danger"
                                onclick="deleteFamily(${f.id})">
                                Delete
                            </button>

                        </div>

                    </td>

                </tr>

            `).join('')

            : `
                <tr>
                    <td colspan="6" class="empty">
                        No families match your filters.
                    </td>
                </tr>
            `;
}


// Family summary

function renderFamilySummary() {

    $('registeredFamilies').textContent =
        families.length;


    $('checkedFamilies').textContent =
        families.filter(
            f => f.status === 'CHECKED_IN'
        ).length;


    $('checkedOutFamilies').textContent =
        families.filter(
            f => f.status === 'CHECKED_OUT'
        ).length;


    $('totalPeople').textContent =
        families
            .filter(
                f => f.status === 'CHECKED_IN'
            )
            .reduce(
                (total, family) =>
                    total + (family.memberCount || 0),
                0
            );
}


// ============================================================
// 14. DISTRIBUTIONS
// ============================================================

// GET /api/distributions

async function loadDistributionsData() {

    try {

        distributions =
            await api('/distributions');

    } catch (error) {

        distributions = [];
    }
}


async function loadDistributions() {

    try {

        distributions =
            await api('/distributions');


        renderDistributions();

        renderDashboardWidgets();

    } catch (error) {

        toast(error.message, true);
    }
}


// Display distributions

function renderDistributions() {

    const search =
        ($('distributionSearch')?.value || '')
            .toLowerCase();


    const list =
        distributions.filter(x =>
            (
                x.campName +
                ' ' +
                x.familyHeadName +
                ' ' +
                pretty(x.supplyType)
            )
                .toLowerCase()
                .includes(search)
        );


    $('distributionRows').innerHTML =
        list.length

            ? list
                .sort(
                    (a, b) =>
                        new Date(b.distributedAt) -
                        new Date(a.distributedAt)
                )
                .map(x => `

                    <tr>

                        <td>
                            ${
                    new Date(
                        x.distributedAt
                    ).toLocaleString(
                        'en-IN',
                        {
                            dateStyle: 'medium',
                            timeStyle: 'short'
                        }
                    )
                }
                        </td>

                        <td>
                            <b>${esc(x.campName)}</b>
                        </td>

                        <td>
                            ${esc(x.familyHeadName)}
                        </td>

                        <td>
                            ${pretty(x.supplyType)}
                        </td>

                        <td>
                            <b>${x.quantity}</b>
                            ${esc(x.unit)}
                        </td>

                        <td>
                            <button
                                class="icon-btn danger"
                                onclick="deleteDistribution(${x.id})">
                                Reverse
                            </button>
                        </td>

                    </tr>

                `).join('')

            : `
                <tr>
                    <td colspan="6" class="empty">
                        No distribution records found.
                    </td>
                </tr>
            `;
}


// ============================================================
// 15. DASHBOARD WIDGETS
// ============================================================

function renderDashboardWidgets() {

    if (!$('capacityBars'))
        return;


    $('capacityBars').innerHTML =
        camps.length

            ? camps
                .slice(0, 6)
                .map(c => {

                    const percentage =
                        c.capacity
                            ? Math.min(
                                100,
                                Math.round(
                                    c.currentOccupancy /
                                    c.capacity *
                                    100
                                )
                            )
                            : 0;


                    return `
                        <div class="capacity-row">

                            <div class="capacity-meta">

                                <span>
                                    <b>${esc(c.name)}</b>
                                    ·
                                    ${c.currentOccupancy}/${c.capacity}
                                </span>

                                <b>
                                    ${percentage}%
                                </b>

                            </div>

                            <div class="bar">

                                <i
                                    class="${
                        percentage >= 90
                            ? 'danger'
                            : percentage >= 70
                                ? 'warning'
                                : ''
                    }"
                                    style="width:${percentage}%">
                                </i>

                            </div>

                        </div>
                    `;

                }).join('')

            : `
                <div class="empty">
                    No camp data yet.
                </div>
            `;


    // Supply grouping

    const grouped = {};


    supplies.forEach(s => {

        grouped[s.type] =
            (grouped[s.type] || 0) +
            (s.quantity || 0);
    });


    const entries =
        Object.entries(grouped);


    $('stockSummary').innerHTML =
        entries.length

            ? entries
                .map(([type, total]) => `

                    <div class="stock-row">

                        <div>
                            <b>${pretty(type)}</b>

                            <small>
                                ${
                    supplies.filter(
                        s => s.type === type
                    ).length
                }
                                inventory record(s)
                            </small>
                        </div>

                        <div class="stock-value">

                            <strong>${total}</strong>

                            <span>units</span>

                        </div>

                    </div>

                `).join('')

            : `
                <div class="empty">
                    No inventory yet.
                </div>
            `;


    // Recent distributions

    const recent =
        [...distributions]
            .sort(
                (a, b) =>
                    new Date(b.distributedAt) -
                    new Date(a.distributedAt)
            )
            .slice(0, 5);


    $('recentRows').innerHTML =
        recent.length

            ? recent.map(x => `

                <tr>

                    <td>
                        ${
                new Date(
                    x.distributedAt
                ).toLocaleString(
                    'en-IN',
                    {
                        dateStyle: 'medium',
                        timeStyle: 'short'
                    }
                )
            }
                    </td>

                    <td>
                        ${esc(x.campName)}
                    </td>

                    <td>
                        ${esc(x.familyHeadName)}
                    </td>

                    <td>
                        ${pretty(x.supplyType)}
                    </td>

                    <td>
                        <b>${x.quantity}</b>
                        ${esc(x.unit)}
                    </td>

                </tr>

            `).join('')

            : `
                <tr>
                    <td colspan="5" class="empty">
                        No distributions recorded yet.
                    </td>
                </tr>
            `;


    renderInventoryCards();

    renderFamilySummary();
}


// ============================================================
// 16. REPORTS
// ============================================================

function renderReports() {

    if (!$('reportCapacity'))
        return;


    // Camp capacity report

    $('reportCapacity').innerHTML =
        camps.length

            ? camps.map(c => {

                const percentage =
                    c.capacity
                        ? Math.round(
                            c.currentOccupancy /
                            c.capacity *
                            100
                        )
                        : 0;


                return `
                    <div class="report-bar">

                        <div class="report-bar-head">

                            <span>
                                ${esc(c.name)}
                            </span>

                            <b>
                                ${c.currentOccupancy}/${c.capacity}
                            </b>

                        </div>

                        <div class="bar">

                            <i
                                class="${
                    percentage >= 90
                        ? 'danger'
                        : percentage >= 70
                            ? 'warning'
                            : ''
                }"
                                style="width:${Math.min(
                    100,
                    percentage
                )}%">
                            </i>

                        </div>

                    </div>
                `;

            }).join('')

            : `
                <div class="empty">
                    No camps available.
                </div>
            `;


    // Family statistics

    $('familyDonutTotal').textContent =
        families.length;


    const counts = [

        families.filter(
            f => f.status === 'CHECKED_IN'
        ).length,

        families.filter(
            f => f.status === 'REGISTERED'
        ).length,

        families.filter(
            f => f.status === 'CHECKED_OUT'
        ).length

    ];


    const total =
        Math.max(families.length, 1);


    const checkedInPercentage =
        counts[0] / total * 100;


    const registeredPercentage =
        counts[1] / total * 100;


    $('familyDonut').style.background =
        `conic-gradient(
            #5aaf83 0 ${checkedInPercentage}%,
            #8a6bd1 ${checkedInPercentage}%
                ${checkedInPercentage + registeredPercentage}%,
            #d5a253
                ${checkedInPercentage + registeredPercentage}%
                100%
        )`;


    $('familyLegend').innerHTML = [

        ['Checked in', counts[0]],
        ['Registered', counts[1]],
        ['Checked out', counts[2]]

    ].map((item, index) => `

        <div>
            <i class="dot-${index}"></i>
            ${item[0]}:
            <b>${item[1]}</b>
        </div>

    `).join('');


    // Supply report

    const grouped = {};


    supplies.forEach(s => {

        grouped[s.type] =
            (grouped[s.type] || 0) +
            s.quantity;
    });


    const max =
        Math.max(
            ...Object.values(grouped),
            1
        );


    $('reportSupply').innerHTML =
        Object.entries(grouped)
            .map(([key, value]) => `

                <div class="report-bar">

                    <div class="report-bar-head">

                        <span>
                            ${pretty(key)}
                        </span>

                        <b>
                            ${value}
                        </b>

                    </div>

                    <div class="bar">

                        <i
                            style="width:${value / max * 100}%">
                        </i>

                    </div>

                </div>

            `).join('')

        || `
                <div class="empty">
                    No inventory available.
                </div>
            `;


    $('qualityCamps').textContent =
        camps.length;

    $('qualityFamilies').textContent =
        families.length;

    $('qualitySupplies').textContent =
        supplies.length;

    $('qualityDist').textContent =
        distributions.length;
}


// ============================================================
// 17. ALERTS / NOTIFICATIONS
// ============================================================

function renderAlerts() {

    const alerts = [];


    // Camp capacity alerts

    camps.forEach(c => {

        const percentage =
            c.capacity
                ? c.currentOccupancy /
                c.capacity * 100
                : 0;


        if (percentage >= 90) {

            alerts.push({

                type: 'danger',

                icon: '!',

                title:
                    `${c.name} is near capacity`,

                text:
                    `${c.availableCapacity} place(s) remaining out of ${c.capacity}.`
            });

        } else if (percentage >= 70) {

            alerts.push({

                type: '',

                icon: '!',

                title:
                    `${c.name} is filling up`,

                text:
                    `Occupancy is at ${Math.round(percentage)}%.`
            });
        }
    });


    // Low inventory alerts

    supplies
        .filter(s => s.quantity < 20)
        .forEach(s => {

            alerts.push({

                type:
                    s.quantity === 0
                        ? 'danger'
                        : '',

                icon: '▣',

                title:
                    `${pretty(s.type)} stock is ${
                        s.quantity === 0
                            ? 'out of stock'
                            : 'low'
                    }`,

                text:
                    `${s.campName}: ${s.quantity} ${s.unit} remaining.`
            });
        });


    if (!alerts.length) {

        alerts.push({

            type: 'ok',

            icon: '✓',

            title:
                'All operations look healthy',

            text:
                'No capacity or low-stock alerts were detected.'
        });
    }


    $('alertCount').textContent =
        alerts.filter(
            a => a.type !== 'ok'
        ).length;


    $('alertList').innerHTML =
        alerts.map(a => `

            <div class="alert-item ${a.type}">

                <div class="alert-icon">
                    ${a.icon}
                </div>

                <div>

                    <b>
                        ${esc(a.title)}
                    </b>

                    <small>
                        ${esc(a.text)}
                    </small>

                </div>

            </div>

        `).join('');
}


// ============================================================
// 18. CREATE CAMP
// ============================================================

// POST /api/camps

async function saveCamp(event) {

    event.preventDefault();


    try {

        await api('/camps', {

            method: 'POST',

            body: JSON.stringify({

                name:
                $('campName').value,

                location:
                $('campLocation').value,

                capacity:
                    +$('campCapacity').value

            })
        });


        closeModal();

        toast(
            'Camp registered successfully'
        );


        campPageNo = 0;

        await refreshAll(false);

    } catch (error) {

        toast(
            error.message,
            true
        );
    }
}


// ============================================================
// 19. CREATE SUPPLY
// ============================================================

// POST /api/supplies

async function saveSupply(event) {

    event.preventDefault();


    try {

        await api('/supplies', {

            method: 'POST',

            body: JSON.stringify({

                campId:
                    +$('supplyCamp').value,

                type:
                $('supplyType').value,

                quantity:
                    +$('supplyQty').value,

                unit:
                $('supplyUnit').value

            })
        });


        closeModal();

        toast(
            'Inventory updated successfully'
        );


        await refreshAll(false);

    } catch (error) {

        toast(
            error.message,
            true
        );
    }
}


// ============================================================
// 20. CREATE FAMILY
// ============================================================

// POST /api/families

async function saveFamily(event) {

    event.preventDefault();


    try {

        await api('/families', {

            method: 'POST',

            body: JSON.stringify({

                headName:
                $('familyHead').value,

                memberCount:
                    +$('familyMembers').value,

                contactNumber:
                $('familyContact').value

            })
        });


        closeModal();

        toast(
            'Family registered successfully'
        );


        await refreshAll(false);

    } catch (error) {

        toast(
            error.message,
            true
        );
    }
}


// ============================================================
// 21. FAMILY CHECK-IN
// ============================================================

async function checkIn(id) {

    openForm('checkInForm');

    $('checkInFamilyId').value =
        id;
}


// PUT /api/families/{id}/check-in

async function saveCheckIn(event) {

    event.preventDefault();


    try {

        await api(
            `/families/${$('checkInFamilyId').value}/check-in`,
            {

                method: 'PUT',

                body: JSON.stringify({

                    campId:
                        +$('checkInCamp').value

                })
            }
        );


        closeModal();

        toast(
            'Family checked in successfully'
        );


        await refreshAll(false);

    } catch (error) {

        toast(
            error.message,
            true
        );
    }
}


// ============================================================
// 22. FAMILY CHECK-OUT
// ============================================================

// PUT /api/families/{id}/check-out

async function checkOut(id) {

    if (!confirm(
        'Check out this family?'
    ))
        return;


    try {

        await api(
            `/families/${id}/check-out`,
            {
                method: 'PUT'
            }
        );


        toast(
            'Family checked out'
        );


        await refreshAll(false);

    } catch (error) {

        toast(
            error.message,
            true
        );
    }
}


// ============================================================
// 23. DISTRIBUTION FORM OPTIONS
// ============================================================

async function refreshDistributionOptions() {

    const campId =
        $('distCamp')?.value;


    if (!$('distFamily'))
        return;


    const familiesForCamp =
        campId
            ? await api(
                `/families?campId=${campId}`
            )
            : [];


    const suppliesForCamp =
        campId
            ? await api(
                `/supplies?campId=${campId}`
            )
            : [];


    $('distFamily').innerHTML =
        '<option value="">Select checked-in family</option>'
        +
        familiesForCamp
            .filter(
                f => f.status === 'CHECKED_IN'
            )
            .map(f =>
                `<option value="${f.id}">
                    ${esc(f.headName)} —
                    ${f.memberCount} members
                </option>`
            )
            .join('');


    $('distSupply').innerHTML =
        '<option value="">Select supply</option>'
        +
        suppliesForCamp
            .filter(
                s => s.quantity > 0
            )
            .map(s =>
                `<option value="${s.id}">
                    ${pretty(s.type)} —
                    ${s.quantity}
                    ${esc(s.unit)} available
                </option>`
            )
            .join('');
}


// ============================================================
// 24. CREATE DISTRIBUTION
// ============================================================

// POST /api/distributions

async function saveDistribution(event) {

    event.preventDefault();


    try {

        await api('/distributions', {

            method: 'POST',

            body: JSON.stringify({

                campId:
                    +$('distCamp').value,

                familyId:
                    +$('distFamily').value,

                supplyId:
                    +$('distSupply').value,

                quantity:
                    +$('distQty').value

            })
        });


        closeModal();

        toast(
            'Distribution recorded; inventory reduced'
        );


        await refreshAll(false);

    } catch (error) {

        toast(
            error.message,
            true
        );
    }
}


// ============================================================
// 25. DELETE CAMP
// ============================================================

// DELETE /api/camps/{id}

async function deleteCamp(id) {

    if (!confirm(
        'Delete this camp?'
    ))
        return;


    try {

        await api(
            '/camps/' + id,
            {
                method: 'DELETE'
            }
        );


        toast('Camp deleted');

        await refreshAll(false);

    } catch (error) {

        toast(
            error.message,
            true
        );
    }
}


// ============================================================
// 26. DELETE SUPPLY
// ============================================================

// DELETE /api/supplies/{id}

async function deleteSupply(id) {

    if (!confirm(
        'Delete this inventory record?'
    ))
        return;


    try {

        await api(
            '/supplies/' + id,
            {
                method: 'DELETE'
            }
        );


        toast(
            'Inventory record deleted'
        );


        await refreshAll(false);

    } catch (error) {

        toast(
            error.message,
            true
        );
    }
}


// ============================================================
// 27. DELETE FAMILY
// ============================================================

// DELETE /api/families/{id}

async function deleteFamily(id) {

    if (!confirm(
        'Delete this family registration?'
    ))
        return;


    try {

        await api(
            '/families/' + id,
            {
                method: 'DELETE'
            }
        );


        toast('Family deleted');

        await refreshAll(false);

    } catch (error) {

        toast(
            error.message,
            true
        );
    }
}


// ============================================================
// 28. REVERSE DISTRIBUTION
// ============================================================

// DELETE /api/distributions/{id}

async function deleteDistribution(id) {

    if (!confirm(
        'Reverse this distribution? Inventory will be restored.'
    ))
        return;


    try {

        await api(
            '/distributions/' + id,
            {
                method: 'DELETE'
            }
        );


        toast(
            'Distribution reversed; inventory restored'
        );


        await refreshAll(false);

    } catch (error) {

        toast(
            error.message,
            true
        );
    }
}


// ============================================================
// 29. EDIT CAMP
// ============================================================

// GET + PUT /api/camps/{id}

async function editCamp(id) {

    try {

        const camp =
            await api(
                '/camps/' + id
            );


        const name =
            prompt(
                'Camp name:',
                camp.name
            );

        if (name === null)
            return;


        const location =
            prompt(
                'Location:',
                camp.location
            );

        if (location === null)
            return;


        const capacity =
            prompt(
                'Capacity:',
                camp.capacity
            );

        if (capacity === null)
            return;


        await api(
            '/camps/' + id,
            {

                method: 'PUT',

                body: JSON.stringify({

                    name: name,

                    location: location,

                    capacity: +capacity

                })
            }
        );


        toast('Camp updated');

        await refreshAll(false);

    } catch (error) {

        toast(
            error.message,
            true
        );
    }
}


// ============================================================
// 30. EDIT FAMILY
// ============================================================

// GET + PUT /api/families/{id}

async function editFamily(id) {

    try {

        const family =
            await api(
                '/families/' + id
            );


        const head =
            prompt(
                'Family head name:',
                family.headName
            );

        if (head === null)
            return;


        const members =
            prompt(
                'Members:',
                family.memberCount
            );

        if (members === null)
            return;


        const contact =
            prompt(
                'Contact:',
                family.contactNumber
            );

        if (contact === null)
            return;


        await api(
            '/families/' + id,
            {

                method: 'PUT',

                body: JSON.stringify({

                    headName: head,

                    memberCount:
                        +members,

                    contactNumber:
                    contact

                })
            }
        );


        toast('Family updated');

        await refreshAll(false);

    } catch (error) {

        toast(
            error.message,
            true
        );
    }
}


// ============================================================
// 31. REFRESH ALL DATA
// ============================================================

async function refreshAll(show = true) {

    try {

        await loadDashboard();


        if (show) {

            toast(
                'Dashboard refreshed'
            );
        }

    } catch (error) {

        toast(
            error.message,
            true
        );
    }
}


// ============================================================
// 32. COMPACT MODE
// ============================================================

function toggleCompact() {

    document.body.classList.toggle(
        'compact',
        $('compactToggle').checked
    );
}


// ============================================================
// 33. APPLICATION START
// ============================================================

// When app.js loads,
// dashboard data is loaded automatically.

(async () => {

    try {

        await loadDashboard();

    } catch (error) {

        toast(
            error.message,
            true
        );
    }

})();