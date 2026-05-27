package com.nhstrial.html

import com.nhstrial.model.TrialSummary
import kotlinx.html.*
import java.time.LocalDate
import java.time.Period

fun FlowContent.dashboardContent(trials: List<TrialSummary>, inlineJson: String = "[]") {

    // ── Inline styles ─────────────────────────────────────────────────────────
    style {
        unsafe {
            +"""
/* Stats bar */
.db-stats-bar { display:flex; align-items:center; gap:20px; margin-bottom:24px; flex-wrap:wrap; }
.db-stat-card {
  background:#fff; border:1px solid #d8dde0; border-radius:6px;
  padding:16px 24px; min-width:140px;
}
.db-stat-label { font-size:14px; color:#425563; margin:0 0 4px; }
.db-stat-value { font-size:40px; font-weight:700; color:#003087; line-height:1; margin:0; }
.db-enrol-btn {
  display:inline-block; padding:10px 20px;
  border:2px solid #005eb8; border-radius:4px;
  color:#005eb8; font-weight:600; font-size:16px;
  text-decoration:none; background:#fff;
}
.db-enrol-btn:hover { background:#f0f4f8; }

/* Chart card */
.db-chart-card {
  background:#fff; border:1px solid #d8dde0; border-radius:8px;
  padding:24px; margin-bottom:24px;
}
.db-chart-card h2 { margin-top:0; font-size:20px; }

/* Filter row */
.db-filters { display:flex; gap:32px; flex-wrap:wrap; align-items:flex-end; margin-bottom:20px; }
.db-filter-group { display:flex; flex-direction:column; gap:6px; }
.db-filter-label { font-size:12px; font-weight:700; color:#425563; letter-spacing:.04em; text-transform:uppercase; }

/* Button group */
.db-btn-group { display:inline-flex; border:1px solid #aeb7bd; border-radius:4px; overflow:hidden; }
.db-btn {
  padding:6px 16px; border:none; border-right:1px solid #aeb7bd;
  background:#fff; cursor:pointer; font-size:14px; color:#212b32;
  line-height:1.4;
}
.db-btn:last-child { border-right:none; }
.db-btn:hover:not(.active) { background:#f0f4f8; }
.db-btn.active { color:#fff; }
/* Sex active colours */
.db-btn.active[data-group="sex"][data-value="Both"]   { background:#003087; border-color:#003087; }
.db-btn.active[data-group="sex"][data-value="Male"]   { background:#003087; border-color:#003087; }
.db-btn.active[data-group="sex"][data-value="Female"] { background:#d4537e; border-color:#d4537e; }
/* Treatment active colours */
.db-btn.active[data-group="tx"][data-value="Both"]    { background:#003087; border-color:#003087; }
.db-btn.active[data-group="tx"][data-value="Drug"]    { background:#007f3b; border-color:#007f3b; }
.db-btn.active[data-group="tx"][data-value="Placebo"] { background:#425563; border-color:#425563; }

/* Toggle switch */
.db-toggle-wrap { display:flex; align-items:center; gap:10px; cursor:pointer; }
.db-toggle { position:relative; display:inline-block; width:48px; height:26px; }
.db-toggle input { opacity:0; width:0; height:0; }
.db-toggle-slider {
  position:absolute; cursor:pointer; top:0; left:0; right:0; bottom:0;
  background:#aeb7bd; border-radius:26px; transition:.3s;
}
.db-toggle-slider:before {
  position:absolute; content:""; height:20px; width:20px;
  left:3px; bottom:3px; background:#fff; border-radius:50%; transition:.3s;
}
input:checked + .db-toggle-slider { background:#005eb8; }
input:checked + .db-toggle-slider:before { transform:translateX(22px); }
.db-toggle-text { font-size:14px; color:#212b32; min-width:140px; }

/* Legend */
.db-legend { display:flex; gap:40px; flex-wrap:wrap; margin-top:16px; }
.db-legend-group { display:flex; flex-direction:column; gap:4px; }
.db-legend-title { font-size:12px; font-weight:700; color:#425563; text-transform:uppercase; letter-spacing:.04em; margin-bottom:2px; }
.db-legend-item { display:flex; align-items:center; gap:6px; font-size:13px; }
.db-legend-dot { width:12px; height:12px; border-radius:50%; display:inline-block; flex-shrink:0; }
.db-legend-sq  { width:12px; height:12px; display:inline-block; flex-shrink:0; border-radius:1px; }
.db-legend-dimmed { opacity:.35; }
"""
        }
    }

    // ── Stats bar ─────────────────────────────────────────────────────────────
    div(classes = "db-stats-bar") {
        div(classes = "db-stat-card") {
            p(classes = "db-stat-label") { +"Total Submissions" }
            p(classes = "db-stat-value") { +"${trials.size}" }
        }
        a(href = "/person", classes = "db-enrol-btn") { +"Enrol New Patient" }
    }

    // ── Chart card ────────────────────────────────────────────────────────────
    div(classes = "db-chart-card") {
        h2 { +"Blood Pressure Plot" }

        if (trials.isEmpty()) {
            p { +"No participants enrolled yet. Submit the first registration to see data here." }
        } else {

            // ── Filter controls ───────────────────────────────────────────────
            div(classes = "db-filters") {

                // Sex filter
                div(classes = "db-filter-group") {
                    span(classes = "db-filter-label") { +"Sex" }
                    div(classes = "db-btn-group") {
                        for (v in listOf("Both", "Male", "Female")) {
                            button(classes = "db-btn${if (v == "Both") " active" else ""}") {
                                attributes["data-group"] = "sex"
                                attributes["data-value"] = v
                                attributes["type"] = "button"
                                +v
                            }
                        }
                    }
                }

                // Treatment filter
                div(classes = "db-filter-group") {
                    span(classes = "db-filter-label") { +"Treatment" }
                    div(classes = "db-btn-group") {
                        for (v in listOf("Both", "Drug", "Placebo")) {
                            button(classes = "db-btn${if (v == "Both") " active" else ""}") {
                                attributes["data-group"] = "tx"
                                attributes["data-value"] = v
                                attributes["type"] = "button"
                                +v
                            }
                        }
                    }
                }

                // Age indicator toggle
                div(classes = "db-filter-group") {
                    span(classes = "db-filter-label") { +"Age Indicator" }
                    label(classes = "db-toggle-wrap") {
                        label(classes = "db-toggle") {
                            input(type = InputType.checkBox) {
                                id = "toggle-age"
                                checked = true
                            }
                            span(classes = "db-toggle-slider") {}
                        }
                        span(classes = "db-toggle-text") {
                            id = "toggle-age-label"
                            +"On — size = age"
                        }
                    }
                }
            }

            // ── Canvas ───────────────────────────────────────────────────────
            div { style = "position:relative;" }
            canvas {
                id = "bpChart"
                attributes["aria-label"] = "Blood pressure scatter chart"
                attributes["role"] = "img"
            }

            // ── Legend ───────────────────────────────────────────────────────
            div(classes = "db-legend") {
                // Sex colour legend
                div(classes = "db-legend-group") {
                    span(classes = "db-legend-title") { +"Sex (Color)" }
                    div(classes = "db-legend-item") {
                        span(classes = "db-legend-dot") { style = "background:#003087;" }
                        span { +"Male" }
                    }
                    div(classes = "db-legend-item") {
                        span(classes = "db-legend-dot") { style = "background:#d4537e;" }
                        span { +"Female" }
                    }
                }
                // Treatment shape legend
                div(classes = "db-legend-group") {
                    span(classes = "db-legend-title") { +"Treatment (Shape)" }
                    div(classes = "db-legend-item") {
                        span(classes = "db-legend-dot") { style = "background:#425563;" }
                        span { +"Drug (circle)" }
                    }
                    div(classes = "db-legend-item") {
                        span(classes = "db-legend-sq") { style = "background:#425563;" }
                        span { +"Placebo (square)" }
                    }
                }
                // Age size legend
                div(classes = "db-legend-group") {
                    id = "age-legend"
                    span(classes = "db-legend-title") { +"Age (Size)" }
                    div(classes = "db-legend-item") {
                        span(classes = "db-legend-dot") {
                            style = "background:#425563;width:8px;height:8px;"
                        }
                        span { +"Younger (20s–30s)" }
                    }
                    div(classes = "db-legend-item") {
                        span(classes = "db-legend-dot") {
                            style = "background:#425563;width:12px;height:12px;"
                        }
                        span { +"Middle (40s–60s)" }
                    }
                    div(classes = "db-legend-item") {
                        span(classes = "db-legend-dot") {
                            style = "background:#425563;width:16px;height:16px;"
                        }
                        span { +"Older (70s+)" }
                    }
                }
            }
        }
    } // end chart card

    // ── Scripts ───────────────────────────────────────────────────────────────
    if (trials.isNotEmpty()) {
        script { unsafe { +"window.__bpData = $inlineJson;" } }
        script {
            unsafe {
                +"""
(function () {
  var allData = window.__bpData || [];

  var MALE_COL   = '#003087';
  var FEMALE_COL = '#d4537e';
  var MIN_AGE = 20, MAX_AGE = 85, MIN_R = 5, MAX_R = 21;

  function ageR(age) {
    var c = Math.max(MIN_AGE, Math.min(MAX_AGE, age));
    return MIN_R + (MAX_R - MIN_R) * (c - MIN_AGE) / (MAX_AGE - MIN_AGE);
  }
  function genderCol(g)   { return g === 'Male' ? MALE_COL : FEMALE_COL; }
  function pointStyle(tx) { return tx === 'Drug' ? 'circle' : 'rect'; }

  var sexFilter = 'Both', txFilter = 'Both', useAge = true;

  function buildDataset(data) {
    return {
      label: 'Participants',
      data: data.map(function(d) { return { x:d.systolic, y:d.diastolic, _d:d }; }),
      backgroundColor: data.map(function(d) { return genderCol(d.gender) + 'bb'; }),
      borderColor:     data.map(function(d) { return genderCol(d.gender); }),
      pointStyle:      data.map(function(d) { return pointStyle(d.treatment); }),
      radius:          useAge ? data.map(function(d) { return ageR(d.age); }) : 8,
      hoverRadius:     useAge ? data.map(function(d) { return ageR(d.age) + 3; }) : 11,
      borderWidth: 1.5,
    };
  }

  var ctx = document.getElementById('bpChart');
  var chart = new Chart(ctx, {
    type: 'scatter',
    data: { datasets: [buildDataset(allData)] },
    options: {
      responsive: true,
      plugins: {
        legend: { display: false },
        tooltip: {
          callbacks: {
            title: function(items) {
              var d = items[0].raw._d;
              return d.gender + ' · ' + d.treatment;
            },
            label: function(item) {
              var d = item.raw._d;
              var lines = [
                'Systolic:    ' + d.systolic + ' mmHg',
                'Diastolic:   ' + d.diastolic + ' mmHg',
                'Age:         ' + d.age,
              ];
              if (d.sideEffects) lines.push('Side effects: ' + d.sideEffects);
              return lines;
            },
          },
        },
      },
      scales: {
        x: {
          title: { display:true, text:'Systolic (mmHg)' },
          min:90, max:180,
        },
        y: {
          title: { display:true, text:'Diastolic (mmHg)' },
          min:45, max:115,
        },
      },
    },
  });

  function applyFilters() {
    var filtered = allData.filter(function(d) {
      return (sexFilter === 'Both' || d.gender === sexFilter) &&
             (txFilter  === 'Both' || d.treatment === txFilter);
    });
    chart.data.datasets = [buildDataset(filtered)];
    chart.update();
  }

  // Button groups
  document.querySelectorAll('.db-btn').forEach(function(btn) {
    btn.addEventListener('click', function() {
      var group = btn.dataset.group;
      document.querySelectorAll('.db-btn[data-group="' + group + '"]').forEach(function(b) {
        b.classList.remove('active');
      });
      btn.classList.add('active');
      if (group === 'sex') sexFilter = btn.dataset.value;
      else txFilter = btn.dataset.value;
      applyFilters();
    });
  });

  // Age toggle
  var toggleEl  = document.getElementById('toggle-age');
  var toggleLbl = document.getElementById('toggle-age-label');
  var ageLegend = document.getElementById('age-legend');

  function syncAgeLegend() {
    ageLegend.querySelectorAll('.db-legend-item').forEach(function(el) {
      el.classList.toggle('db-legend-dimmed', !useAge);
    });
    ageLegend.querySelector('.db-legend-title').classList.toggle('db-legend-dimmed', !useAge);
  }

  toggleEl.addEventListener('change', function() {
    useAge = toggleEl.checked;
    toggleLbl.textContent = useAge ? 'On — size = age' : 'Off — uniform size';
    syncAgeLegend();
    applyFilters();
  });
  syncAgeLegend();
})();
""".trimIndent()
            }
        }
    }
}
