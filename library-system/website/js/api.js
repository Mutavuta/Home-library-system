// ── API helper — borrower website only ──────────────────────────────────────
const API_BASE = 'http://localhost:8080/api';

const api = {
  _token() { return localStorage.getItem('lib_token'); },
  _headers(auth = true) {
    const h = { 'Content-Type': 'application/json' };
    if (auth && this._token()) h['Authorization'] = 'Bearer ' + this._token();
    return h;
  },
  async _fetch(path, options = {}) {
  const url = API_BASE + path;
  const res = await fetch(url, options);

  console.log('[api] request', (options.method||'GET'), url, 'status', res.status, 'ok', res.ok, 'type', res.type, 'redirected', res.redirected);
  const ct = res.headers.get('content-type');
  console.log('[api] content-type:', ct);

  const text = await res.text();
  console.log('[api] response length', text.length, 'preview:', text ? text.slice(0,300) : '(empty)');

  if (!text) {
    if (res.ok) return null; // adapt to your API contract (null means empty-but-success)
    throw new Error(`Empty response from server (status ${res.status})`);
  }

  let json;
  try { json = JSON.parse(text); }
  catch (e) {
    throw new Error('Invalid JSON response: ' + e.message + '\nResponse body preview: ' + text.slice(0,1000));
  }

  if (!json.success) throw new Error(json.message || 'Request failed');
  return json.data;
 },

  // ── Auth ──────────────────────────────────────────────────────────────────
  register: d => api._fetch('/auth/register',
    { method:'POST', headers:api._headers(false), body:JSON.stringify(d) }),

  login: d => api._fetch('/auth/login',
    { method:'POST', headers:api._headers(false), body:JSON.stringify(d) }),

  // ── Books (public — no login needed) ─────────────────────────────────────
  getTitles: category => api._fetch(
    '/book/titles' + (category ? '?category=' + category : ''),
    { headers: api._headers() }),

  getTitle: id => api._fetch('/book/titles/' + id,
    { headers: api._headers() }),

  // ── Holds (borrower) ──────────────────────────────────────────────────────
  placeHold: titleId => api._fetch('/holds',
    { method:'POST', headers:api._headers(), body:JSON.stringify({ titleId }) }),

  myHolds: () => api._fetch('/holds/mine', { headers: api._headers() }),

  cancelHold: id => api._fetch('/holds/' + id,
    { method:'DELETE', headers: api._headers() }),

  // ── Loans (borrower) ──────────────────────────────────────────────────────
  myLoans: () => api._fetch('/loans/mine', { headers: api._headers() }),

  // ── Profile (borrower) ────────────────────────────────────────────────────
  getProfile: () => api._fetch('/users/me', { headers: api._headers() }),

  updateProfile: d => api._fetch('/users/me',
    { method:'PUT', headers: api._headers(), body: JSON.stringify(d) }),

  // ── Notifications (borrower) ──────────────────────────────────────────────
  getNotifications: () => api._fetch('/notifications',
    { headers: api._headers() }),

  getUnreadCount: () => api._fetch('/notifications/unread-count',
    { headers: api._headers() }),

  markAllRead: () => api._fetch('/notifications/mark-read',
    { method:'POST', headers: api._headers() }),
};
