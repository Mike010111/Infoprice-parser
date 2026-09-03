/**
 * InfoPrice Parser — frontend логика.
 *
 * REST API бэкенда (Java, com.sun.net.httpserver), отдаётся тем же сервером на localhost:8080:
 *
 *   POST /api/scan                -> { started: true }
 *   GET  /api/scan/status         -> { state: "IDLE|RUNNING|SUCCESS|ERROR", progress: 0-100, message: "..." }
 *   GET  /api/results/latest      -> { fileName, downloadUrl, stores: [...], items: [{name, prices:{...}}] }
 *   GET  /api/results/file/{name} -> бинарный xlsx файл на скачивание
 */

const API_BASE = '';

const el = (id) => document.getElementById(id);

const scanNowBtn = el('scanNowBtn');
const statusDot = el('statusDot');
const statusText = el('statusText');
const progressFill = el('progressFill');
const progressPercent = el('progressPercent');
const statusLog = el('statusLog');

const resultFileName = el('resultFileName');
const downloadBtn = el('downloadBtn');
const previewTable = el('previewTable');
const previewHead = el('previewHead');
const previewBody = el('previewBody');
const emptyState = el('emptyState');

let pollTimer = null;
let lastLoggedMessage = null;


function logLine(text) {
  statusLog.classList.add('visible');
  const time = new Date().toLocaleTimeString('ru-RU');
  statusLog.innerHTML += `[${time}] ${text}<br>`;
  statusLog.scrollTop = statusLog.scrollHeight;
}

function setStatusVisual(state) {
  statusDot.className = 'status-dot';
  if (state === 'RUNNING') statusDot.classList.add('running');
  if (state === 'SUCCESS') statusDot.classList.add('success');
  if (state === 'ERROR') statusDot.classList.add('error');
}

// ---------- Немедленное сканирование ----------

scanNowBtn.addEventListener('click', async () => {
  scanNowBtn.disabled = true;
  statusLog.innerHTML = '';
  lastLoggedMessage = null;
  logLine('Запрос на запуск сканирования отправлен...');
  try {
    await fetch(`${API_BASE}/api/scan`, { method: 'POST' });
    startPolling();
  } catch (e) {
    logLine('Ошибка: не удалось связаться с бэкендом.');
    scanNowBtn.disabled = false;
  }
});


function startPolling() {
  if (pollTimer) clearInterval(pollTimer);
  pollTimer = setInterval(pollStatus, 1500);
  pollStatus();
}

async function pollStatus() {
  try {
    const res = await fetch(`${API_BASE}/api/scan/status`);
    const data = await res.json();

    setStatusVisual(data.state);
    progressFill.style.width = `${data.progress || 0}%`;
    progressPercent.textContent = `${data.progress || 0}%`;

    const currentMessage = data.message || '';

    if (data.state === 'RUNNING') {
      statusText.textContent = currentMessage || 'Сканирование в процессе...';
      if (currentMessage !== lastLoggedMessage) {
        logLine(currentMessage || 'Обработка...');
        lastLoggedMessage = currentMessage;
      }
    } else if (data.state === 'SUCCESS') {
      statusText.textContent = 'Сканирование завершено успешно';
      if (currentMessage !== lastLoggedMessage) {
        logLine('Готово. Файл сформирован.');
        lastLoggedMessage = currentMessage;
      }
      clearInterval(pollTimer);
      scanNowBtn.disabled = false;
      await loadResults();
    } else if (data.state === 'ERROR') {
      statusText.textContent = 'Ошибка при сканировании';
      if (currentMessage !== lastLoggedMessage) {
        logLine(currentMessage || 'Неизвестная ошибка.');
        lastLoggedMessage = currentMessage;
      }
      clearInterval(pollTimer);
      scanNowBtn.disabled = false;
    } else {
      statusText.textContent = 'Ожидание запуска';
    }
  } catch (e) {
    logLine('Ошибка опроса статуса.');
  }
}


// ---------- Результаты ----------

async function loadResults() {
  try {
    const res = await fetch(`${API_BASE}/api/results/latest`);
    if (!res.ok) return;
    const data = await res.json();

    if (data.fileName) {
      resultFileName.textContent = data.fileName;
      downloadBtn.href = `${API_BASE}${data.downloadUrl}`;
      downloadBtn.classList.remove('btn-disabled');
    }

    renderPreview(data.stores || [], data.items || []);
  } catch (e) {
    console.warn('Не удалось загрузить результаты:', e);
  }
}

function renderPreview(stores, items) {
  if (!items.length || !stores.length) {
    previewTable.style.display = 'none';
    emptyState.style.display = 'block';
    previewHead.innerHTML = '';
    previewBody.innerHTML = '';
    return;
  }

  previewTable.style.display = 'table';
  emptyState.style.display = 'none';

  const headerCells = ['Товар', ...stores].map((s) => `<th>${s}</th>`).join('');
  previewHead.innerHTML = `<tr>${headerCells}</tr>`;

  const rows = items.map((item) => {
    const cells = stores.map((store) => {
      const cellData = item.prices ? item.prices[store] : null;
      if (!cellData || cellData.value == null) return '<td>—</td>';
      const cssClass = cellData.promo ? ' class="promo-price"' : '';
      return `<td${cssClass}>${cellData.value}</td>`;
    }).join('');
    return `<tr><td>${item.name}</td>${cells}</tr>`;
  });

  previewBody.innerHTML = rows.join('');
}


// ---------- Инициализация ----------

(async function init() {
  await loadResults();
})();
