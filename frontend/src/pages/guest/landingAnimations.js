import { gsap } from 'gsap'
import { Flip } from 'gsap/Flip'
import { ScrollTrigger } from 'gsap/ScrollTrigger'
import { SplitText } from 'gsap/SplitText'

gsap.registerPlugin(ScrollTrigger, SplitText, Flip)

export function mountLandingAnimations() {
  const windowListeners = new AbortController()
  const tickerCallbacks = []
  let animationContext = null

  function trackTicker(callback) {
    tickerCallbacks.push(callback)
    gsap.ticker.add(callback)
  }

  function afterFontsLoad(callback) {
    document.fonts.ready.then(() => {
      // Work started after the fonts load must join the context too, or unmounting would not stop it.
      if (!windowListeners.signal.aborted) animationContext.add(callback)
    })
  }

  // The ported script calls the bare global addEventListener; this routes those window listeners through one abort signal.
  function addEventListener(type, listener) {
    window.addEventListener(type, listener, { signal: windowListeners.signal })
  }

  document.documentElement.classList.add('gs')
  gsap.ticker.lagSmoothing(0)
  animationContext = gsap.context(() => {
    runLandingScript({ trackTicker, afterFontsLoad, addEventListener })
    revealOnScroll()
    afterFontsLoad(() => ScrollTrigger.refresh())
  })

  return function unmountLandingAnimations() {
    windowListeners.abort()
    tickerCallbacks.forEach((callback) => gsap.ticker.remove(callback))
    animationContext.revert()
    document.documentElement.classList.remove('gs')
  }
}

function revealOnScroll() {
  const revealElements = document.querySelectorAll('.landing-page .rv')
  if (matchMedia('(prefers-reduced-motion: reduce)').matches) {
    revealElements.forEach((element) => element.classList.add('in'))
    return
  }
  ScrollTrigger.batch(revealElements, {
    start: 'top 90%',
    end: 'max',
    once: true,
    onEnter: (batch) =>
      gsap.to(batch, {
        opacity: 1,
        y: 0,
        duration: 0.9,
        ease: 'power3.out',
        stagger: 0.09,
        overwrite: 'auto',
        onComplete: () => batch.forEach((element) => element.classList.add('in')),
      }),
  })
}

/* Ported from the static landing page; only the GSAP setup and window listener wiring changed. */
function runLandingScript({ trackTicker, afterFontsLoad, addEventListener }) {
  var R = matchMedia('(prefers-reduced-motion: reduce)').matches, G = !R;
  var $ = function(s){ return document.querySelector(s); }, $$ = function(s){ return [].slice.call(document.querySelectorAll(s)); };
  var css = function(v){ return getComputedStyle(document.documentElement).getPropertyValue(v).trim(); };
  var small = function(){ return innerWidth < 760; };
  var RAMP = ' .:-=+*x#X';

  /* run a loop only while its element is on screen (saves battery on phones) */
  function whileVisible(trigger, anim){ if (!G) return; anim.pause(); ScrollTrigger.create({trigger:trigger,start:'top bottom',end:'bottom top',onToggle:function(s){ s.isActive ? anim.play() : anim.pause(); }}); }
  function onTicker(trigger, fps, fn){ if (!G) { fn(0); return; } var vis = false, last = 0; ScrollTrigger.create({trigger:trigger,start:'top bottom',end:'bottom top',onToggle:function(s){ vis = s.isActive; }}); trackTicker(function(){ if (!vis) return; var n = performance.now(); if (n - last > 1000/fps){ last = n; fn(n/1000); } }); }

  /* scramble-decode (Firecrawl's "Navia*Zia-" → "Navigation") */
  var GLYPH = '!<>-_\\/[]{}=+*^?#%01';
  function scramble(el, text, dur){ if (!G) { el.textContent = text; return; } var o = {p:0};
    return gsap.to(o,{p:1,duration:dur||.8,ease:'none',onUpdate:function(){ var n = Math.floor(o.p*text.length); el.textContent = text.slice(0,n) + text.slice(n).replace(/[^ ]/g,function(){ return GLYPH[Math.random()*GLYPH.length|0]; }); },onComplete:function(){ el.textContent = text; }}); }

  /* ========== HERO ========== */
  var hero = $('#hero'), cellsEl = $('#cells'), heroIn = $('#heroIn'), free = [];
  var ICONS = {code:'0010001000100010101000100',check:'0000000001000101010001000',flame:'0010001100011101111101110',cup:'1111111111011100010001110',user:'0111001110001001111110001',lines:'1111000000111000000011111',dots:'1010100000101010000010101',lt:'0001000100010000010000010'};
  var ICON_SEQ = ['code','check','lines','flame','cup','user','lt','dots'];
  function buildGrid(){
    var W = hero.clientWidth, H = hero.clientHeight, target = small() ? 72 : 104;
    var cols = Math.max(4, Math.round(W/target)), cs = W/cols, rows = Math.ceil(H/cs);
    cellsEl.style.gridTemplateColumns = 'repeat('+cols+','+cs+'px)'; cellsEl.style.gridAutoRows = cs+'px';
    var hr = hero.getBoundingClientRect(), br = heroIn.getBoundingClientRect();
    var c0 = Math.max(0, Math.floor((br.left-hr.left)/cs)), c1 = Math.min(cols-1, Math.floor((br.right-hr.left-1)/cs));
    var r0 = Math.max(1, Math.floor((br.top-hr.top+cs*.6)/cs)), r1 = Math.min(rows-1, Math.floor((br.bottom-hr.top-1)/cs));
    if (small()) { c0 = 0; c1 = cols-1; r0 = 1; r1 = rows-2; }
    var html = '';
    for (var r = 0; r < rows; r++) for (var c = 0; c < cols; c++){ var u = c >= c0 && c <= c1 && r >= r0 && r <= r1; html += '<div class="cell'+(u?' under':'')+'" data-c="'+c+'" data-r="'+r+'">'+(u?'':'<i class="f"></i>')+'</div>'; }
    cellsEl.innerHTML = html;
    free = $$('#cells .cell').filter(function(el){ return !el.classList.contains('under'); });
    $$('.spark').forEach(function(s){ s.remove(); });
    if (!small()) [[c0, r0],[c1+1, r0]].forEach(function(p){ var s = document.createElement('span'); s.className = 'spark'; s.innerHTML = '<svg viewBox="0 0 24 24"><path d="M12 0c.6 6.2 5.8 11.4 12 12-6.2.6-11.4 5.8-12 12-.6-6.2-5.8-11.4-12-12C6.2 11.4 11.4 6.2 12 0z"/></svg>'; s.style.left = (p[0]*cs)+'px'; s.style.top = (p[1]*cs)+'px'; hero.appendChild(s); });
    var pad = 14, last = Math.min(rows-1, r1 + (small() ? 1 : 0));
    [['#cl1',0,0],['#cl2',1,0],['#cl3',0,last],['#cl4',1,last]].forEach(function(l){ var el = $(l[0]); el.style.top = (l[2]*cs + pad)+'px'; el.style.left = l[1] ? 'auto' : pad+'px'; el.style.right = l[1] ? pad+'px' : 'auto'; });
    /* icons alternate left/right, on staggered rows and columns, never in the corner-label cells */
    var spots = [];
    if (small()) spots = [[1,0],[cols-2,0]];
    else {
      var leftC = [], rightC = []; for (var cc = 0; cc < c0; cc++) leftC.push(cc); for (var cr = c1+1; cr < cols; cr++) rightC.push(cr);
      var rowsAvail = []; for (var rr = 1; rr < rows-1; rr++) rowsAvail.push(rr);
      [[0,.15],[1,.3],[0,.55],[1,.7],[0,.85],[1,.05]].forEach(function(s, k){
        var side = s[0] ? rightC : leftC; if (!side.length) return;
        spots.push([side[(k >> 1) % side.length], rowsAvail[Math.min(rowsAvail.length-1, Math.floor(s[1]*rowsAvail.length))]]);
      });
    }
    spots.forEach(function(s){ var cell = cellsEl.querySelector('.cell[data-c="'+s[0]+'"][data-r="'+s[1]+'"]'); if (!cell || cell.classList.contains('under') || cell.querySelector('.pix')) return;
      var p = document.createElement('div'); p.className = 'pix'; p.innerHTML = new Array(26).join('<i></i>'); cell.appendChild(p); });
  }
  function morph(pix, name){ var bits = ICONS[name], sq = pix.children;
    for (var i = 0; i < 25; i++){ var on = bits[i] === '1'; if (!G) { sq[i].style.opacity = on ? 1 : 0; sq[i].style.transform = 'none'; continue; }
      gsap.to(sq[i],{opacity:on?1:0,scale:on?1:.4,duration:.25,delay:Math.random()*.45,ease:on?'back.out(3)':'power2.in',overwrite:true}); } }
  var asL = $('#asL'), asR = $('#asR');
  function asciiFrame(t){
    function block(mirror){ var out = '', C = 30, Rw = 11;
      for (var y = 0; y < Rw; y++){ var line = '';
        for (var x = 0; x < C; x++){ var xx = mirror ? C-1-x : x, v = (C-xx)/C*.55 + y/Rw*.55 - .45 + .35*Math.sin(xx*.45 + y*.8 - t*2.2) + .2*Math.sin(xx*.13 - t*1.1);
          line += (Rw-y)/Rw < (C-xx)/C*1.3 ? RAMP[Math.max(0, Math.min(9, Math.round(v*10)))] : ' '; }
        out += line + '\n'; }
      return out; }
    asL.textContent = block(false); asR.textContent = block(true);
  }
  function heroBoot(){
    buildGrid(); var pixes = $$('.pix');
    if (!G) { $$('.cell').forEach(function(c){ c.style.setProperty('--dot',1); }); pixes.forEach(function(p,i){ morph(p, ICON_SEQ[i % 8]); }); asciiFrame(0); return; }
    var flashes = free.map(function(c){ return c.querySelector('.f'); }).filter(Boolean);
    var split = SplitText.create('#h1 span',{type:'lines',mask:'lines'});
    gsap.timeline({defaults:{ease:'power4.out'}})
      .fromTo(flashes,{opacity:0},{opacity:1,duration:.22,yoyo:true,repeat:1,ease:'power1.inOut',stagger:{each:.008,from:'random'}},0)
      .fromTo('.cell',{'--dot':0},{'--dot':1,duration:.6,stagger:{each:.004,from:'center'}},.25)
      .from('.spark',{scale:0,rotate:-90,duration:.8,ease:'back.out(2)',stagger:.12},.45)
      .from(split.lines,{yPercent:110,duration:1,stagger:.12},.35)
      .from('.hero .sub, .hero .cta-row',{y:16,autoAlpha:0,duration:.7,stagger:.1},.7)
      .add(function(){ $$('.corner-l').forEach(function(el){ scramble(el, el.textContent, .9); }); }, 1)
      .from('.ascii',{autoAlpha:0,duration:1.2},1.1)
      .add(function(){ pixes.forEach(function(p,i){ gsap.delayedCall(i*.25, function(){ morph(p, ICON_SEQ[i % 8]); }); }); }, 1.2);
    var step = 0, cycle = gsap.timeline({repeat:-1,repeatDelay:2.6,delay:3.5,onRepeat:function(){ step++; $$('.pix').forEach(function(p,i){ gsap.delayedCall(i*.2, function(){ morph(p, ICON_SEQ[(i+step) % 8]); }); }); }}); cycle.to({},{duration:.01});
    var spin = gsap.to('.spark svg',{rotate:360,duration:12,ease:'none',repeat:-1});
    var blip = gsap.timeline({repeat:-1,repeatDelay:.9,delay:3}); blip.add(function(){ if (!free.length) return; var f = free[Math.random()*free.length|0].querySelector('.f'); if (f) gsap.fromTo(f,{opacity:.9},{opacity:0,duration:1.2,ease:'power2.out'}); });
    ScrollTrigger.create({trigger:hero,start:'top bottom',end:'bottom top',onToggle:function(s){ [cycle, spin, blip].forEach(function(a){ s.isActive ? a.resume() : a.pause(); }); }});
    onTicker(hero, 14, function(t){ if (!small()) asciiFrame(t); });
  }
  afterFontsLoad(heroBoot);
  var lastW = innerWidth, rz;
  addEventListener('resize', function(){ clearTimeout(rz); rz = setTimeout(function(){ if (innerWidth === lastW) return; lastW = innerWidth; buildGrid(); $$('.cell').forEach(function(c){ c.style.setProperty('--dot',1); }); $$('.pix').forEach(function(p,i){ morph(p, ICON_SEQ[i % 8]); }); }, 200); });

  /* ========== NAVBAR SEARCH: combobox with scoped suggestions ========== */
  var IC = {
    p:'<path d="m8 8-4 4 4 4M16 8l4 4-4 4"/>',
    c:'<circle cx="9" cy="8" r="3"/><path d="M3 20a6 6 0 0 1 12 0M16 4.5a3 3 0 0 1 0 6M21 20a6 6 0 0 0-4-5.6"/>',
    k:'<path d="M8 21h8M12 17v4M7 4h10v5a5 5 0 0 1-10 0zM7 6H4v1a3 3 0 0 0 3 3M17 6h3v1a3 3 0 0 1-3 3"/>',
    u:'<circle cx="12" cy="8" r="4"/><path d="M4 21a8 8 0 0 1 16 0"/>',
    h:'<path d="M3 12a9 9 0 1 0 2.6-6.4L3 8M3 3v5h5M12 7v5l3 2"/>',
    s:'<circle cx="11" cy="11" r="7"/><path d="m20 20-3.5-3.5"/>'
  };
  var KIND = {p:['Problems','#solve','problem'],c:['Communities','#communities','community'],k:['Contests','#compete','contest'],u:['People','#compete','person']};
  /* ponytail: static sample index; swap for a fetch to the search endpoint once the backend exists */
  var IDX = [['two-sum','p','Medium'],['valid-parentheses','p','Easy'],['merge-intervals','p','Medium'],['lru-cache','p','Hard'],['binary-search','p','Easy'],['coin-change','p','Medium'],['word-ladder','p','Hard'],['top-k-frequent','p','Medium'],['recursion: power set','p','Easy'],
    ['fpt-algo-club','c','142 members'],['sd21304','c','38 members'],['java-beginners','c','310 members'],['night-owls-cp','c','57 members'],
    ['weekly round 12','k','Sat 20:00'],['sd21304 mid-term','k','Fri 19:00'],
    ['minh.tran','u','512 pts'],['linh_cp','u','498 pts'],['hung.dev','u','471 pts'],['khoa.nexus','u','455 pts']];
  var RECENT = ['two pointers','fpt-algo-club','recursion'];
  var srch = $('#srch'), qIn = $('#q'), sugg = $('#sugg'), opts = [], act = -1;
  function esc(t){ return t.replace(/[&<>"]/g, function(c){ return {'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;'}[c]; }); }
  function svg(k){ return '<svg viewBox="0 0 24 24" aria-hidden="true">'+IC[k]+'</svg>'; }
  function opt(icon, label, meta, href, val){ opts.push({href:href, val:val}); var i = opts.length-1;
    return '<div role="option" id="opt-'+i+'" data-i="'+i+'" aria-selected="false">'+svg(icon)+'<span class="nm">'+label+'</span>'+(meta ? '<span class="meta">'+meta+'</span>' : '<span class="meta"></span>')+'<span class="ent">↵</span></div>'; }
  function render(){ var q = qIn.value.trim(), lq = q.toLowerCase(), h = ''; opts = []; act = -1;
    if (!q) {
      h += '<div class="grp">Recent</div>' + RECENT.map(function(r){ return opt('h', '<b>'+esc(r)+'</b>', '', '#solve', r); }).join('');
      h += '<div class="grp">Popular problems</div>' + IDX.filter(function(x){ return x[1] === 'p'; }).slice(0,3).map(function(x){ return opt('p', '<b>'+esc(x[0])+'</b>', x[2], '#solve', x[0]); }).join('');
    } else {
      h += Object.keys(KIND).map(function(k){ return opt(k, '<b>'+esc(q)+'</b> in '+KIND[k][0], '', KIND[k][1], q); }).join('');
      var hits = IDX.filter(function(x){ return x[0].indexOf(lq) > -1; }).slice(0,5);
      h += '<div class="grp">Top matches</div>';
      h += hits.length ? hits.map(function(x){ var at = x[0].indexOf(lq), name = esc(x[0].slice(0,at)) + '<mark>' + esc(x[0].slice(at, at+lq.length)) + '</mark>' + esc(x[0].slice(at+lq.length));
        return opt(x[1], '<b>'+name+'</b>', KIND[x[1]][2] + ' · ' + x[2], KIND[x[1]][1], x[0]); }).join('') : '<div class="none">No matches for “'+esc(q)+'”. Try a problem or community name.</div>';
    }
    sugg.innerHTML = h + '<div class="ft" aria-hidden="true"><span>↑↓ navigate</span><span>↵ open</span><span>esc close</span></div>';
  }
  function setAct(i){ var els = sugg.querySelectorAll('[role=option]'); if (!els.length) return; act = (i + els.length) % els.length;
    [].forEach.call(els, function(e,k){ e.setAttribute('aria-selected', k === act); }); qIn.setAttribute('aria-activedescendant', 'opt-'+act); els[act].scrollIntoView({block:'nearest'}); }
  function openS(){ render(); sugg.hidden = false; qIn.setAttribute('aria-expanded','true'); srch.classList.add('open'); }
  function closeS(){ sugg.hidden = true; qIn.setAttribute('aria-expanded','false'); qIn.removeAttribute('aria-activedescendant'); srch.classList.remove('open'); }
  function go(i){ var o = opts[i]; if (!o) return; qIn.value = o.val; closeS(); qIn.blur(); location.hash = o.href; }
  qIn.addEventListener('focus', openS);
  qIn.addEventListener('input', function(){ render(); sugg.hidden = false; });
  qIn.addEventListener('keydown', function(e){
    if (e.key === 'ArrowDown') { e.preventDefault(); setAct(act + 1); }
    else if (e.key === 'ArrowUp') { e.preventDefault(); setAct(act - 1); }
    else if (e.key === 'Enter') { e.preventDefault(); go(act > -1 ? act : 0); }
    else if (e.key === 'Escape') { closeS(); qIn.blur(); } });
  sugg.addEventListener('mousedown', function(e){ e.preventDefault(); });   /* keep focus in the input while clicking a row */
  sugg.addEventListener('click', function(e){ var o = e.target.closest('[role=option]'); if (o) go(+o.dataset.i); });
  sugg.addEventListener('mousemove', function(e){ var o = e.target.closest('[role=option]'); if (o && +o.dataset.i !== act) setAct(+o.dataset.i); });
  qIn.addEventListener('blur', closeS);
  $('#sbtn').addEventListener('click', function(){ srch.classList.add('open'); qIn.focus(); });
  addEventListener('keydown', function(e){ var t = e.target.tagName; if (e.key === '/' && t !== 'INPUT' && t !== 'TEXTAREA' && !e.target.isContentEditable) { e.preventDefault(); srch.classList.add('open'); qIn.focus(); } });

  /* ========== ANATOMY SCAN ========== */
  var els = $$('#page .el'), statEl = $('#anatStat');
  els.forEach(function(e){ e.querySelector('.tag').textContent = e.dataset.tag; });
  if (!G) { els.forEach(function(e){ e.classList.add('done'); }); statEl.textContent = els.length+' elements labelled'; }
  else {
    var at = gsap.timeline({repeat:-1,repeatDelay:2.2,onRepeat:function(){ els.forEach(function(e){ e.classList.remove('done','on'); }); $$('#tcs i').forEach(function(i){ i.style.background = ''; }); }});
    els.forEach(function(e, i){ var tag = e.querySelector('.tag'), t0 = i*1.1;
      at.add(function(){ els.forEach(function(x){ x.classList.remove('on'); }); e.classList.add('on'); scramble(tag, e.dataset.tag, .7); statEl.textContent = 'scanning ' + (i+1) + '/' + els.length + ': ' + e.dataset.tag.toLowerCase(); }, t0)
        .fromTo(e,{scale:.985},{scale:1,duration:.5,ease:'back.out(3)'}, t0)
        .add(function(){ e.classList.remove('on'); e.classList.add('done'); }, t0 + 1);
      if (e.classList.contains('tests-el')) $$('#tcs i').forEach(function(dot, k){ at.add(function(){ dot.style.background = css('--ok'); }, t0 + .15 + k*.14); });
    });
    at.add(function(){ statEl.textContent = els.length + ' elements labelled · 200 OK'; }, els.length*1.1);
    whileVisible('#anat', at);
  }

  /* ========== 01 · SOLVE: language rail, code, hidden tests ========== */
  var L = {
    python:{c:['<span class="c"># one pass with a hash map</span>','<span class="k">def</span> <span class="t">two_sum</span>(nums, target):','    seen = {}','    <span class="k">for</span> i, n <span class="k">in</span> <span class="t">enumerate</span>(nums):','        <span class="k">if</span> target - n <span class="k">in</span> seen:','            <span class="k">return</span> [seen[target - n], i]','        seen[n] = i','    <span class="k">return</span> []'],ms:12},
    javascript:{c:['<span class="c">// one pass with a Map</span>','<span class="k">function</span> <span class="t">twoSum</span>(nums, target) {','  <span class="k">const</span> seen = <span class="k">new</span> <span class="t">Map</span>();','  <span class="k">for</span> (<span class="k">let</span> i = 0; i &lt; nums.length; i++) {','    <span class="k">const</span> need = target - nums[i];','    <span class="k">if</span> (seen.<span class="t">has</span>(need)) <span class="k">return</span> [seen.<span class="t">get</span>(need), i];','    seen.<span class="t">set</span>(nums[i], i);','  }','}'],ms:14},
    java:{c:['<span class="c">// one pass with a HashMap</span>','<span class="k">int</span>[] <span class="t">twoSum</span>(<span class="k">int</span>[] nums, <span class="k">int</span> target) {','    Map&lt;Integer, Integer&gt; seen = <span class="k">new</span> <span class="t">HashMap</span>&lt;&gt;();','    <span class="k">for</span> (<span class="k">int</span> i = 0; i &lt; nums.length; i++) {','        Integer j = seen.<span class="t">get</span>(target - nums[i]);','        <span class="k">if</span> (j != <span class="k">null</span>) <span class="k">return new int</span>[] { j, i };','        seen.<span class="t">put</span>(nums[i], i);','    }','    <span class="k">return new int</span>[0];','}'],ms:9},
    csharp:{c:['<span class="c">// one pass with a Dictionary</span>','<span class="k">int</span>[] <span class="t">TwoSum</span>(<span class="k">int</span>[] nums, <span class="k">int</span> target) {','    <span class="k">var</span> seen = <span class="k">new</span> <span class="t">Dictionary</span>&lt;<span class="k">int</span>, <span class="k">int</span>&gt;();','    <span class="k">for</span> (<span class="k">int</span> i = 0; i &lt; nums.Length; i++) {','        <span class="k">if</span> (seen.<span class="t">TryGetValue</span>(target - nums[i], <span class="k">out var</span> j)) <span class="k">return new</span>[] { j, i };','        seen[nums[i]] = i;','    }','    <span class="k">return</span> <span class="t">Array</span>.<span class="t">Empty</span>&lt;<span class="k">int</span>&gt;();','}'],ms:21},
    php:{c:['<span class="c">// one pass with an array map</span>','<span class="k">function</span> <span class="t">twoSum</span>(<span class="k">array</span> $nums, <span class="k">int</span> $t): <span class="k">array</span> {','    $seen = [];','    <span class="k">foreach</span> ($nums <span class="k">as</span> $i =&gt; $n) {','        <span class="k">if</span> (<span class="t">isset</span>($seen[$t - $n])) <span class="k">return</span> [$seen[$t - $n], $i];','        $seen[$n] = $i;','    }','    <span class="k">return</span> [];','}'],ms:18}
  };
  var TESTS = ['[2,7,11,15] → [0,1]','[3,2,4] → [1,2]','[3,3] → [0,1]','negatives','n = 10,000'];
  var srcEl = $('#src'), jEl = $('#judgeT'), vline = $('#vline'), ptabs = $('#ptabs'), cur = 'python', runTl;
  jEl.innerHTML = TESTS.map(function(t,i){ return '<div><i></i>case '+(i+1)+' · '+t+'<small>–</small></div>'; }).join('');
  var rowsJ = [].slice.call(jEl.children);
  function settle(i, ms){ var d = rowsJ[i]; d.classList.add('ok'); d.querySelector('small').textContent = ms+' ms'; }
  function runTests(){ var ms = L[cur].ms;
    rowsJ.forEach(function(d){ d.classList.remove('ok'); d.querySelector('small').textContent = '–'; });
    vline.innerHTML = '<span>running…</span>';
    var done = function(){ vline.innerHTML = '<b>ACCEPTED</b><span>5/5 tests · '+ms+' ms</span>'; };
    if (!G) { rowsJ.forEach(function(d,i){ settle(i, Math.max(1, ms - 4 + i)); }); done(); return; }
    if (runTl) runTl.kill();
    runTl = gsap.timeline();
    rowsJ.forEach(function(d,i){ runTl.add(function(){ settle(i, Math.max(1, ms - 4 + i)); }, .25 + i*.3).fromTo(d,{x:-6},{x:0,duration:.3,ease:'power3.out'}, .25 + i*.3); });
    runTl.add(done, '+=.15');
  }
  function showLang(l){ cur = l;
    [].forEach.call(ptabs.querySelectorAll('.ptab'), function(b){ b.setAttribute('aria-selected', b.dataset.l === l); });
    srcEl.innerHTML = L[l].c.map(function(s,i){ return '<span class="ln" data-n="'+(i+1)+'">'+(s||' ')+'</span>'; }).join('');
    if (G) gsap.from(srcEl.children,{autoAlpha:0,x:-8,duration:.3,stagger:.04,ease:'power2.out'});
    runTests();
  }
  ptabs.addEventListener('click', function(e){ var b = e.target.closest('.ptab'); if (b) showLang(b.dataset.l); });
  $('#runBtn').addEventListener('click', runTests);
  $('#copyBtn').addEventListener('click', function(){ var b = this; try { navigator.clipboard.writeText(srcEl.innerText); } catch(e){} b.textContent = 'Copied'; setTimeout(function(){ b.textContent = 'Copy code'; }, 1400); });
  showLang('python');
  if (G) ScrollTrigger.create({trigger:'#solve .pg-body',start:'top 75%',once:true,onEnter:runTests});

  /* ========== 02 · JUDGE: verdict feed + packet diagram ========== */
  var SUBS = [['two-sum','ACCEPTED','12 ms'],['valid-parentheses','WRONG ANSWER','8 ms'],['merge-intervals','ACCEPTED','31 ms'],['lru-cache','TIME LIMIT','2000 ms'],['binary-search','ACCEPTED','6 ms'],['coin-change','ACCEPTED','52 ms'],['word-ladder','RUNTIME ERROR','–'],['top-k-frequent','ACCEPTED','19 ms']];
  var subrows = $('#subrows'); subrows.innerHTML = SUBS.concat(SUBS).map(function(s){ return '<div class="rw"><b>'+s[0]+'</b><span class="v'+(s[1]==='ACCEPTED'?' ok':'')+'">'+s[1]+'</span><span>'+s[2]+'</span></div>'; }).join('');
  if (G) whileVisible('.feed', gsap.to(subrows,{yPercent:-50,duration:22,ease:'none',repeat:-1}));
  if (G) {
    whileVisible('#diag', gsap.to('#diag svg.arc',{rotate:360,duration:3,ease:'none',repeat:-1,transformOrigin:'50% 50%'}));
    whileVisible('#diag', gsap.to('#diag svg.arc2',{rotate:-360,duration:6,ease:'none',repeat:-1,transformOrigin:'50% 50%'}));
    gsap.matchMedia().add({d:'(min-width: 901px)', m:'(max-width: 900px)'}, function(ctx){
      var tw = gsap.timeline({repeat:-1,repeatDelay:.5});
      $$('.wire i').forEach(function(w,i){ var prop = ctx.conditions.d ? {x:48} : {y:24}, from = ctx.conditions.d ? {x:0} : {y:0}; tw.fromTo(w,Object.assign({autoAlpha:1},from),Object.assign({duration:.7,ease:'power1.inOut'},prop),i*.75).to(w,{autoAlpha:0,duration:.15}); });
      tw.fromTo('.node.mid .box',{scale:1},{scale:1.04,duration:.2,yoyo:true,repeat:1},.7).fromTo('.node:last-child .box',{scale:1},{scale:1.06,duration:.2,yoyo:true,repeat:1},1.45);
      whileVisible('#diag', tw);
    });
  }

  /* ========== 03 · COMPETE: leaderboard, countdown, discussion lock ========== */
  var people = [['minh.tran',512],['linh_cp',498],['hung.dev',471],['khoa.nexus',455],['byte_me',430],['sigma.loop',402]];
  $('#lb').innerHTML = people.map(function(p,i){ return '<div data-n="'+p[0]+'"><b>'+String(i+1).padStart(2,'0')+'</b>'+p[0]+'<span>'+p[1]+'</span></div>'; }).join('');
  if (G) { var lbl = gsap.timeline({repeat:-1,repeatDelay:2}); lbl.add(function(){ var rows = $$('#lb div'), st = Flip.getState(rows), k = 1 + (Math.random()*5|0); people[k][1] += 20 + (Math.random()*40|0); people.sort(function(a,b){ return b[1]-a[1]; });
      var by = {}; rows.forEach(function(r){ by[r.dataset.n] = r; }); people.forEach(function(p,i){ var r = by[p[0]]; r.querySelector('b').textContent = String(i+1).padStart(2,'0'); r.querySelector('span').textContent = p[1]; $('#lb').appendChild(r); }); Flip.from(st,{duration:.7,ease:'power3.inOut'}); });
    whileVisible('#lb', lbl); }
  var secs = 1*3600 + 24*60 + 37, clock = $('#clock');
  function drawClock(){ var h = secs/3600|0, m = (secs%3600)/60|0, s = secs%60; clock.firstChild.textContent = [h,m,s].map(function(n){ return String(n).padStart(2,'0'); }).join(':'); }
  if (G) whileVisible('#clock', gsap.timeline({repeat:-1}).add(function(){ secs = secs > 0 ? secs - 1 : 5400; drawClock(); }, 1));
  if (G) { var lk = gsap.timeline({repeat:-1,repeatDelay:1.5}); lk.set('#bub span',{autoAlpha:0,y:8}).to('#shackle',{y:-4,duration:.3,ease:'back.out(3)'},.4).to('#lock',{rotate:-6,duration:.15,yoyo:true,repeat:1},.4).to('#bub span',{autoAlpha:1,y:0,duration:.4,stagger:.25,ease:'power3.out'},.7).to('#shackle',{y:0,duration:.3},'+=2'); whileVisible('#bub', lk); }

  /* ========== 04 · COMMUNITIES ========== */
  var CM = [
    {n:'FPT Algo Club',u:'devclimb.app/c/fpt-algo-club',vis:'public',d:'Weekly rounds for students who want to get better at algorithms.',roles:[['Owner',1],['Moderators',3],['Problem setters',6],['Members',142]],rows:[['Problems in library',64],['Contributions in review',5],['Contests this term',12],['Next contest','Sat 20:00 · 90 min']]},
    {n:'SD21304 class',u:'devclimb.app/c/sd21304',vis:'private',d:'One class: weekly assignments and a timed mid-term.',roles:[['Owner',1],['Teaching assistants',2],['Members',38]],rows:[['Problems in library',18],['Contributions in review',2],['Assignment sets',4],['Next contest','Fri 19:00 · 60 min']]},
    {n:'Java Beginners',u:'devclimb.app/c/java-beginners',vis:'open',d:'Slow-paced problems for people learning their first language.',roles:[['Owner',1],['Moderators',2],['Problem setters',4],['Members',310]],rows:[['Problems in library',41],['Contributions in review',9],['Language','Java only'],['Next contest','Sun 15:00 · 45 min']]}
  ];
  var cmTabs = $('#cmTabs'), cmCur = 0, cmTl;
  cmTabs.innerHTML = CM.map(function(c,i){ return '<button class="cmt" role="tab" aria-selected="'+(i===0)+'">'+c.n+'<i class="prog"></i></button>'; }).join('');
  function showCM(i){ cmCur = i; var c = CM[i];
    $$('.cmt').forEach(function(b,k){ b.setAttribute('aria-selected', k === i); });
    $('#cmUrl').textContent = c.u;
    $('#cmName').innerHTML = c.n + ' <span class="badge">' + c.vis + '</span>';
    $('#cmDesc').textContent = c.d;
    $('#cmRoles').innerHTML = c.roles.map(function(r){ return '<span><b>'+r[1]+'</b>'+r[0]+'</span>'; }).join('');
    $('#cmRows').innerHTML = c.rows.map(function(r){ var num = typeof r[1] === 'number'; return '<div><span>'+r[0]+'</span><b'+(num?' data-v="'+r[1]+'"':'')+'>'+(num && G ? 0 : r[1])+'</b></div>'; }).join('');
    if (!G) return;
    if (cmTl) cmTl.kill();
    cmTl = gsap.timeline({onComplete:function(){ showCM((cmCur+1) % CM.length); }});
    cmTl.from('.cm-id > *, #cmRows div',{autoAlpha:0,y:8,duration:.4,stagger:.05,ease:'power3.out'},0);
    $$('#cmRows b[data-v]').forEach(function(b,k){ var o = {v:0}; cmTl.to(o,{v:+b.dataset.v,duration:.9,ease:'power2.out',onUpdate:function(){ b.textContent = Math.round(o.v); }}, .1 + k*.15); });
    var pr = $$('.cmt')[i].querySelector('.prog');
    cmTl.fromTo(pr,{scaleX:0},{scaleX:1,duration:6,ease:'none'},0).set(pr,{scaleX:0});
  }
  cmTabs.addEventListener('click', function(e){ var b = e.target.closest('.cmt'); if (b) showCM($$('.cmt').indexOf(b)); });
  showCM(0);
  if (G) ScrollTrigger.create({trigger:'#communities',start:'top bottom',end:'bottom top',onToggle:function(s){ if (cmTl) s.isActive ? cmTl.resume() : cmTl.pause(); }});

  /* ========== 05 · FAQ ========== */
  $$('.qa button').forEach(function(b){ b.addEventListener('click', function(){ var o = b.parentNode.classList.toggle('open'); b.setAttribute('aria-expanded', o); }); });

  /* ========== READY: checklist ticks once on scroll ========== */
  var stEls = $$('#start .st');
  function tick(d){ d.classList.add('ok'); d.querySelector('small').textContent = 'done'; }
  if (!G) stEls.forEach(tick);
  else ScrollTrigger.create({trigger:'#start',start:'top 80%',once:true,onEnter:function(){ stEls.forEach(function(d,i){ gsap.delayedCall(.3 + i*.45, function(){ tick(d); }); }); }});

  /* ========== FOOTER: pixel wordmark filled by scroll ========== */
  var word = $('#word'), wctx = word.getContext('2d'), wbits = [], wcols = 0, wprog = (R || !G) ? 1 : 0;
  function buildWord(){ var W = word.parentNode.clientWidth - (small() ? 40 : 96), cell = small() ? 5 : 8;
    wcols = Math.max(20, Math.floor(W/cell)); var wrows = Math.round(wcols*.16);
    var off = document.createElement('canvas'); off.width = wcols; off.height = wrows; var o = off.getContext('2d');
    o.fillStyle = '#000'; o.fillRect(0,0,wcols,wrows); o.fillStyle = '#fff'; o.textBaseline = 'middle'; o.textAlign = 'center';
    var fs = Math.round(wrows*1.05); o.font = '600 '+fs+'px "Inter Tight", sans-serif'; var m = o.measureText('DEVCLIMB').width; if (m > wcols*.98) o.font = '600 '+Math.round(fs*wcols*.98/m)+'px "Inter Tight", sans-serif';
    o.fillText('DEVCLIMB', wcols/2, wrows*.55);
    var d = o.getImageData(0,0,wcols,wrows).data; wbits = [];
    for (var y = 0; y < wrows; y++) for (var x = 0; x < wcols; x++) if (d[(y*wcols+x)*4] > 120) wbits.push([x,y,Math.random()]);
    var dpr = Math.min(devicePixelRatio||1,2); word.width = wcols*cell*dpr; word.height = wrows*cell*dpr; word.style.aspectRatio = wcols + ' / ' + wrows;
    wctx.setTransform(dpr,0,0,dpr,0,0); word._cell = cell; drawWord(); }
  function drawWord(){ var cell = word._cell, tx = css('--tx3'), ac = css('--ac'); wctx.clearRect(0,0,word.width,word.height);
    wbits.forEach(function(b){ var t = b[0]/wcols*.85 + b[2]*.15; if (t > wprog) return; wctx.fillStyle = (wprog - t < .04 && wprog < 1) ? ac : tx; wctx.fillRect(b[0]*cell+1, b[1]*cell+1, cell-2, cell-2); }); }
  afterFontsLoad(function(){ buildWord(); if (G) ScrollTrigger.create({trigger:'#word',start:'top 95%',end:'max',scrub:.6,onUpdate:function(s){ wprog = s.progress; drawWord(); }}); });
  addEventListener('resize', buildWord); addEventListener('themechange', drawWord);

  if (!G) return;
  gsap.matchMedia().add('(min-width: 901px)', function(){ gsap.fromTo('.browser',{rotateX:14,y:60,transformPerspective:1400,transformOrigin:'50% 0%'},{rotateX:0,y:0,ease:'none',scrollTrigger:{trigger:'#anat',start:'top bottom',end:'top 35%',scrub:.6}}); });
}
