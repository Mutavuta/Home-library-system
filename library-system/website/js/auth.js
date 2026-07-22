// ── Session helpers ──────────────────────────────────────────────────────────
const auth = {
  save(data) {
    localStorage.setItem('lib_token',  data.token);
    localStorage.setItem('lib_userId', data.userId);
    localStorage.setItem('lib_role',   data.role);
    localStorage.setItem('lib_name',   data.fullName);
    localStorage.setItem('lib_status', data.status);
  },
  clear() {
    ['lib_token','lib_userId','lib_role','lib_name','lib_status']
      .forEach(k => localStorage.removeItem(k));
  },
  token()     { return localStorage.getItem('lib_token');  },
  userId()    { return localStorage.getItem('lib_userId'); },
  role()      { return localStorage.getItem('lib_role');   },
  name()      { return localStorage.getItem('lib_name');   },
  status()    { return localStorage.getItem('lib_status'); },
  isLoggedIn(){ return !!this.token(); },
  isApproved(){ return this.status() === 'approved'; },

  requireLogin(redirectTo = '../index.html') {
    if (!this.isLoggedIn()) {
      window.location.href = redirectTo;
      return false;
    }
    return true;
  },

  logout() {
    this.clear();
    window.location.href = '../index.html?loggedOut=1';
  }
};

// ── Toast notifications ──────────────────────────────────────────────────────
function toast(msg, type = 'success') {
  let container = document.getElementById('toast-container');
  if (!container) {
    container = document.createElement('div');
    container.id = 'toast-container';
    container.style.cssText =
      'position:fixed;top:20px;right:20px;z-index:9999;display:flex;flex-direction:column;gap:8px;';
    document.body.appendChild(container);
  }
  const t = document.createElement('div');
  t.style.cssText =
    `padding:12px 20px;border-radius:10px;color:#fff;font-size:14px;max-width:320px;
     box-shadow:0 4px 14px rgba(0,0,0,.15);animation:slideIn .3s ease;font-weight:500;
     background:${type==='success'?'#22c55e':type==='error'?'#ef4444':'#3b82f6'};`;
  t.textContent = msg;
  container.appendChild(t);
  setTimeout(() => t.remove(), 3500);
}

// ── Helpers used across pages ────────────────────────────────────────────────
function fmtDate(d) {
  if (!d) return '—';
  return new Date(d).toLocaleDateString('en-KE',
    { day:'2-digit', month:'short', year:'numeric' });
}

function badge(status) {
  const colors = {
    available:'#22c55e', reserved:'#f59e0b', loaned:'#ef4444',
    pending:'#f59e0b',   approved:'#3b82f6', collected:'#22c55e',
    abandoned:'#6b7280', active:'#3b82f6',   returned:'#22c55e',
    overdue:'#ef4444',   waiting:'#f59e0b',  notified:'#22c55e',
  };
  const c = colors[status] || '#6b7280';
  return `<span style="background:${c};color:#fff;padding:3px 10px;border-radius:20px;
                font-size:12px;font-weight:600;text-transform:capitalize;">${status}</span>`;
}
