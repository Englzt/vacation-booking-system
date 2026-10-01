const calendarElement = document.getElementById("calendar");
const monthYearElement = document.getElementById("monthYear");
const infobox = document.getElementById("infobox");

const dateEntries = Object.entries(dates);

let today = new Date(todayRaw);
let currentMonth = today.getMonth();
let currentYear = today.getFullYear();

function parseDate(dateStr) {
    const date = new Date(dateStr);
    return date.getDate();
}

function generateCalendar(month, year, dates = []) {
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

        if (currentDayFormatted === startDate)
            dayCell.classList.add("selected-start");

        if (currentDayFormatted === endDate)
            dayCell.classList.add("selected-end");

        if (isToday)
            dayCell.classList.add("today");


        if (startDate && endDate) {
            const start = new Date(startDate);
            const end = new Date(endDate);
            const current = new Date(year, month, i);

            if (current > start && (current < end)) {
                dayCell.classList.add("in-range");
            }
        }


        if (dateEntries.some(([key, value]) => key === currentDayFormatted && value === "Personal")) {
            dayCell.classList.add("disabled");
        }

        if (dateEntries.some(([key, value]) => key === currentDayFormatted && value !== "Personal")) {
            dayCell.classList.add("highlight");
        }

        dayCell.addEventListener("click", () => {
            if (!dayCell.classList.contains("disabled")) {
                if (!startDate || (startDate && endDate)) {
                    // set start date
                    startDate = currentDayFormatted;
                    endDate = null;
                    document.getElementById("interval_start").value = startDate;
                    document.getElementById("interval_end").value = '';
                } else if (!endDate) {
                    // set end date
                    const selectedDate = currentDayFormatted;
                    if (new Date(selectedDate) >= new Date(startDate)) {
                        endDate = selectedDate;
                        document.getElementById("interval_end").value = endDate;
                    } else {

                        startDate = selectedDate;
                        document.getElementById("interval_start").value = startDate;
                    }
                }

                generateCalendar(currentMonth, currentYear, dates);

                updateReservationDetails();
            }
        });


        dayCell.addEventListener("mouseover", (event) => {
            if (dayCell.classList.contains("highlight")) {
                infobox.innerHTML = dateEntries.filter(([key, value]) => key === currentDayFormatted).map(([key, value]) => {
                    let l = value.split("|");
                    value = "<b>" + l[0] + "</b><br>" + l[1];
                    return value;
                }).join("<br>");
                infobox.style.display = "block";
                infobox.style.left = `${event.pageX + 10}px`;
                infobox.style.top = `${event.pageY + 10}px`;
            }
        });

        dayCell.addEventListener("mousemove", (event) => {
            infobox.style.left = `${event.pageX + 10}px`;
            infobox.style.top = `${event.pageY + 10}px`;
        });

        dayCell.addEventListener("mouseout", () => {
            infobox.style.display = "none";
        });

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
        generateCalendar(currentMonth, currentYear, dates);
    }
}

function goToNextMonth() {
    currentMonth++;
    if (currentMonth > 11) {
        currentMonth = 0;
        currentYear++;
    }
    generateCalendar(currentMonth, currentYear, dates);
}

function updatePrevMonthButtonState() {
    const today = new Date();
    const prevButton = document.getElementById("prevMonth");

    prevButton.disabled = currentYear === today.getFullYear() && currentMonth === today.getMonth();
}

function updateReservationDetails() {
    const nightsElement = document.getElementById("nightsCount");
    const totalPriceElement = document.getElementById("totalPrice");
    const downPaymentElement = document.getElementById("downPayment");
    const pricePerNight = parseFloat(document.getElementById("pricePerNight").textContent);

    let totalPrice = 0;

    if (startDate && endDate) {
        const start = new Date(startDate);
        const end = new Date(endDate);
        const timeDiff = end - start;
        const nights = Math.ceil(timeDiff / (1000 * 60 * 60 * 24));

        nightsElement.textContent = nights + (nights === 1 ? " Nacht" : " Nächte");
        totalPrice = nights * pricePerNight;
        totalPriceElement.textContent = totalPrice.toFixed(0) + "€";
    } else if (startDate) {
        nightsElement.textContent = "1 Nacht";
        totalPrice = pricePerNight;
        totalPriceElement.textContent = totalPrice.toFixed(0) + "€";
    } else {
        nightsElement.textContent = "0 Nächte";
        totalPriceElement.textContent = "0€";
    }

    // deposit
    const downPayment = totalPrice * 0.10;
    downPaymentElement.textContent = downPayment.toFixed(2) + "€";
}

generateCalendar(currentMonth, currentYear, dates);
updateReservationDetails();

document.getElementById("prevMonth").addEventListener("click", goToPreviousMonth);
document.getElementById("prevMonth").disabled = true;
document.getElementById("nextMonth").addEventListener("click", goToNextMonth);