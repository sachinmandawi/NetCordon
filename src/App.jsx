import React, { useState, useEffect } from 'react';
import {
  Search, X, AlertTriangle,
  ArrowLeft, Trash2, Activity, ChevronRight, CheckCircle2, Circle,
  ExternalLink, Zap, RefreshCw, PauseCircle,
  BarChart3, Lock, Fingerprint, Shield, Sliders, Ban, Clock, Plus
} from 'lucide-react';

/* ─── App Data ─── */
const INIT_APPS = [
  { id:'com.whatsapp',               name:'WhatsApp',        icon:'💬', uid:10185, wifiBlocked:true,  dataBlocked:true  },
  { id:'com.instagram.android',      name:'Instagram',       icon:'📸', uid:10214, wifiBlocked:true,  dataBlocked:true  },
  { id:'org.telegram.messenger',     name:'Telegram',        icon:'✈️', uid:10199, wifiBlocked:false, dataBlocked:false },
  { id:'com.google.android.youtube', name:'YouTube',         icon:'▶️', uid:10045, wifiBlocked:false, dataBlocked:false },
  { id:'com.pubg.imobile',           name:'BGMI / PUBG',     icon:'🎮', uid:10302, wifiBlocked:true,  dataBlocked:true  },
  { id:'com.spotify.music',          name:'Spotify',         icon:'🎧', uid:10255, wifiBlocked:false, dataBlocked:false },
  { id:'com.twitter.android',        name:'X (Twitter)',     icon:'🐦', uid:10142, wifiBlocked:false, dataBlocked:false },
  { id:'com.facebook.katana',        name:'Facebook',        icon:'📘', uid:10289, wifiBlocked:true,  dataBlocked:false },
  { id:'com.snapchat.android',       name:'Snapchat',        icon:'👻', uid:10311, wifiBlocked:true,  dataBlocked:true  },
  { id:'com.netflix.mediaclient',    name:'Netflix',         icon:'🎬', uid:10188, wifiBlocked:false, dataBlocked:false },
  { id:'com.amazon.mShop.android',   name:'Amazon Shopping', icon:'🛒', uid:10241, wifiBlocked:false, dataBlocked:false },
  { id:'com.google.android.gm',      name:'Gmail',           icon:'📧', uid:10052, wifiBlocked:false, dataBlocked:false },
  { id:'com.phonepe.app',            name:'PhonePe',         icon:'💳', uid:10290, wifiBlocked:false, dataBlocked:false },
  { id:'com.swiggy.android',         name:'Swiggy',          icon:'🍔', uid:10334, wifiBlocked:false, dataBlocked:false },
];

/* ─── Shared Styles ─── */
const IB = { width:44, height:44, borderRadius:22, background:'none', border:'none', display:'flex', alignItems:'center', justifyContent:'center', flexShrink:0 };

/* ─── Toggle Switch ─── */
function Toggle({ on, onToggle }) {
  return (
    <button onClick={e=>{e.stopPropagation();onToggle();}} style={{ width:44, height:24, borderRadius:12, border:'none', flexShrink:0, background:on?'#4caf50':'#555', position:'relative', transition:'background .2s' }}>
      <div style={{ width:18, height:18, borderRadius:9, background:'#fff', position:'absolute', top:3, left:on?23:3, transition:'left .18s' }} />
    </button>
  );
}

/* ─── Settings Preference Row ─── */
function Pref({ label, sub, right, onClick, divider=true }) {
  return (
    <>
      <div onClick={onClick} style={{ display:'flex', alignItems:'center', gap:16, padding:'13px 16px', background:'var(--surface)', cursor:onClick?'pointer':'default', transition:'background .1s' }}
        onMouseDown={e=>{if(onClick)e.currentTarget.style.background='var(--surface2)';}}
        onMouseUp={e=>{if(onClick)e.currentTarget.style.background='var(--surface)';}}
        onMouseLeave={e=>{if(onClick)e.currentTarget.style.background='var(--surface)';}}>
        <div style={{ flex:1, minWidth:0 }}>
          <div style={{ fontSize:15, color:'var(--on-bg)', lineHeight:1.3 }}>{label}</div>
          {sub && <div style={{ fontSize:12, color:'var(--dim)', marginTop:2, lineHeight:1.4 }}>{sub}</div>}
        </div>
        {right ?? (onClick ? <ChevronRight size={18} color='var(--dim)' /> : null)}
      </div>
      {divider && <div style={{ height:1, background:'var(--divider)' }} />}
    </>
  );
}

/* ─── Section Header ─── */
function Sec({ label }) {
  return <div style={{ padding:'16px 16px 6px', fontSize:13, fontWeight:600, color:'var(--primary)', letterSpacing:'.04em' }}>{label}</div>;
}

/* ─── Sub-screen Toolbar ─── */
function Toolbar({ title, onBack, right }) {
  return (
    <div style={{ height:56, background:'#1f1f1f', borderBottom:'1px solid var(--divider)', display:'flex', alignItems:'center', paddingLeft:4, paddingRight:4, gap:4, flexShrink:0 }}>
      <button onClick={onBack} style={IB}><ArrowLeft size={22} color='#c8c8c8'/></button>
      <span style={{ flex:1, fontSize:18, fontWeight:500, color:'#f0f0f0', letterSpacing:0.2 }}>{title}</span>
      {right}
    </div>
  );
}

/* ═══════════════════════════
   ANIMATED SPLASH SCREEN
═══════════════════════════ */
function SplashScreen({ onFinish }) {
  const [fadingOut, setFadingOut] = useState(false);
  const [progress, setProgress] = useState(0);

  useEffect(() => {
    // 60FPS continuous smooth progress bar
    const startTime = Date.now();
    const duration = 2400; // 2.4s to fill

    const timer = setInterval(() => {
      const elapsed = Date.now() - startTime;
      const pct = Math.min(100, (elapsed / duration) * 100);
      setProgress(pct);
      if (pct >= 100) clearInterval(timer);
    }, 16);

    // Start exit transition after 2.8s
    const t1 = setTimeout(() => {
      setFadingOut(true);
    }, 2800);

    // Completely unmount splash after 3.3s
    const t2 = setTimeout(() => {
      onFinish();
    }, 3300);

    return () => {
      clearInterval(timer);
      clearTimeout(t1);
      clearTimeout(t2);
    };
  }, [onFinish]);

  return (
    <div
      style={{
        position: 'fixed',
        inset: 0,
        zIndex: 9999,
        background: '#141414',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        animation: fadingOut ? 'splashContainerFadeOut 0.5s cubic-bezier(0.4, 0, 0.2, 1) forwards' : 'none',
        padding: 24,
        overflow: 'hidden',
        userSelect: 'none'
      }}
    >
      {/* Centered Brand Content */}
      <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', textAlign: 'center' }}>
        
        {/* Animated Flat Logo (Zero Glow) */}
        <div
          style={{
            width: 88,
            height: 88,
            borderRadius: 24,
            overflow: 'hidden',
            marginBottom: 22,
            animation: 'splashLogoScale 0.9s cubic-bezier(0.34, 1.56, 0.64, 1) forwards'
          }}
        >
          <img
            src="/logo.png"
            alt="NetCordon"
            style={{ width: '100%', height: '100%', objectFit: 'cover', display: 'block' }}
          />
        </div>

        {/* Animated Title */}
        <h1
          style={{
            fontSize: 28,
            fontWeight: 700,
            color: '#f0f0f0',
            letterSpacing: 0.5,
            margin: '0 0 8px',
            animation: 'splashTextFade 0.8s ease 0.25s both'
          }}
        >
          NetCordon
        </h1>

        {/* Tagline */}
        <p
          style={{
            fontSize: 13,
            color: '#888888',
            letterSpacing: 0.2,
            margin: '0 0 32px',
            animation: 'splashSubFade 0.8s ease 0.45s both'
          }}
        >
          Intelligent App Firewall & Shield
        </p>

        {/* Dynamic 60FPS Progress Bar */}
        <div
          style={{
            width: 140,
            height: 3,
            borderRadius: 2,
            background: 'rgba(255,255,255,0.08)',
            overflow: 'hidden',
            position: 'relative'
          }}
        >
          <div
            style={{
              width: `${progress}%`,
              height: '100%',
              borderRadius: 2,
              background: '#4caf50',
              transition: 'width 0.03s linear'
            }}
          />
        </div>
      </div>
    </div>
  );
}

/* ═══════════════════════════
   ONBOARDING / APP WALKTHROUGH SCREEN
═══════════════════════════ */
function OnboardingWalkthroughScreen({ onDone }) {
  const [slide, setSlide] = useState(0);

  const slides = [
    {
      badge: 'NO-VPN • OPEN SOURCE',
      badgeColor: '#4caf50',
      title: 'No-VPN Android Firewall',
      subtitle: 'Direct Shizuku kernel control with zero battery drain.',
      useLogo: true,
      bullets: [
        { text: '⚡ Zero battery drain & no VPN tunnels', color: '#4caf50' },
        { text: '🛡️ 100% On-device, open-source & free', color: '#64b5f6' },
      ]
    },
    {
      badge: '3 ISOLATION MODES',
      badgeColor: '#ffb300',
      title: 'Smart Per-App Control',
      subtitle: 'Tap any app card from your dashboard to switch mode:',
      icon: <Shield size={38} color="#ffb300" />,
      bullets: [
        { text: '🟢 Allowed • Normal internet access', color: '#4caf50' },
        { text: '🟡 Smart Shield • Cut on close, resume on open', color: '#ffb300' },
        { text: '🔴 Total Blackout • 100% offline & zero ads', color: '#ef5350' },
      ]
    },
    {
      badge: 'AUTOMATION',
      badgeColor: '#64b5f6',
      title: 'Scheduled Firewall',
      subtitle: 'Automate your bedtime and deep work focus hours:',
      icon: <Clock size={38} color="#64b5f6" />,
      bullets: [
        { text: '🌙 Bedtime Shield • Auto-mute social apps at night', color: '#ffb300' },
        { text: '📚 Work Focus • Stop distractions during work hours', color: '#64b5f6' },
      ]
    },
    {
      badge: 'PRIVACY RADAR',
      badgeColor: '#ab47bc',
      title: 'Leak Radar & Security',
      subtitle: 'Stop silent background trackers and protect your device:',
      icon: <Shield size={38} color="#ab47bc" />,
      bullets: [
        { text: '🚨 Instant alert on background tracker attempts', color: '#ef5350' },
        { text: '🔐 Biometric Fingerprint & device PIN lock', color: '#4caf50' },
      ]
    }
  ];

  const cur = slides[slide];

  return (
    <div style={{ flex: 1, display: 'flex', flexDirection: 'column', background: '#0f0f0f', padding: '20px 20px 28px', justifyContent: 'space-between' }}>
      {/* Top Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div style={{ background: '#1e1e1e', border: '1px solid #333', padding: '5px 10px', borderRadius: 8, fontSize: 12, color: '#888', fontWeight: 500 }}>
          Guide {slide + 1} of {slides.length}
        </div>
        {slide < slides.length - 1 ? (
          <button onClick={onDone} style={{ background: 'none', border: 'none', color: '#888', fontSize: 13, fontWeight: 500, cursor: 'pointer', padding: '6px 8px' }}>
            Skip Guide
          </button>
        ) : <div />}
      </div>

      {/* Middle Card */}
      <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', textAlign: 'center', padding: '16px 0' }}>
        <div style={{ width: 76, height: 76, borderRadius: 22, background: `${cur.badgeColor}22`, display: 'flex', alignItems: 'center', justifyContent: 'center', marginBottom: 14 }}>
          {cur.useLogo ? (
            <img src="/logo.png" alt="NetCordon" style={{ width: 52, height: 52, borderRadius: 14, objectFit: 'cover' }} />
          ) : cur.icon}
        </div>

        <div style={{ display: 'inline-block', background: `${cur.badgeColor}24`, border: `1px solid ${cur.badgeColor}55`, color: cur.badgeColor, fontSize: 10, fontWeight: 700, letterSpacing: '0.08em', padding: '3px 9px', borderRadius: 6, marginBottom: 10 }}>
          {cur.badge}
        </div>

        <h2 style={{ fontSize: 21, fontWeight: 700, color: '#f0f0f0', margin: '0 0 6px' }}>{cur.title}</h2>
        <p style={{ fontSize: 13, color: '#888', margin: '0 0 18px', maxWidth: 320, lineHeight: 1.4 }}>{cur.subtitle}</p>

        <div style={{ width: '100%', display: 'flex', flexDirection: 'column', gap: 10 }}>
          {cur.bullets.map((it, idx) => (
            <div key={idx} style={{ background: '#1e1e1e', border: '1px solid #2d2d2d', borderRadius: 12, padding: '12px 14px', display: 'flex', alignItems: 'center', gap: 12, textAlign: 'left' }}>
              <div style={{ width: 8, height: 8, borderRadius: 4, background: it.color, flexShrink: 0 }} />
              <div style={{ fontSize: 13.5, fontWeight: 500, color: '#f0f0f0' }}>{it.text}</div>
            </div>
          ))}
        </div>
      </div>

      {/* Bottom Controls */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
        {/* Dots */}
        <div style={{ display: 'flex', justifyContent: 'center', gap: 6 }}>
          {slides.map((_, i) => (
            <div key={i} style={{ width: i === slide ? 24 : 6, height: 6, borderRadius: 3, background: i === slide ? '#4caf50' : '#444', transition: 'all .25s ease' }} />
          ))}
        </div>

        {/* Buttons */}
        <div style={{ display: 'flex', gap: 12 }}>
          {slide > 0 && (
            <button onClick={() => setSlide(s => s - 1)} style={{ flex: 1, height: 48, borderRadius: 12, border: '1px solid #333', background: '#1e1e1e', color: '#f0f0f0', fontSize: 14, fontWeight: 600, cursor: 'pointer' }}>
              Back
            </button>
          )}
          <button
            onClick={() => {
              if (slide < slides.length - 1) setSlide(s => s + 1);
              else onDone();
            }}
            style={{
              flex: slide > 0 ? 2 : 1,
              height: 48,
              borderRadius: 12,
              border: 'none',
              background: '#4caf50',
              color: '#fff',
              fontSize: 15,
              fontWeight: 700,
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              gap: 6
            }}
          >
            {slide < slides.length - 1 ? 'Continue' : 'Get Started'}
            <ChevronRight size={18} />
          </button>
        </div>
      </div>
    </div>
  );
}

/* ═══════════════════════════
   ULTRA-COMPACT ZERO-SCROLL PERMISSION SCREEN
═══════════════════════════ */
function PermissionScreen({ perms, onGrant, onDone }) {
  const allGranted = perms.filter(p=>p.required).every(p => p.granted);
  return (
    <div style={{ flex:1, display:'flex', flexDirection:'column', justifyContent:'space-between', background:'#141414', padding:'24px 20px 20px', overflow:'hidden' }}>
      {/* Minimal Hero */}
      <div style={{ textAlign:'center', paddingTop:8 }}>
        <div style={{ width:56, height:56, borderRadius:16, overflow:'hidden', margin:'0 auto 12px' }}>
          <img src="/logo.png" alt="NetCordon" style={{ width:'100%', height:'100%', objectFit:'cover', display:'block' }}/>
        </div>
        <h1 style={{ fontSize:20, fontWeight:700, color:'#ececec', margin:'0 0 4px' }}>App Permissions</h1>
        <p style={{ fontSize:12, color:'var(--dim)', margin:0 }}>
          Grant permissions to enable background network firewall
        </p>
      </div>

      {/* 4 Compact Rows */}
      <div style={{ display:'flex', flexDirection:'column', gap:10, margin:'16px 0' }}>
        {perms.map(p => (
          <div key={p.id} style={{ background:'#1e1e1e', borderRadius:12, border:`1px solid ${p.granted ? 'rgba(76,175,80,0.25)' : '#262626'}`, padding:'12px 14px', display:'flex', alignItems:'center', gap:12 }}>
            {p.granted
              ? <CheckCircle2 size={20} color='#4caf50' style={{ flexShrink:0 }}/>
              : <Circle size={20} color='var(--dim)' style={{ flexShrink:0 }}/>}

            <div style={{ flex:1, minWidth:0 }}>
              <div style={{ display:'flex', alignItems:'center', gap:6 }}>
                <span style={{ fontSize:14, fontWeight:600, color:'#ececec', whiteSpace:'nowrap' }}>{p.label}</span>
                {!p.granted && p.required && (
                  <span style={{ fontSize:9, padding:'1px 5px', borderRadius:4, background:'rgba(239,83,80,0.15)', color:'#ef5350', fontWeight:700, whiteSpace:'nowrap' }}>REQUIRED</span>
                )}
              </div>
              <div style={{ fontSize:11, color:'var(--dim)', marginTop:1, overflow:'hidden', textOverflow:'ellipsis', whiteSpace:'nowrap' }}>
                {p.id === 'shizuku' ? 'ADB policy execution' : p.id === 'usage_stats' ? 'Foreground app detection' : p.id === 'notifications' ? 'Background service status' : 'Keep service alive'}
              </div>
            </div>

            {p.granted ? (
              <div style={{ width:76, height:30, borderRadius:8, background:'rgba(76,175,80,0.15)', border:'1px solid rgba(76,175,80,0.3)', color:'#4caf50', fontSize:11, fontWeight:700, display:'flex', alignItems:'center', justifyContent:'center', flexShrink:0 }}>
                DONE
              </div>
            ) : (
              <button
                onClick={() => onGrant(p.id)}
                style={{
                  width:76,
                  height:30,
                  borderRadius:8,
                  border:`1px solid ${p.required ? 'rgba(76,175,80,0.4)' : 'rgba(255,255,255,0.15)'}`,
                  background: p.required ? 'rgba(76,175,80,0.15)' : 'rgba(255,255,255,0.06)',
                  color: p.required ? '#4caf50' : '#ececec',
                  fontSize:11,
                  fontWeight:700,
                  flexShrink:0,
                  cursor:'pointer',
                  display:'flex',
                  alignItems:'center',
                  justifyContent:'center'
                }}
              >
                {p.id === 'shizuku' ? 'Authorize' : p.id === 'battery' ? 'Ignore' : 'Grant'}
              </button>
            )}
          </div>
        ))}
      </div>

      {/* Bottom Action Button */}
      <button
        onClick={onDone}
        disabled={!allGranted}
        style={{ width:'100%', padding:'14px 0', borderRadius:12, border:'none', background: allGranted ? '#4caf50' : '#282828', color: allGranted ? '#fff' : '#666', fontSize:15, fontWeight:700, cursor: allGranted ? 'pointer' : 'not-allowed', transition:'all .2s' }}
      >
        {allGranted ? 'Continue to App' : 'Grant required permissions'}
      </button>
    </div>
  );
}

/* ═══════════════════════════
   MAIN APP
═══════════════════════════ */
export default function App() {
  // ── Permission State (simulates Android runtime permission flow) ──
  const [perms, setPerms] = useState([
    {
      id: 'shizuku',
      label: 'Shizuku Authorization',
      desc: 'Required to execute ADB shell commands (netpolicy) without root access.',
      how: 'Open the Shizuku app → tap "Authorize" when Smart Net Shield requests it.',
      required: true,
      granted: false,
    },
    {
      id: 'usage_stats',
      label: 'Usage Access',
      desc: 'Required to detect which app is currently in the foreground, so internet can be unblocked when you open a protected app.',
      how: 'Settings → Apps → Special app access → Usage access → Smart Net Shield → Allow.',
      required: true,
      granted: false,
    },
    {
      id: 'notifications',
      label: 'Notifications',
      desc: 'Required to show the persistent foreground service notification while the firewall is running (Android 13+).',
      how: 'Tap Grant when the system permission dialog appears.',
      required: true,
      granted: false,
    },
    {
      id: 'battery',
      label: 'Ignore Battery Optimization',
      desc: 'Recommended so Android does not kill the background monitoring service.',
      how: 'Settings → Battery → Not optimized → Smart Net Shield.',
      required: false,
      granted: false,
    },
  ]);

  const [showSplash, setShowSplash] = useState(true);
  const [setupDone, setSetupDone] = useState(false);
  const [showWalkthrough, setShowWalkthrough] = useState(() => localStorage.getItem('netcordon_onboarding_v1') !== 'true');

  const grantPerm = (id) => setPerms(p => p.map(x => x.id === id ? { ...x, granted: true } : x));

  // ── App State ──
  const [screen, setScreen]       = useState('home');
  const [apps, setApps]           = useState(INIT_APPS);
  const [search, setSearch]       = useState('');
  const [searching, setSearching] = useState(false);
  const [shizukuOk, setShizukuOk] = useState(true);

  // Settings
  const [privilege, setPrivilege] = useState('shizuku');
  const [bootStart, setBootStart] = useState(true);
  const [serviceOn, setServiceOn] = useState(true);
  const [minNotif,  setMinNotif]  = useState(false);
  const [showPkg,   setShowPkg]   = useState(true);
  const [blockNotifs, setBlockNotifs] = useState(true);
  const [screenOffShield, setScreenOffShield] = useState(false);
  const [appLockEnabled, setAppLockEnabled]   = useState(false);
  const [isLocked, setIsLocked]               = useState(false);
  const [lockOnScreenOff, setLockOnScreenOff] = useState(true);
  const [analyticsPeriod, setAnalyticsPeriod] = useState('today');
  const [blackoutApps, setBlackoutApps]       = useState(new Set(['com.pubg.imobile']));
  const [darkMode, setDarkMode]               = useState(() => localStorage.getItem('netcordon_dark_mode') === 'true');

  useEffect(() => {
    if (darkMode) {
      document.documentElement.setAttribute('data-theme', 'dark');
      localStorage.setItem('netcordon_dark_mode', 'true');
    } else {
      document.documentElement.setAttribute('data-theme', 'light');
      localStorage.setItem('netcordon_dark_mode', 'false');
    }
  }, [darkMode]);
  // Templates removed - Pure custom schedules only
  const [schedules, setSchedules] = useState([]);
  const [showAddCustomSchedule, setShowAddCustomSchedule] = useState(false);
  const [newTitle, setNewTitle] = useState('');
  const [startHourStr, setStartHourStr] = useState('10');
  const [startMinStr,  setStartMinStr]  = useState('00');
  const [startAmPm,    setStartAmPm]    = useState('PM');
  const [endHourStr,   setEndHourStr]   = useState('07');
  const [endMinStr,    setEndMinStr]    = useState('00');
  const [endAmPm,      setEndAmPm]      = useState('AM');
  const [newMode,  setNewMode]  = useState('TOTAL_BLACKOUT');
  const [newDays,  setNewDays]  = useState([1,2,3,4,5,6,7]);
  const [selectedScheduleApps, setSelectedScheduleApps] = useState([]);
  const [scheduleForAppConfig, setScheduleForAppConfig] = useState(null);
  const [selectedAppForPolicy, setSelectedAppForPolicy] = useState(null);

  const openAddCustomSchedule = () => {
    const initialMode = 'TOTAL_BLACKOUT';
    setNewMode(initialMode);
    const blackoutPkgs = apps.filter(a => blackoutApps.has(a.id)).map(a => a.id);
    setSelectedScheduleApps(blackoutPkgs);
    setShowAddCustomSchedule(true);
  };
  const [touchStart, setTouchStart]           = useState(0);
  const [pullY, setPullY]                     = useState(0);
  const handleRefresh = () => {
    setPullY(0);
    setTouchStart(0);
    pushLog('I', 'Apps', 'Installed applications list refreshed');
  };

  // Live Detailed Logs
  const [logs, setLogs] = useState([
    { id:1, tag:'I', cat:'Shizuku', msg:'Binder connected: rikka.shizuku.ShizukuProvider (UID 10244)', t:'17:20:01.022' },
    { id:2, tag:'I', cat:'Service', msg:'AppShieldService active (400ms polling interval)', t:'17:20:02.109' },
    { id:3, tag:'W', cat:'Shield',  msg:'[BLOCKED] com.whatsapp -> NetPolicy REJECT_ALL + AppOps IGNORE (Single Tick)', t:'17:20:05.412' },
    { id:4, tag:'D', cat:'AppOps',  msg:'POST_NOTIFICATION -> IGNORE (com.whatsapp muted)', t:'17:20:05.430' },
    { id:5, tag:'W', cat:'Shield',  msg:'[BLOCKED] com.instagram.android -> Network FROZEN in background', t:'17:20:08.820' },
    { id:6, tag:'I', cat:'Monitor', msg:'Foreground: com.android.launcher (Protected apps held in background)', t:'17:20:15.110' },
  ]);

  const pushLog = (tag, cat, msg) => {
    const d = new Date();
    const t = `${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}:${String(d.getSeconds()).padStart(2,'0')}.${String(d.getMilliseconds()).padStart(3,'0')}`;
    setLogs(p => [{ id:Date.now() + Math.random(), tag, cat, msg, t }, ...p.slice(0,199)]);
  };

  const [filterMode, setFilterMode] = useState('all'); // all | blocked

  const q = search.toLowerCase();
  const filtered = apps.filter(a => {
    const matchesSearch = a.name.toLowerCase().includes(q) || a.id.toLowerCase().includes(q);
    if (!matchesSearch) return false;
    if (filterMode === 'blocked') return a.wifiBlocked || a.dataBlocked;
    return true;
  });
  const blockedCount = apps.filter(a => a.wifiBlocked || a.dataBlocked).length;
  const tagColor = t => t==='W'?'#ffb300':t==='E'?'#ef5350':t==='D'?'#64b5f6':'#81c784';

  // Periodic Live Background Firewall Interceptions
  useEffect(() => {
    if (!serviceOn || !shizukuOk) return;
    const interval = setInterval(() => {
      const blocked = apps.filter(a => a.wifiBlocked || a.dataBlocked);
      if (blocked.length === 0) return;
      const pick = blocked[Math.floor(Math.random() * blocked.length)];
      const events = [
        { tag:'D', cat:'AppOps',  msg:`Blocked background wake_lock & FCM wakeup for ${pick.id}` },
        { tag:'W', cat:'NetPol',  msg:`Dropped bg packet: ${pick.id} (UID ${pick.uid}) -> socket REJECTED` },
        { tag:'I', cat:'Monitor', msg:`${pick.name} held in restricted standby bucket (0 bg traffic)` },
      ];
      const ev = events[Math.floor(Math.random() * events.length)];
      pushLog(ev.tag, ev.cat, ev.msg);
    }, 6000);
    return () => clearInterval(interval);
  }, [serviceOn, shizukuOk, apps]);




  /* ════ RENDER ════ */
  return (
    <div style={{ width:'100%', height:'100vh', background:'var(--bg)', display:'flex', flexDirection:'column', overflow:'hidden', position:'relative', userSelect:'none', WebkitUserSelect:'none', WebkitTapHighlightColor:'transparent' }}>

      {/* ── Animated Splash Screen ── */}
      {showSplash && (
        <SplashScreen onFinish={() => setShowSplash(false)} />
      )}

      {/* ── Persistent Top Bar (Home, Logs, Settings) ── */}
      {setupDone && !showWalkthrough && (
        screen === 'home' ? (
          <div style={{ background:'#1f1f1f', borderBottom:'1px solid var(--divider)', flexShrink:0 }}>
            <div style={{ height:56, display:'flex', alignItems:'center', paddingLeft: searching ? 4 : 16, paddingRight:4, gap:4 }}>
              {!searching && (
                <img src="/logo.png" alt="NetCordon" style={{ width: 26, height: 26, borderRadius: 7, marginRight: 10, flexShrink: 0, objectFit: 'cover' }} />
              )}
              {searching ? (
                <>
                  <button onClick={()=>{ setSearching(false); setSearch(''); }} style={IB}>
                    <ArrowLeft size={22} color='#c8c8c8'/>
                  </button>
                  <input autoFocus value={search} onChange={e=>setSearch(e.target.value)} placeholder="Search applications…"
                    style={{ flex:1, background:'none', border:'none', outline:'none', fontSize:16, color:'#f0f0f0', minWidth:0 }}/>
                  {search && <button onClick={()=>setSearch('')} style={IB}><X size={18} color='#888'/></button>}
                </>
              ) : (
                <div style={{ flex:1, minWidth:0, display:'flex', alignItems:'center' }}>
                  <span style={{ fontSize:19, fontWeight:600, color:'#f0f0f0', letterSpacing:'-0.2px' }}>NetCordon</span>
                </div>
              )}
              {!searching && (
                <div style={{ display:'flex', alignItems:'center', gap:2 }}>
                  {/* Master ON/OFF Switch (NetGuard style) */}
                  <div
                    onClick={() => {
                      const next = !serviceOn;
                      setServiceOn(next);
                      if (next) {
                        pushLog('I', 'Service', 'Master switch ON — Firewall protection activated');
                      } else {
                        pushLog('W', 'Service', 'Master switch OFF — All network restrictions paused');
                      }
                    }}
                    style={{
                      width: 38,
                      height: 22,
                      borderRadius: 12,
                      background: serviceOn ? '#4caf50' : '#333333',
                      border: `1px solid ${serviceOn ? '#4caf50' : '#444444'}`,
                      position: 'relative',
                      cursor: 'pointer',
                      marginRight: 4,
                      transition: 'background 0.2s ease',
                      display: 'flex',
                      alignItems: 'center',
                      padding: '0 2px'
                    }}
                  >
                    <div
                      style={{
                        width: 16,
                        height: 16,
                        borderRadius: 8,
                        background: '#ffffff',
                        transform: serviceOn ? 'translateX(16px)' : 'translateX(0px)',
                        transition: 'transform 0.2s ease',
                        boxShadow: '0 1px 3px rgba(0,0,0,0.4)'
                      }}
                    />
                  </div>
                  <button onClick={()=>setSearching(true)} style={IB}><Search size={21} color='#c8c8c8'/></button>
                </div>
              )}
            </div>
          </div>
        ) : screen === 'schedules' ? (
          <Toolbar title="Firewall Schedules" onBack={()=>setScreen('home')}/>
        ) : screen === 'analytics' ? (
          <Toolbar title="Network Analytics" onBack={()=>setScreen('home')} right={<button onClick={()=>pushLog('I','Analytics','Network usage statistics refreshed')} style={IB}><RefreshCw size={19} color='#c8c8c8'/></button>}/>
        ) : screen === 'logs' ? (
          <Toolbar title="Logs" onBack={()=>setScreen('home')} right={<button onClick={()=>setLogs([])} style={IB}><Trash2 size={20} color='#c8c8c8'/></button>}/>
        ) : (
          <Toolbar title="Settings" onBack={()=>setScreen('home')}/>
        )
      )}

      {/* ── CONTENT ── */}
      <div style={{ flex:1, overflowY:'auto', display:'flex', flexDirection:'column', position:'relative' }}>

        {/* ══ ONBOARDING WALKTHROUGH SCREEN ══ */}
        {showWalkthrough ? (
          <OnboardingWalkthroughScreen
            onDone={() => {
              localStorage.setItem('netcordon_onboarding_v1', 'true');
              setShowWalkthrough(false);
            }}
          />
        ) : !setupDone ? (
          <PermissionScreen
            perms={perms}
            onGrant={grantPerm}
            onDone={() => setSetupDone(true)}
          />
        ) : (

          <>
            {/* ══ HOME ══ */}
            {screen === 'home' && (
              <>
                {!shizukuOk ? (
                  <div style={{ background:'rgba(239,83,80,0.11)', borderBottom:'1px solid rgba(239,83,80,0.22)', padding:'10px 16px', display:'flex', alignItems:'center', gap:12, flexShrink:0 }}>
                    <AlertTriangle size={17} color='#ef5350' style={{ flexShrink:0 }}/>
                    <div style={{ flex:1, fontSize:13, color:'#ef9a9a', lineHeight:1.4 }}>
                      Shizuku is not running. Open the Shizuku app and start via Wireless Debugging.
                    </div>
                    <button onClick={()=>{setShizukuOk(true);pushLog('I','Shizuku','Binder reconnected');}}
                      style={{ fontSize:12, fontWeight:700, color:'#ef5350', background:'none', border:'none', padding:'4px 6px', flexShrink:0 }}>
                      RETRY
                    </button>
                  </div>
                ) : !serviceOn && (
                  <div style={{ background:'rgba(255,167,38,0.12)', borderBottom:'1px solid rgba(255,167,38,0.25)', padding:'9px 16px', display:'flex', alignItems:'center', gap:10, flexShrink:0 }}>
                    <PauseCircle size={17} color='#ffb74d' style={{ flexShrink:0 }}/>
                    <div style={{ flex:1, fontSize:12, color:'#ffe0b2', lineHeight:1.3 }}>
                      Firewall is paused. Turn on the top switch to activate.
                    </div>
                  </div>
                )}



                {/* Interactive List Header */}
                <div style={{
                  display: 'flex',
                  alignItems: 'center',
                  paddingLeft: 16,
                  paddingRight: 4,
                  height: 42,
                  borderBottom: '1px solid var(--divider)',
                  background: '#1a1a1a',
                  flexShrink: 0
                }}>
                  {/* Left: Quick Filter Pills */}
                  <div style={{ display: 'flex', alignItems: 'center', gap: 8, flex: 1, minWidth: 0 }}>
                    <button
                      onClick={() => setFilterMode('all')}
                      style={{
                        padding: '3px 10px',
                        borderRadius: 14,
                        background: filterMode === 'all' ? 'rgba(76,175,80,0.15)' : '#262626',
                        border: `1px solid ${filterMode === 'all' ? 'rgba(76,175,80,0.4)' : '#333'}`,
                        color: filterMode === 'all' ? '#4caf50' : '#888',
                        fontSize: 11,
                        fontWeight: 700,
                        display: 'flex',
                        alignItems: 'center',
                        gap: 4,
                        cursor: 'pointer',
                        transition: 'all .15s'
                      }}
                    >
                      <span>All</span>
                      <span style={{ fontSize: 10, opacity: 0.85 }}>({apps.length})</span>
                    </button>

                    <button
                      onClick={() => setFilterMode(filterMode === 'blocked' ? 'all' : 'blocked')}
                      style={{
                        padding: '3px 10px',
                        borderRadius: 14,
                        background: filterMode === 'blocked' ? 'rgba(239,83,80,0.15)' : '#262626',
                        border: `1px solid ${filterMode === 'blocked' ? 'rgba(239,83,80,0.4)' : '#333'}`,
                        color: filterMode === 'blocked' ? '#ef5350' : '#888',
                        fontSize: 11,
                        fontWeight: 700,
                        display: 'flex',
                        alignItems: 'center',
                        gap: 4,
                        cursor: 'pointer',
                        transition: 'all .15s'
                      }}
                    >
                      <span>Restricted</span>
                      <span style={{ fontSize: 10, opacity: 0.85 }}>({blockedCount})</span>
                    </button>
                  </div>

                  {/* Right: WiFi & Mobile Data Column Titles */}
                </div>

                {/* App list with touch & mouse pull-to-refresh */}
                <div
                  onTouchStart={e => setTouchStart(e.touches[0].clientY)}
                  onTouchMove={e => {
                    const diff = e.touches[0].clientY - touchStart;
                    if (diff > 0) setPullY(diff);
                  }}
                  onTouchEnd={() => {
                    if (pullY > 40) handleRefresh();
                    else setPullY(0);
                  }}
                  onMouseDown={e => {
                    if (e.button === 0) setTouchStart(e.clientY);
                  }}
                  onMouseMove={e => {
                    if (touchStart > 0) {
                      const diff = e.clientY - touchStart;
                      if (diff > 0) setPullY(diff);
                    }
                  }}
                  onMouseUp={() => {
                    if (touchStart > 0) {
                      if (pullY > 40) handleRefresh();
                      else setPullY(0);
                      setTouchStart(0);
                    }
                  }}
                >
                {filtered.map((app, i) => (
                  <div key={app.id}>
                    <div style={{ display:'flex', alignItems:'center', padding:'8px 16px', cursor:'pointer', transition:'background 0.1s' }}
                      onClick={()=>setSelectedAppForPolicy(app)}
                      onMouseDown={e=>e.currentTarget.style.background='rgba(255,255,255,0.04)'}
                      onMouseUp={e=>e.currentTarget.style.background='transparent'}
                      onMouseLeave={e=>e.currentTarget.style.background='transparent'}>
                      <div style={{ width:44, height:44, borderRadius:12, background:'#2a2a2a', border:'1px solid #333', display:'flex', alignItems:'center', justifyContent:'center', fontSize:24, flexShrink:0 }}>
                        {app.icon}
                      </div>
                      <div style={{ flex:1, minWidth:0, paddingLeft:14, paddingRight:10 }}>
                        <div style={{ fontSize:15, color:'#e8e8e8', fontWeight:600, overflow:'hidden', textOverflow:'ellipsis', whiteSpace:'nowrap' }}>{app.name}</div>
                        {showPkg && <div style={{ fontSize:12, color:'#777', marginTop:2, overflow:'hidden', textOverflow:'ellipsis', whiteSpace:'nowrap' }}>{app.id}</div>}
                      </div>

                      {/* Right: Active Policy Pill + Chevron */}
                      <div style={{ display:'flex', alignItems:'center', gap:6, flexShrink:0 }}>
                        {blackoutApps.has(app.id) ? (
                          <div style={{ fontSize:10, fontWeight:700, color:'#ef5350', background:'rgba(239,83,80,0.18)', border:'1px solid rgba(239,83,80,0.3)', padding:'3px 8px', borderRadius:6 }}>
                            Blackout
                          </div>
                        ) : (app.wifiBlocked || app.dataBlocked) ? (
                          <div style={{ fontSize:10, fontWeight:700, color:'#ffb300', background:'rgba(255,179,0,0.18)', border:'1px solid rgba(255,179,0,0.3)', padding:'3px 8px', borderRadius:6 }}>
                            Smart Shield
                          </div>
                        ) : (
                          <div style={{ fontSize:10, fontWeight:700, color:'#4caf50', background:'rgba(76,175,80,0.18)', border:'1px solid rgba(76,175,80,0.3)', padding:'3px 8px', borderRadius:6 }}>
                            Allowed
                          </div>
                        )}
                        <ChevronRight size={18} color="var(--dim)" style={{ opacity:0.6 }} />
                      </div>
                    </div>
                    {i < filtered.length-1 && <div style={{ height:1, background:'#252525', marginLeft:74 }}/>}
                  </div>
                ))}

                {filtered.length === 0 && (
                  <div style={{ textAlign:'center', padding:48, color:'var(--dim)', display:'flex', flexDirection:'column', alignItems:'center', gap:10 }}>
                    <Search size={34} style={{ opacity:.25 }}/>
                    <div style={{ fontSize:15 }}>No applications found</div>
                  </div>
                )}
                </div>
              </>
            )}

            {/* ══ SCHEDULES ══ */}
            {screen === 'schedules' && (
              <div style={{ flex:1, padding:'14px 16px', overflowY:'auto', display:'flex', flexDirection:'column', gap:14 }}>
                {/* Banner */}
                <div style={{ background:'#1b261d', border:'1px solid rgba(76,175,80,0.25)', borderRadius:16, padding:16, display:'flex', alignItems:'center', gap:14 }}>
                  <div style={{ width:44, height:44, borderRadius:12, background:'rgba(76,175,80,0.18)', display:'flex', alignItems:'center', justifyContent:'center', flexShrink:0 }}>
                    <Clock size={22} color='#4caf50' />
                  </div>
                  <div>
                    <div style={{ fontSize:15, fontWeight:700, color:'#fff' }}>Automated Firewall Timers</div>
                    <div style={{ fontSize:11.5, color:'#888', marginTop:2, lineHeight:1.4 }}>Auto-isolate social apps at bedtime or work hours with 0% battery drain.</div>
                  </div>
                </div>

                {/* Add Custom Schedule Button */}
                <button
                  onClick={openAddCustomSchedule}
                  style={{
                    background: '#1c271e',
                    border: '1px solid rgba(76,175,80,0.5)',
                    borderRadius: 14,
                    padding: '14px 16px',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    gap: 8,
                    color: '#4caf50',
                    fontSize: 14,
                    fontWeight: 700,
                    cursor: 'pointer'
                  }}
                >
                  <Plus size={18} color="#4caf50" />
                  Add Custom Schedule & Time
                </button>

                <div style={{ fontSize:12, fontWeight:700, color:'#888', letterSpacing:'0.05em', padding:'0 4px' }}>
                  ACTIVE SCHEDULES ({schedules.length})
                </div>

                {schedules.length === 0 ? (
                  <div style={{
                    background: '#161616',
                    border: '1px dashed #333',
                    borderRadius: 16,
                    padding: '36px 20px',
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: 'center',
                    textAlign: 'center',
                    gap: 12
                  }}>
                    <div style={{ width: 52, height: 52, borderRadius: 26, background: 'rgba(76,175,80,0.12)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                      <Clock size={26} color="#4caf50" />
                    </div>
                    <div>
                      <div style={{ fontSize: 16, fontWeight: 700, color: '#fff' }}>No Active Schedules</div>
                      <div style={{ fontSize: 12, color: '#888', marginTop: 4, maxWidth: 260, lineHeight: 1.4 }}>
                        All pre-made templates removed. Create your own custom firewall timer with exact hours.
                      </div>
                    </div>
                    <button
                      onClick={openAddCustomSchedule}
                      style={{
                        marginTop: 4,
                        background: '#4caf50',
                        border: 'none',
                        borderRadius: 12,
                        padding: '10px 20px',
                        color: '#fff',
                        fontSize: 13,
                        fontWeight: 700,
                        cursor: 'pointer',
                        display: 'flex',
                        alignItems: 'center',
                        gap: 6
                      }}
                    >
                      <Plus size={16} color="#fff" />
                      Create Custom Schedule
                    </button>
                  </div>
                ) : (
                  schedules.map(s => {
                    const isBlackout = s.mode === 'TOTAL_BLACKOUT';
                    return (
                      <div key={s.id} style={{ background:'#1e1e1e', border: s.enabled ? '1px solid rgba(76,175,80,0.4)' : '1px solid #2d2d2d', borderRadius:16, padding:16 }}>
                        <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center' }}>
                          <div>
                            <div style={{ fontSize:16, fontWeight:700, color:'#fff' }}>{s.title}</div>
                            <div style={{ fontSize:12, color:'#888', marginTop:3 }}>{s.start} – {s.end} ({s.days})</div>
                          </div>
                          <div style={{ display:'flex', alignItems:'center', gap:8 }}>
                            <button
                              onClick={() => setSchedules(schedules.filter(x => x.id !== s.id))}
                              style={{ background:'none', border:'none', color:'#888', cursor:'pointer', padding:4 }}
                            >
                              <Trash2 size={16} color="#888" />
                            </button>
                            <div
                              onClick={() => {
                                setSchedules(schedules.map(x => x.id === s.id ? { ...x, enabled: !x.enabled } : x));
                              }}
                              style={{
                                width: 44, height: 24, borderRadius: 12,
                                background: s.enabled ? '#4caf50' : '#333',
                                display: 'flex', alignItems: 'center',
                                padding: 2, cursor: 'pointer',
                                justifyContent: s.enabled ? 'flex-end' : 'flex-start',
                                transition: 'all .2s ease'
                              }}
                            >
                              <div style={{ width: 20, height: 20, borderRadius: 10, background: '#fff' }} />
                            </div>
                          </div>
                        </div>

                        <div style={{ height:1, background:'#2d2d2d', margin:'12px 0 10px' }} />

                        <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center' }}>
                          <span style={{
                            fontSize:10, fontWeight:700,
                            background: isBlackout ? 'rgba(239,83,80,0.15)' : 'rgba(255,179,0,0.15)',
                            color: isBlackout ? '#ef5350' : '#ffb300',
                            border: isBlackout ? '1px solid rgba(239,83,80,0.3)' : '1px solid rgba(255,179,0,0.3)',
                            padding:'3px 8px', borderRadius:6
                          }}>
                            {isBlackout ? 'TOTAL BLACKOUT' : 'SMART SHIELD'}
                          </span>
                          {(() => {
                            const count = s.targetPackages ? s.targetPackages.length : (s.appsCount !== undefined ? s.appsCount : 0);
                            return (
                              <button
                                type="button"
                                onClick={() => setScheduleForAppConfig(s)}
                                style={{
                                  background: 'none',
                                  border: 'none',
                                  fontSize: 12,
                                  color: count === 0 ? '#ffb300' : '#4caf50',
                                  fontWeight: 700,
                                  cursor: 'pointer',
                                  padding: 0
                                }}
                              >
                                {count === 0 ? '⚠️ 0 Apps Configured ›' : `${count} ${count === 1 ? 'App' : 'Apps'} Configured ›`}
                              </button>
                            );
                          })()}
                        </div>
                      </div>
                    );
                  })
                )}
              </div>
            )}

            {/* Modal: Modern Custom Time Schedule Picker */}
            {showAddCustomSchedule && (
              <div style={{
                position:'fixed', inset:0, zIndex:999, background:'rgba(0,0,0,0.82)',
                display:'flex', alignItems:'center', justifyContent:'center', padding:16
              }}>
                <div style={{
                  background:'#191919', border:'1px solid #333', borderRadius:20,
                  width:'100%', maxWidth:390, padding:20, display:'flex', flexDirection:'column', gap:14,
                  boxShadow: '0 20px 40px rgba(0,0,0,0.8)'
                }}>
                  <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center' }}>
                    <div style={{ fontSize:17, fontWeight:700, color:'#fff' }}>Add Custom Schedule</div>
                    <button
                      onClick={() => setShowAddCustomSchedule(false)}
                      style={{ background:'none', border:'none', color:'#888', cursor:'pointer' }}
                    >
                      <X size={18} />
                    </button>
                  </div>

                  {/* Name Input */}
                  <input
                    value={newTitle}
                    onChange={e => setNewTitle(e.target.value)}
                    placeholder="Schedule Name (e.g. Gaming Session)"
                    style={{
                      background:'#121212', border:'1px solid #2e2e2e', borderRadius:10,
                      padding:'10px 12px', color:'#fff', fontSize:13, outline:'none'
                    }}
                  />

                  {/* Quick Suggestions */}
                  <div style={{ display:'flex', gap:6 }}>
                    {['🎮 Gaming', '📚 Study', '🏃 Gym', '🌙 Sleep'].map(s => (
                      <button
                        key={s}
                        type="button"
                        onClick={() => setNewTitle(s)}
                        style={{
                          flex:1, background:'#222', border:'1px solid #333', borderRadius:8,
                          padding:'5px 0', color:'#aaa', fontSize:11, cursor:'pointer'
                        }}
                      >
                        {s}
                      </button>
                    ))}
                  </div>

                  {/* Unified Ultra-Compact 12-Hour Time Range Card */}
                  <div style={{
                    background:'#141414', border:'1px solid #282828', borderRadius:12,
                    padding:'8px 12px', display:'flex', flexDirection:'column', gap:6
                  }}>
                    {/* START TIME ROW */}
                    <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center' }}>
                      <div style={{ display:'flex', alignItems:'center', gap:6 }}>
                        <div style={{ width:7, height:7, borderRadius:'50%', background:'#4caf50' }} />
                        <span style={{ fontSize:11, fontWeight:700, color:'#4caf50', letterSpacing:'0.05em' }}>START</span>
                      </div>
                      <div style={{ display:'flex', alignItems:'center', gap:6 }}>
                        {/* Time Input Box [ HH : MM ] */}
                        <div style={{ display:'flex', alignItems:'center', background:'#222', border:'1px solid #333', borderRadius:8, padding:'3px 6px' }}>
                          <input
                            type="text"
                            inputMode="numeric"
                            pattern="[0-9]*"
                            maxLength={2}
                            autoFocus={false}
                            value={startHourStr}
                            onChange={e => setStartHourStr(e.target.value.replace(/\D/g, '').slice(0, 2))}
                            onBlur={() => {
                              let num = parseInt(startHourStr, 10);
                              if (isNaN(num) || num < 1) num = 12;
                              if (num > 12) num = 12;
                              setStartHourStr(String(num).padStart(2, '0'));
                            }}
                            placeholder="10"
                            style={{
                              width:26, height:22, background:'transparent', border:'none', color:'#fff',
                              fontSize:14, fontWeight:700, textAlign:'center', outline:'none'
                            }}
                          />
                          <span style={{ fontSize:14, fontWeight:700, color:'#4caf50', padding:'0 1px' }}>:</span>
                          <input
                            type="text"
                            inputMode="numeric"
                            pattern="[0-9]*"
                            maxLength={2}
                            autoFocus={false}
                            value={startMinStr}
                            onChange={e => setStartMinStr(e.target.value.replace(/\D/g, '').slice(0, 2))}
                            onBlur={() => {
                              let num = parseInt(startMinStr, 10);
                              if (isNaN(num) || num < 0) num = 0;
                              if (num > 59) num = 59;
                              setStartMinStr(String(num).padStart(2, '0'));
                            }}
                            placeholder="00"
                            style={{
                              width:26, height:22, background:'transparent', border:'none', color:'#fff',
                              fontSize:14, fontWeight:700, textAlign:'center', outline:'none'
                            }}
                          />
                        </div>
                        {/* AM/PM Switch */}
                        <div style={{ display:'flex', background:'#222', borderRadius:8, padding:2, border:'1px solid #333' }}>
                          {['AM', 'PM'].map(p => (
                            <button
                              key={p}
                              type="button"
                              onClick={() => setStartAmPm(p)}
                              style={{
                                padding:'2px 7px', borderRadius:6, border:'none',
                                background: startAmPm === p ? '#4caf50' : 'transparent',
                                color: startAmPm === p ? '#000' : '#888', fontWeight:700, fontSize:10, cursor:'pointer'
                              }}
                            >
                              {p}
                            </button>
                          ))}
                        </div>
                      </div>
                    </div>

                    <div style={{ height:1, background:'#262626' }} />

                    {/* END TIME ROW */}
                    <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center' }}>
                      <div style={{ display:'flex', alignItems:'center', gap:6 }}>
                        <div style={{ width:7, height:7, borderRadius:'50%', background:'#ffb300' }} />
                        <span style={{ fontSize:11, fontWeight:700, color:'#ffb300', letterSpacing:'0.05em' }}>END</span>
                      </div>
                      <div style={{ display:'flex', alignItems:'center', gap:6 }}>
                        {/* Time Input Box [ HH : MM ] */}
                        <div style={{ display:'flex', alignItems:'center', background:'#222', border:'1px solid #333', borderRadius:8, padding:'3px 6px' }}>
                          <input
                            type="text"
                            inputMode="numeric"
                            pattern="[0-9]*"
                            maxLength={2}
                            autoFocus={false}
                            value={endHourStr}
                            onChange={e => setEndHourStr(e.target.value.replace(/\D/g, '').slice(0, 2))}
                            onBlur={() => {
                              let num = parseInt(endHourStr, 10);
                              if (isNaN(num) || num < 1) num = 7;
                              if (num > 12) num = 12;
                              setEndHourStr(String(num).padStart(2, '0'));
                            }}
                            placeholder="07"
                            style={{
                              width:26, height:22, background:'transparent', border:'none', color:'#fff',
                              fontSize:14, fontWeight:700, textAlign:'center', outline:'none'
                            }}
                          />
                          <span style={{ fontSize:14, fontWeight:700, color:'#ffb300', padding:'0 1px' }}>:</span>
                          <input
                            type="text"
                            inputMode="numeric"
                            pattern="[0-9]*"
                            maxLength={2}
                            autoFocus={false}
                            value={endMinStr}
                            onChange={e => setEndMinStr(e.target.value.replace(/\D/g, '').slice(0, 2))}
                            onBlur={() => {
                              let num = parseInt(endMinStr, 10);
                              if (isNaN(num) || num < 0) num = 0;
                              if (num > 59) num = 59;
                              setEndMinStr(String(num).padStart(2, '0'));
                            }}
                            placeholder="00"
                            style={{
                              width:26, height:22, background:'transparent', border:'none', color:'#fff',
                              fontSize:14, fontWeight:700, textAlign:'center', outline:'none'
                            }}
                          />
                        </div>
                        {/* AM/PM Switch */}
                        <div style={{ display:'flex', background:'#222', borderRadius:8, padding:2, border:'1px solid #333' }}>
                          {['AM', 'PM'].map(p => (
                            <button
                              key={p}
                              type="button"
                              onClick={() => setEndAmPm(p)}
                              style={{
                                padding:'2px 7px', borderRadius:6, border:'none',
                                background: endAmPm === p ? '#ffb300' : 'transparent',
                                color: endAmPm === p ? '#000' : '#888', fontWeight:700, fontSize:10, cursor:'pointer'
                              }}
                            >
                              {p}
                            </button>
                          ))}
                        </div>
                      </div>
                    </div>
                  </div>

                  {/* Firewall Mode Toggle */}
                  <div style={{ display:'flex', gap:8 }}>
                    <button
                      type="button"
                      onClick={() => {
                        setNewMode('TOTAL_BLACKOUT');
                        const blackoutPkgs = apps.filter(a => blackoutApps.has(a.id)).map(a => a.id);
                        setSelectedScheduleApps(blackoutPkgs);
                      }}
                      style={{
                        flex:1, padding:9, borderRadius:10, cursor:'pointer',
                        background: newMode === 'TOTAL_BLACKOUT' ? 'rgba(239,83,80,0.2)' : '#1a1a1a',
                        border: newMode === 'TOTAL_BLACKOUT' ? '1px solid #ef5350' : '1px solid #2d2d2d',
                        color: newMode === 'TOTAL_BLACKOUT' ? '#ef5350' : '#888',
                        fontSize:12, fontWeight:700
                      }}
                    >
                      🔴 Total Blackout
                    </button>
                    <button
                      type="button"
                      onClick={() => {
                        setNewMode('SMART_SHIELD');
                        const smartPkgs = apps.filter(a => (a.wifiBlocked || a.dataBlocked) && !blackoutApps.has(a.id)).map(a => a.id);
                        setSelectedScheduleApps(smartPkgs);
                      }}
                      style={{
                        flex:1, padding:9, borderRadius:10, cursor:'pointer',
                        background: newMode === 'SMART_SHIELD' ? 'rgba(255,179,0,0.2)' : '#1a1a1a',
                        border: newMode === 'SMART_SHIELD' ? '1px solid #ffb300' : '1px solid #2d2d2d',
                        color: newMode === 'SMART_SHIELD' ? '#ffb300' : '#888',
                        fontSize:12, fontWeight:700
                      }}
                    >
                      🟡 Smart Shield
                    </button>
                  </div>

                  {/* Save & Cancel Buttons */}
                  <div style={{ display:'flex', gap:8, marginTop:4 }}>
                    <button
                      type="button"
                      onClick={() => setShowAddCustomSchedule(false)}
                      style={{ flex:1, height:42, borderRadius:10, border:'1px solid #333', background:'#222', color:'#888', fontSize:13, fontWeight:600, cursor:'pointer' }}
                    >
                      Cancel
                    </button>
                    <button
                      type="button"
                      onClick={() => {
                        const title = newTitle.trim() || 'Custom Schedule';
                        const sh = String(parseInt(startHourStr, 10) || 10).padStart(2, '0');
                        const sm = String(parseInt(startMinStr, 10) || 0).padStart(2, '0');
                        const eh = String(parseInt(endHourStr, 10) || 7).padStart(2, '0');
                        const em = String(parseInt(endMinStr, 10) || 0).padStart(2, '0');
                        const startStr = `${sh}:${sm} ${startAmPm}`;
                        const endStr = `${eh}:${em} ${endAmPm}`;
                        const autoPkgs = newMode === 'TOTAL_BLACKOUT'
                          ? apps.filter(a => blackoutApps.has(a.id)).map(a => a.id)
                          : apps.filter(a => (a.wifiBlocked || a.dataBlocked) && !blackoutApps.has(a.id)).map(a => a.id);
                        setSchedules([
                          ...schedules,
                          {
                            id: 'sched_' + Date.now(),
                            title,
                            start: startStr,
                            end: endStr,
                            days: 'Every Day',
                            mode: newMode,
                            targetPackages: autoPkgs,
                            appsCount: autoPkgs.length,
                            enabled: true
                          }
                        ]);
                        setShowAddCustomSchedule(false);
                        setNewTitle('');
                      }}
                      style={{ flex:2, height:42, borderRadius:10, border:'none', background:'#4caf50', color:'#fff', fontSize:13, fontWeight:700, cursor:'pointer' }}
                    >
                      Save Schedule
                    </button>
                  </div>
                </div>
              </div>
            )}

            {/* Modal: View Configured Apps for Existing Schedule (View-Only) */}
            {scheduleForAppConfig && (
              <div style={{
                position:'fixed', inset:0, zIndex:1000, background:'rgba(0,0,0,0.85)',
                display:'flex', alignItems:'center', justifyContent:'center', padding:16
              }}>
                <div style={{
                  background:'#1e1e1e', border:'1px solid #333', borderRadius:20,
                  width:'100%', maxWidth:380, padding:20, display:'flex', flexDirection:'column', gap:14
                }}>
                  <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center' }}>
                    <div>
                      <div style={{ fontSize:16, fontWeight:700, color:'#fff' }}>
                        Configured Apps ({(scheduleForAppConfig.targetPackages || []).length})
                      </div>
                      <div style={{ fontSize:12, color: scheduleForAppConfig.mode === 'TOTAL_BLACKOUT' ? '#ef5350' : '#ffb300', fontWeight:600 }}>
                        {scheduleForAppConfig.title} • {scheduleForAppConfig.mode === 'TOTAL_BLACKOUT' ? 'Total Blackout' : 'Smart Shield'}
                      </div>
                    </div>
                    <button
                      onClick={() => setScheduleForAppConfig(null)}
                      style={{ background:'none', border:'none', color:'#888', cursor:'pointer' }}
                    >
                      <X size={18} />
                    </button>
                  </div>
                  <div style={{ maxHeight:260, overflowY:'auto', display:'flex', flexDirection:'column', gap:8 }}>
                    {(() => {
                      const currentPkgs = scheduleForAppConfig.targetPackages || [];
                      const configuredList = apps.filter(a => currentPkgs.includes(a.id));
                      if (configuredList.length === 0) {
                        return (
                          <div style={{ textAlign:'center', padding:'24px 0', color:'#888', fontSize:13 }}>
                            No apps configured for this schedule
                          </div>
                        );
                      }
                      return configuredList.map(app => (
                        <div
                          key={app.id}
                          style={{
                            display:'flex', alignItems:'center', justifyContent:'space-between',
                            padding:'10px 12px', background:'#161616',
                            borderRadius:10, border:'1px solid #282828'
                          }}
                        >
                          <div style={{ display:'flex', alignItems:'center', gap:10 }}>
                            <span style={{ fontSize:20 }}>{app.icon}</span>
                            <div>
                              <div style={{ fontSize:13, fontWeight:600, color:'#fff' }}>{app.name}</div>
                              <div style={{ fontSize:10, color:'#777' }}>{app.id}</div>
                            </div>
                          </div>
                          <span style={{
                            fontSize:10, fontWeight:700,
                            padding:'3px 7px', borderRadius:6,
                            background: scheduleForAppConfig.mode === 'TOTAL_BLACKOUT' ? 'rgba(239,83,80,0.15)' : 'rgba(255,179,0,0.15)',
                            color: scheduleForAppConfig.mode === 'TOTAL_BLACKOUT' ? '#ef5350' : '#ffb300',
                            border: scheduleForAppConfig.mode === 'TOTAL_BLACKOUT' ? '1px solid rgba(239,83,80,0.3)' : '1px solid rgba(255,179,0,0.3)'
                          }}>
                            {scheduleForAppConfig.mode === 'TOTAL_BLACKOUT' ? 'Blackout' : 'Smart Shield'}
                          </span>
                        </div>
                      ));
                    })()}
                  </div>
                  <button
                    type="button"
                    onClick={() => setScheduleForAppConfig(null)}
                    style={{ height:40, borderRadius:10, border:'none', background:'#4caf50', color:'#fff', fontSize:13, fontWeight:700, cursor:'pointer' }}
                  >
                    Close
                  </button>
                </div>
              </div>
            )}

            {/* ══ ANALYTICS ══ */}
            {screen === 'analytics' && (
              <div style={{ flex:1, padding:'14px 16px', overflowY:'auto', display:'flex', flexDirection:'column', gap:14 }}>
                {/* Period Selector */}
                <div style={{ display:'flex', gap:8 }}>
                  {[['today','Today'],['week','Last 7 Days'],['month','Last 30 Days']].map(([key, label]) => {
                    const sel = analyticsPeriod === key;
                    return (
                      <button key={key} onClick={()=>setAnalyticsPeriod(key)} style={{
                        padding:'6px 14px', borderRadius:20, border:`1px solid ${sel?'#4caf50':'#333'}`,
                        background: sel ? 'rgba(76,175,80,0.15)' : 'var(--surface)',
                        color: sel ? '#4caf50' : '#888', fontSize:12, fontWeight: sel ? 700 : 500, cursor:'pointer'
                      }}>
                        {label}
                      </button>
                    );
                  })}
                </div>

                {/* 🚨 Leak Radar & Tracker Shield */}
                <div style={{ background:'#1e1719', border:'1px solid rgba(239,83,80,0.25)', borderRadius:16, padding:16, display:'flex', flexDirection:'column', gap:12 }}>
                  <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center' }}>
                    <div style={{ display:'flex', alignItems:'center', gap:10 }}>
                      <div style={{ width:34, height:34, borderRadius:17, background:'rgba(239,83,80,0.18)', display:'flex', alignItems:'center', justifyContent:'center' }}>
                        <Shield size={18} color='#ef5350' />
                      </div>
                      <div>
                        <div style={{ fontSize:14, fontWeight:700, color:'#fff' }}>Leak Radar & Trackers</div>
                        <div style={{ fontSize:11, color:'#888' }}>Real-time background shield</div>
                      </div>
                    </div>
                    <span style={{ fontSize:11, fontWeight:700, color:'#4caf50', background:'rgba(76,175,80,0.15)', padding:'3px 8px', borderRadius:10 }}>
                      ● Active
                    </span>
                  </div>

                  <div style={{ display:'flex', gap:10 }}>
                    <div style={{ flex:1, background:'#261d20', borderRadius:12, padding:12 }}>
                      <div style={{ fontSize:11, color:'#888' }}>Blocked Pings</div>
                      <div style={{ fontSize:18, fontWeight:700, color:'#ef5350', marginTop:4 }}>1,480</div>
                    </div>
                    <div style={{ flex:1, background:'#261d20', borderRadius:12, padding:12 }}>
                      <div style={{ fontSize:11, color:'#888' }}>Est. Data Saved</div>
                      <div style={{ fontSize:18, fontWeight:700, color:'#4caf50', marginTop:4 }}>86 MB</div>
                    </div>
                  </div>

                  <div style={{ fontSize:11.5, color:'#888', fontWeight:500 }}>
                    Top Background Offenders Today:
                  </div>
                  <div style={{ display:'flex', flexDirection:'column', gap:6 }}>
                    {[
                      { name: 'Instagram', attempts: 642 },
                      { name: 'Facebook Services', attempts: 489 },
                      { name: 'TikTok / ByteDance', attempts: 349 },
                    ].map((app, i) => (
                      <div key={i} style={{ display:'flex', justifyContent:'space-between', alignItems:'center', fontSize:12.5 }}>
                        <span style={{ color:'#f0f0f0' }}>{app.name}</span>
                        <span style={{ color:'#ef5350', fontWeight:700, fontSize:11.5 }}>{app.attempts} attempts cut</span>
                      </div>
                    ))}
                  </div>
                </div>

                {/* Hero Metric Cards */}
                <div style={{ display:'flex', gap:10 }}>
                  <div style={{ flex:1, background:'#1b2e1e', border:'1px solid rgba(76,175,80,0.3)', borderRadius:14, padding:14 }}>
                    <div style={{ display:'flex', alignItems:'center', gap:6, color:'#4caf50', fontSize:12, fontWeight:600 }}>
                      <Shield size={16} /> Data Saved
                    </div>
                    <div style={{ fontSize:22, fontWeight:700, color:'#fff', marginTop:8 }}>
                      {analyticsPeriod === 'today' ? '184.2 MB' : analyticsPeriod === 'week' ? '1.24 GB' : '4.85 GB'}
                    </div>
                    <div style={{ fontSize:10, color:'#888', marginTop:2 }}>Est. background freeze</div>
                  </div>

                  <div style={{ flex:1, background:'#2e2619', border:'1px solid rgba(255,160,0,0.3)', borderRadius:14, padding:14 }}>
                    <div style={{ display:'flex', alignItems:'center', gap:6, color:'#ffb300', fontSize:12, fontWeight:600 }}>
                      <Zap size={16} /> Pings Blocked
                    </div>
                    <div style={{ fontSize:22, fontWeight:700, color:'#fff', marginTop:8 }}>
                      {analyticsPeriod === 'today' ? '1,280' : analyticsPeriod === 'week' ? '8,960' : '38,400'}
                    </div>
                    <div style={{ fontSize:10, color:'#888', marginTop:2 }}>Background leaks cut</div>
                  </div>
                </div>

                {/* Total Traffic & Ratio Bar */}
                <div style={{ background:'var(--surface)', border:'1px solid var(--divider)', borderRadius:14, padding:16 }}>
                  <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center' }}>
                    <div>
                      <div style={{ fontSize:12, color:'#888' }}>Total Network Monitored</div>
                      <div style={{ fontSize:22, fontWeight:700, color:'#f0f0f0', marginTop:2 }}>
                        {analyticsPeriod === 'today' ? '2.14 GB' : analyticsPeriod === 'week' ? '14.8 GB' : '58.2 GB'}
                      </div>
                    </div>
                    <Activity size={24} color="#64b5f6"/>
                  </div>
                  
                  {/* Visual Split Bar */}
                  <div style={{ height:8, borderRadius:4, background:'#333', overflow:'hidden', display:'flex', marginTop:14 }}>
                    <div style={{ width:'74%', background:'#64b5f6' }}/>
                    <div style={{ width:'26%', background:'#ffb300' }}/>
                  </div>

                  <div style={{ display:'flex', justifyContent:'space-between', marginTop:10, fontSize:12 }}>
                    <div style={{ display:'flex', alignItems:'center', gap:6, color:'#f0f0f0' }}>
                      <div style={{ width:8, height:8, borderRadius:4, background:'#64b5f6' }}/>
                      WiFi: {analyticsPeriod === 'today' ? '1.58 GB' : analyticsPeriod === 'week' ? '11.0 GB' : '43.1 GB'} (74%)
                    </div>
                    <div style={{ display:'flex', alignItems:'center', gap:6, color:'#f0f0f0' }}>
                      <div style={{ width:8, height:8, borderRadius:4, background:'#ffb300' }}/>
                      Mobile: {analyticsPeriod === 'today' ? '560 MB' : analyticsPeriod === 'week' ? '3.8 GB' : '15.1 GB'} (26%)
                    </div>
                  </div>
                </div>

                {/* App Data Usage Breakdown */}
                <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center', marginTop:4 }}>
                  <div style={{ fontSize:14, fontWeight:600, color:'#f0f0f0' }}>App Data Breakdown</div>
                  <div style={{ fontSize:11, color:'#888' }}>Ranked by bandwidth</div>
                </div>

                <div style={{ display:'flex', flexDirection:'column', gap:8, paddingBottom:20 }}>
                  {[
                    { name:'YouTube', icon:'▶️', total:'680 MB', wifi:'560 MB', data:'120 MB', pct: 90, blocked: false },
                    { name:'Instagram', icon:'📸', total:'308 MB', wifi:'240 MB', data:'68 MB', pct: 65, blocked: true },
                    { name:'Netflix', icon:'🎬', total:'290 MB', wifi:'240 MB', data:'50 MB', pct: 60, blocked: false },
                    { name:'BGMI / PUBG', icon:'🎮', total:'225 MB', wifi:'180 MB', data:'45 MB', pct: 50, blocked: true },
                    { name:'WhatsApp', icon:'💬', total:'107 MB', wifi:'85 MB', data:'22 MB', pct: 35, blocked: true },
                    { name:'Spotify', icon:'🎧', total:'95 MB', wifi:'75 MB', data:'20 MB', pct: 25, blocked: false },
                  ].map(app => (
                    <div key={app.name} style={{ background:'var(--surface)', border:'1px solid var(--divider)', borderRadius:12, padding:12 }}>
                      <div style={{ display:'flex', alignItems:'center', gap:10 }}>
                        <div style={{ width:34, height:34, borderRadius:8, background:'#262626', display:'flex', alignItems:'center', justifyContent:'center', fontSize:18 }}>
                          {app.icon}
                        </div>
                        <div style={{ flex:1, minWidth:0 }}>
                          <div style={{ display:'flex', alignItems:'center', gap:6 }}>
                            <span style={{ fontSize:14, fontWeight:500, color:'#f0f0f0' }}>{app.name}</span>
                            {app.blocked && (
                              <span style={{ fontSize:9, fontWeight:700, color:'#ef5350', background:'rgba(239,83,80,0.18)', padding:'1px 5px', borderRadius:4 }}>
                                RESTRICTED
                              </span>
                            )}
                          </div>
                          <div style={{ fontSize:11, color:'#888', marginTop:2 }}>
                            WiFi: {app.wifi}  •  Data: {app.data}
                          </div>
                        </div>
                        <div style={{ fontSize:13, fontWeight:700, color:'#f0f0f0' }}>{app.total}</div>
                      </div>
                      <div style={{ height:4, borderRadius:2, background:'#262626', overflow:'hidden', marginTop:8 }}>
                        <div style={{ width:`${app.pct}%`, height:'100%', background: app.blocked ? '#ef5350' : '#4caf50' }}/>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* ══ LOGS ══ */}
            {screen === 'logs' && (
              <div style={{ flex:1, background:'#0e0e0e', fontFamily:"'Roboto Mono',monospace", display:'flex', flexDirection:'column' }}>
                <div style={{ display:'flex', gap:16, padding:'8px 14px', borderBottom:'1px solid #1e1e1e', flexShrink:0 }}>
                  {[['V','#aaa','Verbose'],['D','#64b5f6','Debug'],['I','#81c784','Info'],['W','#ffb300','Warn'],['E','#ef5350','Error']].map(([t,c,l]) => (
                    <div key={t} style={{ display:'flex', alignItems:'center', gap:4 }}>
                      <span style={{ fontSize:11, fontWeight:800, color:c }}>{t}</span>
                      <span style={{ fontSize:10, color:'#444' }}>{l}</span>
                    </div>
                  ))}
                </div>
                <div style={{ flex:1, overflowY:'auto', padding:'8px 12px', display:'flex', flexDirection:'column', gap:5 }}>
                  {logs.length===0
                    ? <div style={{ color:'#333', textAlign:'center', paddingTop:48, fontSize:13 }}>No log entries.</div>
                    : logs.map(l => (
                      <div key={l.id} style={{ display:'flex', gap:7, fontSize:11, lineHeight:'1.7', flexWrap:'wrap' }}>
                        <span style={{ color:'#3a3a3a', flexShrink:0 }}>{l.t}</span>
                        <span style={{ fontWeight:800, color:tagColor(l.tag), flexShrink:0 }}>{l.tag}</span>
                        <span style={{ color:'#4a5568', flexShrink:0 }}>{l.cat}:</span>
                        <span style={{ color:l.tag==='W'?'#ffe082':l.tag==='E'?'#fca5a5':l.tag==='D'?'#93c5fd':'#b0bec5', wordBreak:'break-all' }}>{l.msg}</span>
                      </div>
                    ))
                  }
                </div>
              </div>
            )}

            {/* ══ SETTINGS ══ */}
            {screen === 'settings' && (
              <div style={{ paddingBottom:40 }}>
                <Sec label="Privilege Provider"/>
                <div style={{ background:'var(--surface)', borderTop:'1px solid var(--divider)', borderBottom:'1px solid var(--divider)' }}>
                  {[
                    { id:'shizuku', label:'Shizuku',   sub:'ADB Wireless Debugging — Recommended' },
                    { id:'dhizuku', label:'Dhizuku',   sub:'Device Owner — Survives reboot' },
                    { id:'root',    label:'Root (su)', sub:'Superuser binary — Full root access' },
                  ].map((opt, i, arr) => (
                    <Pref key={opt.id} label={opt.label} sub={opt.sub} onClick={()=>setPrivilege(opt.id)} divider={i<arr.length-1}
                      right={
                        <div onClick={()=>setPrivilege(opt.id)} style={{ width:22, height:22, borderRadius:11, border:`2px solid ${privilege===opt.id?'var(--primary)':'var(--dim)'}`, display:'flex', alignItems:'center', justifyContent:'center', flexShrink:0, background:privilege===opt.id?'var(--primary)':'transparent' }}>
                          {privilege===opt.id && <div style={{ width:9, height:9, borderRadius:5, background:'#fff' }}/>}
                        </div>
                      }
                    />
                  ))}
                </div>

                <Sec label="Permissions"/>
                <div style={{ background:'var(--surface)', borderTop:'1px solid var(--divider)', borderBottom:'1px solid var(--divider)' }}>
                  {perms.map((p, i, arr) => (
                    <Pref key={p.id} label={p.label}
                      sub={p.granted ? '✓ Granted' : (p.required ? 'Not granted — required' : 'Not granted — optional')}
                      divider={i<arr.length-1}
                      onClick={p.granted ? undefined : ()=>grantPerm(p.id)}
                      right={
                        p.granted
                          ? <CheckCircle2 size={20} color='#4caf50'/>
                          : <button onClick={()=>grantPerm(p.id)} style={{ padding:'4px 12px', borderRadius:16, border:'1px solid #444', background:'transparent', color:'#888', fontSize:12, cursor:'pointer' }}>Grant</button>
                      }
                    />
                  ))}
                </div>

                <Sec label="Service"/>
                <div style={{ background:'var(--surface)', borderTop:'1px solid var(--divider)', borderBottom:'1px solid var(--divider)' }}>
                  <Pref label="Enabled" sub="Master on/off switch for NetCordon" right={<Toggle on={serviceOn} onToggle={()=>{setServiceOn(p=>!p);pushLog('I','Service',`AppShieldService ${!serviceOn?'started':'stopped'}`);}}/>}/>
                  <Pref label="Start on boot" sub="Automatically start after device reboot" right={<Toggle on={bootStart} onToggle={()=>setBootStart(p=>!p)}/>} divider={false}/>
                </div>

                <Sec label="Protection Rules"/>
                <div style={{ background:'var(--surface)', borderTop:'1px solid var(--divider)', borderBottom:'1px solid var(--divider)' }}>
                  <Pref label="Block when screen is off" sub="Auto-cut background traffic & mute notifications when device is locked" right={<Toggle on={screenOffShield} onToggle={()=>{ const n = !screenOffShield; setScreenOffShield(n); pushLog('I','Rule',`Screen Lock Auto-Shield ${n?'Enabled':'Disabled'}`); }}/>}/>
                  <Pref label="Block app notifications" sub="Auto-silence alerts, popups & vibrations when app is restricted" right={<Toggle on={blockNotifs} onToggle={()=>setBlockNotifs(p=>!p)}/>} divider={false}/>
                </div>

                <Sec label="Security Shield (App Lock)"/>
                <div style={{ background:'var(--surface)', borderTop:'1px solid var(--divider)', borderBottom:'1px solid var(--divider)' }}>
                  <Pref label="Biometric & PIN Lock" sub="Require Fingerprint, Face, or PIN to access NetCordon" right={<Toggle on={appLockEnabled} onToggle={()=>{
                    const next = !appLockEnabled;
                    setAppLockEnabled(next);
                    if (next) setIsLocked(true);
                    pushLog('I','Security',`Biometric & PIN Lock ${next?'Enabled':'Disabled'}`);
                  }}/>}/>
                  <Pref label="Lock on screen off" sub="Re-lock NetCordon automatically when screen turns off" right={<Toggle on={lockOnScreenOff} onToggle={()=>setLockOnScreenOff(p=>!p)}/>} divider={false}/>
                </div>

                <Sec label="Display & Theme"/>
                <div style={{ background:'var(--surface)', borderTop:'1px solid var(--divider)', borderBottom:'1px solid var(--divider)' }}>
                  <Pref label="Dark Mode" sub={darkMode ? "High-contrast dark theme enabled" : "Material 3 light theme enabled"} right={<Toggle on={darkMode} onToggle={()=>setDarkMode(p=>!p)}/>} divider={true}/>
                  <Pref label="Show package names" sub="Display package name below each app" right={<Toggle on={showPkg} onToggle={()=>setShowPkg(p=>!p)}/>} divider={false}/>
                </div>

                <Sec label="Notification"/>
                <div style={{ background:'var(--surface)', borderTop:'1px solid var(--divider)', borderBottom:'1px solid var(--divider)' }}>
                  <Pref label="Minimal notification" sub="Show a minimal icon while service is running" right={<Toggle on={minNotif} onToggle={()=>setMinNotif(p=>!p)}/>} divider={false}/>
                </div>

                <Sec label="Developer & Support"/>
                <div style={{ background:'var(--surface)', borderTop:'1px solid var(--divider)', borderBottom:'1px solid var(--divider)' }}>
                  <Pref
                    label="Developer Email"
                    sub="sachinmandawi@gmail.com · Tap to open Gmail"
                    onClick={() => window.open('mailto:sachinmandawi@gmail.com')}
                    right={<ExternalLink size={17} color='var(--primary)'/>}
                    divider={true}
                  />
                  <Pref
                    label="GitHub Profile"
                    sub="github.com/sachinmandawi"
                    onClick={() => window.open('https://github.com/sachinmandawi', '_blank')}
                    right={<ExternalLink size={17} color='var(--primary)'/>}
                    divider={true}
                  />
                  <Pref
                    label="GitHub Repository & Releases"
                    sub="github.com/sachinmandawi/NetCordon"
                    onClick={() => window.open('https://github.com/sachinmandawi/NetCordon', '_blank')}
                    right={<ExternalLink size={17} color='var(--primary)'/>}
                    divider={false}
                  />
                </div>

                <Sec label="Guide & Tour"/>
                <div style={{ background:'var(--surface)', borderTop:'1px solid var(--divider)', borderBottom:'1px solid var(--divider)' }}>
                  <Pref
                    label="App Walkthrough"
                    sub="Replay interactive feature tour & isolation guide"
                    onClick={() => setShowWalkthrough(true)}
                    right={<ChevronRight size={18} color='var(--dim)'/>}
                    divider={false}
                  />
                </div>

                <Sec label="About"/>
                <div style={{ background:'var(--surface)', borderTop:'1px solid var(--divider)', borderBottom:'1px solid var(--divider)', marginBottom: 30 }}>
                  <Pref label="NetCordon" sub="Version 1.0 · Shizuku-powered user app firewall" divider={false}/>
                </div>
              </div>
            )}
          </>
        )}
      </div>

      {/* ── Bottom Navigation Bar (Android M3 style) ── */}
      {setupDone && !showWalkthrough && !isLocked && (
        <div style={{ height:60, background:'#171717', borderTop:'1px solid #242424', display:'flex', alignItems:'center', justifyContent:'space-around', flexShrink:0, zIndex:8 }}>
          {[
            { id:'home',      label:'Firewall',  icon: Shield },
            { id:'schedules', label:'Schedules', icon: Clock },
            { id:'analytics', label:'Analytics', icon: BarChart3 },
            { id:'logs',      label:'Logs',      icon: Activity },
            { id:'settings',  label:'Settings',  icon: Sliders },
          ].map(tab => {
            const IconComp = tab.icon;
            const sel = screen === tab.id;
            return (
              <button
                key={tab.id}
                onClick={()=>setScreen(tab.id)}
                style={{
                  background: 'none', border:'none', cursor:'pointer', display:'flex', flexDirection:'column',
                  alignItems:'center', justifyContent:'center', gap:3, flex:1, height:'100%',
                  color: sel ? '#4caf50' : '#888'
                }}
              >
                <div style={{
                  width: sel ? 42 : 'auto', height: 26, borderRadius: 13,
                  background: sel ? 'rgba(76,175,80,0.18)' : 'transparent',
                  display:'flex', alignItems:'center', justifyContent:'center',
                  transition: 'background 0.2s ease'
                }}>
                  <IconComp size={19} color={sel ? '#4caf50' : '#888'} />
                </div>
                <span style={{ fontSize:10, fontWeight: sel ? 700 : 500 }}>{tab.label}</span>
              </button>
            );
          })}
        </div>
      )}

      {/* ── Biometric & PIN Security Lock Screen ── */}
      {setupDone && !showWalkthrough && isLocked && (
        <div style={{
          position:'absolute', inset:0, zIndex:99, background:'#121212',
          display:'flex', flexDirection:'column', alignItems:'center', justifyContent:'center', padding:24
        }}>
          <div style={{
            width:100, height:100, borderRadius:50, background:'rgba(76,175,80,0.15)',
            border:'1px solid rgba(76,175,80,0.3)', display:'flex', alignItems:'center', justifyContent:'center',
            marginBottom:20
          }}>
            <Lock size={46} color='#4caf50' />
          </div>
          <div style={{ fontSize:22, fontWeight:700, color:'#fff', marginBottom:8 }}>NetCordon Locked</div>
          <div style={{ fontSize:13, color:'#888', textAlign:'center', lineHeight:1.5, maxWidth:260, marginBottom:32 }}>
            Security Shield is active.<br/>Authenticate with Biometrics or PIN to continue.
          </div>
          <button
            onClick={() => {
              setIsLocked(false);
              pushLog('I','Security','Biometric unlock successful');
            }}
            style={{
              width:'100%', maxWidth:280, height:48, borderRadius:12, border:'none',
              background:'#4caf50', color:'#fff', fontSize:14, fontWeight:700,
              display:'flex', alignItems:'center', justifyContent:'center', gap:8, cursor:'pointer'
            }}
          >
            <Fingerprint size={20} />
            Unlock with Biometrics / PIN
          </button>
        </div>
      )}

      {/* ── 3-Tier App Isolation Policy Bottom Sheet ── */}
      {selectedAppForPolicy && (
        <>
          {/* Backdrop */}
          <div
            onClick={() => setSelectedAppForPolicy(null)}
            style={{ position:'absolute', inset:0, background:'rgba(0,0,0,0.65)', zIndex:90, backdropFilter:'blur(2px)' }}
          />

          {/* Bottom Sheet Container */}
          <div style={{
            position:'absolute', bottom:0, left:0, right:0, zIndex:91,
            background:'#1c1c1c', borderTopLeftRadius:20, borderTopRightRadius:20,
            borderTop:'1px solid #333', padding:'16px 20px 24px', display:'flex', flexDirection:'column', gap:14,
            boxShadow:'0 -8px 32px rgba(0,0,0,0.7)',
            userSelect:'none', WebkitUserSelect:'none', WebkitTapHighlightColor:'transparent'
          }}>
            {/* Drag Handle */}
            <div style={{ width:36, height:4, borderRadius:2, background:'#444', margin:'0 auto' }} />

            {/* App Header */}
            <div style={{ display:'flex', alignItems:'center', gap:12 }}>
              <div style={{ width:48, height:48, borderRadius:12, background:'#262626', display:'flex', alignItems:'center', justifyContent:'center', fontSize:24, flexShrink:0 }}>
                {selectedAppForPolicy.icon}
              </div>
              <div style={{ flex:1, minWidth:0 }}>
                <div style={{ fontSize:17, fontWeight:700, color:'#f0f0f0', overflow:'hidden', textOverflow:'ellipsis', whiteSpace:'nowrap' }}>
                  {selectedAppForPolicy.name}
                </div>
                <div style={{ fontSize:11, color:'#888', overflow:'hidden', textOverflow:'ellipsis', whiteSpace:'nowrap', marginTop:1 }}>
                  {selectedAppForPolicy.id}
                </div>
              </div>
              {/* Current Mode Badge */}
              {blackoutApps.has(selectedAppForPolicy.id) ? (
                <div style={{ fontSize:10, fontWeight:700, color:'#ef5350', background:'rgba(239,83,80,0.18)', border:'1px solid rgba(239,83,80,0.3)', padding:'3px 8px', borderRadius:6 }}>
                  BLACKOUT
                </div>
              ) : (selectedAppForPolicy.wifiBlocked || selectedAppForPolicy.dataBlocked) ? (
                <div style={{ fontSize:10, fontWeight:700, color:'#ffb300', background:'rgba(255,179,0,0.18)', border:'1px solid rgba(255,179,0,0.3)', padding:'3px 8px', borderRadius:6 }}>
                  SMART SHIELD
                </div>
              ) : (
                <div style={{ fontSize:10, fontWeight:700, color:'#4caf50', background:'rgba(76,175,80,0.18)', border:'1px solid rgba(76,175,80,0.3)', padding:'3px 8px', borderRadius:6 }}>
                  ALLOWED
                </div>
              )}
            </div>

            {/* 3 Policy Mode Cards */}
            <div style={{ display:'flex', flexDirection:'column', gap:10, marginTop:4 }}>
              {/* Mode 1: Always Allowed */}
              {(() => {
                const isSel = !blackoutApps.has(selectedAppForPolicy.id) && !selectedAppForPolicy.wifiBlocked && !selectedAppForPolicy.dataBlocked;
                return (
                  <div
                    onClick={() => {
                      setBlackoutApps(p => { const n = new Set(p); n.delete(selectedAppForPolicy.id); return n; });
                      setApps(p => p.map(a => a.id === selectedAppForPolicy.id ? { ...a, wifiBlocked:false, dataBlocked:false } : a));
                      setSelectedAppForPolicy(p => ({ ...p, wifiBlocked:false, dataBlocked:false }));
                      pushLog('I', 'Policy', `${selectedAppForPolicy.name} -> Always Allowed`);
                    }}
                    style={{
                      padding:'14px 16px', borderRadius:14, cursor:'pointer',
                      background: isSel ? 'rgba(76,175,80,0.12)' : '#242424',
                      border: `1px solid ${isSel ? '#4caf50' : '#333'}`,
                      display:'flex', alignItems:'center', gap:14
                    }}
                  >
                    <div style={{ width:36, height:36, borderRadius:18, background: isSel ? 'rgba(76,175,80,0.22)' : '#2c2c2c', display:'flex', alignItems:'center', justifyContent:'center', flexShrink:0 }}>
                      <CheckCircle2 size={19} color={isSel ? '#4caf50' : '#888'} />
                    </div>
                    <div style={{ flex:1, minWidth:0 }}>
                      <div style={{ fontSize:15, fontWeight:600, color: isSel ? '#fff' : '#eee' }}>Always Allowed</div>
                      <div style={{ fontSize:12, color:'#888', marginTop:2 }}>Normal internet access</div>
                    </div>
                    <div style={{ width:18, height:18, borderRadius:9, border:`2px solid ${isSel ? '#4caf50' : '#555'}`, display:'flex', alignItems:'center', justifyContent:'center' }}>
                      {isSel && <div style={{ width:8, height:8, borderRadius:4, background:'#4caf50' }} />}
                    </div>
                  </div>
                );
              })()}

              {/* Mode 2: Smart Shield */}
              {(() => {
                const isSel = !blackoutApps.has(selectedAppForPolicy.id) && (selectedAppForPolicy.wifiBlocked || selectedAppForPolicy.dataBlocked);
                return (
                  <div
                    onClick={() => {
                      setBlackoutApps(p => { const n = new Set(p); n.delete(selectedAppForPolicy.id); return n; });
                      setApps(p => p.map(a => a.id === selectedAppForPolicy.id ? { ...a, wifiBlocked:true, dataBlocked:true } : a));
                      setSelectedAppForPolicy(p => ({ ...p, wifiBlocked:true, dataBlocked:true }));
                      pushLog('W', 'Policy', `${selectedAppForPolicy.name} -> Smart Shield`);
                    }}
                    style={{
                      padding:'14px 16px', borderRadius:14, cursor:'pointer',
                      background: isSel ? 'rgba(255,179,0,0.12)' : '#242424',
                      border: `1px solid ${isSel ? '#ffb300' : '#333'}`,
                      display:'flex', alignItems:'center', gap:14
                    }}
                  >
                    <div style={{ width:36, height:36, borderRadius:18, background: isSel ? 'rgba(255,179,0,0.22)' : '#2c2c2c', display:'flex', alignItems:'center', justifyContent:'center', flexShrink:0 }}>
                      <Shield size={19} color={isSel ? '#ffb300' : '#888'} />
                    </div>
                    <div style={{ flex:1, minWidth:0 }}>
                      <div style={{ fontSize:15, fontWeight:600, color: isSel ? '#fff' : '#eee' }}>Smart Shield</div>
                      <div style={{ fontSize:12, color:'#888', marginTop:2 }}>Cut on close • Resume when open</div>
                    </div>
                    <div style={{ width:18, height:18, borderRadius:9, border:`2px solid ${isSel ? '#ffb300' : '#555'}`, display:'flex', alignItems:'center', justifyContent:'center' }}>
                      {isSel && <div style={{ width:8, height:8, borderRadius:4, background:'#ffb300' }} />}
                    </div>
                  </div>
                );
              })()}

              {/* Mode 3: Total Blackout */}
              {(() => {
                const isSel = blackoutApps.has(selectedAppForPolicy.id);
                return (
                  <div
                    onClick={() => {
                      setBlackoutApps(p => new Set(p).add(selectedAppForPolicy.id));
                      setApps(p => p.map(a => a.id === selectedAppForPolicy.id ? { ...a, wifiBlocked:true, dataBlocked:true } : a));
                      setSelectedAppForPolicy(p => ({ ...p, wifiBlocked:true, dataBlocked:true }));
                      pushLog('E', 'Policy', `${selectedAppForPolicy.name} -> Total Blackout`);
                    }}
                    style={{
                      padding:'14px 16px', borderRadius:14, cursor:'pointer',
                      background: isSel ? 'rgba(239,83,80,0.12)' : '#242424',
                      border: `1px solid ${isSel ? '#ef5350' : '#333'}`,
                      display:'flex', alignItems:'center', gap:14
                    }}
                  >
                    <div style={{ width:36, height:36, borderRadius:18, background: isSel ? 'rgba(239,83,80,0.22)' : '#2c2c2c', display:'flex', alignItems:'center', justifyContent:'center', flexShrink:0 }}>
                      <Ban size={19} color={isSel ? '#ef5350' : '#888'} />
                    </div>
                    <div style={{ flex:1, minWidth:0 }}>
                      <div style={{ fontSize:15, fontWeight:600, color: isSel ? '#fff' : '#eee' }}>Total Blackout</div>
                      <div style={{ fontSize:12, color:'#888', marginTop:2 }}>Block all internet • Zero ads</div>
                    </div>
                    <div style={{ width:18, height:18, borderRadius:9, border:`2px solid ${isSel ? '#ef5350' : '#555'}`, display:'flex', alignItems:'center', justifyContent:'center' }}>
                      {isSel && <div style={{ width:8, height:8, borderRadius:4, background:'#ef5350' }} />}
                    </div>
                  </div>
                );
              })()}
            </div>

            {/* Done Button */}
            <button
              onClick={() => setSelectedAppForPolicy(null)}
              style={{
                width:'100%', height:46, borderRadius:12, border:'none', marginTop:6,
                background:'#4caf50', color:'#fff', fontSize:15, fontWeight:700, cursor:'pointer'
              }}
            >
              Done
            </button>
          </div>
        </>
      )}
    </div>
  );
}
