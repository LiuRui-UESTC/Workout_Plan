/* ==========================================
   FitMate - 健身记录 SPA (Auth 版本)
   ========================================== */

// ========== 全局状态 ==========
const state = {
    token: localStorage.getItem('fitmate_token') || null,
    userId: localStorage.getItem('fitmate_userId') || null,
    nickname: localStorage.getItem('fitmate_nickname') || '',
    avatarEmoji: localStorage.getItem('fitmate_avatar') || '',
    partnerId: null,
    workoutTypes: {},
    selectedWorkoutType: null,
    selectedMood: 4,
    currentPeriod: 'week',
    exerciseCount: 0,
    selectedAvatar: '💪'
};

// ========== API 工具 (自动携带 JWT) ==========
function authHeaders() {
    const h = { 'Content-Type': 'application/json' };
    if (state.token) h['Authorization'] = 'Bearer ' + state.token;
    return h;
}

function handle401(res) {
    if (res.status === 401) { logout(); return true; }
    return false;
}

const API = {
    async get(url) {
        const res = await fetch(url, { headers: authHeaders() });
        if (handle401(res)) throw new Error('unauthorized');
        if (!res.ok) throw new Error(`GET ${url}: ${res.status}`);
        return res.json();
    },
    async post(url, data) {
        const res = await fetch(url, { method: 'POST', headers: authHeaders(), body: JSON.stringify(data) });
        if (handle401(res)) throw new Error('unauthorized');
        const json = await res.json();
        if (!res.ok) throw new Error(json.error || `POST ${url}: ${res.status}`);
        return json;
    },
    async postNoAuth(url, data) {
        const res = await fetch(url, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(data) });
        const json = await res.json();
        if (!res.ok) throw new Error(json.error || `POST ${url}: ${res.status}`);
        return json;
    },
    async del(url) {
        const res = await fetch(url, { method: 'DELETE', headers: authHeaders() });
        if (handle401(res)) throw new Error('unauthorized');
        if (!res.ok) throw new Error(`DELETE ${url}: ${res.status}`);
    }
};

// ========== 运动类型 Emoji ==========
const TYPE_EMOJIS = {
    RUNNING: '🏃', STRENGTH: '🏋️', YOGA: '🧘', SWIMMING: '🏊',
    CYCLING: '🚴', HIIT: '⚡', WALKING: '🚶', DANCE: '💃',
    BADMINTON: '🏸', BASKETBALL: '🏀', OTHER: '🎯'
};
const CHART_COLORS = [
    '#6C63FF', '#FF6B9D', '#00D2FF', '#4CAF50', '#FF9800',
    '#F44336', '#9C27B0', '#795548', '#607D8B', '#E91E63', '#3F51B5'
];

// ========== 认证管理 ==========
function saveAuth(data) {
    state.token = data.token;
    state.userId = data.userId;
    state.nickname = data.nickname;
    state.avatarEmoji = data.avatarEmoji;
    localStorage.setItem('fitmate_token', data.token);
    localStorage.setItem('fitmate_userId', data.userId);
    localStorage.setItem('fitmate_nickname', data.nickname);
    localStorage.setItem('fitmate_avatar', data.avatarEmoji);
}

function logout() {
    state.token = null; state.userId = null; state.nickname = ''; state.avatarEmoji = '';
    localStorage.removeItem('fitmate_token');
    localStorage.removeItem('fitmate_userId');
    localStorage.removeItem('fitmate_nickname');
    localStorage.removeItem('fitmate_avatar');
    document.getElementById('authOverlay').style.display = 'flex';
    document.getElementById('appContainer').style.display = 'none';
}

function showApp() {
    document.getElementById('authOverlay').style.display = 'none';
    document.getElementById('appContainer').style.display = '';
    updateHeader();
}

function updateHeader() {
    document.getElementById('headerEmoji').textContent = state.avatarEmoji || '💪';
    document.getElementById('headerName').textContent = state.nickname || '用户';
}

// ========== 初始化 ==========
document.addEventListener('DOMContentLoaded', async () => {
    initAuthForms();
    initNavigation();
    initWorkoutForm();
    initMeasurementForm();
    initPeriodSelector();
    setTodayDate();

    if (state.token && state.userId) {
        showApp();
        await loadUserInfo();
        await loadWorkoutTypes();
        await refreshHomePage();
    }
});

// ========== 认证表单 ==========
function initAuthForms() {
    // 登录/注册切换
    document.getElementById('showRegister').addEventListener('click', (e) => {
        e.preventDefault();
        document.getElementById('loginForm').classList.remove('active');
        document.getElementById('registerForm').classList.add('active');
    });
    document.getElementById('showLogin').addEventListener('click', (e) => {
        e.preventDefault();
        document.getElementById('registerForm').classList.remove('active');
        document.getElementById('loginForm').classList.add('active');
    });

    // 头像选择
    document.getElementById('avatarGrid').addEventListener('click', (e) => {
        const btn = e.target.closest('.avatar-btn');
        if (!btn) return;
        document.querySelectorAll('.avatar-btn').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        state.selectedAvatar = btn.dataset.emoji;
    });

    // 登录
    document.getElementById('loginBtn').addEventListener('click', doLogin);
    document.getElementById('loginPassword').addEventListener('keydown', (e) => { if (e.key === 'Enter') doLogin(); });

    // 注册
    document.getElementById('registerBtn').addEventListener('click', doRegister);

    // 退出
    document.getElementById('logoutBtn').addEventListener('click', logout);
}

async function doLogin() {
    const username = document.getElementById('loginUsername').value.trim();
    const password = document.getElementById('loginPassword').value;
    if (!username || !password) { showToast('请输入用户名和密码'); return; }

    try {
        const data = await API.postNoAuth('/api/auth/login', { username, password });
        saveAuth(data);
        showApp();
        await loadUserInfo();
        await loadWorkoutTypes();
        await refreshHomePage();
        showToast('登录成功! ' + data.avatarEmoji);
    } catch (err) {
        showToast(err.message || '登录失败');
    }
}

async function doRegister() {
    const username = document.getElementById('regUsername').value.trim();
    const nickname = document.getElementById('regNickname').value.trim();
    const password = document.getElementById('regPassword').value;
    const gender = document.getElementById('regGender').value;
    const height = parseFloat(document.getElementById('regHeight').value) || null;

    if (!username || !nickname || !password) { showToast('请填写必填项'); return; }
    if (username.length < 3 || username.length > 20) { showToast('用户名3-20个字符'); return; }
    if (password.length < 6) { showToast('密码至少6位'); return; }

    try {
        const data = await API.postNoAuth('/api/auth/register', {
            username, password, nickname,
            gender: gender || 'other',
            avatarEmoji: state.selectedAvatar,
            height
        });
        saveAuth(data);
        showApp();
        await loadUserInfo();
        await loadWorkoutTypes();
        await refreshHomePage();
        showToast('注册成功! 欢迎 ' + data.nickname + ' ' + data.avatarEmoji);
    } catch (err) {
        showToast(err.message || '注册失败');
    }
}

// ========== 加载当前用户详情 ==========
async function loadUserInfo() {
    try {
        const user = await API.get(`/api/users/${state.userId}`);
        state.partnerId = user.partnerId || null;
        state.avatarEmoji = user.avatarEmoji;
        state.nickname = user.nickname;
        updateHeader();
        // 显示绑定码
        if (user.partnerCode) {
            document.getElementById('myBindCode').textContent = user.partnerCode;
        }
        updatePartnerUI(user);
    } catch (err) {
        console.error('加载用户信息失败', err);
    }
}

function updatePartnerUI(user) {
    const unbindDiv = document.getElementById('partnerUnbind');
    const bindDiv = document.getElementById('partnerBindcode');
    if (user.partnerId) {
        // 已绑定
        unbindDiv.style.display = 'block';
        bindDiv.style.display = 'none';
        // 加载伴侣信息
        API.get(`/api/users/${user.partnerId}`).then(partner => {
            document.getElementById('partnerEmoji').textContent = partner.avatarEmoji;
            document.getElementById('partnerName').textContent = partner.nickname;
        }).catch(() => {});
    } else {
        unbindDiv.style.display = 'none';
        bindDiv.style.display = 'block';
    }
}

// ========== 情侣绑定 ==========
document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('bindPartnerBtn').addEventListener('click', async () => {
        const code = document.getElementById('partnerCodeInput').value.trim();
        if (!code) { showToast('请输入绑定码'); return; }
        try {
            const res = await API.post('/api/auth/bind-partner', { partnerCode: code });
            showToast(res.message || '绑定成功!');
            await loadUserInfo();
            await refreshHomePage();
        } catch (err) {
            showToast(err.message || '绑定失败');
        }
    });
});

// ========== 导航 ==========
function initNavigation() {
    document.querySelectorAll('.nav-btn').forEach(btn => {
        btn.addEventListener('click', () => switchPage(btn.dataset.page));
    });
}

function switchPage(page) {
    document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
    document.querySelectorAll('.nav-btn').forEach(b => b.classList.remove('active'));
    document.getElementById(`page-${page}`).classList.add('active');
    document.querySelector(`.nav-btn[data-page="${page}"]`).classList.add('active');
    if (page === 'home') refreshHomePage();
    if (page === 'stats') refreshStatsPage();
    if (page === 'profile') refreshProfilePage();
}

function setTodayDate() {
    const now = new Date();
    const options = { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' };
    document.getElementById('todayDate').textContent = now.toLocaleDateString('zh-CN', options);
    document.getElementById('workoutDate').value = now.toISOString().split('T')[0];
    document.getElementById('measurementDate').value = now.toISOString().split('T')[0];
}

function updateGreeting() {
    const hour = new Date().getHours();
    let greeting = hour < 12 ? '早上好' : hour < 18 ? '下午好' : '晚上好';
    document.getElementById('greetingText').textContent = `${greeting}, ${state.nickname} ${state.avatarEmoji}`;
}

// ========== 首页刷新 ==========
async function refreshHomePage() {
    updateGreeting();
    await Promise.all([loadTodayStats(), loadRecentActivity()]);
}

async function loadTodayStats() {
    try {
        const today = new Date().toISOString().split('T')[0];
        const stats = await API.get(`/api/workouts/stats/user/${state.userId}?start=${today}&end=${today}`);
        document.getElementById('todayCalories').textContent = Math.round(stats.totalCalories || 0);
        document.getElementById('todayDuration').textContent = stats.totalDuration || 0;
        document.getElementById('weekStreak').textContent = stats.weekStreakDays || 0;
    } catch (err) {
        console.error('加载今日统计失败', err);
    }
}

async function loadRecentActivity() {
    try {
        const today = new Date();
        const weekAgo = new Date(today);
        weekAgo.setDate(weekAgo.getDate() - 14);
        const workouts = await API.get(
            `/api/workouts/user/${state.userId}/range?start=${weekAgo.toISOString().split('T')[0]}&end=${today.toISOString().split('T')[0]}`
        );
        const container = document.getElementById('recentActivity');
        if (workouts.length === 0) {
            container.innerHTML = '<div class="empty-state">暂无记录，快去运动吧! 💪</div>';
            return;
        }
        container.innerHTML = workouts.slice(0, 10).map(w => {
            const emoji = TYPE_EMOJIS[w.workoutType] || '🎯';
            const typeName = state.workoutTypes[w.workoutType] || w.workoutType;
            const moodEmojis = ['😩', '😔', '🙂', '😄', '🤩'];
            const mood = w.moodRating ? moodEmojis[w.moodRating - 1] : '';
            return `
                <div class="activity-item">
                    <div class="activity-emoji">${emoji}</div>
                    <div class="activity-info">
                        <div class="activity-title">${typeName} ${mood}</div>
                        <div class="activity-meta">${w.workoutDate} · ${w.durationMinutes || 0}分钟 · ${w.caloriesBurned || 0}kcal</div>
                    </div>
                    <div class="activity-badge">${w.distance ? w.distance + 'km' : ''}</div>
                </div>
            `;
        }).join('');
    } catch (err) {
        console.error('加载活动失败', err);
    }
}

// ========== 运动类型加载 ==========
async function loadWorkoutTypes() {
    try {
        state.workoutTypes = await API.get('/api/workouts/types');
        const grid = document.getElementById('workoutTypeGrid');
        grid.innerHTML = Object.entries(state.workoutTypes).map(([key, name]) => {
            const emoji = TYPE_EMOJIS[key] || '🎯';
            return `<button type="button" class="type-btn" data-type="${key}">
                <span class="type-emoji">${emoji}</span>
                <span>${name}</span>
            </button>`;
        }).join('');

        grid.addEventListener('click', (e) => {
            const btn = e.target.closest('.type-btn');
            if (!btn) return;
            document.querySelectorAll('.type-btn').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            state.selectedWorkoutType = btn.dataset.type;
            document.getElementById('exerciseSection').style.display = state.selectedWorkoutType === 'STRENGTH' ? 'block' : 'none';
        });
    } catch (err) {
        showToast('加载运动类型失败');
    }
}

// ========== 运动记录表单 ==========
function initWorkoutForm() {
    document.getElementById('moodSelector').addEventListener('click', (e) => {
        const btn = e.target.closest('.mood-btn');
        if (!btn) return;
        document.querySelectorAll('.mood-btn').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        state.selectedMood = parseInt(btn.dataset.mood);
    });
    document.getElementById('addExerciseBtn').addEventListener('click', addExerciseItem);
    document.getElementById('workoutForm').addEventListener('submit', submitWorkout);
}

function addExerciseItem() {
    state.exerciseCount++;
    const list = document.getElementById('exerciseList');
    const div = document.createElement('div');
    div.className = 'exercise-item';
    div.innerHTML = `
        <div class="exercise-item-header">
            <input type="text" placeholder="动作名称（如：深蹲）" class="ex-name">
            <button type="button" class="btn-remove-exercise">✕</button>
        </div>
        <div class="exercise-details">
            <div><input type="number" placeholder="0" min="0" class="ex-sets"><label>组数</label></div>
            <div><input type="number" placeholder="0" min="0" class="ex-reps"><label>次数</label></div>
            <div><input type="number" placeholder="0" min="0" step="0.5" class="ex-weight"><label>重量(kg)</label></div>
        </div>
    `;
    list.appendChild(div);
    div.querySelector('.btn-remove-exercise').addEventListener('click', () => div.remove());
}

async function submitWorkout(e) {
    e.preventDefault();
    if (!state.selectedWorkoutType) { showToast('请选择运动类型'); return; }

    const payload = {
        userId: parseInt(state.userId),
        workoutType: state.selectedWorkoutType,
        workoutDate: document.getElementById('workoutDate').value,
        durationMinutes: parseInt(document.getElementById('workoutDuration').value) || null,
        caloriesBurned: parseFloat(document.getElementById('workoutCalories').value) || null,
        distance: parseFloat(document.getElementById('workoutDistance').value) || null,
        notes: document.getElementById('workoutNotes').value || null,
        moodRating: state.selectedMood
    };

    try {
        const workout = await API.post('/api/workouts', payload);
        if (state.selectedWorkoutType === 'STRENGTH') {
            const items = document.querySelectorAll('.exercise-item');
            for (const item of items) {
                const name = item.querySelector('.ex-name').value;
                if (!name) continue;
                await API.post(`/api/workouts/${workout.id}/exercises`, {
                    exerciseName: name,
                    sets: parseInt(item.querySelector('.ex-sets').value) || null,
                    reps: parseInt(item.querySelector('.ex-reps').value) || null,
                    weight: parseFloat(item.querySelector('.ex-weight').value) || null
                });
            }
        }
        showToast('记录成功! 💪');
        resetWorkoutForm();
        switchPage('home');
    } catch (err) {
        showToast('保存失败，请重试');
        console.error(err);
    }
}

function resetWorkoutForm() {
    document.getElementById('workoutForm').reset();
    document.querySelectorAll('.type-btn').forEach(b => b.classList.remove('active'));
    state.selectedWorkoutType = null;
    state.selectedMood = 4;
    document.querySelectorAll('.mood-btn').forEach(b => b.classList.remove('active'));
    document.querySelector('.mood-btn[data-mood="4"]').classList.add('active');
    document.getElementById('exerciseList').innerHTML = '';
    document.getElementById('exerciseSection').style.display = 'none';
    setTodayDate();
}

// ========== 统计页 ==========
function initPeriodSelector() {
    document.querySelectorAll('.period-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('.period-btn').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            state.currentPeriod = btn.dataset.period;
            refreshStatsPage();
        });
    });
}

async function refreshStatsPage() {
    const { start, end } = getDateRange(state.currentPeriod);
    try {
        const stats = await API.get(`/api/workouts/stats/user/${state.userId}?start=${start}&end=${end}`);
        document.getElementById('statWorkoutCount').textContent = stats.workoutCount;
        document.getElementById('statTotalCalories').textContent = Math.round(stats.totalCalories);
        document.getElementById('statTotalDuration').textContent = stats.totalDuration;
        document.getElementById('statTotalDistance').textContent = stats.totalDistance.toFixed(1);
        drawPieChart(stats.typeDistribution);

        // 情侣对比 (如果有伴侣)
        if (state.partnerId) {
            document.getElementById('coupleCompare').style.display = 'block';
            loadCoupleCompare(start, end);
        } else {
            document.getElementById('coupleCompare').style.display = 'none';
        }
    } catch (err) {
        console.error('加载统计失败', err);
    }
}

function getDateRange(period) {
    const today = new Date();
    let start;
    if (period === 'week') {
        start = new Date(today);
        start.setDate(start.getDate() - start.getDay() + 1);
    } else if (period === 'month') {
        start = new Date(today.getFullYear(), today.getMonth(), 1);
    } else {
        start = new Date(2024, 0, 1);
    }
    return { start: start.toISOString().split('T')[0], end: today.toISOString().split('T')[0] };
}

// ========== Canvas 饼图 ==========
function drawPieChart(typeDistribution) {
    const canvas = document.getElementById('typeChart');
    const ctx = canvas.getContext('2d');
    const size = Math.min(canvas.parentElement.clientWidth - 40, 250);
    canvas.width = size * 2; canvas.height = size * 2;
    canvas.style.width = size + 'px'; canvas.style.height = size + 'px';
    ctx.scale(2, 2);
    const cx = size / 2, cy = size / 2, radius = size / 2 - 10;
    ctx.clearRect(0, 0, size, size);

    if (!typeDistribution || typeDistribution.length === 0) {
        ctx.fillStyle = '#B2BEC3'; ctx.font = '14px sans-serif'; ctx.textAlign = 'center';
        ctx.fillText('暂无数据', cx, cy);
        document.getElementById('typeLegend').innerHTML = '';
        return;
    }

    const total = typeDistribution.reduce((sum, t) => sum + t.count, 0);
    let startAngle = -Math.PI / 2;
    typeDistribution.forEach((item, i) => {
        const sliceAngle = (item.count / total) * 2 * Math.PI;
        const color = CHART_COLORS[i % CHART_COLORS.length];
        ctx.beginPath(); ctx.moveTo(cx, cy);
        ctx.arc(cx, cy, radius, startAngle, startAngle + sliceAngle);
        ctx.closePath(); ctx.fillStyle = color; ctx.fill();
        ctx.strokeStyle = '#fff'; ctx.lineWidth = 2; ctx.stroke();
        startAngle += sliceAngle;
    });
    ctx.beginPath(); ctx.arc(cx, cy, radius * 0.55, 0, Math.PI * 2);
    ctx.fillStyle = '#fff'; ctx.fill();
    ctx.fillStyle = '#2D3436'; ctx.font = 'bold 22px sans-serif'; ctx.textAlign = 'center'; ctx.textBaseline = 'middle';
    ctx.fillText(total, cx, cy - 8);
    ctx.font = '12px sans-serif'; ctx.fillStyle = '#636E72'; ctx.fillText('总次数', cx, cy + 12);

    document.getElementById('typeLegend').innerHTML = typeDistribution.map((item, i) => {
        const pct = Math.round((item.count / total) * 100);
        return `<span class="legend-item"><span class="legend-dot" style="background:${CHART_COLORS[i % CHART_COLORS.length]}"></span>${item.displayName} ${pct}%</span>`;
    }).join('');
}

// ========== 情侣对比 ==========
async function loadCoupleCompare(start, end) {
    try {
        const compare = await API.get(
            `/api/workouts/stats/couple?user1Id=${state.userId}&user2Id=${state.partnerId}&start=${start}&end=${end}`
        );
        const partner = await API.get(`/api/users/${state.partnerId}`);

        const metrics = [
            { key: 'workoutCount', label: '运动次数', unit: '次' },
            { key: 'totalCalories', label: '卡路里', unit: 'kcal' },
            { key: 'totalDuration', label: '时长', unit: '分钟' },
            { key: 'totalDistance', label: '距离', unit: 'km' }
        ];

        const container = document.getElementById('compareBars');
        container.innerHTML = metrics.map(m => {
            const v1 = compare.user1[m.key] || 0;
            const v2 = compare.user2[m.key] || 0;
            const max = Math.max(v1, v2, 1);
            const fmt = (v) => m.key === 'totalDistance' ? v.toFixed(1) : Math.round(v);
            return `
                <div class="compare-item">
                    <div class="compare-names">
                        <span>${state.avatarEmoji} ${state.nickname}: ${fmt(v1)}${m.unit}</span>
                        <span>${partner.avatarEmoji} ${partner.nickname}: ${fmt(v2)}${m.unit}</span>
                    </div>
                    <div class="compare-bar-container">
                        <div class="compare-bar"><div class="compare-bar-fill user1" style="width:${(v1 / max) * 100}%"></div></div>
                        <div class="compare-bar"><div class="compare-bar-fill user2" style="width:${(v2 / max) * 100}%"></div></div>
                    </div>
                </div>`;
        }).join('');
    } catch (err) {
        console.error('加载对比失败', err);
    }
}

// ========== 个人页 ==========
async function refreshProfilePage() {
    document.getElementById('profileAvatar').textContent = state.avatarEmoji || '💪';
    document.getElementById('profileName').textContent = state.nickname || '用户';

    try {
        const user = await API.get(`/api/users/${state.userId}`);
        document.getElementById('profileMeta').textContent =
            `身高 ${user.height || '--'}cm · 目标体重 ${user.targetWeight || '--'}kg`;
    } catch (err) {}

    await loadMeasurements();
}

async function loadMeasurements() {
    try {
        const measurements = await API.get(`/api/measurements/user/${state.userId}`);
        const container = document.getElementById('measurementList');
        if (measurements.length === 0) {
            container.innerHTML = '<div class="empty-state">暂无体测数据</div>';
            return;
        }
        container.innerHTML = measurements.slice(0, 10).map(m => `
            <div class="measurement-item">
                <div class="measurement-date">${m.recordDate}</div>
                <div class="measurement-grid">
                    ${m.weight ? `<div class="measurement-value"><div class="value">${m.weight}</div><div class="label">体重(kg)</div></div>` : ''}
                    ${m.bodyFatPercentage ? `<div class="measurement-value"><div class="value">${m.bodyFatPercentage}%</div><div class="label">体脂率</div></div>` : ''}
                    ${m.chestCircumference ? `<div class="measurement-value"><div class="value">${m.chestCircumference}</div><div class="label">胸围(cm)</div></div>` : ''}
                    ${m.waistCircumference ? `<div class="measurement-value"><div class="value">${m.waistCircumference}</div><div class="label">腰围(cm)</div></div>` : ''}
                    ${m.hipCircumference ? `<div class="measurement-value"><div class="value">${m.hipCircumference}</div><div class="label">臀围(cm)</div></div>` : ''}
                    ${m.armCircumference ? `<div class="measurement-value"><div class="value">${m.armCircumference}</div><div class="label">臂围(cm)</div></div>` : ''}
                    ${m.thighCircumference ? `<div class="measurement-value"><div class="value">${m.thighCircumference}</div><div class="label">大腿围(cm)</div></div>` : ''}
                </div>
            </div>`).join('');
    } catch (err) {
        console.error('加载体测数据失败', err);
    }
}

// ========== 体测表单 ==========
function initMeasurementForm() {
    document.getElementById('addMeasurementBtn').addEventListener('click', () => {
        document.getElementById('measurementForm').style.display = 'block';
        document.getElementById('measurementDate').value = new Date().toISOString().split('T')[0];
    });
    document.getElementById('cancelMeasurement').addEventListener('click', () => {
        document.getElementById('measurementForm').style.display = 'none';
    });
    document.getElementById('saveMeasurement').addEventListener('click', saveMeasurement);
}

async function saveMeasurement() {
    const payload = {
        userId: parseInt(state.userId),
        recordDate: document.getElementById('measurementDate').value,
        weight: parseFloat(document.getElementById('measurementWeight').value) || null,
        bodyFatPercentage: parseFloat(document.getElementById('measurementBodyFat').value) || null,
        chestCircumference: parseFloat(document.getElementById('measurementChest').value) || null,
        waistCircumference: parseFloat(document.getElementById('measurementWaist').value) || null,
        hipCircumference: parseFloat(document.getElementById('measurementHip').value) || null,
        armCircumference: parseFloat(document.getElementById('measurementArm').value) || null,
        thighCircumference: parseFloat(document.getElementById('measurementThigh').value) || null
    };
    try {
        await API.post('/api/measurements', payload);
        showToast('体测数据保存成功!');
        document.getElementById('measurementForm').style.display = 'none';
        document.getElementById('measurementForm').querySelectorAll('input').forEach(i => i.value = '');
        await loadMeasurements();
    } catch (err) {
        showToast('保存失败，请重试');
    }
}

// ========== Service Worker ==========
if ('serviceWorker' in navigator) {
    window.addEventListener('load', () => {
        navigator.serviceWorker.register('/sw.js')
            .then(() => console.log('SW registered'))
            .catch(err => console.error('SW failed', err));
    });
}

// ========== Toast ==========
function showToast(message) {
    const toast = document.getElementById('toast');
    toast.textContent = message;
    toast.classList.add('show');
    setTimeout(() => toast.classList.remove('show'), 2500);
}
