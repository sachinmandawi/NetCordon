import React, { useState, useEffect } from 'react';
import {
  ShieldCheck, Search, MoreVertical, X,
  Wifi, WifiOff, Signal, SignalZero, AlertTriangle,
  ArrowLeft, Trash2, Bell, Power, Eye, Package,
  Info, Activity, ChevronRight, CheckCircle2, Circle,
  ExternalLink, Zap, RefreshCw, PauseCircle
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

/* ─── WiFi / Data Icon Toggle ─── */
function NetIcon({ blocked, type, onToggle }) {
  return (
    <button
      onClick={onToggle}
      style={{ width:52, height:52, flexShrink:0, display:'flex', alignItems:'center', justifyContent:'center', background:'none', border:'none' }}
    >
      <div style={{
        width:38, height:38, borderRadius:11,
        backgroundColor: blocked ? 'rgba(239,83,80,0.18)' : 'rgba(76,175,80,0.14)',
        display:'flex', alignItems:'center', justifyContent:'center',
        transition:'background-color 0.18s',
        position:'relative', overflow:'hidden',
      }}>
        {/* Full icon — always shown, color changes */}
        {type === 'wifi'
          ? <Wifi   size={20} color={blocked ? '#ef5350' : '#4caf50'} strokeWidth={2.2} />
          : <Signal size={20} color={blocked ? '#ef5350' : '#4caf50'} strokeWidth={2.2} />
        }
        {/* Single diagonal slash \ when blocked */}
        {blocked && (
          <svg
            width="38" height="38"
            viewBox="0 0 38 38"
            style={{ position:'absolute', top:0, left:0, pointerEvents:'none' }}
          >
            <line x1="9" y1="9" x2="29" y2="29" stroke="#ef5350" strokeWidth="2.8" strokeLinecap="round"/>
          </svg>
        )}
      </div>
    </button>
  );
}

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

  useEffect(() => {
    // Start exit transition after 2.8s
    const t1 = setTimeout(() => {
      setFadingOut(true);
    }, 2800);

    // Completely unmount splash after 3.3s
    const t2 = setTimeout(() => {
      onFinish();
    }, 3300);

    return () => {
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

        {/* Minimal Progress Indicator */}
        <div
          style={{
            width: 140,
            height: 3,
            borderRadius: 2,
            background: 'rgba(255,255,255,0.08)',
            overflow: 'hidden',
            position: 'relative',
            animation: 'splashSubFade 0.6s ease 0.55s both'
          }}
        >
          <div
            style={{
              height: '100%',
              borderRadius: 2,
              background: '#4caf50',
              animation: 'splashBarProgress 2.2s cubic-bezier(0.4, 0, 0.2, 1) 0.25s both'
            }}
          />
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

  const grantPerm = (id) => setPerms(p => p.map(x => x.id === id ? { ...x, granted: true } : x));

  // ── App State ──
  const [screen, setScreen]       = useState('home');
  const [apps, setApps]           = useState(INIT_APPS);
  const [search, setSearch]       = useState('');
  const [searching, setSearching] = useState(false);
  const [showMenu, setShowMenu]   = useState(false);
  const [shizukuOk, setShizukuOk] = useState(true);

  // Settings
  const [privilege, setPrivilege] = useState('shizuku');
  const [bootStart, setBootStart] = useState(true);
  const [serviceOn, setServiceOn] = useState(true);
  const [minNotif,  setMinNotif]  = useState(false);
  const [showPkg,   setShowPkg]   = useState(true);
  const [blockNotifs, setBlockNotifs] = useState(true);
  const [screenOffShield, setScreenOffShield] = useState(false);

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

  const toggleWifi = id => setApps(p => p.map(a => {
    if (a.id !== id) return a;
    const n = !a.wifiBlocked;
    if (n) {
      pushLog('W', 'NetPol', `uid-policy ${a.uid} wifi -> REJECT_ALL (${a.name})`);
      pushLog('D', 'AppOps', `RUN_IN_BACKGROUND -> IGNORE (${a.id})`);
      if (blockNotifs) pushLog('D', 'AppOps', `POST_NOTIFICATION -> IGNORE (${a.id})`);
      pushLog('W', 'Shield', `[BLOCKED] ${a.name} (UID ${a.uid}) WiFi & FCM wakeups frozen -> Single Tick (✓)`);
    } else {
      pushLog('I', 'NetPol', `uid-policy ${a.uid} wifi -> ALLOW_ALL (${a.name})`);
      pushLog('I', 'AppOps', `RUN_IN_BACKGROUND & NOTIFICATIONS -> ALLOWED (${a.id})`);
      pushLog('I', 'Shield', `[ALLOWED] ${a.name} WiFi restored -> Double Tick (✓✓)`);
    }
    return { ...a, wifiBlocked: n };
  }));

  const toggleData = id => setApps(p => p.map(a => {
    if (a.id !== id) return a;
    const n = !a.dataBlocked;
    if (n) {
      pushLog('W', 'NetPol', `uid-policy ${a.uid} data -> REJECT_ALL (${a.name})`);
      pushLog('D', 'AppOps', `RUN_IN_BACKGROUND -> IGNORE (${a.id})`);
      if (blockNotifs) pushLog('D', 'AppOps', `POST_NOTIFICATION -> IGNORE (${a.id})`);
      pushLog('W', 'Shield', `[BLOCKED] ${a.name} (UID ${a.uid}) Mobile Data frozen -> Single Tick (✓)`);
    } else {
      pushLog('I', 'NetPol', `uid-policy ${a.uid} data -> ALLOW_ALL (${a.name})`);
      pushLog('I', 'AppOps', `RUN_IN_BACKGROUND & NOTIFICATIONS -> ALLOWED (${a.id})`);
      pushLog('I', 'Shield', `[ALLOWED] ${a.name} Mobile Data restored -> Double Tick (✓✓)`);
    }
    return { ...a, dataBlocked: n };
  }));

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



  const menuItems = [
    { label:'Logs',         action:()=>{ setScreen('logs');     setShowMenu(false); } },
    { label:'Settings',     action:()=>{ setScreen('settings'); setShowMenu(false); } },
    { label: shizukuOk ? 'Disconnect Shizuku' : 'Connect Shizuku', action:()=>{ setShizukuOk(p=>!p); setShowMenu(false); pushLog('I','Shizuku', shizukuOk?'Binder disconnected':'Binder reconnected'); }},
  ];

  /* ════ RENDER ════ */
  return (
    <div style={{ width:'100%', height:'100vh', background:'var(--bg)', display:'flex', flexDirection:'column', overflow:'hidden', position:'relative' }}>

      {/* ── Animated Splash Screen ── */}
      {showSplash && (
        <SplashScreen onFinish={() => setShowSplash(false)} />
      )}

      {/* ── Persistent Top Bar (Home, Logs, Settings) ── */}
      {setupDone && (
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
              <div style={{ position:'relative', flexShrink:0 }}>
                <button onClick={()=>setShowMenu(p=>!p)} style={IB}><MoreVertical size={22} color='#c8c8c8'/></button>
                {showMenu && (
                  <>
                    <div onClick={()=>setShowMenu(false)} style={{ position:'fixed', inset:0, zIndex:9 }}/>
                    <div style={{ position:'absolute', top:46, right:0, zIndex:10, background:'#2c2c2c', borderRadius:4, boxShadow:'0 6px 20px rgba(0,0,0,0.6)', minWidth:196, overflow:'hidden' }}>
                      {menuItems.map(m => (
                        <button key={m.label} onClick={m.action}
                          style={{ display:'block', width:'100%', textAlign:'left', padding:'13px 20px', background:'none', border:'none', fontSize:14, color:'#e0e0e0' }}
                          onMouseEnter={e=>e.currentTarget.style.background='#3a3a3a'}
                          onMouseLeave={e=>e.currentTarget.style.background='none'}
                        >{m.label}</button>
                      ))}
                    </div>
                  </>
                )}
              </div>
            </div>
          </div>
        ) : screen === 'logs' ? (
          <Toolbar title="Logs" onBack={()=>setScreen('home')} right={<button onClick={()=>setLogs([])} style={IB}><Trash2 size={20} color='#c8c8c8'/></button>}/>
        ) : (
          <Toolbar title="Settings" onBack={()=>setScreen('home')}/>
        )
      )}

      {/* ── CONTENT ── */}
      <div style={{ flex:1, overflowY:'auto', display:'flex', flexDirection:'column', position:'relative' }}>

        {/* ══ PERMISSION SETUP SCREEN (First Launch) ══ */}
        {!setupDone ? (
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
                  <div style={{ display: 'flex' }}>
                    <div style={{ width: 52, display: 'flex', flexDirection: 'column', alignItems: 'center', padding: '4px 0' }}>
                      <Wifi size={13} color="#666"/>
                      <span style={{ fontSize: 9, color: '#555', marginTop: 1, fontWeight: 700 }}>WiFi</span>
                    </div>
                    <div style={{ width: 52, display: 'flex', flexDirection: 'column', alignItems: 'center', padding: '4px 0' }}>
                      <Signal size={13} color="#666"/>
                      <span style={{ fontSize: 9, color: '#555', marginTop: 1, fontWeight: 700 }}>Data</span>
                    </div>
                  </div>
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
                    <div style={{ display:'flex', alignItems:'center', paddingLeft:16, transition:'background 0.1s' }}
                      onMouseDown={e=>e.currentTarget.style.background='rgba(255,255,255,0.04)'}
                      onMouseUp={e=>e.currentTarget.style.background='transparent'}
                      onMouseLeave={e=>e.currentTarget.style.background='transparent'}>
                      <div style={{ width:44, height:44, borderRadius:22, background:'#2a2a2a', border:'1px solid #333', display:'flex', alignItems:'center', justifyContent:'center', fontSize:24, flexShrink:0 }}>
                        {app.icon}
                      </div>
                      <div style={{ flex:1, minWidth:0, paddingLeft:14, paddingRight:4 }}>
                        <div style={{ fontSize:15, color:'#e8e8e8', fontWeight:500, overflow:'hidden', textOverflow:'ellipsis', whiteSpace:'nowrap' }}>{app.name}</div>
                        {showPkg && <div style={{ fontSize:12, color:'#666', marginTop:2, overflow:'hidden', textOverflow:'ellipsis', whiteSpace:'nowrap' }}>{app.id}</div>}
                      </div>
                      <NetIcon blocked={app.wifiBlocked} type="wifi" onToggle={()=>toggleWifi(app.id)}/>
                      <NetIcon blocked={app.dataBlocked} type="data" onToggle={()=>toggleData(app.id)}/>
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

                <Sec label="Display"/>
                <div style={{ background:'var(--surface)', borderTop:'1px solid var(--divider)', borderBottom:'1px solid var(--divider)' }}>
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

                <Sec label="About"/>
                <div style={{ background:'var(--surface)', borderTop:'1px solid var(--divider)', borderBottom:'1px solid var(--divider)', marginBottom: 30 }}>
                  <Pref label="NetCordon" sub="Version 1.0 · Shizuku-powered user app firewall" divider={false}/>
                </div>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
}
