(function () {
  // ---- syntax highlighting (Java, Groovy, XML, Bash) ----
  var JAVA_KW = 'abstract|boolean|break|byte|case|catch|char|class|continue|default|do|double|else|enum|extends|false|final|finally|float|for|if|implements|import|instanceof|int|interface|long|new|null|package|private|protected|public|return|short|static|super|switch|this|throw|throws|true|try|void|while|var|def|apply|plugins|dependencies|repositories|maven|url|api|implementation|natives|include';
  function esc(s) { return s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;'); }
  function span(c, s) { return '<span class="tk-' + c + '">' + esc(s) + '</span>'; }

  function highlightCode(src, lang) {
    if (lang === 'xml') {
      return src.replace(/(<!--[\s\S]*?-->)|(<\/?)([\w.:-]+)|([\w.:-]+)(=)("[^"]*"|'[^']*')|([^<]+?(?=[\w.:-]+=|<|$))|([\s\S])/g,
        function (m, com, open, tag, attr, eq, val, text, other) {
          if (com) return span('com', com);
          if (tag) return esc(open) + span('tag', tag);
          if (attr) return span('attr', attr) + esc(eq) + span('str', val);
          return esc(m);
        });
    }
    if (lang === 'bash') {
      return src.replace(/(#.*$)|("[^"]*")|([\s\S])/gm, function (m, com, str) {
        if (com) return span('com', com);
        if (str) return span('str', str);
        return esc(m);
      });
    }
    var re = new RegExp('(//.*$|/\\*[\\s\\S]*?\\*/)|("(?:[^"\\\\]|\\\\.)*"|\'(?:[^\'\\\\]|\\\\.)*\')|\\b(' + JAVA_KW + ')\\b|\\b(\\d+(?:\\.\\d+)?f?)\\b|\\b([A-Z][A-Za-z0-9_]*)\\b|([\\s\\S])', 'gm');
    return src.replace(re, function (m, com, str, kw, num, type) {
      if (com) return span('com', com);
      if (str) return span('str', str);
      if (kw) return span('kw', kw);
      if (num) return span('num', num);
      if (type) return span('type', type);
      return esc(m);
    });
  }

  document.querySelectorAll('pre.code').forEach(function (pre) {
    var code = pre.querySelector('code');
    var text = code.textContent;
    code.innerHTML = highlightCode(text, pre.dataset.lang);
    var btn = document.createElement('button');
    btn.className = 'copy';
    btn.textContent = 'Copy';
    btn.onclick = function () {
      var done = function () { btn.textContent = 'Copied'; setTimeout(function () { btn.textContent = 'Copy'; }, 1400); };
      if (navigator.clipboard) navigator.clipboard.writeText(text).then(done, function () {});
      else { var t = document.createElement('textarea'); t.value = text; document.body.appendChild(t); t.select(); try { document.execCommand('copy'); done(); } catch (e) {} t.remove(); }
    };
    pre.appendChild(btn);
  });

  // ---- dark mode toggle ----
  var root = document.documentElement;
  document.querySelector('.theme-btn').onclick = function () {
    var dark = root.dataset.theme ? root.dataset.theme === 'dark' : matchMedia('(prefers-color-scheme: dark)').matches;
    root.dataset.theme = dark ? 'light' : 'dark';
    try { localStorage.setItem('theme', root.dataset.theme); } catch (e) {}
  };

  // ---- highlight the current section in "On this page" ----
  var links = Array.prototype.slice.call(document.querySelectorAll('.toc a'));
  if (links.length && 'IntersectionObserver' in window) {
    var byId = {};
    links.forEach(function (a) { byId[a.getAttribute('href').slice(1)] = a; });
    var visible = {};
    var obs = new IntersectionObserver(function (entries) {
      entries.forEach(function (e) { visible[e.target.id] = e.isIntersecting; });
      var heads = document.querySelectorAll('.content h2[id]');
      var current = null;
      for (var i = 0; i < heads.length; i++) if (visible[heads[i].id]) { current = heads[i].id; break; }
      if (current) links.forEach(function (a) { a.classList.toggle('current', a === byId[current]); });
    }, { rootMargin: '-60px 0px -60% 0px' });
    document.querySelectorAll('.content h2[id]').forEach(function (h) { obs.observe(h); });
  }

  // close the mobile menu after choosing a page
  document.querySelectorAll('.sidebar a').forEach(function (a) {
    a.addEventListener('click', function () { document.body.classList.remove('nav-open'); });
  });
})();
