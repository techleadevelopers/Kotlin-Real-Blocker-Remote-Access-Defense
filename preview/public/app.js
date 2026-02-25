const state = {
  isAlertMode: false,
  sensorData: { ax: [], ay: [], az: [], gx: [], gy: [] },
  logs: [],
  permissions: [
    { name: "TeamViewer", pkg: "com.teamviewer.teamviewer.market.mobile", risk: "HIGH", blocked: false, a11y: true },
    { name: "AnyDesk", pkg: "com.anydesk.anydeskandroid", risk: "HIGH", blocked: false, a11y: true },
    { name: "Chrome Remote", pkg: "com.google.chromeremotedesktop", risk: "MEDIUM", blocked: false, a11y: true },
    { name: "Accessibility Menu", pkg: "com.google.android.marvin.talkback", risk: "LOW", blocked: false, a11y: true },
    { name: "LastPass", pkg: "com.lastpass.lpandroid", risk: "MEDIUM", blocked: false, a11y: true },
    { name: "Tasker", pkg: "net.dinglisch.android.taskerm", risk: "MEDIUM", blocked: false, a11y: true },
    { name: "Unknown Service", pkg: "com.suspect.remote.access", risk: "CRITICAL", blocked: true, a11y: true }
  ],
  switches: { motion: true, network: true, a11y: true },
  radar: {
    angle: 0,
    blips: [],
    pulseRings: [],
    scanLines: []
  }
};

let paywallVisible = false;
const MAX_POINTS = 80;
const TWO_PI = Math.PI * 2;

function init() {
  updateClock();
  setInterval(updateClock, 1000);

  addLog("INFO", "BOOT", "BlockRemote Sentinel v2.0 initialized");
  addLog("INFO", "DEVICE", "Device ID: BR-a1b2c3d4e5f6");
  addLog("INFO", "SENSOR", "Accelerometer calibration complete");
  addLog("INFO", "GUARD", "Accessibility service monitor active");
  addLog("INFO", "NETWORK", "Network traffic analyzer online");

  renderPermissions();
  renderLogs();
  startSensorStream();
  initRadarBlips();
  startRadar();
  simulateBillingCheckin();
  startHeartbeat();
}

function simulateBillingCheckin() {
  setTimeout(() => {
    addLog("INFO", "BILLING", "Checking license status...");
    renderLogs();
    setTimeout(() => {
      addLog("INFO", "BILLING", "License active — plan: SENTINEL PRO");
      addLog("INFO", "WS", "Connecting to sentinel relay: wss://api.blockremote.io/v1/signals");
      renderLogs();
      setTimeout(() => {
        addLog("INFO", "WS", "Sentinel relay connected");
        addLog("INFO", "WS", "Zero-trust authentication confirmed");
        renderLogs();
      }, 800);
    }, 600);
  }, 1500);
}

function startHeartbeat() {
  setInterval(() => {
    addLog("INFO", "HEARTBEAT", "Signal dispatched to sentinel relay");
    renderLogs();
  }, 30000);
}

function updateClock() {
  const now = new Date();
  document.getElementById('statusTime').textContent =
    now.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
}

function switchScreen(name) {
  document.querySelectorAll('.screen').forEach(s => s.classList.remove('active'));
  document.querySelectorAll('.nav-item').forEach(n => n.classList.remove('active'));

  document.getElementById(`screen-${name}`).classList.add('active');
  event.currentTarget.classList.add('active');
}

function simulateThreat() {
  if (state.isAlertMode) return;
  state.isAlertMode = true;

  document.getElementById('appTitle').classList.add('alert');
  document.getElementById('statusCard').classList.add('alert');
  document.getElementById('statusText').textContent = '⚠ THREAT DETECTED';
  document.getElementById('statusText').classList.add('alert');
  document.getElementById('statusDesc').textContent = 'Anomalous activity detected.\nCountermeasures engaged. Stand by.';
  document.getElementById('threatLevel').textContent = '85%';
  document.getElementById('threatLevel').classList.add('red');
  document.getElementById('threatBtn').classList.add('alert');
  document.querySelector('#threatBtn .btn-text').textContent = 'NEUTRALIZING...';

  for (let i = 0; i < 5; i++) {
    state.radar.blips.push(createBlip(true));
  }

  addLog("CRIT", "THREAT", "Unauthorized remote access pattern detected");
  addLog("WARN", "SENSOR", "Anomalous accelerometer spike: dx=4.2g");
  addLog("CRIT", "A11Y", "Suspicious accessibility service injection: com.suspect.remote");
  addLog("INFO", "SHIELD", "Countermeasures engaged — blocking threat vector");
  renderLogs();

  if (navigator.vibrate) navigator.vibrate([100, 50, 200]);

  setTimeout(resetThreat, 5000);
}

function resetThreat() {
  state.isAlertMode = false;

  document.getElementById('appTitle').classList.remove('alert');
  document.getElementById('statusCard').classList.remove('alert');
  document.getElementById('statusText').textContent = 'ALL SYSTEMS NOMINAL';
  document.getElementById('statusText').classList.remove('alert');
  document.getElementById('statusDesc').textContent = 'No unauthorized remote access detected.\nAll sensors operating within parameters.';
  document.getElementById('threatLevel').textContent = '0%';
  document.getElementById('threatLevel').classList.remove('red');
  document.getElementById('threatBtn').classList.remove('alert');
  document.querySelector('#threatBtn .btn-text').textContent = 'SIMULAR AMEAÇA';

  state.radar.blips = state.radar.blips.filter(b => !b.threat);
  initRadarBlips();

  addLog("INFO", "SYSTEM", "Threat neutralized — system restored to safe state");
  renderLogs();
}

function createBlip(threat = false) {
  const angle = Math.random() * TWO_PI;
  const dist = 0.2 + Math.random() * 0.7;
  return {
    angle,
    dist,
    size: threat ? 3 + Math.random() * 3 : 1.5 + Math.random() * 2,
    alpha: 0,
    maxAlpha: threat ? 1.0 : 0.5 + Math.random() * 0.4,
    pulse: Math.random() * TWO_PI,
    threat,
    drift: (Math.random() - 0.5) * 0.002,
    label: threat ? 'THR' : null
  };
}

function initRadarBlips() {
  state.radar.blips = [];
  for (let i = 0; i < 8; i++) {
    state.radar.blips.push(createBlip(false));
  }
}

function startRadar() {
  const canvas = document.getElementById('radarCanvas');
  if (!canvas) return;
  const ctx = canvas.getContext('2d');

  function frame(time) {
    drawRadar(ctx, canvas, time);
    requestAnimationFrame(frame);
  }
  requestAnimationFrame(frame);
}

function drawRadar(ctx, canvas, time) {
  const dpr = window.devicePixelRatio || 1;
  const displayW = canvas.clientWidth;
  const displayH = canvas.clientHeight;

  if (canvas.width !== displayW * dpr || canvas.height !== displayH * dpr) {
    canvas.width = displayW * dpr;
    canvas.height = displayH * dpr;
  }

  ctx.setTransform(dpr, 0, 0, dpr, 0, 0);

  const w = displayW;
  const h = displayH;
  const cx = w / 2;
  const cy = h / 2;
  const maxR = Math.min(cx, cy) - 8;

  const isAlert = state.isAlertMode;
  const neon = isAlert ? [255, 0, 64] : [0, 255, 65];
  const neonHex = isAlert ? '#ff0040' : '#00ff41';
  const limeHex = isAlert ? '#ff4466' : '#adff2f';
  const neonRgb = `${neon[0]},${neon[1]},${neon[2]}`;

  const sweepSpeed = isAlert ? 0.0015 : 0.0008;
  state.radar.angle = (state.radar.angle + sweepSpeed * 16.67) % TWO_PI;
  const sweep = state.radar.angle;

  ctx.clearRect(0, 0, w, h);

  const bgGrad = ctx.createRadialGradient(cx, cy, 0, cx, cy, maxR);
  bgGrad.addColorStop(0, `rgba(${neonRgb},0.03)`);
  bgGrad.addColorStop(0.5, `rgba(${neonRgb},0.015)`);
  bgGrad.addColorStop(1, 'rgba(0,0,0,0)');
  ctx.fillStyle = bgGrad;
  ctx.beginPath();
  ctx.arc(cx, cy, maxR, 0, TWO_PI);
  ctx.fill();

  const ringCount = 5;
  for (let i = 1; i <= ringCount; i++) {
    const r = (maxR / ringCount) * i;
    const alpha = i === ringCount ? 0.25 : 0.08 + (i * 0.02);
    ctx.strokeStyle = `rgba(${neonRgb},${alpha})`;
    ctx.lineWidth = i === ringCount ? 1.5 : 0.5;
    ctx.beginPath();
    ctx.arc(cx, cy, r, 0, TWO_PI);
    ctx.stroke();

    if (i < ringCount) {
      const tickAlpha = 0.15;
      ctx.fillStyle = `rgba(${neonRgb},${tickAlpha})`;
      ctx.font = '8px "JetBrains Mono", monospace';
      ctx.textAlign = 'center';
      ctx.fillText(`${i * 20}`, cx + r + 1, cy - 3);
    }
  }

  const crossAlpha = 0.12;
  ctx.strokeStyle = `rgba(${neonRgb},${crossAlpha})`;
  ctx.lineWidth = 0.5;
  for (let a = 0; a < 12; a++) {
    const ang = (a / 12) * TWO_PI;
    ctx.beginPath();
    ctx.moveTo(cx, cy);
    ctx.lineTo(cx + Math.cos(ang) * maxR, cy + Math.sin(ang) * maxR);
    ctx.stroke();
  }

  ctx.strokeStyle = `rgba(${neonRgb},0.2)`;
  ctx.lineWidth = 0.3;
  ctx.setLineDash([2, 4]);
  for (let a = 0; a < 36; a++) {
    const ang = (a / 36) * TWO_PI;
    ctx.beginPath();
    ctx.moveTo(cx + Math.cos(ang) * maxR * 0.15, cy + Math.sin(ang) * maxR * 0.15);
    ctx.lineTo(cx + Math.cos(ang) * maxR, cy + Math.sin(ang) * maxR);
    ctx.stroke();
  }
  ctx.setLineDash([]);

  const sweepGrad = ctx.createConicalGradient
    ? null
    : null;

  ctx.save();
  ctx.translate(cx, cy);
  ctx.rotate(sweep);

  const trailAngle = isAlert ? 0.7 : 0.5;
  const segments = 60;
  for (let i = 0; i < segments; i++) {
    const t = i / segments;
    const a1 = -trailAngle * t;
    const a2 = -trailAngle * (i + 1) / segments;
    const alpha = (1 - t) * (isAlert ? 0.25 : 0.18);

    ctx.fillStyle = `rgba(${neonRgb},${alpha})`;
    ctx.beginPath();
    ctx.moveTo(0, 0);
    ctx.arc(0, 0, maxR, a1, a2, true);
    ctx.closePath();
    ctx.fill();
  }

  ctx.strokeStyle = neonHex;
  ctx.lineWidth = 2;
  ctx.shadowColor = neonHex;
  ctx.shadowBlur = 12;
  ctx.beginPath();
  ctx.moveTo(0, 0);
  ctx.lineTo(maxR, 0);
  ctx.stroke();

  ctx.shadowColor = neonHex;
  ctx.shadowBlur = 20;
  ctx.strokeStyle = `rgba(${neonRgb},0.6)`;
  ctx.lineWidth = 4;
  ctx.beginPath();
  ctx.moveTo(0, 0);
  ctx.lineTo(maxR, 0);
  ctx.stroke();

  ctx.shadowBlur = 0;
  ctx.restore();

  const blipTime = time * 0.003;
  state.radar.blips.forEach(blip => {
    blip.angle += blip.drift;
    blip.pulse += 0.05;

    let angleDiff = sweep - blip.angle;
    while (angleDiff < 0) angleDiff += TWO_PI;
    while (angleDiff > TWO_PI) angleDiff -= TWO_PI;

    if (angleDiff < 0.15) {
      blip.alpha = blip.maxAlpha;
    } else {
      blip.alpha *= 0.985;
    }

    if (blip.alpha < 0.01) return;

    const bx = cx + Math.cos(blip.angle) * blip.dist * maxR;
    const by = cy + Math.sin(blip.angle) * blip.dist * maxR;
    const pulseSize = blip.size + Math.sin(blip.pulse) * 1.5;

    const blipColor = blip.threat ? '#ff0040' : neonHex;
    const blipRgb = blip.threat ? '255,0,64' : neonRgb;

    ctx.shadowColor = blipColor;
    ctx.shadowBlur = blip.threat ? 15 : 8;

    const blipGrad = ctx.createRadialGradient(bx, by, 0, bx, by, pulseSize * 3);
    blipGrad.addColorStop(0, `rgba(${blipRgb},${blip.alpha})`);
    blipGrad.addColorStop(0.4, `rgba(${blipRgb},${blip.alpha * 0.4})`);
    blipGrad.addColorStop(1, `rgba(${blipRgb},0)`);
    ctx.fillStyle = blipGrad;
    ctx.beginPath();
    ctx.arc(bx, by, pulseSize * 3, 0, TWO_PI);
    ctx.fill();

    ctx.fillStyle = `rgba(${blipRgb},${blip.alpha})`;
    ctx.beginPath();
    ctx.arc(bx, by, pulseSize, 0, TWO_PI);
    ctx.fill();

    if (blip.threat && blip.alpha > 0.3) {
      ctx.strokeStyle = `rgba(255,0,64,${blip.alpha * 0.6})`;
      ctx.lineWidth = 1;
      const crossSize = pulseSize + 6;
      ctx.beginPath();
      ctx.moveTo(bx - crossSize, by);
      ctx.lineTo(bx + crossSize, by);
      ctx.stroke();
      ctx.beginPath();
      ctx.moveTo(bx, by - crossSize);
      ctx.lineTo(bx, by + crossSize);
      ctx.stroke();

      ctx.strokeStyle = `rgba(255,0,64,${blip.alpha * 0.3})`;
      ctx.beginPath();
      ctx.arc(bx, by, crossSize + 4, 0, TWO_PI);
      ctx.stroke();

      ctx.fillStyle = `rgba(255,0,64,${blip.alpha * 0.8})`;
      ctx.font = 'bold 7px "JetBrains Mono", monospace';
      ctx.textAlign = 'center';
      ctx.fillText('THR', bx, by - crossSize - 4);
    }

    ctx.shadowBlur = 0;
  });

  if (Math.random() < (isAlert ? 0.04 : 0.015)) {
    state.radar.pulseRings.push({ r: 0, alpha: 0.4, speed: isAlert ? 1.5 : 0.8 });
  }
  state.radar.pulseRings = state.radar.pulseRings.filter(p => p.alpha > 0.01);
  state.radar.pulseRings.forEach(p => {
    p.r += p.speed;
    p.alpha *= 0.98;
    ctx.strokeStyle = `rgba(${neonRgb},${p.alpha * 0.3})`;
    ctx.lineWidth = 1;
    ctx.beginPath();
    ctx.arc(cx, cy, p.r, 0, TWO_PI);
    ctx.stroke();
  });

  ctx.shadowColor = neonHex;
  ctx.shadowBlur = 20;
  ctx.fillStyle = neonHex;
  ctx.beginPath();
  ctx.arc(cx, cy, 4, 0, TWO_PI);
  ctx.fill();

  ctx.shadowBlur = 10;
  ctx.strokeStyle = `rgba(${neonRgb},0.5)`;
  ctx.lineWidth = 1.5;
  ctx.beginPath();
  ctx.arc(cx, cy, 8, 0, TWO_PI);
  ctx.stroke();

  ctx.shadowBlur = 0;

  ctx.fillStyle = `rgba(${neonRgb},0.4)`;
  ctx.font = '9px "JetBrains Mono", monospace';
  ctx.textAlign = 'left';
  ctx.fillText('N', cx - 3, cy - maxR + 14);
  ctx.fillText('S', cx - 3, cy + maxR - 8);
  ctx.textAlign = 'left';
  ctx.fillText('E', cx + maxR - 14, cy + 3);
  ctx.textAlign = 'right';
  ctx.fillText('W', cx - maxR + 14, cy + 3);

  const deg = Math.round((sweep / TWO_PI) * 360) % 360;
  ctx.fillStyle = `rgba(${neonRgb},0.5)`;
  ctx.font = '8px "JetBrains Mono", monospace';
  ctx.textAlign = 'right';
  ctx.fillText(`${deg.toString().padStart(3, '0')}°`, cx + maxR - 2, cy - maxR + 14);

  ctx.textAlign = 'left';
  ctx.fillStyle = `rgba(${neonRgb},0.3)`;
  ctx.fillText(`RNG: ${maxR.toFixed(0)}m`, cx - maxR + 4, cy + maxR - 8);

  if (isAlert) {
    const flashAlpha = (Math.sin(time * 0.008) * 0.5 + 0.5) * 0.08;
    ctx.strokeStyle = `rgba(255,0,64,${flashAlpha + 0.1})`;
    ctx.lineWidth = 2;
    ctx.beginPath();
    ctx.arc(cx, cy, maxR, 0, TWO_PI);
    ctx.stroke();
  }
}

function addLog(level, tag, message) {
  const now = new Date();
  const ts = now.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit', second: '2-digit', fractionalSecondDigits: 3 });
  state.logs.push({ ts, level, tag, message });
  if (state.logs.length > 200) state.logs.shift();
}

function renderLogs() {
  const body = document.getElementById('terminalBody');
  body.innerHTML = state.logs.map(log => {
    const levelClass = log.level === 'CRIT' ? 'log-level-crit' : log.level === 'WARN' ? 'log-level-warn' : 'log-level-info';
    return `<div class="log-line">` +
      `<span class="log-time">[${log.ts}] </span>` +
      `<span class="${levelClass}">${log.level.padEnd(4)} </span>` +
      `<span class="log-tag">[${log.tag}] </span>` +
      `<span class="log-msg">${log.message}</span>` +
      `</div>`;
  }).join('');
  body.scrollTop = body.scrollHeight;
}

function renderPermissions() {
  const container = document.getElementById('permissionsList');
  container.innerHTML = state.permissions.map((p, i) => {
    const riskClass = p.risk.toLowerCase();
    return `<div class="permission-card ${riskClass}">
      <div class="perm-header">
        <div>
          <div class="perm-name">${p.name}</div>
          <div class="perm-pkg">${p.pkg}</div>
        </div>
        <div class="risk-badge ${riskClass}">
          <div class="risk-dot"></div>
          ${p.risk}
        </div>
      </div>
      <div class="switch-row" onclick="togglePermission(${i})">
        <div class="switch-info">
          <span class="switch-label">${p.blocked ? 'BLOCKED' : 'ALLOWED'}</span>
          <span class="switch-sub">${p.a11y ? 'Accessibility Service' : 'Standard Permission'}</span>
        </div>
        <div class="sentinel-switch ${p.blocked ? 'active' : ''}" id="perm-switch-${i}">
          <div class="switch-thumb"></div>
        </div>
      </div>
    </div>`;
  }).join('');
  updateBlockedCount();
}

function togglePermission(index) {
  state.permissions[index].blocked = !state.permissions[index].blocked;
  const p = state.permissions[index];
  addLog("INFO", "SHIELD", `${p.blocked ? 'BLOCKED' : 'UNBLOCKED'}: ${p.name} (${p.pkg})`);
  renderPermissions();
  renderLogs();
}

function updateBlockedCount() {
  const count = state.permissions.filter(p => p.blocked).length;
  document.getElementById('blockedCount').textContent = count;
}

function toggleSwitch(id) {
  state.switches[id] = !state.switches[id];
  const el = document.getElementById(`switch-${id}`);
  el.classList.toggle('active');
  const labels = { motion: 'Motion detection', network: 'Network monitor', a11y: 'Accessibility guard' };
  addLog("INFO", id === 'a11y' ? 'GUARD' : id === 'network' ? 'NETWORK' : 'SENSOR',
    `${labels[id]} ${state.switches[id] ? 'ENABLED' : 'DISABLED'}`);
  renderLogs();
}

function updateThreshold(val) {
  document.getElementById('thresholdVal').textContent = val + '%';
}

function updateSensitivity(val) {
  document.getElementById('sensitivityVal').textContent = val + '%';
}

function startSensorStream() {
  setInterval(() => {
    const noise = state.isAlertMode ? 3.5 : 0.3;
    const ax = (Math.random() - 0.5) * noise;
    const ay = (Math.random() - 0.5) * noise;
    const az = (Math.random() - 0.5) * noise;
    const gx = (Math.random() - 0.5) * noise * 0.5;
    const gy = (Math.random() - 0.5) * noise * 0.5;

    pushData('ax', ax);
    pushData('ay', ay);
    pushData('az', az);
    pushData('gx', gx);
    pushData('gy', gy);

    document.getElementById('statAX').textContent = formatStat(ax);
    document.getElementById('statAY').textContent = formatStat(ay);
    document.getElementById('statAZ').textContent = formatStat(9.81 + az);
    document.getElementById('statGX').textContent = formatStat(gx);
    document.getElementById('statGY').textContent = formatStat(gy);

    drawWave('canvasAccelX', state.sensorData.ax, '#00ff41');
    drawWave('canvasAccelY', state.sensorData.ay, '#adff2f');
    drawWave('canvasAccelZ', state.sensorData.az, '#ff0040');
    drawWave('canvasGyro', state.sensorData.gx, 'rgba(0, 255, 65, 0.7)');
  }, 50);
}

function pushData(key, val) {
  state.sensorData[key].push(val);
  if (state.sensorData[key].length > MAX_POINTS) state.sensorData[key].shift();
}

function formatStat(v) {
  return (v >= 0 ? '+' : '') + v.toFixed(3);
}

function drawWave(canvasId, data, color) {
  const canvas = document.getElementById(canvasId);
  if (!canvas) return;

  const ctx = canvas.getContext('2d');
  const dpr = window.devicePixelRatio || 1;

  if (canvas.width !== canvas.clientWidth * dpr) {
    canvas.width = canvas.clientWidth * dpr;
    canvas.height = canvas.clientHeight * dpr;
    ctx.scale(dpr, dpr);
  }

  const w = canvas.clientWidth;
  const h = canvas.clientHeight;
  const centerY = h / 2;
  const maxVal = 4;

  ctx.clearRect(0, 0, w, h);

  ctx.strokeStyle = color + '18';
  ctx.lineWidth = 0.5;
  for (let i = 0; i <= 4; i++) {
    const y = (h / 4) * i;
    ctx.beginPath();
    ctx.moveTo(0, y);
    ctx.lineTo(w, y);
    ctx.stroke();
  }

  ctx.strokeStyle = color + '33';
  ctx.lineWidth = 0.5;
  ctx.beginPath();
  ctx.moveTo(0, centerY);
  ctx.lineTo(w, centerY);
  ctx.stroke();

  if (data.length < 2) return;

  const step = w / (MAX_POINTS - 1);

  ctx.strokeStyle = color + '30';
  ctx.lineWidth = 5;
  ctx.lineCap = 'round';
  ctx.lineJoin = 'round';
  ctx.beginPath();
  data.forEach((v, i) => {
    const x = i * step;
    const y = centerY - (v / maxVal) * (h / 2);
    i === 0 ? ctx.moveTo(x, y) : ctx.lineTo(x, y);
  });
  ctx.stroke();

  ctx.strokeStyle = color;
  ctx.lineWidth = 1.5;
  ctx.beginPath();
  data.forEach((v, i) => {
    const x = i * step;
    const y = centerY - (v / maxVal) * (h / 2);
    i === 0 ? ctx.moveTo(x, y) : ctx.lineTo(x, y);
  });
  ctx.stroke();
}

function showPaywall() {
  paywallVisible = true;
  document.getElementById('subscriptionOverlay').style.display = 'flex';
  document.querySelector('.phone-frame').classList.add('grayscale');
}

function dismissPaywall() {
  paywallVisible = false;
  document.getElementById('subscriptionOverlay').style.display = 'none';
  document.querySelector('.phone-frame').classList.remove('grayscale');
  addLog("INFO", "BILLING", "License activated — SENTINEL PRO");
  renderLogs();
}

init();
