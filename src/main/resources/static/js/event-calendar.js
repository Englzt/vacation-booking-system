/*<![CDATA[*/
const calendarElement = document.getElementById("calendar");
const monthYearElement = document.getElementById("monthYear");

let today = new Date(todayRaw);
let currentMonth = today.getMonth();
let currentYear = today.getFullYear();

function convertDateFormat(dateStr) {
    let parts = dateStr.split('-');
    if (parts.length === 1) {
        parts = dateStr.split('.');
    }
    return parts[2] + '-' + parts[1].padStart(2, '0') + '-' + parts[0].padStart(2, '0');
}

const startDateFormatted = convertDateFormat(startDateStr);
const endDateFormatted = convertDateFormat(endDateStr);

function parseDate(dateStr) {
    let parts = dateStr.split('-');
    if (parts[2].length === 4) {
        parts = dateStr.split('-').reverse();
    }
    return new Date(parts[0], parts[1] - 1, parts[2]);
}

// let dates = [];
let currentDate = parseDate(startDateFormatted);
const endDateObj = parseDate(endDateFormatted);

while (currentDate <= endDateObj) {
    const year = currentDate.getFullYear();
    const month = String(currentDate.getMonth() + 1).padStart(2, '0');
    const day = String(currentDate.getDate()).padStart(2, '0');
    // const dateKey = `${year}-${month}-${day}`;
    // dates[dateKey] = 'Ereignis';
    currentDate.setDate(currentDate.getDate() + 1);
}

function generateCalendar(month, year) {
    const firstDay = (new Date(year, month).getDay() + 6) % 7;
    const daysInMonth = new Date(year, month + 1, 0).getDate();

    calendarElement.innerHTML = '';

    monthYearElement.textContent = `${monthNames[month]} ${year}`;

    daysOfWeek.forEach(day => {
        const header = document.createElement("div");
        header.className = "header";
        header.textContent = day;
        calendarElement.appendChild(header);
    });

    for (let i = 0; i < firstDay; i++) {
        const emptyCell = document.createElement("div");
        calendarElement.appendChild(emptyCell);
    }

    for (let i = 1; i <= daysInMonth; i++) {
        const dayCell = document.createElement("div");
        dayCell.classList.add("day");
        dayCell.textContent = i;

        const currentDayFormatted = `${year}-${String(month + 1).padStart(2, "0")}-${String(i).padStart(2, "0")}`;
        const isToday = today.getDate() === i && today.getMonth() === month && today.getFullYear() === year;

        if (isToday) {
            dayCell.classList.add("today");
        }

        if (dates.includes(currentDayFormatted)) {
            dayCell.classList.add("highlight");
        }

        calendarElement.appendChild(dayCell);
    }
    updatePrevMonthButtonState();
}

function goToPreviousMonth() {
    if (currentYear > today.getFullYear() || (currentYear === today.getFullYear() && currentMonth > today.getMonth())) {
        currentMonth--;
        if (currentMonth < 0) {
            currentMonth = 11;
            currentYear--;
        }
        generateCalendar(currentMonth, currentYear);
    }
}

function goToNextMonth() {
    currentMonth++;
    if (currentMonth > 11) {
        currentMonth = 0;
        currentYear++;
    }
    generateCalendar(currentMonth, currentYear);
}

function updatePrevMonthButtonState() {
    const prevButton = document.getElementById("prevMonth");
    prevButton.disabled = currentYear === today.getFullYear() && currentMonth === today.getMonth();
}

generateCalendar(currentMonth, currentYear);

document.getElementById("prevMonth").addEventListener("click", goToPreviousMonth);
document.getElementById("prevMonth").disabled = true;
document.getElementById("nextMonth").addEventListener("click", goToNextMonth);