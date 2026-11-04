/*
 * Analytics charts. Calls our REST API (GET /api/creator/events/{id}/stats),
 * receives JSON and draws Chart.js graphs. No page reload needed.
 */
document.addEventListener("DOMContentLoaded", function () {
    const root = document.getElementById("analytics");
    if (!root) return;
    const eventId = root.dataset.eventId;
    const palette = ["#5b4bdb", "#22b07d", "#f59f00", "#e8590c", "#1c7ed6", "#d6336c", "#7048e8", "#0ca678", "#868e96"];

    fetch("/api/creator/events/" + eventId + "/stats", { headers: { "Accept": "application/json" } })
        .then(function (r) {
            if (!r.ok) throw new Error("HTTP " + r.status);
            return r.json();
        })
        .then(function (stats) {
            document.querySelectorAll(".chart-loading").forEach(function (el) { el.remove(); });
            drawTimeline(stats.timeline);
            drawBar("deptChart", stats.byDepartment.map(x => x.label), stats.byDepartment.map(x => x.count), "Responses");
            stats.questions.forEach(function (q) {
                const canvas = document.getElementById("qchart-" + q.questionId);
                if (!canvas || !q.breakdown || q.breakdown.length === 0) return;
                const labels = q.breakdown.map(x => x.label);
                const data = q.breakdown.map(x => x.count);
                if (q.type === "YES_NO" || q.type === "RADIO" || q.type === "DROPDOWN") {
                    new Chart(canvas, {
                        type: "doughnut",
                        data: { labels: labels, datasets: [{ data: data, backgroundColor: palette }] },
                        options: { plugins: { legend: { position: "bottom" } }, maintainAspectRatio: false }
                    });
                } else {
                    drawBar(canvas.id, labels, data, "Selections");
                }
            });
        })
        .catch(function (err) {
            document.querySelectorAll(".chart-loading").forEach(function (el) {
                el.textContent = "Could not load chart data (" + err.message + ").";
            });
        });

    function drawTimeline(timeline) {
        const canvas = document.getElementById("timelineChart");
        if (!canvas) return;
        let running = 0;
        const cumulative = timeline.map(function (x) { running += x.count; return running; });
        new Chart(canvas, {
            type: "line",
            data: {
                labels: timeline.map(x => x.label),
                datasets: [
                    { label: "Total so far", data: cumulative, borderColor: "#5b4bdb", backgroundColor: "rgba(91,75,219,.12)", fill: true, tension: .3 },
                    { label: "Per day", data: timeline.map(x => x.count), type: "bar", backgroundColor: "#c9c3f5" }
                ]
            },
            options: { maintainAspectRatio: false, scales: { y: { beginAtZero: true, ticks: { precision: 0 } } } }
        });
    }

    function drawBar(id, labels, data, label) {
        const canvas = document.getElementById(id);
        if (!canvas) return;
        new Chart(canvas, {
            type: "bar",
            data: { labels: labels, datasets: [{ label: label, data: data, backgroundColor: "#5b4bdb", borderRadius: 6 }] },
            options: { maintainAspectRatio: false, plugins: { legend: { display: false } },
                       scales: { y: { beginAtZero: true, ticks: { precision: 0 } } } }
        });
    }
});
