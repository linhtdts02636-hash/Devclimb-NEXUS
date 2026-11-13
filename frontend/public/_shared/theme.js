/* Shared: dark/light toggle + GSAP scroll-reveal (falls back to IntersectionObserver if the CDN is unreachable).
   Load AFTER gsap + ScrollTrigger, in <head> (no defer) so the theme is set before first paint. */
(function () {
  var KEY = 'dc-theme', root = document.documentElement;
  var saved = null;
  try { saved = localStorage.getItem(KEY); } catch (e) {}
  root.dataset.theme = saved || (matchMedia('(prefers-color-scheme: light)').matches ? 'light' : 'dark');
  var R = matchMedia('(prefers-reduced-motion: reduce)').matches;
  window.DC_REDUCED = R;
  window.DC_GSAP = !!(window.gsap && window.ScrollTrigger) && !R;
  if (window.DC_GSAP) { gsap.registerPlugin(ScrollTrigger); gsap.ticker.lagSmoothing(0); root.classList.add('gs'); }
  /* with GSAP, CSS transitions on .rv would fight the tweens */
  var st = document.createElement('style');
  st.textContent = 'html.gs .rv{transition:none!important}';
  document.head.appendChild(st);

  window.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('[data-theme-toggle]').forEach(function (b) {
      b.addEventListener('click', function () {
        var n = root.dataset.theme === 'dark' ? 'light' : 'dark';
        root.dataset.theme = n;
        try { localStorage.setItem(KEY, n); } catch (e) {}
        window.dispatchEvent(new CustomEvent('themechange', { detail: n }));
      });
    });
    if (window.DC_GSAP) {
      /* fonts and late layout shift change page height; keep trigger positions honest */
      var rf = function () { ScrollTrigger.refresh(); };
      window.addEventListener('load', rf);
      if (document.fonts && document.fonts.ready) document.fonts.ready.then(rf);
      setTimeout(rf, 1200);
    }
    var els = document.querySelectorAll('.rv');
    if (R) { els.forEach(function (el) { el.classList.add('in'); }); return; }
    if (window.DC_GSAP) {
      ScrollTrigger.batch(els, {
        start: 'top 90%', end: 'max', once: true,
        onEnter: function (b) {
          gsap.to(b, { opacity: 1, y: 0, duration: 0.9, ease: 'power3.out', stagger: 0.09, overwrite: 'auto',
            onComplete: function () { b.forEach(function (e) { e.classList.add('in'); }); } });
        }
      });
    } else {
      var io = new IntersectionObserver(function (es) {
        es.forEach(function (e) { if (e.isIntersecting) { e.target.classList.add('in'); io.unobserve(e.target); } });
      }, { threshold: 0.12 });
      els.forEach(function (el) { el.style.transitionDelay = (el.dataset.d || 0) + 'ms'; io.observe(el); });
    }
  });
})();
